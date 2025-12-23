package com.software.contract_system.dto;

import lombok.Data;
import java.util.List;

/**
 * 合同类型DTO（按主类型分组）
 */
@Data
public class ContractTypeDTO {
    
    /**
     * 主类型代码
     */
    private String typeCode;
    
    /**
     * 主类型名称
     */
    private String typeName;
    
    /**
     * 子类型列表
     */
    private List<SubType> subTypes;
    
    @Data
    public static class SubType {
        private String subTypeCode;
        private String subTypeName;
        private String description;
    }
}
