package com.software.contract_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI生成条款请求
 */
@Data
@Schema(description = "AI生成条款请求")
public class AIGenerateRequest {
    
    @Schema(description = "合同类型", example = "base_station")
    private String contractType;
    
    @Schema(description = "条款类型", example = "租赁期限")
    private String clauseType;
    
    @Schema(description = "具体需求", example = "需要包含续租条件和提前解约条款")
    private String requirement;
}


