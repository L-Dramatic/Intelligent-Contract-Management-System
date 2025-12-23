package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合同编辑历史实体
 * 用于Agent模式的撤销功能
 */
@Data
@TableName("t_contract_edit_history")
public class ContractEditHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 合同ID
     */
    private Long contractId;

    /**
     * 关联的AI会话ID
     */
    private String sessionId;

    /**
     * 编辑类型: MANUAL, AI_AGENT
     */
    private String editType;

    /**
     * 操作: MODIFY, INSERT, DELETE, REPLACE
     */
    private String action;

    /**
     * 修改的字段路径
     */
    private String fieldPath;

    /**
     * 位置描述（第几条第几款）
     */
    private String locationDesc;

    /**
     * 修改前的值
     */
    private String oldValue;

    /**
     * 修改后的值
     */
    private String newValue;

    /**
     * 修改前的完整内容（用于撤销）
     */
    private String fullContentBefore;

    /**
     * 是否已撤销
     */
    private Integer isUndone;

    /**
     * 撤销令牌
     */
    private String undoToken;

    /**
     * 操作人ID
     */
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ==================== 常量 ====================
    public static final String EDIT_TYPE_MANUAL = "MANUAL";
    public static final String EDIT_TYPE_AI_AGENT = "AI_AGENT";

    public static final String ACTION_MODIFY = "MODIFY";
    public static final String ACTION_INSERT = "INSERT";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_REPLACE = "REPLACE";
}
