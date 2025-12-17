package com.software.contract_system.service.impl;

import com.software.contract_system.dto.*;
import com.software.contract_system.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * AI服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    
    private final WebClient aiWebClient;
    
    @Override
    public Mono<AIResponse<String>> generateClause(AIGenerateRequest request) {
        log.info("调用AI生成条款: contractType={}, clauseType={}", 
                request.getContractType(), request.getClauseType());
        
        return aiWebClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AIResponse<String>>(){})
                .timeout(Duration.ofSeconds(60)) // 生成条款需要较长时间，设置60秒
                .doOnSuccess(response -> log.info("AI生成条款成功"))
                .doOnError(e -> log.error("AI生成条款失败", e))
                .onErrorReturn(createErrorResponse("AI服务调用失败，请稍后重试"));
    }
    
    @Override
    public Mono<AIResponse<String>> checkCompliance(AICheckRequest request) {
        log.info("调用AI合规检查: contractType={}", request.getContractType());
        
        return aiWebClient.post()
                .uri("/api/check")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AIResponse<String>>(){})
                .timeout(Duration.ofSeconds(45)) // 合规检查设置45秒
                .doOnSuccess(response -> log.info("AI合规检查成功"))
                .doOnError(e -> log.error("AI合规检查失败", e))
                .onErrorReturn(createErrorResponse("AI服务调用失败，请稍后重试"));
    }
    
    @Override
    public Mono<AIResponse<Object>> getKnowledgeStats() {
        log.info("获取AI知识库统计");
        
        return aiWebClient.get()
                .uri("/api/knowledge/stats")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AIResponse<Object>>(){})
                .timeout(Duration.ofSeconds(10))
                .doOnError(e -> log.error("获取知识库统计失败", e))
                .onErrorReturn(createErrorResponseObject("获取统计信息失败"));
    }
    
    private AIResponse<String> createErrorResponse(String message) {
        return AIResponse.error(message);
    }
    
    private AIResponse<Object> createErrorResponseObject(String message) {
        AIResponse<Object> response = new AIResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}

