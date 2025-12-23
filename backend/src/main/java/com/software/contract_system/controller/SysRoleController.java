package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.entity.SysRole;
import com.software.contract_system.entity.SysUserRole;
import com.software.contract_system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 管理审批角色定义和用户角色分配
 */
@RestController
@RequestMapping("/role")
@Tag(name = "角色管理", description = "角色定义和用户角色分配")
public class SysRoleController {
    
    @Autowired
    private SysRoleService roleService;
    
    /**
     * 获取所有角色
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有角色", description = "返回系统中定义的所有角色")
    public Result<List<SysRole>> getAll() {
        List<SysRole> roles = roleService.getAll();
        return Result.success(roles);
    }
    
    /**
     * 获取所有审批角色（排除系统管理员）
     */
    @GetMapping("/approval-roles")
    @Operation(summary = "获取审批角色", description = "返回可用于审批流程的角色列表")
    public Result<List<SysRole>> getApprovalRoles() {
        List<SysRole> roles = roleService.getApprovalRoles();
        return Result.success(roles);
    }
    
    /**
     * 根据角色编码查询
     */
    @GetMapping("/{roleCode}")
    @Operation(summary = "根据编码查询", description = "根据角色编码获取角色详情")
    public Result<SysRole> getByRoleCode(@PathVariable String roleCode) {
        SysRole role = roleService.getByRoleCode(roleCode);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }
    
    /**
     * 根据类别查询角色
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "根据类别查询", description = "查询指定类别的角色（BUSINESS/TECHNICAL/LEGAL/FINANCE等）")
    public Result<List<SysRole>> getByCategory(
            @Parameter(description = "角色类别") @PathVariable String category) {
        List<SysRole> roles = roleService.getByCategory(category);
        return Result.success(roles);
    }
    
    /**
     * 获取用户的所有角色
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户角色", description = "获取指定用户拥有的所有角色")
    public Result<List<SysRole>> getUserRoles(@PathVariable Long userId) {
        List<SysRole> roles = roleService.getUserRoles(userId);
        return Result.success(roles);
    }
    
    /**
     * 获取用户的角色详情（包含关联信息）
     */
    @GetMapping("/user/{userId}/details")
    @Operation(summary = "获取用户角色详情", description = "包含生效部门等关联信息")
    public Result<List<SysUserRole>> getUserRoleDetails(@PathVariable Long userId) {
        List<SysUserRole> details = roleService.getUserRoleDetails(userId);
        return Result.success(details);
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "分配角色", description = "为用户分配指定角色，需要 user:manage 权限")
    public Result<String> assignRole(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色编码") @RequestParam String roleCode,
            @Parameter(description = "生效部门ID（可选）") @RequestParam(required = false) Long effectiveDeptId,
            @Parameter(description = "是否主要角色") @RequestParam(defaultValue = "false") Boolean isPrimary) {
        try {
            boolean success = roleService.assignRole(userId, roleCode, effectiveDeptId, isPrimary);
            return success ? Result.success("分配成功") : Result.error("分配失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 移除用户的指定角色
     */
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "移除角色", description = "移除用户的指定角色，需要 user:manage 权限")
    public Result<String> removeRole(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色编码") @RequestParam String roleCode) {
        try {
            boolean success = roleService.removeRole(userId, roleCode);
            return success ? Result.success("移除成功") : Result.error("移除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量设置用户角色
     */
    @PostMapping("/user/{userId}/batch")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "批量设置角色", description = "清空原有角色后重新分配，需要 user:manage 权限")
    public Result<String> setUserRoles(
            @PathVariable Long userId,
            @RequestBody List<SysUserRole> roles) {
        try {
            boolean success = roleService.setUserRoles(userId, roles);
            return success ? Result.success("设置成功") : Result.error("设置失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查用户是否拥有指定角色
     */
    @GetMapping("/check")
    @Operation(summary = "检查角色", description = "检查用户是否拥有指定角色")
    public Result<Boolean> hasRole(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色编码") @RequestParam String roleCode) {
        boolean hasRole = roleService.hasRole(userId, roleCode);
        return Result.success(hasRole);
    }
}
