package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 用户角色关联表数据库访问接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    
    /**
     * 查询用户的所有角色关联
     */
    @Select("SELECT ur.*, r.role_name, r.role_category, u.real_name as user_name, d.name as dept_name " +
            "FROM sys_user_role ur " +
            "LEFT JOIN sys_role r ON ur.role_code = r.role_code " +
            "LEFT JOIN sys_user u ON ur.user_id = u.id " +
            "LEFT JOIN sys_dept d ON ur.effective_dept_id = d.id " +
            "WHERE ur.user_id = #{userId} " +
            "ORDER BY ur.is_primary DESC")
    List<SysUserRole> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询拥有指定角色的用户关联
     */
    @Select("SELECT ur.*, r.role_name, r.role_category, u.real_name as user_name, d.name as dept_name " +
            "FROM sys_user_role ur " +
            "LEFT JOIN sys_role r ON ur.role_code = r.role_code " +
            "LEFT JOIN sys_user u ON ur.user_id = u.id " +
            "LEFT JOIN sys_dept d ON ur.effective_dept_id = d.id " +
            "WHERE ur.role_code = #{roleCode}")
    List<SysUserRole> selectByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 查询在指定部门拥有指定角色的用户
     */
    @Select("SELECT ur.*, u.real_name as user_name " +
            "FROM sys_user_role ur " +
            "INNER JOIN sys_user u ON ur.user_id = u.id " +
            "WHERE ur.role_code = #{roleCode} " +
            "AND (ur.effective_dept_id = #{deptId} OR ur.effective_dept_id IS NULL) " +
            "AND u.dept_id = #{deptId} " +
            "AND u.is_active = 1")
    List<SysUserRole> selectByRoleAndDept(@Param("roleCode") String roleCode, @Param("deptId") Long deptId);
    
    /**
     * 删除用户的所有角色关联
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户是否拥有指定角色
     */
    @Select("SELECT COUNT(*) FROM sys_user_role WHERE user_id = #{userId} AND role_code = #{roleCode}")
    int countByUserAndRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}
