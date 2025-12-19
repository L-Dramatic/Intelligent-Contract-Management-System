package com.software.contract_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 提交合同审批请求DTO
 */
@Data
@Schema(description = "提交合同审批请求")
public class SubmitContractRequest {

    @Schema(description = "合同ID", required = true)
    private Long contractId;

    @Schema(description = "附件文件名列表")
    private List<String> attachments;

    @Schema(description = "用户是否已确认警告", defaultValue = "false")
    private Boolean userConfirmed = false;
}

