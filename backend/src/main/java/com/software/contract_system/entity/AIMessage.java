package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI会话消息实体
 * 记录每一轮对话
 */
@Data
@TableName(value = "t_ai_message", autoResultMap = true)
public class AIMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 角色: USER, ASSISTANT, SYSTEM
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 模式: ASK, AGENT
     */
    private String mode;

    /**
     * Agent操作详情（如果是Agent模式）
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> agentAction;

    /**
     * Token消耗
     */
    private Integer tokenCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ==================== 常量 ====================
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ASSISTANT = "ASSISTANT";
    public static final String ROLE_SYSTEM = "SYSTEM";
}
