package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 场景审批节点实体类
 * 对应数据库表 wf_scenario_node
 * 定义每个审批场景的具体审批路径
 */
@Data
@TableName("wf_scenario_node")
public class WfScenarioNode {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 场景ID（关联wf_scenario_config.scenario_id） */
    private String scenarioId;
    
    /** 节点顺序（从1开始） */
    private Integer nodeOrder;
    
    /** 审批角色编码（关联sys_role.role_code） */
    private String roleCode;
    
    /** 审批级别（COUNTY-县级, CITY-市级, PROVINCE-省级） */
    private String nodeLevel;
    
    /** 节点名称 */
    private String nodeName;
    
    /** 动作类型 */
    private String actionType;
    
    /** 是否必须节点 */
    private Integer isMandatory;
    
    /** 是否可跳过 */
    private Integer canSkip;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    // ========== 非数据库字段 ==========
    
    /** 角色名称 */
    @TableField(exist = false)
    private String roleName;
    
    /** 角色类别 */
    @TableField(exist = false)
    private String roleCategory;
    
    // ========== 审批级别常量 ==========
    public static final String LEVEL_COUNTY = "COUNTY";    // 县级
    public static final String LEVEL_CITY = "CITY";        // 市级
    public static final String LEVEL_PROVINCE = "PROVINCE"; // 省级
    
    // ========== 动作类型常量 ==========
    public static final String ACTION_INITIATE = "INITIATE";           // 发起
    public static final String ACTION_REVIEW = "REVIEW";               // 审查
    public static final String ACTION_VERIFY = "VERIFY";               // 核实
    public static final String ACTION_APPROVE = "APPROVE";             // 审批
    public static final String ACTION_FINAL_APPROVE = "FINAL_APPROVE"; // 最终审批
}
