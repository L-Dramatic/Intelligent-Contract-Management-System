package com.software.contract_system.service.impl;

import com.software.contract_system.dto.PreFlightCheckResult;
import com.software.contract_system.dto.PreFlightCheckResult.CheckItem;
import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.ContractReviewRule;
import com.software.contract_system.mapper.ContractReviewRuleMapper;
import com.software.contract_system.service.ContractReviewRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 合同审查规则引擎实现类
 * 
 * 实现Pre-Flight Check的核心逻辑：
 * 1. 从数据库加载规则配置
 * 2. 执行附件检查、数值逻辑校验、风险关键词检测
 * 3. 返回结构化的检查结果
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractReviewRuleEngineImpl implements ContractReviewRuleEngine {

    private final ContractReviewRuleMapper ruleMapper;

    @Override
    public PreFlightCheckResult preFlightCheck(Contract contract, List<String> attachments) {
        log.info("执行Pre-Flight Check: contractId={}, type={}", contract.getId(), contract.getType());
        
        PreFlightCheckResult result = PreFlightCheckResult.builder()
                .passed(true)
                .requiresConfirmation(false)
                .build();
        
        String contractType = normalizeContractType(contract.getType());
        
        // 1. 附件检查
        PreFlightCheckResult attachmentResult = checkAttachments(contractType, attachments);
        mergeResults(result, attachmentResult);
        
        // 2. 数值逻辑校验
        PreFlightCheckResult logicResult = validateLogic(contract);
        mergeResults(result, logicResult);
        
        // 3. 风险关键词检查（可选，根据配置决定是否在Pre-Flight阶段执行）
        if (contract.getContent() != null && !contract.getContent().isEmpty()) {
            PreFlightCheckResult riskResult = checkRiskKeywords(contractType, contract.getContent());
            mergeResults(result, riskResult);
        }
        
        log.info("Pre-Flight Check完成: passed={}, blockingErrors={}, warnings={}", 
                result.isPassed(), result.getBlockingErrors().size(), result.getWarnings().size());
        
        return result;
    }

    @Override
    public PreFlightCheckResult checkAttachments(String contractType, List<String> attachments) {
        log.debug("检查附件: contractType={}, attachmentCount={}", contractType, 
                attachments != null ? attachments.size() : 0);
        
        PreFlightCheckResult result = PreFlightCheckResult.builder()
                .passed(true)
                .requiresConfirmation(false)
                .build();
        
        if (attachments == null) {
            attachments = Collections.emptyList();
        }
        
        // 获取附件检查规则
        List<ContractReviewRule> rules = ruleMapper.selectByTypeAndCategory(
                normalizeContractType(contractType), "ATTACHMENT");
        
        for (ContractReviewRule rule : rules) {
            boolean found = checkAttachmentRule(rule, attachments);
            
            CheckItem item = CheckItem.builder()
                    .ruleCode(rule.getRuleCode())
                    .ruleName(rule.getRuleName())
                    .category("ATTACHMENT")
                    .mandateLevel(rule.getMandateLevel())
                    .build();
            
            if (found) {
                item.setMessage("✓ " + rule.getRuleName() + " 已提供");
                result.addPassedItem(item);
            } else {
                item.setMessage("✗ 缺少必须附件: " + rule.getRuleName());
                item.setSuggestion("请上传" + rule.getRuleName() + "相关文件");
                
                addItemByLevel(result, item, rule.getMandateLevel());
            }
        }
        
        return result;
    }

    @Override
    public PreFlightCheckResult validateLogic(Contract contract) {
        log.debug("执行数值逻辑校验: contractId={}", contract.getId());
        
        PreFlightCheckResult result = PreFlightCheckResult.builder()
                .passed(true)
                .requiresConfirmation(false)
                .build();
        
        String contractType = normalizeContractType(contract.getType());
        
        // 获取数值逻辑规则
        List<ContractReviewRule> rules = ruleMapper.selectByTypeAndCategory(contractType, "LOGIC");
        
        // 从合同扩展属性中获取数值
        Map<String, Object> attributes = contract.getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        
        // 添加基础字段
        attributes.put("amount", contract.getAmount());
        
        for (ContractReviewRule rule : rules) {
            boolean passed = checkLogicRule(rule, attributes, contract);
            
            CheckItem item = CheckItem.builder()
                    .ruleCode(rule.getRuleCode())
                    .ruleName(rule.getRuleName())
                    .category("LOGIC")
                    .mandateLevel(rule.getMandateLevel())
                    .build();
            
            if (passed) {
                item.setMessage("✓ " + rule.getRuleName() + " 校验通过");
                result.addPassedItem(item);
            } else {
                Map<String, Object> config = rule.getRuleConfig();
                String errorMsg = config.containsKey("message") 
                        ? config.get("message").toString() 
                        : rule.getRuleName() + " 校验不通过";
                item.setMessage("✗ " + errorMsg);
                item.setSuggestion(rule.getDescription());
                
                addItemByLevel(result, item, rule.getMandateLevel());
            }
        }
        
        return result;
    }

    @Override
    public PreFlightCheckResult checkRiskKeywords(String contractType, String content) {
        log.debug("执行风险关键词检查: contractType={}", contractType);
        
        PreFlightCheckResult result = PreFlightCheckResult.builder()
                .passed(true)
                .requiresConfirmation(false)
                .build();
        
        if (content == null || content.isEmpty()) {
            return result;
        }
        
        // 获取风险检查规则
        List<ContractReviewRule> rules = ruleMapper.selectByTypeAndCategory(
                normalizeContractType(contractType), "RISK");
        
        for (ContractReviewRule rule : rules) {
            boolean riskFound = checkRiskRule(rule, content);
            
            if (riskFound) {
                Map<String, Object> config = rule.getRuleConfig();
                String message = config.containsKey("message") 
                        ? config.get("message").toString() 
                        : "检测到风险: " + rule.getRuleName();
                
                CheckItem item = CheckItem.builder()
                        .ruleCode(rule.getRuleCode())
                        .ruleName(rule.getRuleName())
                        .category("RISK")
                        .mandateLevel(rule.getMandateLevel())
                        .message("⚠ " + message)
                        .suggestion(rule.getDescription())
                        .build();
                
                addItemByLevel(result, item, rule.getMandateLevel());
            }
        }
        
        return result;
    }

    @Override
    public boolean canSubmit(Contract contract, List<String> attachments, boolean userConfirmed) {
        PreFlightCheckResult result = preFlightCheck(contract, attachments);
        
        // 如果有阻断性错误，不能提交
        if (!result.isPassed()) {
            return false;
        }
        
        // 如果需要确认但用户未确认，不能提交
        if (result.isRequiresConfirmation() && !userConfirmed) {
            return false;
        }
        
        return true;
    }

    /**
     * 检查附件规则
     */
    private boolean checkAttachmentRule(ContractReviewRule rule, List<String> attachments) {
        Map<String, Object> config = rule.getRuleConfig();
        
        // 获取关键词列表
        List<String> keywords = getKeywords(config);
        
        // 获取文件名模式
        String filePattern = config.containsKey("filePattern") 
                ? config.get("filePattern").toString() 
                : null;
        
        for (String attachment : attachments) {
            // 关键词匹配
            for (String keyword : keywords) {
                if (attachment.toLowerCase().contains(keyword.toLowerCase())) {
                    return true;
                }
            }
            
            // 正则匹配
            if (filePattern != null) {
                try {
                    if (Pattern.matches(filePattern, attachment)) {
                        return true;
                    }
                } catch (Exception e) {
                    log.warn("正则表达式匹配失败: pattern={}", filePattern, e);
                }
            }
        }
        
        return false;
    }

    /**
     * 检查数值逻辑规则
     */
    private boolean checkLogicRule(ContractReviewRule rule, Map<String, Object> attributes, Contract contract) {
        Map<String, Object> config = rule.getRuleConfig();
        
        String field = config.containsKey("field") ? config.get("field").toString() : null;
        String operator = config.containsKey("operator") ? config.get("operator").toString() : null;
        
        if (field == null) {
            return true; // 配置不完整，默认通过
        }
        
        // 获取字段值
        Object fieldValue = getFieldValue(field, attributes, contract);
        if (fieldValue == null) {
            // 字段不存在，根据规则类型决定是否通过
            return config.containsKey("allowNull") && Boolean.TRUE.equals(config.get("allowNull"));
        }
        
        // 条件规则（如DICT项目检查）
        if (config.containsKey("condition")) {
            return checkConditionRule(config, attributes, contract);
        }
        
        // 比较规则
        if (operator != null && config.containsKey("threshold")) {
            BigDecimal value = toBigDecimal(fieldValue);
            BigDecimal threshold = toBigDecimal(config.get("threshold"));
            
            if (value == null || threshold == null) {
                return true;
            }
            
            return compareValues(value, operator, threshold, config);
        }
        
        return true;
    }

    /**
     * 检查条件规则（如DICT项目必须使用背靠背付款）
     */
    private boolean checkConditionRule(Map<String, Object> config, Map<String, Object> attributes, Contract contract) {
        String condition = config.get("condition").toString();
        String requiredField = config.containsKey("requiredField") ? config.get("requiredField").toString() : null;
        String requiredValue = config.containsKey("requiredValue") ? config.get("requiredValue").toString() : null;
        
        // 检查条件是否满足
        Object conditionValue = attributes.get(config.get("field").toString());
        boolean conditionMet = "true".equalsIgnoreCase(condition) 
                ? Boolean.TRUE.equals(conditionValue)
                : condition.equals(String.valueOf(conditionValue));
        
        if (!conditionMet) {
            return true; // 条件不满足，规则不适用
        }
        
        // 条件满足，检查必须字段
        if (requiredField != null && requiredValue != null) {
            Object actualValue = attributes.get(requiredField);
            return requiredValue.equals(String.valueOf(actualValue));
        }
        
        return true;
    }

    /**
     * 检查风险规则
     */
    private boolean checkRiskRule(ContractReviewRule rule, String content) {
        Map<String, Object> config = rule.getRuleConfig();
        List<String> keywords = getKeywords(config);
        
        String contentLower = content.toLowerCase();
        
        for (String keyword : keywords) {
            if (contentLower.contains(keyword.toLowerCase())) {
                return true; // 发现风险关键词
            }
        }
        
        return false;
    }

    /**
     * 从配置中获取关键词列表
     */
    @SuppressWarnings("unchecked")
    private List<String> getKeywords(Map<String, Object> config) {
        Object keywords = config.get("keywords");
        if (keywords instanceof List) {
            return (List<String>) keywords;
        } else if (keywords instanceof String) {
            return Collections.singletonList((String) keywords);
        }
        return Collections.emptyList();
    }

    /**
     * 获取字段值
     */
    private Object getFieldValue(String field, Map<String, Object> attributes, Contract contract) {
        // 先从attributes中查找
        if (attributes.containsKey(field)) {
            return attributes.get(field);
        }
        
        // 再从contract基础字段中查找
        switch (field) {
            case "amount":
                return contract.getAmount();
            case "type":
                return contract.getType();
            default:
                return null;
        }
    }

    /**
     * 转换为BigDecimal
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 比较数值
     */
    private boolean compareValues(BigDecimal value, String operator, BigDecimal threshold, Map<String, Object> config) {
        int cmp = value.compareTo(threshold);
        
        switch (operator) {
            case ">=":
                return cmp >= 0;
            case ">":
                return cmp > 0;
            case "<=":
                return cmp <= 0;
            case "<":
                return cmp < 0;
            case "==":
            case "=":
                // 支持容差
                if (config.containsKey("tolerance")) {
                    BigDecimal tolerance = toBigDecimal(config.get("tolerance"));
                    if (tolerance != null) {
                        return value.subtract(threshold).abs().compareTo(tolerance) <= 0;
                    }
                }
                return cmp == 0;
            case "!=":
                return cmp != 0;
            default:
                return true;
        }
    }

    /**
     * 根据级别添加检查项
     */
    private void addItemByLevel(PreFlightCheckResult result, CheckItem item, String mandateLevel) {
        switch (mandateLevel) {
            case "CRITICAL":
                result.addBlockingError(item);
                break;
            case "HIGH":
                result.addWarning(item);
                break;
            case "MEDIUM":
            case "LOW":
            default:
                result.addNotice(item);
                break;
        }
    }

    /**
     * 合并检查结果
     */
    private void mergeResults(PreFlightCheckResult target, PreFlightCheckResult source) {
        target.getBlockingErrors().addAll(source.getBlockingErrors());
        target.getWarnings().addAll(source.getWarnings());
        target.getNotices().addAll(source.getNotices());
        target.getPassedItems().addAll(source.getPassedItems());
        
        if (!source.isPassed()) {
            target.setPassed(false);
        }
        if (source.isRequiresConfirmation()) {
            target.setRequiresConfirmation(true);
        }
    }

    /**
     * 标准化合同类型
     */
    private String normalizeContractType(String type) {
        if (type == null) return "TYPE_A";
        
        type = type.toUpperCase().trim();
        
        // 支持多种格式
        if (type.startsWith("TYPE_")) {
            return type;
        }
        if (type.equals("A") || type.contains("工程") || type.contains("施工")) {
            return "TYPE_A";
        }
        if (type.equals("B") || type.contains("代维") || type.contains("维护")) {
            return "TYPE_B";
        }
        if (type.equals("C") || type.contains("IT") || type.contains("软件")) {
            return "TYPE_C";
        }
        
        return "TYPE_A"; // 默认
    }
}

