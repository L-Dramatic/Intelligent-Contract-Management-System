package com.software.contract_system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.software.contract_system.dto.LoginDTO;
import com.software.contract_system.dto.RegisterDTO;
import com.software.contract_system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    /**
     * 登录方法
     * @param loginDTO 登录参数
     * @return 登录成功的用户信息
     */
    Object login(LoginDTO loginDTO);
    void register(RegisterDTO registerDTO);
    
    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param username 用户名（模糊查询）
     * @param role 角色
     * @return 分页结果
     */
    IPage<SysUser> getUserList(int pageNum, int pageSize, String username, String role);
    
    /**
     * 更新用户信息
     */
    boolean updateUserInfo(SysUser user);
    
    /**
     * 重置用户密码
     */
    boolean resetPassword(Long userId, String newPassword);
}