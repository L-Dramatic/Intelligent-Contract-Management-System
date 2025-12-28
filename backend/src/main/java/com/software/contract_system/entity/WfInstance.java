package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 流程实例实体类
 * 对应数据库表 wf_instance
 * 支持新版场景审批和旧版流程定义两种模式
 */
@Data
@TableName("wf_instance")
public class WfInstance {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 流程定义ID（旧版） */
    private Long defId;
    
    /** 审批场景ID（新版，关联wf_scenario_config.scenario_id） */
    private String scenarioId;
    
    /** 合同ID */
    private Long contractId;
    
    /** 当前节点ID（旧版） */
    private Long currentNodeId;
    
    /** 当前节点顺序（新版） */
    private Integer currentNodeOrder;
    
    /** 状态：1-进行中，2-已完成，3-已驳回，4-已撤销 */
    private Integer status;
    
    /** 发起人ID */
    private Long requesterId;
    
    /** 开始时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    // ========== 非数据库字段 ==========
    
    /** 发起人姓名 */
    @TableField(exist = false)
    private String requesterName;
    
    /** 合同名称 */
    @TableField(exist = false)
    private String contractName;
    
    /** 场景名称 */
    @TableField(exist = false)
    private String scenarioName;
    
    /** 当前节点名称 */
    @TableField(exist = false)
    private String currentNodeName;
    
    /** 备注（如CONTRACT_CHANGE表示变更审批） */
    private String remark;
    
    // ========== 状态常量 ==========
    public static final int STATUS_RUNNING = 1;    // 进行中
    public static final int STATUS_COMPLETED = 2;  // 已完成
    public static final int STATUS_REJECTED = 3;   // 已驳回
    public static final int STATUS_CANCELLED = 4;  // 已撤销
    public static final int STATUS_TERMINATED = 5; // 已终止
}