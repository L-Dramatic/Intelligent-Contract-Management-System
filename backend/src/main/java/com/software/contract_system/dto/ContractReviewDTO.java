package com.software.contract_system.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 合同审查报告DTO
 * 用于前端展示和API响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractReviewDTO {

    /**
     * 审查报告ID
     */
    private Long id;

    /**
     * 关联的合同ID
     */
    private Long contractId;

    /**
     * 合同名称（关联查询）
     */
    private String contractName;

    /**
     * 合同类型（关联查询）
     */
    private String contractType;

    /**
     * 风险等级: LOW, MEDIUM, HIGH
     */
    private String riskLevel;

    /**
     * 风险等级中文描述
     */
    private String riskLevelText;

    /**
     * 综合评分 (0-100)
     */
    private Integer score;

    /**
     * 高风险项列表
     */
    private List<RiskItem> highRisks;

    /**
     * 中风险项列表
     */
    private List<RiskItem> mediumRisks;

    /**
     * 低风险项列表
     */
    private List<RiskItem> lowRisks;

    /**
     * 已包含的优质条款
     */
    private List<String> positivePoints;

    /**
     * 审查类型: AUTO(自动), MANUAL(手动)
     */
    private String reviewType;

    /**
     * 审查状态: PENDING, COMPLETED, FAILED
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 风险项详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskItem {
        /**
         * 检查点ID (如 RISK_A_001)
         */
        private String checkpointId;

        /**
         * 风险描述
         */
        private String message;

        /**
         * 原文引用
         */
        private String sourceText;

        /**
         * 修改建议
         */
        private String suggestion;

        /**
         * 置信度 (0-1)
         */
        private Double confidence;
    }

    /**
     * 获取风险等级中文描述
     */
    public String getRiskLevelText() {
        if (riskLevel == null) return "未知";
        switch (riskLevel) {
            case "LOW": return "低风险";
            case "MEDIUM": return "中风险";
            case "HIGH": return "高风险";
            default: return riskLevel;
        }
    }
}

