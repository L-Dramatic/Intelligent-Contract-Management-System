package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.dto.LoginDTO;
import com.software.contract_system.dto.RegisterDTO;
import com.software.contract_system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户登录、注册与信息管理") // ★★★ 整个模块的标题
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "返回 Token 和用户信息") // ★★★ 接口描述
    public Result<Object> login(@RequestBody LoginDTO loginDTO) {
        // 调用 Service
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

    // 获取当前登录用户信息的测试接口
    @GetMapping("/info")
    public Result<String> getUserInfo() {
        return Result.success("如果你看到这句话，说明 Token 验证成功了！");
    }

    // 测试：只有拥有 user:manage 权限的人能删 (对应 ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:manage')") 
    public Result<String> deleteUser(@PathVariable Long id) {
        return Result.success("删除成功");
    }

    // 测试：只有拥有 contract:audit 权限的人能批 (对应 MANAGER, LEGAL)
    @GetMapping("/audit_test")
    @PreAuthorize("hasAuthority('contract:audit')")
    public Result<String> auditTest() {
        return Result.success("审批权限验证通过");
    }
}