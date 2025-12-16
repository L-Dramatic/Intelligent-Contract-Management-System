package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 合同变更申请实体
 * 对应表 t_contract_change
 */
@Data
@TableName(value = "t_contract_change", autoResultMap = true)
public class ContractChange {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contractId;  // 原合同ID
    private String changeNo;  // 变更单号
    private String title;     // 标题

    // 变更详情
    private String changeType; // AMOUNT, TIME...
    private String reasonType; // ACTIVE, PASSIVE
    private String description;

    // ★★★ 核心：变更前后的对比数据 (JSON) ★★★
    // 自动映射为 Map，方便前端展示 "变更前 vs 变更后"
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> diffData;

    // 状态: 0:草稿, 1:审批中, 2:已通过, 3:已驳回
    private Integer status;

    private Long initiatorId; // 发起人

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private LocalDateTime approvedAt;
}