package com.software.contract_system.service;

import com.software.contract_system.dto.ApproveDTO;
import com.software.contract_system.dto.PreFlightCheckResult;
import com.software.contract_system.entity.WfTask;
import java.util.List;

public interface WorkflowService {

    /**
     * 提交合同，开启审批流程（简易版，无附件检查）
     * @param contractId 合同ID
     * @param userId 提交人ID
     */
    void submitContract(Long contractId, Long userId);

    /**
     * 提交合同，开启审批流程（完整版，带Pre-Flight Check）
     * @param contractId 合同ID
     * @param userId 提交人ID
     * @param attachments 附件文件名列表
     * @param userConfirmed 用户是否已确认警告
     */
    void submitContract(Long contractId, Long userId, List<String> attachments, boolean userConfirmed);

    /**
     * 执行Pre-Flight Check（提交前检查）
     * @param contractId 合同ID
     * @param attachments 附件文件名列表
     * @return 检查结果
     */
    PreFlightCheckResult preFlightCheck(Long contractId, List<String> attachments);

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