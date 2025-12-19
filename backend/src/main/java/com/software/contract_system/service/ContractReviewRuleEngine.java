package com.software.contract_system.service;

import com.software.contract_system.dto.PreFlightCheckResult;
import com.software.contract_system.entity.Contract;

import java.util.List;
import java.util.Map;

/**
 * 合同审查规则引擎接口
 * 
 * 负责执行Pre-Flight Check：
 * 1. 必须附件检查
 * 2. 数值逻辑校验
 * 3. 基础合规检查
 */
public interface ContractReviewRuleEngine {

    /**
     * 执行完整的Pre-Flight Check
     * 
     * @param contract 合同实体
     * @param attachments 附件列表（文件名列表）
     * @return 检查结果
     */
    PreFlightCheckResult preFlightCheck(Contract contract, List<String> attachments);

    /**
     * 仅检查必须附件
     * 
     * @param contractType 合同类型
     * @param attachments 附件列表（文件名列表）
     * @return 检查结果
     */
    PreFlightCheckResult checkAttachments(String contractType, List<String> attachments);

    /**
     * 仅执行数值逻辑校验
     * 
     * @param contract 合同实体
     * @return 检查结果
     */
    PreFlightCheckResult validateLogic(Contract contract);

    /**
     * 仅执行风险关键词检查
     * 
     * @param contractType 合同类型
     * @param content 合同内容
     * @return 检查结果
     */
    PreFlightCheckResult checkRiskKeywords(String contractType, String content);

    /**
     * 检查是否可以提交审批
     * 
     * @param contract 合同实体
     * @param attachments 附件列表
     * @param userConfirmed 用户是否已确认警告
     * @return true表示可以提交，false表示需要修复或确认
     */
    boolean canSubmit(Contract contract, List<String> attachments, boolean userConfirmed);
}

