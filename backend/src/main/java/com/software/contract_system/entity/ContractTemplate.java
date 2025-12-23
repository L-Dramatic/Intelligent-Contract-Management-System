package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 合同模板实体
 * 每个合同子类型对应一个标准模板
 */
@Data
@TableName(value = "t_contract_template", autoResultMap = true)
public class ContractTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 主类型: TYPE_A, TYPE_B, TYPE_C
     */
    private String type;

    /**
     * 子类型代码: A1, A2, A3, B1-B4, C1-C3
     */
    private String subTypeCode;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板说明
     */
    private String description;

    /**
     * 模板内容（HTML/Markdown格式）
     */
    private String content;

    /**
     * 可编辑字段定义（JSON Schema）
     * 定义哪些字段是可编辑的，以及字段的类型、验证规则等
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> fieldsSchema;

    /**
     * 锁定区域标记
     * 标记哪些条款是不可修改的（法务审核过的标准条款）
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> lockedSections;

    /**
     * 是否默认模板
     */
    private Integer isDefault;

    /**
     * 是否启用
     */
    private Integer isActive;

    /**
     * 模板版本
     */
    private String version;

    /**
     * 创建人ID
     */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
