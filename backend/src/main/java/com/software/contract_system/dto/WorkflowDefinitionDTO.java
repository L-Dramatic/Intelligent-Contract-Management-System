package com.software.contract_system.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程定义DTO
 * 用于前端展示流程模板
 */
@Data
public class WorkflowDefinitionDTO {
    
    private Long id;
    
    /** 流程名称 */
    private String name;
    
    /** 流程描述 */
    private String description;
    
    /** 适用的合同类型列表 */
    private List<String> applicableContractTypes;
    
    /** 匹配条件表达式 */
    private String conditionExpression;
    
    /** 版本号 */
    private Integer version;
    
    /** 是否启用 */
    private Boolean enabled;
    
    /** 场景数量 */
    private Integer scenarioCount;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}

