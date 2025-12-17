package com.software.contract_system.common;

/**
 * 合同状态常量
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
}

