package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.dto.WorkflowDefinitionDTO;
import com.software.contract_system.service.WorkflowDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程定义控制器
 * 提供流程模板的查询接口
 */
@RestController
@RequestMapping("/workflow/definition")
@Tag(name = "流程定义管理", description = "流程模板的查询与管理")
public class WorkflowDefinitionController {
    
    @Autowired
    private WorkflowDefinitionService workflowDefinitionService;
    
    /**
     * 获取所有流程定义
     */
    @GetMapping("/list")
    @Operation(summary = "获取流程定义列表", description = "获取所有流程模板")
    public Result<List<WorkflowDefinitionDTO>> getList() {
        List<WorkflowDefinitionDTO> list = workflowDefinitionService.getAllDefinitions();
        return Result.success(list);
    }
    
    /**
     * 获取流程定义详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取流程定义详情", description = "根据ID获取流程模板详情")
    public Result<WorkflowDefinitionDTO> getById(@PathVariable Long id) {
        WorkflowDefinitionDTO dto = workflowDefinitionService.getDefinitionById(id);
        if (dto == null) {
            return Result.error("流程定义不存在");
        }
        return Result.success(dto);
    }
    
    /**
     * 启用/停用流程（占位，实际操作场景配置）
     */
    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @Operation(summary = "切换流程状态", description = "启用或停用流程模板")
    public Result<String> toggleStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        // 这里可以实现批量更新该大类下所有场景的状态
        // 暂时返回成功
        return Result.success(enabled ? "已启用" : "已停用");
    }
}

