package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表 sys_user
 * 支持中国移动省级公司组织架构和Z岗级体系
 */
@Data
@TableName("sys_user")
public class SysUser {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录账号） */
    private String username;
    
    /** 加密密码 */
    private String password;
    
    /** 真实姓名 */
    private String realName;
    
    /** 角色标识（兼容旧版：ADMIN, MANAGER, USER, LEGAL, BOSS） */
    private String role;
    
    /** 主要角色编码（关联sys_role.role_code，如 PROJECT_MANAGER, LEGAL_REVIEWER） */
    private String primaryRole;
    
    /** Z岗级（Z8, Z9, Z10, Z11, Z12, Z13, Z14, Z15） */
    private String zLevel;
    
    /** 所属部门ID */
    private Long deptId;
    
    /** 直属上级用户ID */
    private Long directLeaderId;
    
    /** 手机号 */
    private String mobile;
    
    /** 邮箱 */
    private String email;
    
    /** 是否启用（1=启用，0=禁用） */
    private Integer isActive;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
    
    /** 所属部门（非数据库字段，用于关联查询） */
    @TableField(exist = false)
    private SysDept dept;
}