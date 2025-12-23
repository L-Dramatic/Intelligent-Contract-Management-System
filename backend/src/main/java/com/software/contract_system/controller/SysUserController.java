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

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户登录、注册与信息管理")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

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
    public Result<String> getUserInfo() {
        return Result.success("如果你看到这句话，说明 Token 验证成功了！");
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