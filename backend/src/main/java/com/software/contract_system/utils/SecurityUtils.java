package com.software.contract_system.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.common.BusinessException;
import com.software.contract_system.entity.SysUser;
import com.software.contract_system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 * 统一处理用户认证相关的逻辑
 */
@Component
public class SecurityUtils {
    
    @Autowired
    private SysUserMapper userMapper;
    
    /**
     * 获取当前登录用户ID
     * 使用ThreadLocal缓存避免重复查询
     */
    private static final ThreadLocal<Long> userIdCache = new ThreadLocal<>();
    
    public Long getCurrentUserId() {
        // 尝试从缓存获取
        Long userId = userIdCache.get();
        if (userId != null) {
            return userId;
        }
        
        // 从Security上下文获取
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw BusinessException.forbidden("用户未登录");
        }
        
        Object principal = auth.getPrincipal();
        if (!(principal instanceof String)) {
            throw new BusinessException(500, "用户认证信息格式错误");
        }
        
        String username = (String) principal;
        
        // 查询用户
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        
        if (user == null || user.getId() == null) {
            throw BusinessException.notFound("用户数据异常");
        }
        
        // 缓存到ThreadLocal
        userIdCache.set(user.getId());
        
        return user.getId();
    }
    
    /**
     * 获取当前登录用户信息
     */
    public SysUser getCurrentUser() {
        Long userId = getCurrentUserId();
        return userMapper.selectById(userId);
    }
    
    /**
     * 清除ThreadLocal缓存（在请求结束时调用）
     */
    public static void clearCache() {
        userIdCache.remove();
    }
}

