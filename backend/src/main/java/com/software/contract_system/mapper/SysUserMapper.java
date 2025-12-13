package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表数据库访问接口
 * 继承 BaseMapper 后，自动拥有 CRUD 能力
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 这里暂时不需要写任何代码，MyBatis-Plus 包办了
}