package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wf_instance")
public class WfInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long defId;
    private Long contractId;
    private Long currentNodeId; // 当前停留在哪个节点
    
    // 1:运行中, 2:完成, 3:驳回, 4:终止
    private Integer status;
    
    private Long requesterId;   // 发起人
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startTime;
}