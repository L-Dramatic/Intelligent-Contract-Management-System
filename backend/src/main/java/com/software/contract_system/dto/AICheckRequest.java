package com.software.contract_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI合规检查请求
 */
@Data
@Schema(description = "AI合规检查请求")
public class AICheckRequest {
    
    @Schema(description = "合同类型", example = "base_station")
    private String contractType;
    
    @Schema(description = "待检查条款内容")
    private String clauseContent;
}


