package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批场景配置实体类
 * 对应数据库表 wf_scenario_config
 * 对应 Master Workflow Configuration Matrix 中的场景定义
 */
@Data
@TableName("wf_scenario_config")
public class WfScenarioConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 场景ID（如 A1-Tier1, B2-Tier2, C3-Tier3） */
    private String scenarioId;
    
    /** 合同子类型代码（A1, A2, A3, B1, B2, B3, B4, C1, C2, C3） */
    private String subTypeCode;
    
    /** 合同子类型名称（如 土建工程, 基站代维, DICT集成） */
    private String subTypeName;
    
    /** 金额下限（含），单位：元 */
    private BigDecimal amountMin;
    
    /** 金额上限（不含），NULL表示无上限，单位：元 */
    private BigDecimal amountMax;
    
    /** 是否快速通道（应急保障类） */
    private Integer isFastTrack;
    
    /** 场景描述 */
    private String description;
    
    /** 是否启用 */
    private Integer isActive;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
    
    // ========== 非数据库字段 ==========
    
    /** 审批节点列表 */
    @TableField(exist = false)
    private List<WfScenarioNode> nodes;
    
    // ========== 合同子类型常量 ==========
    // Type A - 工程施工合同
    public static final String TYPE_A1 = "A1";  // 土建工程
    public static final String TYPE_A2 = "A2";  // 装修工程
    public static final String TYPE_A3 = "A3";  // 零星维修
    
    // Type B - 代维服务合同
    public static final String TYPE_B1 = "B1";  // 光缆代维
    public static final String TYPE_B2 = "B2";  // 基站代维
    public static final String TYPE_B3 = "B3";  // 家宽代维
    public static final String TYPE_B4 = "B4";  // 应急保障
    
    // Type C - IT/DICT合同
    public static final String TYPE_C1 = "C1";  // 定制开发
    public static final String TYPE_C2 = "C2";  // 商用软件采购
    public static final String TYPE_C3 = "C3";  // DICT集成
    
    // ========== 金额阶梯常量（单位：元） ==========
    public static final BigDecimal TIER1_MAX = new BigDecimal("10000");      // < 1万
    public static final BigDecimal TIER2_MAX = new BigDecimal("50000");      // 1-5万
    public static final BigDecimal TIER3_MAX = new BigDecimal("500000");     // 5-50万
    // Tier4: > 50万 (三重一大)
}
