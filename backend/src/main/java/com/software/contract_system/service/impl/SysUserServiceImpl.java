package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.software.contract_system.dto.LoginDTO;
import com.software.contract_system.dto.RegisterDTO;
import com.software.contract_system.entity.SysUser;
import com.software.contract_system.mapper.SysUserMapper;
import com.software.contract_system.service.SysUserService;
import com.software.contract_system.utils.JwtUtils; // 引入工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired private JwtUtils jwtUtils; // 注入 JWT 工具
    @Autowired private com.software.contract_system.mapper.SysRolePermissionMapper rolePermissionMapper;
    @Autowired private com.software.contract_system.mapper.SysPermissionMapper permissionMapper;

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
        data.put("user", user);
        data.put("permissions", permissionCodes); // 把权限也返回给前端，前端好控制按钮显示

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
}