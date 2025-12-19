package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批任务实体
 * 对应表 wf_task
 * 
 * 任务状态：待处理、已通过、已驳回、已否决
 * 支持会签模式（通过parallelGroupId分组）
 */
@Data
@TableName("wf_task")
public class WfTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long instanceId;    // 关联的流程实例
    private Long nodeId;        // 关联的流程节点
    private Long approverId;    // 审批人ID
    
    // 0:待处理, 1:已通过, 2:已驳回, 3:已否决
    private Integer status;
    
    // 并行组ID（会签任务分组标识，同一组的任务需要全部/部分通过才算节点通过）
    private String parallelGroupId;
    
    private String comment;     // 审批意见
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
    
    // ========== 任务状态常量 ==========
    public static final int STATUS_PENDING = 0;   // 待处理
    public static final int STATUS_APPROVED = 1;  // 已通过
    public static final int STATUS_REJECTED = 2;  // 已驳回
    public static final int STATUS_VETOED = 3;    // 已否决
}