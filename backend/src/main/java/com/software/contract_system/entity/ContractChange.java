package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 合同变更申请实体
 * 对应表 t_contract_change
 * 
 * 变更类型：金额变更、时间变更、技术方案变更、附件补充、联系人变更、其他
 * 变更原因：本方主动、应乙方要求、不可抗力、政策调整、其他
 */
@Data
@TableName(value = "t_contract_change", autoResultMap = true)
public class ContractChange {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contractId;  // 原合同ID
    private String changeNo;  // 变更单号
    private String title;     // 标题
    
    // 变更版本号 (v2.0, v3.0...)
    private String changeVersion;

    // 变更详情
    // 变更类型: AMOUNT(金额), TIME(时间), TECH(技术方案), ATTACHMENT(附件), CONTACT(联系人), OTHER(其他)
    private String changeType;
    // 变更原因: ACTIVE(本方主动), PASSIVE(应乙方要求), FORCE_MAJEURE(不可抗力), POLICY(政策调整), OTHER(其他)
    private String reasonType;
    private String description;  // 变更说明
    
    // 乙方沟通记录
    private String partyBCommunication;

    // ★★★ 核心：变更前后的对比数据 (JSON) ★★★
    // 包含 beforeContent 和 afterContent
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> diffData;
    
    // 是否重大变更（金额>20%或核心条款变更）
    private Boolean isMajorChange;
    
    // 变更金额差额
    private BigDecimal amountDiff;

    // 状态: 0:草稿, 1:审批中, 2:已通过, 3:已驳回, 4:已撤销
    private Integer status;
    
    // 审批流程实例ID
    private Long instanceId;

    private Long initiatorId; // 发起人

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 审批完成时间
    private LocalDateTime approvedAt;
    
    // 变更生效时间
    private LocalDateTime effectiveAt;
    
    // 附件路径（多个用逗号分隔）
    private String attachmentPath;
}