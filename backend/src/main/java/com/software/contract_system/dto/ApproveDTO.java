package com.software.contract_system.dto;

import lombok.Data;

@Data
public class ApproveDTO {
    private Long taskId;    // 任务ID
    private Boolean pass;   // true:通过, false:驳回
    private String comment; // 意见
}