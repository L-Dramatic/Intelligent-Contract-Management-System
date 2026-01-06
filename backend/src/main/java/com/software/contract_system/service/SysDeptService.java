package com.software.contract_system.service;

import com.software.contract_system.entity.SysDept;
import java.util.List;

/**
 * 组织架构服务接口
 * 支持省-市-县三级法人架构，组织树为四级层级结构管理
 */
public interface SysDeptService {
    
    /**
     * 获取完整的组织树（树形结构）
     */
    List<SysDept> getTree();
    
    /**
     * 获取指定部门的子部门列表
     */
    List<SysDept> getChildren(Long parentId);
    
    /**
     * 根据ID查询部门详情
     */
    SysDept getById(Long id);
    
    /**
     * 根据部门代码查询
     */
    SysDept getByCode(String code);
    
    /**
     * 创建部门
     */
    Long create(SysDept dept);
    
    /**
     * 更新部门
     */
    boolean update(SysDept dept);
    
    /**
     * 删除部门（软删除）
     */
    boolean delete(Long id);
    
    /**
     * 查询部门的祖先链（从根节点到当前部门）
     */
    List<SysDept> getAncestors(Long deptId);
    
    /**
     * 查询部门的所有后代
     */
    List<SysDept> getDescendants(Long deptId);
    
    /**
     * 根据类型查询部门
     * @param type PROVINCE/CITY/COUNTY/DEPT
     */
    List<SysDept> getByType(String type);
    
    /**
     * 查找县级部门对应的市级部门
     * 用于审批流程中的县→市自动映射
     * 
     * @param countyDeptId 县级部门ID
     * @return 对应的市级部门
     */
    SysDept findCityLevelDept(Long countyDeptId);
    
    /**
     * 获取部门所属的市级公司
     * 向上递归查找 type=CITY 的祖先节点
     */
    SysDept getCityCompany(Long deptId);
    
    /**
     * 获取部门所属的县级公司（如果有）
     */
    SysDept getCountyCompany(Long deptId);
    
    /**
     * 验证部门是否可以删除（无子部门且无关联用户）
     */
    boolean canDelete(Long deptId);
}
