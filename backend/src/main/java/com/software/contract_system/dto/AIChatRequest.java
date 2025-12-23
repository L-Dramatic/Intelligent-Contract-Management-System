package com.software.contract_system.dto;

import lombok.Data;

/**
 * AI对话请求（Ask模式和Agent模式共用）
 */
@Data
public class AIChatRequest {
    
    /**
     * 会话ID（首次对话不传，后续对话必传）
     */
    private String sessionId;
    
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 模式: ASK, AGENT
     */
    private String mode;
    
    /**
     * 合同ID（如果是编辑已有合同）
     */
    private Long contractId;
    
    /**
     * 合同子类型代码（如果是新建合同）
     */
    private String subTypeCode;
    
    /**
     * 当前合同内容（前端传递，用于Agent模式分析）
     */
    private String currentContent;
    
    /**
     * 是否流式输出
     */
    private Boolean stream = true;
}
