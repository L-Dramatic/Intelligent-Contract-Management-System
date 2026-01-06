package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色定义实体类
 * 对应数据库表 sys_role
 * 对应 Master Workflow Configuration Matrix 中的审批角色
 */
@Data
@TableName("sys_role")
public class SysRole {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 角色编码（对应Master Workflow中的Role Code，如 PROJECT_MANAGER, LEGAL_REVIEWER） */
    private String roleCode;
    
    /** 角色名称（如 项目经理, 法务审查员） */
    private String roleName;
    
    /** 角色类别 */
    private String roleCategory;
    
    /** 要求的部门类型关键字（如 NET-网络部, LEGAL-法务部, FIN-财务部） */
    private String deptTypeRequired;
    
    /** 角色说明 */
    private String description;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    // ========== 角色类别常量 ==========
    public static final String CATEGORY_BUSINESS = "BUSINESS";       // 业务类
    public static final String CATEGORY_TECHNICAL = "TECHNICAL";     // 技术类
    public static final String CATEGORY_LEGAL = "LEGAL";             // 法务类
    public static final String CATEGORY_FINANCE = "FINANCE";         // 财务类
    public static final String CATEGORY_IT = "IT";                   // IT类
    public static final String CATEGORY_MANAGEMENT = "MANAGEMENT";   // 管理类
    public static final String CATEGORY_EXECUTIVE = "EXECUTIVE";     // 高管类
    public static final String CATEGORY_PROCUREMENT = "PROCUREMENT"; // 采购类
    public static final String CATEGORY_DICT = "DICT";               // DICT类
    public static final String CATEGORY_ADMIN = "ADMIN";             // 系统管理
    
    // ========== 常用角色编码常量 ==========
    // 发起人与管理层
    public static final String ROLE_INITIATOR = "INITIATOR";              // 发起人
    public static final String ROLE_DEPT_MANAGER = "DEPT_MANAGER";        // 部门经理
    public static final String ROLE_VICE_PRESIDENT = "VICE_PRESIDENT";    // 副总经理
    public static final String ROLE_GENERAL_MANAGER = "GENERAL_MANAGER";  // 总经理
    public static final String ROLE_T1M = "T1M";                          // 三重一大会议
    
    // 工程技术类
    public static final String ROLE_PROJECT_MANAGER = "PROJECT_MANAGER";  // 项目经理
    public static final String ROLE_DESIGN_REVIEWER = "DESIGN_REVIEWER";  // 设计审查
    public static final String ROLE_FACILITY_COORDINATOR = "FACILITY_COORDINATOR"; // 设施协调员
    
    // 网络技术类
    public static final String ROLE_RF_ENGINEER = "RF_ENGINEER";          // 射频工程师
    public static final String ROLE_NETWORK_ENGINEER = "NETWORK_ENGINEER"; // 网络工程师
    public static final String ROLE_NETWORK_PLANNING = "NETWORK_PLANNING"; // 网络规划
    public static final String ROLE_SITE_ACQUISITION = "SITE_ACQUISITION"; // 站点获取
    public static final String ROLE_BROADBAND_SPECIALIST = "BROADBAND_SPECIALIST"; // 家宽专家
    public static final String ROLE_OPS_CENTER = "OPS_CENTER";            // 运营中心
    public static final String ROLE_CUSTOMER_SERVICE_LEAD = "CUSTOMER_SERVICE_LEAD"; // 客服主管
    
    // 法务财务类
    public static final String ROLE_LEGAL_REVIEWER = "LEGAL_REVIEWER";    // 法务审查员
    public static final String ROLE_COST_AUDITOR = "COST_AUDITOR";        // 成本审计员
    public static final String ROLE_FINANCE_RECEIVABLE = "FINANCE_RECEIVABLE"; // 财务应收检查
    
    // IT类
    public static final String ROLE_TECHNICAL_LEAD = "TECHNICAL_LEAD";    // IT技术负责人
    public static final String ROLE_SECURITY_REVIEWER = "SECURITY_REVIEWER"; // IT安全审查
    public static final String ROLE_IT_ARCHITECT = "IT_ARCHITECT";        // IT架构师
    
    // 采购类
    public static final String ROLE_PROCUREMENT_SPECIALIST = "PROCUREMENT_SPECIALIST"; // 采购专员
    public static final String ROLE_VENDOR_MANAGER = "VENDOR_MANAGER";    // 供应商管理
    
    // DICT类
    public static final String ROLE_SOLUTION_ARCHITECT = "SOLUTION_ARCHITECT"; // 解决方案架构师
    public static final String ROLE_DICT_PM = "DICT_PM";                  // DICT项目经理
    
    // 系统管理
    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";        // 系统管理员
}
