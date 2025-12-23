package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.entity.SysDept;
import com.software.contract_system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织架构管理控制器
 * 支持省-市-县三级组织树的增删改查
 */
@RestController
@RequestMapping("/dept")
@Tag(name = "组织架构管理", description = "组织树的增删改查操作")
public class SysDeptController {
    
    @Autowired
    private SysDeptService deptService;
    
    /**
     * 获取完整的组织树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取组织树", description = "返回完整的树形组织架构")
    public Result<List<SysDept>> getTree() {
        List<SysDept> tree = deptService.getTree();
        return Result.success(tree);
    }
    
    /**
     * 获取指定部门的子部门列表
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子部门", description = "根据父ID获取直接子部门列表")
    public Result<List<SysDept>> getChildren(
            @Parameter(description = "父部门ID，0表示根节点") @PathVariable Long parentId) {
        List<SysDept> children = deptService.getChildren(parentId);
        return Result.success(children);
    }
    
    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情", description = "根据ID获取部门信息")
    public Result<SysDept> getById(@PathVariable Long id) {
        SysDept dept = deptService.getById(id);
        if (dept == null) {
            return Result.error("部门不存在");
        }
        return Result.success(dept);
    }
    
    /**
     * 根据部门代码查询
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据代码查询", description = "根据部门代码精确查询")
    public Result<SysDept> getByCode(@PathVariable String code) {
        SysDept dept = deptService.getByCode(code);
        if (dept == null) {
            return Result.error("部门不存在");
        }
        return Result.success(dept);
    }
    
    /**
     * 根据类型查询部门列表
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型查询", description = "查询指定类型的所有部门（PROVINCE/CITY/COUNTY/DEPT）")
    public Result<List<SysDept>> getByType(
            @Parameter(description = "部门类型") @PathVariable String type) {
        List<SysDept> depts = deptService.getByType(type);
        return Result.success(depts);
    }
    
    /**
     * 获取部门的祖先链
     */
    @GetMapping("/{id}/ancestors")
    @Operation(summary = "获取祖先链", description = "获取从根节点到当前部门的路径")
    public Result<List<SysDept>> getAncestors(@PathVariable Long id) {
        List<SysDept> ancestors = deptService.getAncestors(id);
        return Result.success(ancestors);
    }
    
    /**
     * 获取部门的所有后代
     */
    @GetMapping("/{id}/descendants")
    @Operation(summary = "获取所有后代", description = "递归获取所有子孙部门")
    public Result<List<SysDept>> getDescendants(@PathVariable Long id) {
        List<SysDept> descendants = deptService.getDescendants(id);
        return Result.success(descendants);
    }
    
    /**
     * 创建部门
     */
    @PostMapping
    @PreAuthorize("hasAuthority('dept:manage')")
    @Operation(summary = "创建部门", description = "需要 dept:manage 权限")
    public Result<Long> create(@RequestBody SysDept dept) {
        try {
            Long id = deptService.create(dept);
            return Result.success(id);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:manage')")
    @Operation(summary = "更新部门", description = "需要 dept:manage 权限，不允许修改父部门")
    public Result<String> update(@PathVariable Long id, @RequestBody SysDept dept) {
        try {
            dept.setId(id);
            boolean success = deptService.update(dept);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除部门（软删除）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:manage')")
    @Operation(summary = "删除部门", description = "软删除，需要 dept:manage 权限，部门下有子部门或用户时不允许删除")
    public Result<String> delete(@PathVariable Long id) {
        try {
            boolean success = deptService.delete(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查找县级部门对应的市级部门
     */
    @GetMapping("/{id}/city-level")
    @Operation(summary = "查找市级部门", description = "用于审批流程中的县→市自动映射")
    public Result<SysDept> findCityLevelDept(@PathVariable Long id) {
        SysDept cityDept = deptService.findCityLevelDept(id);
        if (cityDept == null) {
            return Result.error("未找到对应的市级部门");
        }
        return Result.success(cityDept);
    }
    
    /**
     * 获取部门所属的市级公司
     */
    @GetMapping("/{id}/city-company")
    @Operation(summary = "获取市级公司", description = "向上查找所属的市级公司")
    public Result<SysDept> getCityCompany(@PathVariable Long id) {
        SysDept cityCompany = deptService.getCityCompany(id);
        if (cityCompany == null) {
            return Result.error("未找到所属市级公司");
        }
        return Result.success(cityCompany);
    }
}
