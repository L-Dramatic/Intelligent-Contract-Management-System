package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * 对应数据库表 sys_user_role
 * 支持一人多角色（如项目经理同时兼任网络规划）
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 角色编码（关联sys_role.role_code） */
    private String roleCode;
    
    /** 生效部门ID（可选，用于跨部门兼职场景） */
    private Long effectiveDeptId;
    
    /** 是否主要角色（1=主要，0=次要） */
    private Integer isPrimary;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    // ========== 非数据库字段（用于关联查询） ==========
    
    /** 角色名称 */
    @TableField(exist = false)
    private String roleName;
    
    /** 角色类别 */
    @TableField(exist = false)
    private String roleCategory;
    
    /** 用户姓名 */
    @TableField(exist = false)
    private String userName;
    
    /** 部门名称 */
    @TableField(exist = false)
    private String deptName;
}
