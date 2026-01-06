package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织架构实体类
 * 对应数据库表 sys_dept
 * 支持中国移动省-市-县三级法人架构，组织树为四级层级结构
 * （省级职能部门与市级分公司平级，市级职能部门与县级分公司平级）
 */
@Data
@TableName("sys_dept")
public class SysDept {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 父部门ID（0表示根节点-省公司） */
    private Long parentId;
    
    /** 部门名称 */
    private String name;
    
    /** 部门代码（如 PROVINCE, CITY-A, COUNTY-C, CITY-A-NET） */
    private String code;
    
    /** 类型: PROVINCE（省公司）, CITY（市公司）, COUNTY（县公司）, DEPT（职能部门） */
    private String type;
    
    /** 树层级（1=省公司，2=市公司/省级部门，3=县公司/市级部门，4=县级部门） */
    private Integer level;
    
    /** 部门负责人用户ID */
    private Long managerId;
    
    /** 排序序号 */
    private Integer sortOrder;
    
    /** 是否删除（软删除：0=正常，1=已删除） */
    @TableLogic
    private Integer isDeleted;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
    
    // ========== 非数据库字段（用于树形结构展示） ==========
    
    /** 子部门列表 */
    @TableField(exist = false)
    private List<SysDept> children;
    
    /** 父部门名称 */
    @TableField(exist = false)
    private String parentName;
    
    /** 负责人姓名 */
    @TableField(exist = false)
    private String managerName;
    
    // ========== 类型常量 ==========
    public static final String TYPE_PROVINCE = "PROVINCE";  // 省公司
    public static final String TYPE_CITY = "CITY";          // 市公司
    public static final String TYPE_COUNTY = "COUNTY";      // 县公司
    public static final String TYPE_DEPT = "DEPT";          // 职能部门
}