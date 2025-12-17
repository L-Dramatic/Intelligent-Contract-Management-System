package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wf_transition")
public class WfTransition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long defId;
    private Long fromNodeId;
    private Long toNodeId;
    private String conditionExpr; // 条件表达式（用于条件分支）
}