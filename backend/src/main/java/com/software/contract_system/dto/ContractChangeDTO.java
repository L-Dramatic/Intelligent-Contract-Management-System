package com.software.contract_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 合同变更请求DTO
 */
@Data
public class ContractChangeDTO {
    
    private Long contractId;           // 原合同ID
    private String title;              // 变更标题
    private String changeType;         // 变更类型: AMOUNT, TIME, TECH, ATTACHMENT, CONTACT, OTHER
    private String reasonType;         // 变更原因: ACTIVE, PASSIVE, FORCE_MAJEURE, POLICY, OTHER
    private String description;        // 变更说明
    private String partyBCommunication; // 乙方沟通记录
    
    // 变更后的合同数据
    private String newName;            // 新名称
    private BigDecimal newAmount;      // 新金额
    private String newContent;         // 新正文
    private String newPartyB;          // 新乙方
    private Map<String, Object> newAttributes; // 新扩展属性
    
    private String attachmentPath;     // 附件路径
}

