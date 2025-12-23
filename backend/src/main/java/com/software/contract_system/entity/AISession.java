package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI会话实体
 * 用于管理AI对话的上下文
 */
@Data
@TableName(value = "t_ai_session", autoResultMap = true)
public class AISession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话唯一标识
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联的合同ID（起草中可能还没保存）
     */
    private Long contractId;

    /**
     * 当前模式: ASK, AGENT
     */
    private String mode;

    /**
     * 会话上下文（历史消息摘要等）
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> contextData;

    /**
     * 合同内容快照（用于AI上下文）
     */
    private String contractSnapshot;

    /**
     * 合同子类型（用于上下文）
     */
    private String subTypeCode;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ==================== 常量 ====================
    public static final String MODE_ASK = "ASK";
    public static final String MODE_AGENT = "AGENT";
}
