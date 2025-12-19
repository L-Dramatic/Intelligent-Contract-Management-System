package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 合同审查报告实体类
 * 对应数据库表 t_contract_review
 * 
 * 存储AI审查服务生成的风险审查报告
 */
@Data
@TableName(value = "t_contract_review", autoResultMap = true)
public class ContractReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的合同ID
     */
    private Long contractId;

    /**
     * 风险等级: LOW, MEDIUM, HIGH
     */
    private String riskLevel;

    /**
     * 综合评分 (0-100)
     */
    private Integer score;

    /**
     * 审查内容 (JSON格式)
     * 包含高风险项、中风险项、低风险项列表
     * 结构示例:
     * {
     *   "highRisks": [...],
     *   "mediumRisks": [...],
     *   "lowRisks": [...],
     *   "positivePoints": [...]
     * }
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> reviewContent;

    /**
     * 审查类型: AUTO(自动), MANUAL(手动)
     */
    private String reviewType;

    /**
     * 是否使用了RAG检索
     */
    private Boolean ragUsed;

    /**
     * 审查状态: PENDING(进行中), COMPLETED(完成), FAILED(失败)
     */
    private String status;

    /**
     * 错误信息(如果审查失败)
     */
    private String errorMessage;

    /**
     * 审查触发人ID (如果是手动触发)
     */
    private Long triggeredBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}

