package com.software.contract_system.service;

import com.software.contract_system.entity.SysRole;
import com.software.contract_system.entity.SysUserRole;
import java.util.List;

/**
 * 角色服务接口
 * 管理审批角色定义和用户角色分配
 */
public interface SysRoleService {
    
    /**
     * 获取所有角色
     */
    List<SysRole> getAll();
    
    /**
     * 根据角色编码查询
     */
    SysRole getByRoleCode(String roleCode);
    
    /**
     * 根据类别查询角色
     */
    List<SysRole> getByCategory(String category);
    
    /**
     * 获取所有审批相关角色（排除系统管理员）
     */
    List<SysRole> getApprovalRoles();
    
    /**
     * 获取用户的所有角色
     */
    List<SysRole> getUserRoles(Long userId);
    
    /**
     * 获取用户的主要角色
     */
    SysRole getUserPrimaryRole(Long userId);
    
    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @param effectiveDeptId 生效部门ID（可选）
     * @param isPrimary 是否主要角色
     */
    boolean assignRole(Long userId, String roleCode, Long effectiveDeptId, boolean isPrimary);
    
    /**
     * 移除用户的指定角色
     */
    boolean removeRole(Long userId, String roleCode);
    
    /**
     * 批量设置用户角色（清空原有角色后重新分配）
     */
    boolean setUserRoles(Long userId, List<SysUserRole> roles);
    
    /**
     * 检查用户是否拥有指定角色
     */
    boolean hasRole(Long userId, String roleCode);
    
    /**
     * 获取用户的角色关联详情
     */
    List<SysUserRole> getUserRoleDetails(Long userId);
}
