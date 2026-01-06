package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.WfInstance;
import com.software.contract_system.entity.WfScenarioConfig;
import com.software.contract_system.entity.WfTask;
import com.software.contract_system.entity.WfScenarioNode;
import com.software.contract_system.mapper.ContractMapper;
import com.software.contract_system.service.WorkflowScenarioService;
import com.software.contract_system.service.ScenarioMatchService;
import com.software.contract_system.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * 获取我发起的流程实例列表
     */
    @GetMapping("/instances/my")
    @Operation(summary = "我发起的", description = "获取当前用户发起的所有审批流程")
    public Result<List<WfInstance>> getMyInitiatedInstances() {
        Long userId = securityUtils.getCurrentUserId();
        List<WfInstance> instances = workflowScenarioService.getMyInitiatedInstances(userId);
        return Result.success(instances);
    }
    
    /**
     * 获取任务详情（包含合同信息）
     */
    @GetMapping("/tasks/{taskId}/detail")
    @Operation(summary = "获取任务详情", description = "获取审批任务的详细信息")
    public Result<WfTask> getTaskDetail(@PathVariable Long taskId) {
        WfTask task = workflowScenarioService.getTaskDetail(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        return Result.success(task);
    }
    
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
     * 更新审批场景配置（管理员）
     */
    @PutMapping("/scenarios/{id}")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @Operation(summary = "更新场景配置", description = "管理员更新审批场景的基本配置")
    public Result<String> updateScenario(@PathVariable Long id, @RequestBody WfScenarioConfig config) {
        try {
            config.setId(id);
            config.setUpdatedAt(java.time.LocalDateTime.now());
            boolean success = scenarioMatchService.updateScenario(config);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 切换场景启用状态（管理员）
     */
    @PutMapping("/scenarios/{id}/toggle-active")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @Operation(summary = "切换启用状态", description = "启用或禁用审批场景")
    public Result<String> toggleScenarioActive(@PathVariable Long id) {
        try {
            boolean success = scenarioMatchService.toggleScenarioActive(id);
            return success ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
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
     * 创建新的审批场景（管理员）
     */
    @PostMapping("/scenarios")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @Operation(summary = "创建场景", description = "管理员创建新的审批场景")
    public Result<WfScenarioConfig> createScenario(@RequestBody WfScenarioConfig config) {
        try {
            WfScenarioConfig created = scenarioMatchService.createScenario(config);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除审批场景（管理员）
     */
    @DeleteMapping("/scenarios/{id}")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @Operation(summary = "删除场景", description = "管理员删除审批场景（同时删除关联节点）")
    public Result<String> deleteScenario(@PathVariable Long id) {
        try {
            boolean success = scenarioMatchService.deleteScenario(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 为场景添加审批节点（管理员）
     */
    @PostMapping("/scenarios/{scenarioId}/nodes")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @Operation(summary = "添加节点", description = "为审批场景添加新的审批节点")
    public Result<WfScenarioNode> addNode(@PathVariable String scenarioId, @RequestBody WfScenarioNode node) {
        try {
            node.setScenarioId(scenarioId);
            WfScenarioNode created = scenarioMatchService.addNode(node);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("添加失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新审批节点（管理员）
     */
    @PutMapping("/scenarios/{scenarioId}/nodes/{nodeId}")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @Operation(summary = "更新节点", description = "更新审批节点配置")
    public Result<String> updateNode(@PathVariable String scenarioId, @PathVariable Long nodeId, @RequestBody WfScenarioNode node) {
        try {
            node.setId(nodeId);
            node.setScenarioId(scenarioId);
            boolean success = scenarioMatchService.updateNode(node);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除审批节点（管理员）
     */
    @DeleteMapping("/scenarios/{scenarioId}/nodes/{nodeId}")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @Operation(summary = "删除节点", description = "删除审批节点")
    public Result<String> deleteNode(@PathVariable String scenarioId, @PathVariable Long nodeId) {
        try {
            boolean success = scenarioMatchService.deleteNode(nodeId);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量保存场景的所有节点（管理员）
     */
    @PutMapping("/scenarios/{scenarioId}/nodes")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @Operation(summary = "批量保存节点", description = "替换场景的所有审批节点")
    public Result<String> saveScenarioNodes(@PathVariable String scenarioId, @RequestBody List<WfScenarioNode> nodes) {
        try {
            boolean success = scenarioMatchService.saveScenarioNodes(scenarioId, nodes);
            return success ? Result.success("保存成功") : Result.error("保存失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
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
