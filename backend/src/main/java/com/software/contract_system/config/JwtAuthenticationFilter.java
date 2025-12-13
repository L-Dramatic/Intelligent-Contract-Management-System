package com.software.contract_system.config;

import com.software.contract_system.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 认证过滤器 (RBAC 升级版)
 * 解析 Token 中的 Role 和 Permissions
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取请求头中的 Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. 检查格式是否正确 (必须以 Bearer 开头)
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            // 3. 截取 Token 部分
            String token = authHeader.substring(7);

            try {
                // 4. 解析 Token
                Claims claims = jwtUtils.parseToken(token);
                String username = claims.getSubject();
                
                // 从 Token 中获取 角色 (String)
                String role = claims.get("role", String.class);
                
                // ★★★ 从 Token 中获取 权限列表 (List) ★★★
                @SuppressWarnings("unchecked")
                List<String> perms = claims.get("perms", List.class);

                // 5. 如果解析成功
                if (username != null) {
                    // 准备一个列表存放所有的权限对象
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    // A. 添加角色 (约定前缀 ROLE_)
                    // 这样 @PreAuthorize("hasRole('ADMIN')") 依然有效
                    if (role != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }

                    // B. 添加具体权限 (如 user:manage)
                    // 这样 @PreAuthorize("hasAuthority('user:manage')") 就生效了
                    if (perms != null) {
                        for (String perm : perms) {
                            authorities.add(new SimpleGrantedAuthority(perm));
                        }
                    }

                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    // 放入安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token 无效或过期，打印日志，不抛出异常，让 Security 拦截
                System.out.println("Token 验证失败: " + e.getMessage());
            }
        }

        // 6. 继续执行下一个过滤器
        filterChain.doFilter(request, response);
    }
}