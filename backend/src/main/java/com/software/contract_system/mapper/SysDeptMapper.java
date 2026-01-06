package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 组织架构表数据库访问接口
 * 支持省-市-县三级法人架构，组织树为四级层级结构查询
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
    
    /**
     * 查询所有部门（树形结构用）
     */
    @Select("SELECT d.*, p.name as parent_name, u.real_name as manager_name " +
            "FROM sys_dept d " +
            "LEFT JOIN sys_dept p ON d.parent_id = p.id " +
            "LEFT JOIN sys_user u ON d.manager_id = u.id " +
            "WHERE d.is_deleted = 0 " +
            "ORDER BY d.level, d.sort_order, d.id")
    List<SysDept> selectAllWithDetails();
    
    /**
     * 根据父ID查询子部门
     */
    @Select("SELECT * FROM sys_dept WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order, id")
    List<SysDept> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据类型查询部门
     */
    @Select("SELECT * FROM sys_dept WHERE type = #{type} AND is_deleted = 0 ORDER BY sort_order, id")
    List<SysDept> selectByType(@Param("type") String type);
    
    /**
     * 根据部门代码查询（精确匹配）
     */
    @Select("SELECT * FROM sys_dept WHERE code = #{code} AND is_deleted = 0")
    SysDept selectByCode(@Param("code") String code);
    
    /**
     * 查询指定部门的所有上级部门（递归查询祖先链）
     */
    @Select("WITH RECURSIVE ancestors AS (" +
            "  SELECT * FROM sys_dept WHERE id = #{deptId} AND is_deleted = 0 " +
            "  UNION ALL " +
            "  SELECT d.* FROM sys_dept d " +
            "  INNER JOIN ancestors a ON d.id = a.parent_id " +
            "  WHERE d.is_deleted = 0" +
            ") SELECT * FROM ancestors ORDER BY level")
    List<SysDept> selectAncestors(@Param("deptId") Long deptId);
    
    /**
     * 查询指定部门的所有下级部门（递归查询子孙）
     */
    @Select("WITH RECURSIVE descendants AS (" +
            "  SELECT * FROM sys_dept WHERE id = #{deptId} AND is_deleted = 0 " +
            "  UNION ALL " +
            "  SELECT d.* FROM sys_dept d " +
            "  INNER JOIN descendants p ON d.parent_id = p.id " +
            "  WHERE d.is_deleted = 0" +
            ") SELECT * FROM descendants ORDER BY level, sort_order")
    List<SysDept> selectDescendants(@Param("deptId") Long deptId);
    
    /**
     * 查询市级部门对应的职能部门（用于县级→市级部门匹配）
     * 例如：C县网络部 → A市网络部
     */
    @Select("SELECT d.* FROM sys_dept d " +
            "WHERE d.parent_id = #{cityId} " +
            "AND d.type = 'DEPT' " +
            "AND d.code LIKE CONCAT('%-', #{deptKeyword}, '%') " +
            "AND d.is_deleted = 0 " +
            "LIMIT 1")
    SysDept selectCityDeptByKeyword(@Param("cityId") Long cityId, @Param("deptKeyword") String deptKeyword);
}
