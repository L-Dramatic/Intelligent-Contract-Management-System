package com.software.contract_system.service;

import com.software.contract_system.dto.*;
import reactor.core.publisher.Mono;

/**
 * AI服务接口
 */
public interface AIService {
    
    /**
     * 生成合同条款
     */
    Mono<AIResponse<String>> generateClause(AIGenerateRequest request);
    
    /**
     * 合规性检查
     */
    Mono<AIResponse<String>> checkCompliance(AICheckRequest request);
    
    /**
     * 获取知识库统计
     */
    Mono<AIResponse<Object>> getKnowledgeStats();
}

