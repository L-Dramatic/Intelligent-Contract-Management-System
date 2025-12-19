package com.software.contract_system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.software.contract_system.dto.AICheckRequest;
import com.software.contract_system.dto.ContractReviewDTO;
import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.ContractReview;
import com.software.contract_system.mapper.ContractMapper;
import com.software.contract_system.mapper.ContractReviewMapper;
import com.software.contract_system.service.AIService;
import com.software.contract_system.service.ContractReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 合同审查服务实现类
 * 
 * 负责：
 * 1. 异步调用AI服务进行合同审查
 * 2. 解析AI审查结果并存储
 * 3. 提供审查报告的查询接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractReviewServiceImpl implements ContractReviewService {

    private final ContractReviewMapper reviewMapper;
    private final ContractMapper contractMapper;
    private final AIService aiService;

    /**
     * 异步触发AI审查
     * 使用@Async注解实现异步执行，不阻塞主流程
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Long> triggerReviewAsync(Long contractId, Long triggeredBy, String reviewType) {
        log.info("开始异步AI审查: contractId={}, triggeredBy={}, type={}", contractId, triggeredBy, reviewType);
        
        // 1. 创建审查记录（状态为PENDING）
        ContractReview review = new ContractReview();
        review.setContractId(contractId);
        review.setReviewType(reviewType);
        review.setStatus("PENDING");
        review.setTriggeredBy(triggeredBy);
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        
        Long reviewId = review.getId();
        log.info("创建审查记录: reviewId={}", reviewId);
        
        try {
            // 2. 获取合同信息
            Contract contract = contractMapper.selectById(contractId);
            if (contract == null) {
                throw new RuntimeException("合同不存在: " + contractId);
            }
            
            // 3. 构建AI检查请求
            AICheckRequest request = new AICheckRequest();
            request.setContractType(contract.getType());
            request.setClauseContent(contract.getContent());
            
            // 4. 调用AI服务进行审查
            log.info("调用AI服务进行合规检查: contractId={}", contractId);
            aiService.checkCompliance(request)
                .subscribe(
                    response -> {
                        if (Boolean.TRUE.equals(response.getSuccess())) {
                            log.info("AI审查成功: contractId={}", contractId);
                            updateReviewResult(reviewId, response.getData());
                        } else {
                            log.error("AI审查失败: contractId={}, message={}", contractId, response.getMessage());
                            markReviewFailed(reviewId, response.getMessage());
                        }
                    },
                    error -> {
                        log.error("AI审查异常: contractId={}", contractId, error);
                        markReviewFailed(reviewId, error.getMessage());
                    }
                );
            
            return CompletableFuture.completedFuture(reviewId);
            
        } catch (Exception e) {
            log.error("触发AI审查失败: contractId={}", contractId, e);
            markReviewFailed(reviewId, e.getMessage());
            return CompletableFuture.completedFuture(reviewId);
        }
    }

    @Override
    @Transactional
    public ContractReview saveReviewReport(ContractReview review) {
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        reviewMapper.insert(review);
        return review;
    }

    @Override
    @Transactional
    public void updateReviewResult(Long reviewId, String aiResponse) {
        log.info("更新审查结果: reviewId={}", reviewId);
        
        ContractReview review = reviewMapper.selectById(reviewId);
        if (review == null) {
            log.error("审查记录不存在: reviewId={}", reviewId);
            return;
        }
        
        try {
            // 解析AI响应
            Map<String, Object> parsedResult = parseAIResponse(aiResponse);
            
            review.setReviewContent(parsedResult);
            review.setRiskLevel(determineRiskLevel(parsedResult));
            review.setScore(calculateScore(parsedResult));
            review.setRagUsed(true);
            review.setStatus("COMPLETED");
            review.setCompletedAt(LocalDateTime.now());
            
            reviewMapper.updateById(review);
            log.info("审查结果更新完成: reviewId={}, riskLevel={}, score={}", 
                    reviewId, review.getRiskLevel(), review.getScore());
            
        } catch (Exception e) {
            log.error("解析AI响应失败: reviewId={}", reviewId, e);
            markReviewFailed(reviewId, "解析AI响应失败: " + e.getMessage());
        }
    }

    @Override
    public ContractReviewDTO getLatestReview(Long contractId) {
        ContractReview review = reviewMapper.selectLatestByContractId(contractId);
        if (review == null) {
            return null;
        }
        return convertToDTO(review);
    }

    @Override
    public List<ContractReviewDTO> getReviewHistory(Long contractId) {
        List<ContractReview> reviews = reviewMapper.selectByContractId(contractId);
        List<ContractReviewDTO> dtoList = new ArrayList<>();
        for (ContractReview review : reviews) {
            dtoList.add(convertToDTO(review));
        }
        return dtoList;
    }

    @Override
    public ContractReviewDTO getReviewById(Long reviewId) {
        ContractReview review = reviewMapper.selectById(reviewId);
        if (review == null) {
            return null;
        }
        return convertToDTO(review);
    }

    @Override
    public Long retriggerReview(Long contractId, Long userId) {
        log.info("手动重新触发AI审查: contractId={}, userId={}", contractId, userId);
        triggerReviewAsync(contractId, userId, "MANUAL");
        // 返回占位值，实际审查ID会在异步完成后生成
        return null;
    }

    /**
     * 标记审查失败
     */
    private void markReviewFailed(Long reviewId, String errorMessage) {
        ContractReview review = reviewMapper.selectById(reviewId);
        if (review != null) {
            review.setStatus("FAILED");
            review.setErrorMessage(errorMessage);
            review.setCompletedAt(LocalDateTime.now());
            reviewMapper.updateById(review);
            log.info("审查标记为失败: reviewId={}", reviewId);
        }
    }

    /**
     * 解析AI响应为结构化数据
     */
    private Map<String, Object> parseAIResponse(String aiResponse) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 尝试解析为JSON
            if (aiResponse != null && aiResponse.trim().startsWith("{")) {
                JSONObject json = JSON.parseObject(aiResponse);
                result.putAll(json);
            } else {
                // 如果不是JSON，按文本处理
                result.put("rawContent", aiResponse);
                result.put("highRisks", extractRisksFromText(aiResponse, "高风险"));
                result.put("mediumRisks", extractRisksFromText(aiResponse, "中风险"));
                result.put("lowRisks", extractRisksFromText(aiResponse, "低风险"));
            }
        } catch (Exception e) {
            log.warn("解析AI响应为JSON失败，使用原始文本: {}", e.getMessage());
            result.put("rawContent", aiResponse);
            result.put("highRisks", new ArrayList<>());
            result.put("mediumRisks", new ArrayList<>());
            result.put("lowRisks", new ArrayList<>());
        }
        
        return result;
    }

    /**
     * 从文本中提取风险项（简单实现）
     */
    private List<Map<String, Object>> extractRisksFromText(String text, String riskKeyword) {
        List<Map<String, Object>> risks = new ArrayList<>();
        if (text == null) return risks;
        
        // 简单的关键词匹配，实际项目应使用更复杂的NLP
        if (text.contains(riskKeyword)) {
            Map<String, Object> risk = new HashMap<>();
            risk.put("message", "检测到" + riskKeyword + "相关内容");
            risk.put("sourceText", text.substring(0, Math.min(200, text.length())));
            risks.add(risk);
        }
        
        return risks;
    }

    /**
     * 根据审查结果确定风险等级
     */
    private String determineRiskLevel(Map<String, Object> parsedResult) {
        // 如果有高风险项，返回HIGH
        Object highRisks = parsedResult.get("highRisks");
        if (highRisks instanceof List && !((List<?>) highRisks).isEmpty()) {
            return "HIGH";
        }
        
        // 如果有中风险项，返回MEDIUM
        Object mediumRisks = parsedResult.get("mediumRisks");
        if (mediumRisks instanceof List && !((List<?>) mediumRisks).isEmpty()) {
            return "MEDIUM";
        }
        
        // 检查是否有直接的riskLevel字段
        Object riskLevel = parsedResult.get("riskLevel");
        if (riskLevel != null) {
            return riskLevel.toString().toUpperCase();
        }
        
        return "LOW";
    }

    /**
     * 计算综合评分
     */
    private Integer calculateScore(Map<String, Object> parsedResult) {
        // 检查是否有直接的score字段
        Object score = parsedResult.get("score");
        if (score instanceof Number) {
            return ((Number) score).intValue();
        }
        
        // 基于风险项数量计算评分
        int baseScore = 100;
        
        Object highRisks = parsedResult.get("highRisks");
        if (highRisks instanceof List) {
            baseScore -= ((List<?>) highRisks).size() * 20;
        }
        
        Object mediumRisks = parsedResult.get("mediumRisks");
        if (mediumRisks instanceof List) {
            baseScore -= ((List<?>) mediumRisks).size() * 10;
        }
        
        Object lowRisks = parsedResult.get("lowRisks");
        if (lowRisks instanceof List) {
            baseScore -= ((List<?>) lowRisks).size() * 5;
        }
        
        return Math.max(0, Math.min(100, baseScore));
    }

    /**
     * 将实体转换为DTO
     */
    private ContractReviewDTO convertToDTO(ContractReview review) {
        ContractReviewDTO dto = ContractReviewDTO.builder()
                .id(review.getId())
                .contractId(review.getContractId())
                .riskLevel(review.getRiskLevel())
                .score(review.getScore())
                .reviewType(review.getReviewType())
                .status(review.getStatus())
                .errorMessage(review.getErrorMessage())
                .createdAt(review.getCreatedAt())
                .completedAt(review.getCompletedAt())
                .build();
        
        // 解析reviewContent中的风险项
        if (review.getReviewContent() != null) {
            Map<String, Object> content = review.getReviewContent();
            
            dto.setHighRisks(parseRiskItems(content.get("highRisks")));
            dto.setMediumRisks(parseRiskItems(content.get("mediumRisks")));
            dto.setLowRisks(parseRiskItems(content.get("lowRisks")));
            
            Object positivePoints = content.get("positivePoints");
            if (positivePoints instanceof List) {
                List<String> points = new ArrayList<>();
                for (Object point : (List<?>) positivePoints) {
                    points.add(point.toString());
                }
                dto.setPositivePoints(points);
            }
        }
        
        // 获取合同信息（用于展示）
        Contract contract = contractMapper.selectById(review.getContractId());
        if (contract != null) {
            dto.setContractName(contract.getName());
            dto.setContractType(contract.getType());
        }
        
        return dto;
    }

    /**
     * 解析风险项列表
     */
    @SuppressWarnings("unchecked")
    private List<ContractReviewDTO.RiskItem> parseRiskItems(Object risksObj) {
        List<ContractReviewDTO.RiskItem> items = new ArrayList<>();
        
        if (risksObj instanceof List) {
            for (Object riskObj : (List<?>) risksObj) {
                if (riskObj instanceof Map) {
                    Map<String, Object> riskMap = (Map<String, Object>) riskObj;
                    ContractReviewDTO.RiskItem item = ContractReviewDTO.RiskItem.builder()
                            .checkpointId(getStringValue(riskMap, "checkpointId", "checkpoint_id"))
                            .message(getStringValue(riskMap, "message", "description"))
                            .sourceText(getStringValue(riskMap, "sourceText", "source_text"))
                            .suggestion(getStringValue(riskMap, "suggestion", "advice"))
                            .confidence(getDoubleValue(riskMap, "confidence", "confidence_score"))
                            .build();
                    items.add(item);
                }
            }
        }
        
        return items;
    }

    /**
     * 从Map中获取字符串值（支持多个可能的key名）
     */
    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    /**
     * 从Map中获取Double值
     */
    private Double getDoubleValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return null;
    }
}

