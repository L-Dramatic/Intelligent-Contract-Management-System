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
 * 
 * 支持中国移动三种合同类型：
 * - TYPE_A: 工程施工合同（基站建设、传输管线、室分系统）
 * - TYPE_B: 代维服务合同（基站代维、铁塔维护、机房维护）
 * - TYPE_C: IT服务合同（软件开发、系统集成、DICT项目）
 */
@Data
@TableName(value = "t_contract", autoResultMap = true) // 开启 ResultMap 自动映射，为了 JSON 字段
public class Contract {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 合同编号 (HT-类型-日期-序号)
    private String contractNo;
    
    // 版本号 (v1.0, v2.0...)
    private String version;

    // 合同名称
    private String name;

    // 类型: TYPE_A(工程施工), TYPE_B(代维服务), TYPE_C(IT服务)
    private String type;

    // 甲方（默认为中国移动）& 乙方（外部合作单位）
    private String partyA;
    private String partyB;

    // 金额
    private BigDecimal amount;

    // 合同正文 (长文本)
    private String content;

    // 状态: 0:草稿, 1:审批中, 2:已生效, 3:已驳回, 4:已终止, 5:待签署, 6:已作废
    private Integer status;

    // 是否 AI 生成 (0:否, 1:是)
    private Integer isAiGenerated;

    // 扩展属性 (存储Type A/B/C的特定字段，如安全费比例、SLA指标、DICT标志等)
    // 数据库是 JSON 类型，这里映射为 Map
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> attributes;

    // 创建人 ID
    private Long creatorId;

    // 自动填充创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 自动填充更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}