package com.software.contract_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI服务响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI服务响应")
public class AIResponse<T> {
    
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "消息")
    private String message;
    
    @Schema(description = "数据")
    private T data;
    
    @Schema(description = "是否使用RAG")
    private Boolean ragUsed;
    
    public static <T> AIResponse<T> success(T data) {
        AIResponse<T> response = new AIResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    public static <T> AIResponse<T> error(String message) {
        AIResponse<T> response = new AIResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}


