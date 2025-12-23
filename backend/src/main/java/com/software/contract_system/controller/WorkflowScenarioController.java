package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.WfInstance;
import com.software.contract_system.entity.WfScenarioConfig;
import com.software.contract_system.entity.WfTask;
import com.software.contract_system.mapper.ContractMapper;
import com.software.contract_system.service.WorkflowScenarioService;
import com.software.contract_system.service.ScenarioMatchService;
import com.software.contract_system.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 基于场景的审批流程控制器
 * 提供审批任务的待办、已办、审批操作等API
 */
@RestController
@RequestMapping("/workflow/scenario")
@Tag(name = "审批流程管理", description = "基于场景配置的审批流程API")
public class WorkflowScenarioController {
    
    @Autowired
    private WorkflowScenarioService workflowScenarioService;
    
    @Autowired
    private ScenarioMatchService scenarioMatchService;
    
    @Autowired
    private ContractMapper contractMapper;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    // ========== 审批任务相关 ==========
    
    /**
     * 获取当前用户的待办任务列表
     */
    @GetMapping("/tasks/pending")
    @Operation(summary = "获取待办任务", description = "获取当前登录用户的待审批任务列表")
    public Result<List<WfTask>> getPendingTasks() {
        Long userId = securityUtils.getCurrentUserId();
        List<WfTask> tasks = workflowScenarioService.getPendingTasks(userId);
        return Result.success(tasks);
    }
    
    /**
     * 获取当前用户的已办任务列表
     */
    @GetMapping("/tasks/completed")
    @Operation(summary = "获取已办任务", description = "获取当前登录用户已处理的任务列表")
    public Result<List<WfTask>> getCompletedTasks() {
        Long userId = securityUtils.getCurrentUserId();
        List<WfTask> tasks = workflowScenarioService.getCompletedTasks(userId);
        return Result.success(tasks);
    }
    
    /**
     * 审批通过
     */
    @PostMapping("/tasks/{taskId}/approve")
    @Operation(summary = "审批通过", description = "通过指定的审批任务")
    public Result<String> approve(
            @PathVariable Long taskId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            Long userId = securityUtils.getCurrentUserId();
            String comment = body != null ? body.get("comment") : null;
            workflowScenarioService.approve(taskId, userId, comment);
            return Result.success("审批通过");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 审批驳回
     */
    @PostMapping("/tasks/{taskId}/reject")
    @Operation(summary = "审批驳回", description = "驳回指定的审批任务")
    public Result<String> reject(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body) {
        try {
            Long userId = securityUtils.getCurrentUserId();
            String comment = body != null ? body.get("comment") : "无";
            if (comment == null || comment.trim().isEmpty()) {
                return Result.error("驳回时必须填写原因");
            }
            workflowScenarioService.reject(taskId, userId, comment);
            return Result.success("已驳回");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 流程实例相关 ==========
    
    /**
     * 获取合同的审批进度
     */
    @GetMapping("/contract/{contractId}/progress")
    @Operation(summary = "获取审批进度", description = "获取指定合同的当前审批状态")
    public Result<WfInstance> getWorkflowProgress(@PathVariable Long contractId) {
        WfInstance instance = workflowScenarioService.getWorkflowProgress(contractId);
        if (instance == null) {
            return Result.error("该合同暂无审批流程");
        }
        return Result.success(instance);
    }
    
    /**
     * 获取合同的审批历史
     */
    @GetMapping("/contract/{contractId}/history")
    @Operation(summary = "获取审批历史", description = "获取指定合同的所有审批记录")
    public Result<List<WfTask>> getApprovalHistory(@PathVariable Long contractId) {
        List<WfTask> history = workflowScenarioService.getApprovalHistory(contractId);
        return Result.success(history);
    }
    
    /**
     * 撤销审批流程
     */
    @PostMapping("/instance/{instanceId}/cancel")
    @Operation(summary = "撤销流程", description = "撤销审批流程（仅发起人可操作）")
    public Result<String> cancelWorkflow(@PathVariable Long instanceId) {
        try {
            Long userId = securityUtils.getCurrentUserId();
            workflowScenarioService.cancelWorkflow(instanceId, userId);
            return Result.success("流程已撤销");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 场景配置相关 ==========
    
    /**
     * 获取所有审批场景配置
     */
    @GetMapping("/scenarios")
    @Operation(summary = "获取所有场景", description = "获取系统中配置的所有审批场景")
    public Result<List<WfScenarioConfig>> getAllScenarios() {
        List<WfScenarioConfig> scenarios = scenarioMatchService.getAllScenarios();
        return Result.success(scenarios);
    }
    
    /**
     * 获取场景详情（包含节点列表）
     */
    @GetMapping("/scenarios/{scenarioId}")
    @Operation(summary = "获取场景详情", description = "获取指定场景的完整配置（包含审批节点）")
    public Result<WfScenarioConfig> getScenarioDetail(@PathVariable String scenarioId) {
        WfScenarioConfig scenario = scenarioMatchService.getScenarioWithNodes(scenarioId);
        if (scenario == null) {
            return Result.error("场景不存在");
        }
        return Result.success(scenario);
    }
    
    /**
     * 预览合同匹配的审批场景
     */
    @GetMapping("/contract/{contractId}/matched-scenario")
    @Operation(summary = "预览审批场景", description = "根据合同类型和金额预览将匹配的审批场景")
    public Result<WfScenarioConfig> previewMatchedScenario(@PathVariable Long contractId) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return Result.error("合同不存在");
        }
        
        WfScenarioConfig scenario = workflowScenarioService.getMatchedScenario(contract);
        if (scenario == null) {
            return Result.error("未找到匹配的审批场景，请检查合同类型配置");
        }
        return Result.success(scenario);
    }
    
    /**
     * 检查合同是否可以提交审批
     */
    @GetMapping("/contract/{contractId}/check-submittable")
    @Operation(summary = "检查是否可提交", description = "检查合同是否满足提交审批的条件")
    public Result<Map<String, Object>> checkSubmittable(@PathVariable Long contractId) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return Result.error("合同不存在");
        }
        
        // 检查合同状态
        if (contract.getStatus() != 0 && contract.getStatus() != 3) {
            return Result.success(Map.of(
                    "submittable", false,
                    "reason", "只有草稿或已驳回的合同可以提交"
            ));
        }
        
        // 检查是否有匹配的场景
        WfScenarioConfig scenario = workflowScenarioService.checkSubmittable(contract);
        if (scenario == null) {
            return Result.success(Map.of(
                    "submittable", false,
                    "reason", "未找到匹配的审批场景，请检查合同类型和金额"
            ));
        }
        
        return Result.success(Map.of(
                "submittable", true,
                "scenarioId", scenario.getScenarioId(),
                "scenarioName", scenario.getSubTypeName()
        ));
    }
}
