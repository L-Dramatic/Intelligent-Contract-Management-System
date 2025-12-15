package com.software.contract_system.controller;

import com.software.contract_system.dto.*;
import com.software.contract_system.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * AI服务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI服务", description = "AI辅助合同起草和审核")
public class AIController {
    
    private final AIService aiService;
    
    /**
     * 生成合同条款
     */
    @PostMapping("/generate")
    @Operation(summary = "生成合同条款", description = "根据需求生成专业的合同条款")
    public Mono<AIResponse<String>> generateClause(@RequestBody AIGenerateRequest request) {
        log.info("收到生成条款请求: {}", request);
        return aiService.generateClause(request);
    }
    
    /**
     * 合规性检查
     */
    @PostMapping("/check")
    @Operation(summary = "合规性检查", description = "检查合同条款的合规性和风险")
    public Mono<AIResponse<String>> checkCompliance(@RequestBody AICheckRequest request) {
        log.info("收到合规检查请求: {}", request);
        return aiService.checkCompliance(request);
    }
    
    /**
     * 获取知识库统计
     */
    @GetMapping("/stats")
    @Operation(summary = "知识库统计", description = "获取AI知识库的统计信息")
    public Mono<AIResponse<Object>> getStats() {
        return aiService.getKnowledgeStats();
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查AI服务代理是否正常")
    public Mono<String> health() {
        return Mono.just("AI Service Proxy is running");
    }
}


