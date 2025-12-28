package com.software.contract_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 合同变更视图对象
 */
@Data
public class ContractChangeVO {
    
    private Long id;
    private Long contractId;
    private String contractNo;         // 原合同编号
    private String contractName;       // 原合同名称
    private String changeNo;           // 变更单号
    private String title;
    private String changeVersion;
    
    private String changeType;
    private String changeTypeName;     // 变更类型名称
    private String reasonType;
    private String reasonTypeName;     // 变更原因名称
    private String description;
    private String partyBCommunication;
    
    // 变更对比数据
    private Map<String, Object> diffData;
    private List<DiffItem> diffItems;  // 格式化的对比项
    
    private Boolean isMajorChange;
    private BigDecimal amountDiff;
    private Double changePercent;      // 变更百分比
    
    private Integer status;
    private String statusName;
    
    private Long instanceId;
    private Long initiatorId;
    private String initiatorName;
    
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime effectiveAt;
    
    private String attachmentPath;
    
    /**
     * 变更对比项
     */
    @Data
    public static class DiffItem {
        private String fieldName;      // 字段名
        private String fieldLabel;     // 字段显示名
        private Object beforeValue;    // 变更前值
        private Object afterValue;     // 变更后值
        private String changeDesc;     // 变更描述（如：+20万，+20%）
    }
}

