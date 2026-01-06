package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批任务实体类
 * 对应数据库表 wf_task
 * 
 * 任务状态：待处理、已通过、已驳回、已否决
 * 支持会签模式（通过parallelGroupId分组）
 */
@Data
@TableName("wf_task")
public class WfTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流程实例ID */
    private Long instanceId;

    /** 流程节点ID（旧版） */
    private Long nodeId;

    /** 场景节点ID（新版，关联wf_scenario_node.id） */
    private Long scenarioNodeId;

    /** 并行审批组ID（用于会签场景） */
    private String parallelGroupId;

    /** 审批人ID */
    private Long assigneeId;

    /** 状态：0-待审批，1-已通过，2-已驳回，3-已否决 */
    private Integer status;

    /** 审批意见 */
    private String comment;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 完成时间 */
    private LocalDateTime finishTime;

    // ========== 非数据库字段 ==========

    /** 审批人姓名 */
    @TableField(exist = false)
    private String assigneeName;

    /** 角色名称 */
    @TableField(exist = false)
    private String roleName;

    /** 节点名称 */
    @TableField(exist = false)
    private String nodeName;

    /** 合同名称 */
    @TableField(exist = false)
    private String contractName;

    /** 合同编号 */
    @TableField(exist = false)
    private String contractNo;

    /** 合同金额 */
    @TableField(exist = false)
    private java.math.BigDecimal contractAmount;

    /** 发起人姓名 */
    @TableField(exist = false)
    private String initiatorName;

    /** 合同类型 */
    @TableField(exist = false)
    private String contractType;

    /** 合同ID（非数据库字段，从流程实例获取） */
    @TableField(exist = false)
    private Long contractId;

    /** 是否为变更审批 */
    @TableField(exist = false)
    private Boolean isChange;

    /** 变更申请ID */
    @TableField(exist = false)
    private Long changeId;

    // ========== 任务状态常量 ==========
    public static final int STATUS_PENDING = 0; // 待处理
    public static final int STATUS_APPROVED = 1; // 已通过
    public static final int STATUS_REJECTED = 2; // 已驳回
    public static final int STATUS_VETOED = 3; // 已否决
    public static final int STATUS_CANCELLED = 4; // 已取消（流程终止时）
}