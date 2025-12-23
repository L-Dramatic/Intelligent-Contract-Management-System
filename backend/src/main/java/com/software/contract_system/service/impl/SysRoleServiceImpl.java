package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.entity.SysRole;
import com.software.contract_system.entity.SysUser;
import com.software.contract_system.entity.SysUserRole;
import com.software.contract_system.mapper.SysRoleMapper;
import com.software.contract_system.mapper.SysUserMapper;
import com.software.contract_system.mapper.SysUserRoleMapper;
import com.software.contract_system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类
 */
@Service
public class SysRoleServiceImpl implements SysRoleService {
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Override
    public List<SysRole> getAll() {
        return roleMapper.selectList(null);
    }
    
    @Override
    public SysRole getByRoleCode(String roleCode) {
        return roleMapper.selectByRoleCode(roleCode);
    }
    
    @Override
    public List<SysRole> getByCategory(String category) {
        return roleMapper.selectByCategory(category);
    }
    
    @Override
    public List<SysRole> getApprovalRoles() {
        return roleMapper.selectApprovalRoles();
    }
    
    @Override
    public List<SysRole> getUserRoles(Long userId) {
        return roleMapper.selectByUserId(userId);
    }
    
    @Override
    public SysRole getUserPrimaryRole(Long userId) {
        // 先从用户表获取主要角色编码
        SysUser user = userMapper.selectById(userId);
        if (user != null && user.getPrimaryRole() != null) {
            return roleMapper.selectByRoleCode(user.getPrimaryRole());
        }
        
        // 如果用户表没有设置，从关联表查询
        List<SysRole> roles = roleMapper.selectByUserId(userId);
        return roles.isEmpty() ? null : roles.get(0);
    }
    
    @Override
    @Transactional
    public boolean assignRole(Long userId, String roleCode, Long effectiveDeptId, boolean isPrimary) {
        // 检查角色是否存在
        SysRole role = roleMapper.selectByRoleCode(roleCode);
        if (role == null) {
            throw new RuntimeException("角色不存在: " + roleCode);
        }
        
        // 检查是否已分配
        int count = userRoleMapper.countByUserAndRole(userId, roleCode);
        if (count > 0) {
            return true; // 已存在，直接返回成功
        }
        
        // 插入关联记录
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleCode(roleCode);
        userRole.setEffectiveDeptId(effectiveDeptId);
        userRole.setIsPrimary(isPrimary ? 1 : 0);
        userRoleMapper.insert(userRole);
        
        // 如果是主要角色，同时更新用户表
        if (isPrimary) {
            SysUser user = new SysUser();
            user.setId(userId);
            user.setPrimaryRole(roleCode);
            userMapper.updateById(user);
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean removeRole(Long userId, String roleCode) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleCode, roleCode);
        
        int deleted = userRoleMapper.delete(wrapper);
        
        // 如果删除的是主要角色，清空用户表的 primaryRole
        SysUser user = userMapper.selectById(userId);
        if (user != null && roleCode.equals(user.getPrimaryRole())) {
            user.setPrimaryRole(null);
            userMapper.updateById(user);
        }
        
        return deleted > 0;
    }
    
    @Override
    @Transactional
    public boolean setUserRoles(Long userId, List<SysUserRole> roles) {
        // 清空现有角色
        userRoleMapper.deleteByUserId(userId);
        
        // 批量插入新角色
        String primaryRoleCode = null;
        for (SysUserRole role : roles) {
            role.setUserId(userId);
            userRoleMapper.insert(role);
            
            if (role.getIsPrimary() != null && role.getIsPrimary() == 1) {
                primaryRoleCode = role.getRoleCode();
            }
        }
        
        // 更新用户表的主要角色
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPrimaryRole(primaryRoleCode);
        userMapper.updateById(user);
        
        return true;
    }
    
    @Override
    public boolean hasRole(Long userId, String roleCode) {
        return userRoleMapper.countByUserAndRole(userId, roleCode) > 0;
    }
    
    @Override
    public List<SysUserRole> getUserRoleDetails(Long userId) {
        return userRoleMapper.selectByUserId(userId);
    }
}
