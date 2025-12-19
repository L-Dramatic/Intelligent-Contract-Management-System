package com.software.contract_system.service.impl;

import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.WfDefinition;
import com.software.contract_system.mapper.WfDefinitionMapper;
import com.software.contract_system.service.WorkflowRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 审批流程引擎实现类（临时版本）
 * 
 * 当前实现：使用硬编码的默认流程（ID=1）
 * 
 * TODO: 等待审批流程引擎设计方案完成后替换为正式实现
 * 
 * 预期实现逻辑：
 * 1. 优先级1：指定专用流程（如Type A专用流程）
 * 2. 优先级2：部门专用流程
 * 3. 优先级3：通用金额流程（根据金额范围）
 * 4. 优先级4：系统默认兜底流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowRuleEngineImpl implements WorkflowRuleEngine {

    private final WfDefinitionMapper definitionMapper;

    // 金额阈值常量（可配置化）
    private static final BigDecimal AMOUNT_SMALL = new BigDecimal("50000");      // 5万
    private static final BigDecimal AMOUNT_MEDIUM = new BigDecimal("500000");    // 50万

    @Override
    public WfDefinition matchDefinition(Contract contract) {
        log.info("匹配审批流程: contractId={}, type={}, amount={}", 
                contract.getId(), contract.getType(), contract.getAmount());
        
        // TODO: 正式实现应该从配置表中读取规则
        // 目前使用硬编码的临时实现
        
        String contractType = normalizeContractType(contract.getType());
        BigDecimal amount = contract.getAmount() != null ? contract.getAmount() : BigDecimal.ZERO;
        
        // 尝试按优先级匹配
        WfDefinition definition = null;
        
        // 优先级1：Type专用流程
        // TODO: 从 wf_definition 表中查询 apply_type = contractType 的流程
        // definition = definitionMapper.selectByApplyType(contractType);
        
        // 优先级2：金额流程
        if (definition == null) {
            // TODO: 根据金额匹配流程
            // 小于5万 -> 简易流程
            // 5万-50万 -> 标准流程
            // 大于50万 -> 重大流程
            String amountLevel = getAmountLevel(amount);
            log.debug("金额级别: {}", amountLevel);
            // definition = definitionMapper.selectByAmountLevel(amountLevel);
        }
        
        // 优先级3：默认兜底流程
        if (definition == null) {
            log.info("使用默认兜底流程: ID=1");
            definition = definitionMapper.selectById(1L);
        }
        
        if (definition != null) {
            log.info("匹配到审批流程: id={}, name={}", definition.getId(), definition.getName());
        } else {
            log.warn("未找到匹配的审批流程");
        }
        
        return definition;
    }

    @Override
    public boolean hasMatchingDefinition(Contract contract) {
        return matchDefinition(contract) != null;
    }

    @Override
    public String getMatchReason(Contract contract) {
        String contractType = normalizeContractType(contract.getType());
        BigDecimal amount = contract.getAmount() != null ? contract.getAmount() : BigDecimal.ZERO;
        String amountLevel = getAmountLevel(amount);
        
        // TODO: 返回实际匹配原因
        return String.format("合同类型=%s, 金额级别=%s, 使用默认流程（临时实现）", 
                contractType, amountLevel);
    }

    /**
     * 标准化合同类型
     */
    private String normalizeContractType(String type) {
        if (type == null) return "TYPE_A";
        
        type = type.toUpperCase().trim();
        
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
        
        return "TYPE_A";
    }

    /**
     * 根据金额获取金额级别
     */
    private String getAmountLevel(BigDecimal amount) {
        if (amount.compareTo(AMOUNT_SMALL) < 0) {
            return "SMALL";      // 小额（<5万）
        } else if (amount.compareTo(AMOUNT_MEDIUM) < 0) {
            return "MEDIUM";     // 中等金额（5万-50万）
        } else {
            return "LARGE";      // 大额（≥50万）
        }
    }
}

