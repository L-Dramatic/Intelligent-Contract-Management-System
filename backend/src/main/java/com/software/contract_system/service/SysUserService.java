package com.software.contract_system.service;

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
}