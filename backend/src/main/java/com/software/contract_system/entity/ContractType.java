package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 合同类型定义实体
 * 用于前端获取合同类型列表
 */
@Data
@TableName("t_contract_type")
public class ContractType {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 主类型代码: TYPE_A, TYPE_B, TYPE_C
     */
    private String typeCode;

    /**
     * 主类型名称
     */
    private String typeName;

    /**
     * 子类型代码: A1, A2, B1...
     */
    private String subTypeCode;

    /**
     * 子类型名称
     */
    private String subTypeName;

    /**
     * 说明
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Integer isActive;

    // ==================== 主类型常量 ====================
    public static final String TYPE_A = "TYPE_A";  // 采购类
    public static final String TYPE_B = "TYPE_B";  // 工程类
    public static final String TYPE_C = "TYPE_C";  // 服务类

    // ==================== 子类型常量 ====================
    // TYPE_A 采购类
    public static final String SUB_TYPE_A1 = "A1";  // 土建工程
    public static final String SUB_TYPE_A2 = "A2";  // 装修工程
    public static final String SUB_TYPE_A3 = "A3";  // 零星维修

    // TYPE_B 工程类
    public static final String SUB_TYPE_B1 = "B1";  // 光缆代维
    public static final String SUB_TYPE_B2 = "B2";  // 基站代维
    public static final String SUB_TYPE_B3 = "B3";  // 家宽代维
    public static final String SUB_TYPE_B4 = "B4";  // 应急保障

    // TYPE_C 服务类
    public static final String SUB_TYPE_C1 = "C1";  // 定制开发
    public static final String SUB_TYPE_C2 = "C2";  // 商用软件采购
    public static final String SUB_TYPE_C3 = "C3";  // DICT集成
}
