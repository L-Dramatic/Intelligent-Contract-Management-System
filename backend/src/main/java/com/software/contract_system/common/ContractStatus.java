package com.software.contract_system.common;

/**
 * 合同状态常量
 * 
 * 完整生命周期：草稿 -> 审批中 -> 待签署 -> 已生效 -> 已终止/已作废
 *              草稿 -> 审批中 -> 已驳回 -> (可重新编辑提交)
 */
public class ContractStatus {
    
    /** 草稿 */
    public static final int DRAFT = 0;
    
    /** 审批中 */
    public static final int APPROVING = 1;
    
    /** 已生效 */
    public static final int EFFECTIVE = 2;
    
    /** 已驳回 */
    public static final int REJECTED = 3;
    
    /** 已终止 */
    public static final int TERMINATED = 4;
    
    /** 待签署（审批通过后等待签署） */
    public static final int PENDING_SIGN = 5;
    
    /** 已作废 */
    public static final int VOIDED = 6;
    
    private ContractStatus() {
        // 工具类，不允许实例化
    }
    
    /**
     * 获取状态名称
     */
    public static String getStatusName(int status) {
        switch (status) {
            case DRAFT: return "草稿";
            case APPROVING: return "审批中";
            case EFFECTIVE: return "已生效";
            case REJECTED: return "已驳回";
            case TERMINATED: return "已终止";
            case PENDING_SIGN: return "待签署";
            case VOIDED: return "已作废";
            default: return "未知状态";
        }
    }
    
    /**
     * 判断是否可以编辑
     */
    public static boolean canEdit(int status) {
        return status == DRAFT || status == REJECTED;
    }
    
    /**
     * 判断是否可以提交审批
     */
    public static boolean canSubmit(int status) {
        return status == DRAFT || status == REJECTED;
    }
    
    /**
     * 判断是否可以签署
     */
    public static boolean canSign(int status) {
        return status == PENDING_SIGN;
    }
    
    /**
     * 判断是否可以作废
     */
    public static boolean canVoid(int status) {
        return status == DRAFT || status == REJECTED;
    }
    
    /**
     * 判断是否可以终止
     */
    public static boolean canTerminate(int status) {
        return status == EFFECTIVE;
    }
}

