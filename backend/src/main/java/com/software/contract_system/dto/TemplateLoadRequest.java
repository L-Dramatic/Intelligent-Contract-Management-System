package com.software.contract_system.dto;

import lombok.Data;

/**
 * 模板加载请求
 */
@Data
public class TemplateLoadRequest {
    
    /**
     * 子类型代码（必填）
     */
    private String subTypeCode;
    
    /**
     * 模板ID（可选，不填则使用默认模板）
     */
    private Long templateId;
}
