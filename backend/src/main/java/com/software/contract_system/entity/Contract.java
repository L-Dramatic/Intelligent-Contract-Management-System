package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler; // 刚才加的 fastjson2 派上用场了
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 合同实体类
 * 对应数据库表 t_contract
 */
@Data
@TableName(value = "t_contract", autoResultMap = true) // 开启 ResultMap 自动映射，为了 JSON 字段
public class Contract {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 合同编号 (HT-类型-日期-序号)
    private String contractNo;
    private String version;

    // 合同名称
    private String name;

    // 类型: BASE_STATION(基站), NETWORK(网络建设), PURCHASE(采购), SERVICE(服务)
    private String type;

    // 甲方 & 乙方
    private String partyA;
    private String partyB;

    // 金额
    private BigDecimal amount;

    // 合同正文 (长文本)
    private String content;

    // 状态: 0:草稿, 1:审批中, 2:已生效, 3:已驳回
    private Integer status;

    // 是否 AI 生成 (0:否, 1:是)
    private Integer isAiGenerated;

    // 扩展属性 (存储基站地址、SLA指标等动态字段)
    // 数据库是 JSON 类型，这里映射为 Map
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> attributes;

    // 创建人 ID
    private Long creatorId;

    // 自动填充创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}