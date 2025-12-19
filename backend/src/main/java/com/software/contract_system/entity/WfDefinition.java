package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 流程定义实体
 * 对应表 wf_definition
 * 
 * 支持按合同类型和条件表达式匹配流程
 * - 适用合同类型: TYPE_A, TYPE_B, TYPE_C（可多选，逗号分隔）
 * - 适用条件表达式: 如 amount < 50000, amount >= 500000 等
 */
@Data
@TableName("wf_definition")
public class WfDefinition {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 流程名称 (如: Type A工程施工标准流程)
    private String name;
    
    // 流程描述
    private String description;
    
    // 适用合同类型 (TYPE_A, TYPE_B, TYPE_C，多个用逗号分隔)
    private String applyType;
    
    // 适用条件表达式 (如: amount < 50000)
    private String conditionExpr;
    
    // 版本号
    private Integer version;
    
    // 是否启用 (0:禁用, 1:启用)
    private Integer isActive;
    
    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}