package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色定义表数据库访问接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    
    /**
     * 根据角色编码查询
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 根据角色类别查询
     */
    @Select("SELECT * FROM sys_role WHERE role_category = #{category} ORDER BY role_code")
    List<SysRole> selectByCategory(@Param("category") String category);
    
    /**
     * 查询用户拥有的所有角色
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.role_code = ur.role_code " +
            "WHERE ur.user_id = #{userId} " +
            "ORDER BY ur.is_primary DESC, r.role_code")
    List<SysRole> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有审批相关角色（非系统管理员角色）
     */
    @Select("SELECT * FROM sys_role WHERE role_category != 'ADMIN' ORDER BY role_category, role_code")
    List<SysRole> selectApprovalRoles();
}
