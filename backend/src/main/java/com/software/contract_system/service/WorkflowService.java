package com.software.contract_system.service;

import com.software.contract_system.dto.ApproveDTO;
import com.software.contract_system.entity.WfTask;
import java.util.List;

public interface WorkflowService {

    /**
     * 提交合同，开启审批流程
     * @param contractId 合同ID
     * @param userId 提交人ID
     */
    void submitContract(Long contractId, Long userId);

    /**
     * 审批任务 (通过/驳回)
     * @param approveDTO 审批参数
     * @param userId 当前操作人ID
     */
    void approveTask(ApproveDTO approveDTO, Long userId);

    /**
     * 获取某人的待办任务列表
     * @param userId 用户ID
     * @return 任务列表
     */
    List<WfTask> getMyTasks(Long userId);
}