package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wf_node")
public class WfNode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long defId;        // 关联流程定义ID
    private String nodeCode;   // 节点编码 (node_1)
    private String nodeName;   // 节点名称 (部门经理审批)
    private String type;       // START, APPROVE, END
    private String approverType; // ROLE, USER
    private String approverValue;// MANAGER, ADMIN
}