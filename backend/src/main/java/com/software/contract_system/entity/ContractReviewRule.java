package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 合同审查规则配置实体类
 * 对应数据库表 t_contract_review_rule
 * 
 * 用于配置Pre-Flight Check的检查规则
 */
@Data
@TableName(value = "t_contract_review_rule", autoResultMap = true)
public class ContractReviewRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 合同类型: TYPE_A, TYPE_B, TYPE_C
     */
    private String contractType;

    /**
     * 规则类别:
     * - ATTACHMENT: 附件检查
     * - LOGIC: 数值逻辑检查
     * - RISK: 风险检查（语义）
     */
    private String ruleCategory;

    /**
     * 规则编码: DOC_A_001, LOGIC_A_001, RISK_A_001等
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则配置 (JSON格式)
     * 附件规则示例: {"keywords": ["安全生产协议"], "filePattern": ".*安全.*"}
     * 逻辑规则示例: {"field": "safetyFeeRatio", "operator": ">=", "threshold": 0.015}
     * 风险规则示例: {"keywords": ["转包"], "context": "negative"}
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> ruleConfig;

    /**
     * 强制级别:
     * - CRITICAL: 阻断，必须满足才能提交
     * - HIGH: 警告，需要用户确认后才能继续
     * - MEDIUM: 警告，显示但不阻断
     * - LOW: 提示信息
     */
    private String mandateLevel;

    /**
     * 优先级 (数值越小优先级越高)
     */
    private Integer priority;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

