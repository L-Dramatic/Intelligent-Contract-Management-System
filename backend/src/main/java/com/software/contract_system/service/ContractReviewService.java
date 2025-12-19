package com.software.contract_system.service;

import com.software.contract_system.dto.ContractReviewDTO;
import com.software.contract_system.entity.ContractReview;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 合同审查服务接口
 * 
 * 负责AI审查报告的触发、存储和查询
 */
public interface ContractReviewService {

    /**
     * 异步触发AI审查
     * 
     * @param contractId 合同ID
     * @param triggeredBy 触发人ID (null表示自动触发)
     * @param reviewType 审查类型: AUTO/MANUAL
     * @return 异步结果，包含审查报告ID
     */
    CompletableFuture<Long> triggerReviewAsync(Long contractId, Long triggeredBy, String reviewType);

    /**
     * 保存审查报告
     * 
     * @param review 审查报告实体
     * @return 保存后的审查报告
     */
    ContractReview saveReviewReport(ContractReview review);

    /**
     * 更新审查报告（AI审查完成后调用）
     * 
     * @param reviewId 审查报告ID
     * @param aiResponse AI服务返回的审查结果
     */
    void updateReviewResult(Long reviewId, String aiResponse);

    /**
     * 根据合同ID获取最新的审查报告
     * 
     * @param contractId 合同ID
     * @return 审查报告DTO，如果不存在返回null
     */
    ContractReviewDTO getLatestReview(Long contractId);

    /**
     * 根据合同ID获取所有审查报告
     * 
     * @param contractId 合同ID
     * @return 审查报告列表
     */
    List<ContractReviewDTO> getReviewHistory(Long contractId);

    /**
     * 根据审查报告ID获取详情
     * 
     * @param reviewId 审查报告ID
     * @return 审查报告DTO
     */
    ContractReviewDTO getReviewById(Long reviewId);

    /**
     * 手动重新触发AI审查
     * 
     * @param contractId 合同ID
     * @param userId 操作人ID
     * @return 新创建的审查报告ID
     */
    Long retriggerReview(Long contractId, Long userId);
}

