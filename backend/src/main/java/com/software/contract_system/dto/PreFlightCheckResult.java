package com.software.contract_system.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Pre-Flight Check 检查结果
 * 
 * 用于返回提交前检查的结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreFlightCheckResult {

    /**
     * 是否通过检查（无CRITICAL级别的错误）
     */
    private boolean passed;

    /**
     * 是否需要用户确认（有HIGH级别的警告）
     */
    private boolean requiresConfirmation;

    /**
     * 阻断性错误列表（CRITICAL级别，必须修复才能提交）
     */
    @Builder.Default
    private List<CheckItem> blockingErrors = new ArrayList<>();

    /**
     * 警告列表（HIGH级别，需要用户确认）
     */
    @Builder.Default
    private List<CheckItem> warnings = new ArrayList<>();

    /**
     * 提示列表（MEDIUM/LOW级别，仅供参考）
     */
    @Builder.Default
    private List<CheckItem> notices = new ArrayList<>();

    /**
     * 通过的检查项
     */
    @Builder.Default
    private List<CheckItem> passedItems = new ArrayList<>();

    /**
     * 检查项详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckItem {
        /**
         * 规则编码
         */
        private String ruleCode;

        /**
         * 规则名称
         */
        private String ruleName;

        /**
         * 规则类别: ATTACHMENT, LOGIC, RISK
         */
        private String category;

        /**
         * 强制级别: CRITICAL, HIGH, MEDIUM, LOW
         */
        private String mandateLevel;

        /**
         * 检查结果消息
         */
        private String message;

        /**
         * 详细说明
         */
        private String detail;

        /**
         * 修复建议
         */
        private String suggestion;

        /**
         * 是否通过
         */
        private boolean passed;
    }

    /**
     * 添加阻断性错误
     */
    public void addBlockingError(CheckItem item) {
        item.setPassed(false);
        this.blockingErrors.add(item);
        this.passed = false;
    }

    /**
     * 添加警告
     */
    public void addWarning(CheckItem item) {
        item.setPassed(false);
        this.warnings.add(item);
        this.requiresConfirmation = true;
    }

    /**
     * 添加提示
     */
    public void addNotice(CheckItem item) {
        item.setPassed(false);
        this.notices.add(item);
    }

    /**
     * 添加通过的检查项
     */
    public void addPassedItem(CheckItem item) {
        item.setPassed(true);
        this.passedItems.add(item);
    }

    /**
     * 获取所有错误和警告的总数
     */
    public int getTotalIssues() {
        return blockingErrors.size() + warnings.size() + notices.size();
    }

    /**
     * 获取摘要信息
     */
    public String getSummary() {
        if (passed && !requiresConfirmation) {
            return "所有检查已通过";
        } else if (!passed) {
            return String.format("发现 %d 个阻断性问题，请修复后再提交", blockingErrors.size());
        } else {
            return String.format("发现 %d 个警告，请确认后继续", warnings.size());
        }
    }

    /**
     * 创建通过的结果
     */
    public static PreFlightCheckResult success() {
        return PreFlightCheckResult.builder()
                .passed(true)
                .requiresConfirmation(false)
                .build();
    }

    /**
     * 创建失败的结果
     */
    public static PreFlightCheckResult failure(String message) {
        PreFlightCheckResult result = PreFlightCheckResult.builder()
                .passed(false)
                .requiresConfirmation(false)
                .build();
        result.addBlockingError(CheckItem.builder()
                .ruleCode("SYSTEM")
                .ruleName("系统错误")
                .message(message)
                .build());
        return result;
    }
}

