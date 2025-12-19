package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.dto.ApproveDTO;
import com.software.contract_system.dto.PreFlightCheckResult;
import com.software.contract_system.dto.SubmitContractRequest;
import com.software.contract_system.entity.WfTask;
import com.software.contract_system.service.WorkflowService;
import com.software.contract_system.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/workflow")
@Tag(name = "工作流引擎", description = "审批流程管理")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private SecurityUtils securityUtils;

    // ==========================================
    // 0. Pre-Flight Check (提交前检查)
    // ==========================================
    @PostMapping("/pre-flight-check/{contractId}")
    @Operation(summary = "提交前检查", description = "执行Pre-Flight Check，检查合同是否满足提交条件")
    public Result<PreFlightCheckResult> preFlightCheck(
            @Parameter(description = "合同ID") @PathVariable Long contractId,
            @Parameter(description = "附件文件名列表") @RequestBody(required = false) List<String> attachments) {
        log.info("执行Pre-Flight Check: contractId={}", contractId);
        
        if (attachments == null) {
            attachments = Collections.emptyList();
        }
        
        PreFlightCheckResult result = workflowService.preFlightCheck(contractId, attachments);
        return Result.success(result);
    }

    // ==========================================
    // 1. 提交审批 (启动流程) - 简易版
    // ==========================================
    @PostMapping("/submit/{contractId}")
    @Operation(summary = "提交合同审批（简易版）", description = "将草稿状态的合同提交进入审批流")
    @PreAuthorize("hasAuthority('contract:add')") // 发起人权限
    public Result<String> submit(@PathVariable Long contractId) {
        Long userId = securityUtils.getCurrentUserId();
        workflowService.submitContract(contractId, userId);
        return Result.success("提交成功，流程已启动");
    }

    // ==========================================
    // 1.1 提交审批 (启动流程) - 完整版（带Pre-Flight Check）
    // ==========================================
    @PostMapping("/submit")
    @Operation(summary = "提交合同审批（完整版）", description = "带附件检查和用户确认的提交")
    @PreAuthorize("hasAuthority('contract:add')")
    public Result<String> submitWithCheck(@RequestBody SubmitContractRequest request) {
        log.info("提交合同审批（完整版）: contractId={}", request.getContractId());
        
        Long userId = securityUtils.getCurrentUserId();
        List<String> attachments = request.getAttachments() != null 
                ? request.getAttachments() 
                : Collections.emptyList();
        Boolean userConfirmed = request.getUserConfirmed() != null 
                ? request.getUserConfirmed() 
                : false;
        
        try {
            workflowService.submitContract(request.getContractId(), userId, attachments, userConfirmed);
            return Result.success("提交成功，流程已启动，AI审查已触发");
        } catch (RuntimeException e) {
            String message = e.getMessage();
            // 检查是否是需要确认的情况
            if (message != null && message.startsWith("REQUIRES_CONFIRMATION:")) {
                return Result.error(400, message.substring("REQUIRES_CONFIRMATION:".length()));
            }
            return Result.error(message);
        }
    }

    // ==========================================
    // 2. 获取我的待办任务
    // ==========================================
    @GetMapping("/tasks")
    @Operation(summary = "获取我的待办任务", description = "查询当前登录用户需要审批的任务")
    public Result<List<WfTask>> getMyTasks() {
        Long userId = securityUtils.getCurrentUserId();
        List<WfTask> tasks = workflowService.getMyTasks(userId);
        return Result.success(tasks);
    }

    // ==========================================
    // 3. 执行审批 (通过/驳回)
    // ==========================================
    @PostMapping("/approve")
    @Operation(summary = "执行审批", description = "通过或驳回任务")
    @PreAuthorize("hasAuthority('contract:audit')") // 审批人权限
    public Result<String> approve(@RequestBody ApproveDTO approveDTO) {
        Long userId = securityUtils.getCurrentUserId();
        workflowService.approveTask(approveDTO, userId);
        return Result.success("审批操作成功");
    }
}