package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.software.contract_system.dto.LoginDTO;
import com.software.contract_system.dto.RegisterDTO;
import com.software.contract_system.entity.SysDept;
import com.software.contract_system.entity.SysUser;
import com.software.contract_system.mapper.SysDeptMapper;
import com.software.contract_system.mapper.SysUserMapper;
import com.software.contract_system.service.SysUserService;
import com.software.contract_system.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired private JwtUtils jwtUtils;
    @Autowired private com.software.contract_system.mapper.SysRolePermissionMapper rolePermissionMapper;
    @Autowired private com.software.contract_system.mapper.SysPermissionMapper permissionMapper;
    @Autowired private SysDeptMapper deptMapper;

    // 修改返回值类型为 Map<String, Object>，或者你也可以新建一个 LoginVO 类
    // 这里为了方便，我们先暂时返回 Object，实际是返回 Map
    @Override
    public Object login(LoginDTO loginDTO) {
        // 1. 查用户
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 比对密码
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // ★★★ 新增：查询权限逻辑 ★★★
        List<String> permissionCodes = new ArrayList<>();
    
        // 1. 查关联表: Role -> PermissionIds
        List<com.software.contract_system.entity.SysRolePermission> relations = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<com.software.contract_system.entity.SysRolePermission>()
                .eq(com.software.contract_system.entity.SysRolePermission::getRoleCode, user.getRole())
        );

        if (!relations.isEmpty()) {
            // 2. 提取 ID 列表
            List<Long> pIds = relations.stream()
                .map(com.software.contract_system.entity.SysRolePermission::getPermissionId)
                .toList();
        
            // 3. 查权限表: PermissionIds -> Codes
            List<com.software.contract_system.entity.SysPermission> perms = permissionMapper.selectBatchIds(pIds);
            permissionCodes = perms.stream()
                .map(com.software.contract_system.entity.SysPermission::getCode)
                .toList();
        }

        // 4. 生成 Token (传入权限列表)
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole(), permissionCodes);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        // 前端期望字段名为 userInfo
        data.put("userInfo", user);
        // 把权限也返回给前端（以及让前端可将其合并进 userInfo.permissions）
        data.put("permissions", permissionCodes);

        return data;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        SysUser existUser = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, registerDTO.getUsername()));
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建新用户对象
        SysUser newUser = new SysUser();
        newUser.setUsername(registerDTO.getUsername());
        newUser.setPassword(registerDTO.getPassword()); // 实际项目这里必须加密，我们稍后处理
        newUser.setRealName(registerDTO.getRealName());
        // 如果没传角色，默认给 USER
        newUser.setRole(registerDTO.getRole() == null ? "USER" : registerDTO.getRole());
        newUser.setCreatedAt(java.time.LocalDateTime.now());
        // 3. 保存到数据库
        this.save(newUser);
    }
    
    @Override
    public IPage<SysUser> getUserList(int pageNum, int pageSize, String username, String role) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询启用的用户
        wrapper.eq(SysUser::getIsActive, 1);
        
        // 模糊查询用户名
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        // 按角色筛选
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        // 按创建时间倒序
        wrapper.orderByDesc(SysUser::getCreatedAt);
        
        IPage<SysUser> result = this.page(page, wrapper);
        
        // 填充部门信息
        for (SysUser user : result.getRecords()) {
            if (user.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(user.getDeptId());
                user.setDept(dept);
            }
            // 清空密码，不返回给前端
            user.setPassword(null);
        }
        
        return result;
    }
    
    @Override
    public boolean updateUserInfo(SysUser user) {
        // 不允许通过此接口修改密码
        user.setPassword(null);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return this.updateById(user);
    }
    
    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(newPassword); // 实际应加密
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return this.updateById(user);
    }
}