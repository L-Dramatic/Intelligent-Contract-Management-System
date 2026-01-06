package com.software.contract_system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.software.contract_system.common.Result;
import com.software.contract_system.dto.LoginDTO;
import com.software.contract_system.dto.RegisterDTO;
import com.software.contract_system.entity.SysUser;
import com.software.contract_system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.software.contract_system.utils.SecurityUtils;
import com.software.contract_system.mapper.SysDeptMapper;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户登录、注册与信息管理")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @Autowired
    private SysDeptMapper deptMapper;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "返回 Token 和用户信息")
    public Result<Object> login(@RequestBody LoginDTO loginDTO) {
        try {
            Object data = sysUserService.login(loginDTO);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO registerDTO) {
        try {
            sysUserService.register(registerDTO);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<SysUser> getUserInfo() {
        SysUser user = securityUtils.getCurrentUser();
        if (user == null) {
            return Result.error("用户信息不存在");
        }
        // 填充部门信息
        if (user.getDeptId() != null) {
            user.setDept(deptMapper.selectById(user.getDeptId()));
        }
        // 清空密码
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 更新当前用户个人信息（无需管理员权限）
     */
    @PutMapping("/profile")
    @Operation(summary = "更新个人信息", description = "用户更新自己的基本信息")
    public Result<String> updateProfile(@RequestBody SysUser updateData) {
        Long currentUserId = securityUtils.getCurrentUserId();
        
        // 只允许修改部分字段
        SysUser user = new SysUser();
        user.setId(currentUserId);
        user.setRealName(updateData.getRealName());
        user.setEmail(updateData.getEmail());
        user.setMobile(updateData.getMobile());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        
        boolean success = sysUserService.updateById(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "获取用户列表", description = "分页查询用户，支持按用户名和角色筛选")
    public Result<IPage<SysUser>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        IPage<SysUser> page = sysUserService.getUserList(pageNum, pageSize, username, role);
        return Result.success(page);
    }
    
    /**
     * 创建用户（管理员）
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    public Result<String> createUser(@RequestBody SysUser user) {
        try {
            // 检查用户名是否已存在
            SysUser existUser = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, user.getUsername()));
            if (existUser != null) {
                return Result.error("用户名已存在");
            }
            
            // 设置默认值
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword("123456"); // 默认密码
            }
            if (user.getIsActive() == null) {
                user.setIsActive(1);
            }
            user.setCreatedAt(java.time.LocalDateTime.now());
            
            // 保存用户（primaryRole和zLevel会直接保存到sys_user表中）
            boolean success = sysUserService.save(user);
            
            return success ? Result.success("创建成功") : Result.error("创建失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改当前用户密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "用户修改自己的登录密码")
    public Result<String> changePassword(@RequestBody java.util.Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码至少6位");
        }
        
        Long currentUserId = securityUtils.getCurrentUserId();
        SysUser user = sysUserService.getById(currentUserId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 验证旧密码（注意：实际项目应该用加密比较）
        if (!oldPassword.equals(user.getPassword())) {
            return Result.error("当前密码错误");
        }
        
        // 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUserId);
        updateUser.setPassword(newPassword);
        updateUser.setUpdatedAt(java.time.LocalDateTime.now());
        
        boolean success = sysUserService.updateById(updateUser);
        return success ? Result.success("密码修改成功") : Result.error("密码修改失败");
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "更新用户", description = "更新用户基本信息")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        boolean success = sysUserService.updateUserInfo(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:manage')") 
    @Operation(summary = "删除用户", description = "删除指定用户")
    public Result<String> deleteUser(@PathVariable Long id) {
        boolean success = sysUserService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "重置密码", description = "重置用户密码为默认值123456")
    public Result<String> resetPassword(@PathVariable Long id) {
        boolean success = sysUserService.resetPassword(id, "123456");
        return success ? Result.success("密码已重置为123456") : Result.error("重置失败");
    }

    @GetMapping("/audit_test")
    @PreAuthorize("hasAuthority('contract:audit')")
    public Result<String> auditTest() {
        return Result.success("审批权限验证通过");
    }
}