package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 知识库管理控制器
 * 代理请求到AI服务
 */
@RestController
@RequestMapping("/knowledge")
@Tag(name = "知识库管理", description = "知识库监控与搜索")
@SuppressWarnings({"rawtypes", "unchecked"})
public class KnowledgeController {
    
    @Value("${ai.service.url:http://localhost:8765}")
    private String aiServiceUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 获取知识库统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取知识库统计", description = "获取知识库文档数量和文件统计")
    public Result<Object> getStats() {
        try {
            String url = aiServiceUrl + "/api/knowledge/stats";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return Result.success(response.getBody());
        } catch (Exception e) {
            // AI服务不可用时返回默认数据
            return Result.success(Map.of(
                "document_count", 0,
                "rag_enabled", false,
                "files", Map.of("contracts", 0, "laws", 0, "total", 0),
                "error", "AI服务未启动"
            ));
        }
    }
    
    /**
     * 获取知识库文件列表
     */
    @GetMapping("/files")
    @Operation(summary = "获取文件列表", description = "列出知识库中所有文件")
    public Result<Object> getFiles() {
        try {
            String url = aiServiceUrl + "/api/knowledge/files";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return Result.success(response.getBody());
        } catch (Exception e) {
            return Result.success(Map.of(
                "files", java.util.List.of(),
                "total", 0,
                "error", "AI服务未启动"
            ));
        }
    }
    
    /**
     * 搜索知识库
     */
    @GetMapping("/search")
    @Operation(summary = "搜索知识库", description = "使用RAG搜索相关知识")
    public Result<Object> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            String url = aiServiceUrl + "/api/knowledge/search?query=" + query + "&limit=" + limit;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return Result.success(response.getBody());
        } catch (Exception e) {
            return Result.success(Map.of(
                "results", java.util.List.of(),
                "error", "AI服务未启动，无法搜索"
            ));
        }
    }
    
    /**
     * 重建知识库索引
     */
    @PostMapping("/rebuild-index")
    @Operation(summary = "重建索引", description = "重新处理知识库文件并生成向量索引")
    public Result<Object> rebuildIndex() {
        try {
            String url = aiServiceUrl + "/api/knowledge/rebuild";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            return Result.success(response.getBody());
        } catch (Exception e) {
            return Result.error("AI服务未启动，无法重建索引");
        }
    }
}

