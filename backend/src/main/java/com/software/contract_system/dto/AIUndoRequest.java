package com.software.contract_system.dto;

import lombok.Data;

/**
 * Agent撤销请求
 */
@Data
public class AIUndoRequest {
    
    /**
     * 撤销令牌
     */
    private String undoToken;
    
    /**
     * 合同ID
     */
    private Long contractId;
}
