package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.entity.SysDept;
import com.software.contract_system.entity.SysUser;
import com.software.contract_system.mapper.SysDeptMapper;
import com.software.contract_system.mapper.SysUserMapper;
import com.software.contract_system.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织架构服务实现类
 */
@Service
public class SysDeptServiceImpl implements SysDeptService {
    
    @Autowired
    private SysDeptMapper deptMapper;
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Override
    public List<SysDept> getTree() {
        // 获取所有部门（带关联信息）
        List<SysDept> allDepts = deptMapper.selectAllWithDetails();
        
        // 构建树形结构
        return buildTree(allDepts);
    }
    
    @Override
    public List<SysDept> getChildren(Long parentId) {
        return deptMapper.selectByParentId(parentId);
    }
    
    @Override
    public SysDept getById(Long id) {
        return deptMapper.selectById(id);
    }
    
    @Override
    public SysDept getByCode(String code) {
        return deptMapper.selectByCode(code);
    }
    
    @Override
    @Transactional
    public Long create(SysDept dept) {
        // 设置默认值
        if (dept.getSortOrder() == null) {
            dept.setSortOrder(0);
        }
        if (dept.getIsDeleted() == null) {
            dept.setIsDeleted(0);
        }
        
        // 根据父部门计算层级
        if (dept.getParentId() != null && dept.getParentId() > 0) {
            SysDept parent = deptMapper.selectById(dept.getParentId());
            if (parent != null) {
                dept.setLevel(parent.getLevel() + 1);
            }
        } else {
            dept.setLevel(1);
            dept.setParentId(0L);
        }
        
        deptMapper.insert(dept);
        return dept.getId();
    }
    
    @Override
    @Transactional
    public boolean update(SysDept dept) {
        // 不允许修改父部门ID（避免循环引用）
        SysDept existing = deptMapper.selectById(dept.getId());
        if (existing == null) {
            return false;
        }
        
        // 保持原有的 parentId 和 level
        dept.setParentId(existing.getParentId());
        dept.setLevel(existing.getLevel());
        
        return deptMapper.updateById(dept) > 0;
    }
    
    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!canDelete(id)) {
            throw new RuntimeException("该部门下有子部门或关联用户，无法删除");
        }
        
        SysDept dept = new SysDept();
        dept.setId(id);
        dept.setIsDeleted(1);
        return deptMapper.updateById(dept) > 0;
    }
    
    @Override
    public List<SysDept> getAncestors(Long deptId) {
        return deptMapper.selectAncestors(deptId);
    }
    
    @Override
    public List<SysDept> getDescendants(Long deptId) {
        return deptMapper.selectDescendants(deptId);
    }
    
    @Override
    public List<SysDept> getByType(String type) {
        return deptMapper.selectByType(type);
    }
    
    @Override
    public SysDept findCityLevelDept(Long countyDeptId) {
        // 获取县级部门信息
        SysDept countyDept = deptMapper.selectById(countyDeptId);
        if (countyDept == null) {
            return null;
        }
        
        // 获取祖先链
        List<SysDept> ancestors = deptMapper.selectAncestors(countyDeptId);
        
        // 找到县级公司（type=COUNTY）
        SysDept countyCompany = ancestors.stream()
                .filter(d -> SysDept.TYPE_COUNTY.equals(d.getType()))
                .findFirst()
                .orElse(null);
        
        if (countyCompany == null) {
            return null;
        }
        
        // 找到市级公司（县公司的父节点应该是市公司）
        SysDept cityCompany = ancestors.stream()
                .filter(d -> SysDept.TYPE_CITY.equals(d.getType()))
                .findFirst()
                .orElse(null);
        
        if (cityCompany == null) {
            return null;
        }
        
        // 从县级部门代码提取关键字（如 COUNTY-C-NET → NET）
        String deptKeyword = extractDeptKeyword(countyDept.getCode());
        if (deptKeyword == null) {
            return null;
        }
        
        // 查找市级对应的部门
        return deptMapper.selectCityDeptByKeyword(cityCompany.getId(), deptKeyword);
    }
    
    @Override
    public SysDept getCityCompany(Long deptId) {
        List<SysDept> ancestors = deptMapper.selectAncestors(deptId);
        return ancestors.stream()
                .filter(d -> SysDept.TYPE_CITY.equals(d.getType()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public SysDept getCountyCompany(Long deptId) {
        List<SysDept> ancestors = deptMapper.selectAncestors(deptId);
        return ancestors.stream()
                .filter(d -> SysDept.TYPE_COUNTY.equals(d.getType()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public boolean canDelete(Long deptId) {
        // 检查是否有子部门
        List<SysDept> children = deptMapper.selectByParentId(deptId);
        if (!children.isEmpty()) {
            return false;
        }
        
        // 检查是否有关联用户
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(SysUser::getDeptId, deptId);
        Long userCount = userMapper.selectCount(userQuery);
        
        return userCount == 0;
    }
    
    /**
     * 构建树形结构
     */
    private List<SysDept> buildTree(List<SysDept> allDepts) {
        // 按父ID分组
        Map<Long, List<SysDept>> parentIdMap = allDepts.stream()
                .collect(Collectors.groupingBy(d -> d.getParentId() == null ? 0L : d.getParentId()));
        
        // 为每个部门设置子部门
        allDepts.forEach(dept -> {
            List<SysDept> children = parentIdMap.get(dept.getId());
            dept.setChildren(children != null ? children : new ArrayList<>());
        });
        
        // 返回根节点（parentId = 0）
        return parentIdMap.getOrDefault(0L, new ArrayList<>());
    }
    
    /**
     * 从部门代码提取关键字
     * 例如：COUNTY-C-NET → NET, CITY-A-LEGAL → LEGAL
     */
    private String extractDeptKeyword(String code) {
        if (code == null || !code.contains("-")) {
            return null;
        }
        String[] parts = code.split("-");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }
}
