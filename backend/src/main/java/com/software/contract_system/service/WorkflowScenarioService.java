package com.software.contract_system.service;

import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.WfInstance;
import com.software.contract_system.entity.WfScenarioConfig;
import com.software.contract_system.entity.WfTask;

import java.util.List;

/**
 * 基于场景的审批流程服务
 * 实现 Master Workflow Configuration Matrix 中定义的审批流程
 */
public interface WorkflowScenarioService {
    
    /**
     * 启动审批流程
     * 根据合同类型和金额自动匹配场景，创建流程实例和首个审批任务
     * 
     * @param contract 合同实体
     * @param initiatorId 发起人ID
     * @return 流程实例
     */
    WfInstance startWorkflow(Contract contract, Long initiatorId);
    
    /**
     * 审批通过
     * 处理当前任务，流转到下一节点
     * 
     * @param taskId 任务ID
     * @param approverId 审批人ID
     * @param comment 审批意见
     */
    void approve(Long taskId, Long approverId, String comment);
    
    /**
     * 审批驳回
     * 终止流程，合同状态回退
     * 
     * @param taskId 任务ID
     * @param approverId 审批人ID
     * @param comment 驳回原因
     */
    void reject(Long taskId, Long approverId, String comment);
    
    /**
     * 获取用户的待办任务列表
     */
    List<WfTask> getPendingTasks(Long userId);
    
    /**
     * 获取用户的已办任务列表
     */
    List<WfTask> getCompletedTasks(Long userId);
    
    /**
     * 获取合同的审批进度
     * 
     * @param contractId 合同ID
     * @return 流程实例（包含当前节点信息）
     */
    WfInstance getWorkflowProgress(Long contractId);
    
    /**
     * 获取合同的审批历史
     * 
     * @param contractId 合同ID
     * @return 所有相关任务列表
     */
    List<WfTask> getApprovalHistory(Long contractId);
    
    /**
     * 撤销审批流程
     * 仅允许发起人在首个节点前撤销
     * 
     * @param instanceId 流程实例ID
     * @param userId 操作人ID
     */
    void cancelWorkflow(Long instanceId, Long userId);
    
    /**
     * 检查合同是否可以提交审批
     * 
     * @param contract 合同实体
     * @return 匹配的场景配置，null 表示无法提交
     */
    WfScenarioConfig checkSubmittable(Contract contract);
    
    /**
     * 获取合同匹配的审批场景详情（包含节点列表）
     */
    WfScenarioConfig getMatchedScenario(Contract contract);
}
