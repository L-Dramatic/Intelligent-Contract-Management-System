package com.software.contract_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 合同传输对象
 * 用于接收前端创建/修改合同的参数
 */
@Data
public class ContractDTO {
    
    // 修改时必传，创建时不传
    private Long id;

    // 合同名称
    private String name;

    // 类型: BASE_STATION, NETWORK...
    private String type;

    // 甲方 (通常默认是运营商自己，但也允许前端传)
    private String partyA;

    // 乙方 (必填)
    private String partyB;

    // 金额
    private BigDecimal amount;

    // 合同正文 (HTML 或 Markdown)
    private String content;

    // 扩展属性 (基站地址、SLA指标等，前端传 JSON 对象)
    private Map<String, Object> attributes;
    
    // 是否是草稿 (true: 保存草稿, false: 直接提交审批)
    private Boolean isDraft;
}