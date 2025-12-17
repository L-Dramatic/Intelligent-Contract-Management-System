package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.dto.ApproveDTO;
import com.software.contract_system.entity.SysUser;
import com.software.contract_system.entity.WfTask;
import com.software.contract_system.mapper.SysUserMapper;
import com.software.contract_system.service.WorkflowService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow")
@Tag(name = "工作流引擎", description = "审批流程管理")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private SysUserMapper userMapper;

    // ==========================================
    // 1. 提交审批 (启动流程)
    // ==========================================
    @PostMapping("/submit/{contractId}")
    @Operation(summary = "提交合同审批", description = "将草稿状态的合同提交进入审批流")
    @PreAuthorize("hasAuthority('contract:add')") // 发起人权限
    public Result<String> submit(@PathVariable Long contractId) {
        Long userId = getCurrentUserId();
        workflowService.submitContract(contractId, userId);
        return Result.success("提交成功，流程已启动");
    }

    // ==========================================
    // 2. 获取我的待办任务
    // ==========================================
    @GetMapping("/tasks")
    @Operation(summary = "获取我的待办任务", description = "查询当前登录用户需要审批的任务")
    public Result<List<WfTask>> getMyTasks() {
        Long userId = getCurrentUserId();
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
        Long userId = getCurrentUserId();
        workflowService.approveTask(approveDTO, userId);
        return Result.success("审批操作成功");
    }

    /**
     * 辅助方法：获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        return user.getId();
    }
}