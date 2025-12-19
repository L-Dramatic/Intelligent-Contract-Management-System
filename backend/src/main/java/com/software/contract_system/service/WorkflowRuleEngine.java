package com.software.contract_system.service;

import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.WfDefinition;

/**
 * 审批流程引擎接口
 * 
 * 负责根据合同属性（类型、金额等）匹配合适的审批流程
 * 
 * TODO: 等待审批流程引擎设计方案完成后实现
 * 
 * 预期功能：
 * 1. 根据合同类型匹配专用流程（Type A/B/C专用流程）
 * 2. 根据合同金额匹配金额流程（简易/标准/重大）
 * 3. 支持多条件组合匹配
 * 4. 支持优先级配置
 */
public interface WorkflowRuleEngine {

    /**
     * 根据合同匹配审批流程定义
     * 
     * @param contract 合同实体
     * @return 匹配的流程定义，如果没有匹配则返回null
     */
    WfDefinition matchDefinition(Contract contract);

    /**
     * 检查是否存在匹配的流程
     * 
     * @param contract 合同实体
     * @return true表示存在匹配的流程
     */
    boolean hasMatchingDefinition(Contract contract);

    /**
     * 获取流程匹配的说明（用于日志和调试）
     * 
     * @param contract 合同实体
     * @return 匹配说明
     */
    String getMatchReason(Contract contract);
}

