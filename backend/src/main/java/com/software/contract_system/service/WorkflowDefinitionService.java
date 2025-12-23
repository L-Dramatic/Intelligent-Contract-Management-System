package com.software.contract_system.service;

import com.software.contract_system.dto.WorkflowDefinitionDTO;
import java.util.List;

/**
 * 流程定义服务接口
 * 基于场景配置聚合展示流程模板
 */
public interface WorkflowDefinitionService {
    
    /**
     * 获取所有流程定义（按合同大类聚合）
     */
    List<WorkflowDefinitionDTO> getAllDefinitions();
    
    /**
     * 获取流程定义详情
     */
    WorkflowDefinitionDTO getDefinitionById(Long id);
}

