package com.software.contract_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI对话响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatResponse {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 模式: ASK, AGENT
     */
    private String mode;
    
    /**
     * AI回复内容
     */
    private String content;
    
    /**
     * 是否执行成功（Agent模式）
     */
    private Boolean success;
    
    /**
     * Agent执行的操作列表
     */
    private List<AgentAction> actions;
    
    /**
     * 建议操作（Ask模式可能给出的建议）
     */
    private List<String> suggestions;
    
    /**
     * 错误信息
     */
    private String error;

    /**
     * Agent操作详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentAction {
        /**
         * 操作类型: MODIFY, INSERT, DELETE, REPLACE, GENERATE
         */
        private String actionType;
        
        /**
         * 修改的字段路径
         */
        private String fieldPath;
        
        /**
         * 位置描述
         */
        private String locationDesc;
        
        /**
         * 旧值
         */
        private String oldValue;
        
        /**
         * 新值
         */
        private String newValue;
        
        /**
         * 撤销令牌
         */
        private String undoToken;
        
        /**
         * 是否可撤销
         */
        private Boolean canUndo;
    }
}
