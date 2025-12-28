package com.software.contract_system.config;

import com.software.contract_system.utils.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 清理 SecurityUtils 的 ThreadLocal 缓存
 * 确保每次请求都获取正确的用户信息
 */
@Component
@Order(1)
public class SecurityUtilsFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 请求开始前清理缓存，确保获取最新的用户信息
            SecurityUtils.clearCache();
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后再次清理，防止线程复用导致的数据污染
            SecurityUtils.clearCache();
        }
    }
}

