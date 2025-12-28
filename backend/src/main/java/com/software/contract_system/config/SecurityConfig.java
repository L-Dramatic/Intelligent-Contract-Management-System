package com.software.contract_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // 导入这个
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * ★★★配置忽略路径 ★★★
     * 这里的路径会直接绕过 Spring Security 的所有过滤器
     * 专门用于解决 Swagger 403 问题
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/doc.html",                // Knife4j 入口
                "/swagger-ui.html",         // Swagger 兼容入口
                "/swagger-ui/**",           // ★★★ 关键：你刚才跳转的那个路径就在这里
                "/v3/api-docs/**",          // 接口数据源
                "/swagger-resources/**",    // 资源文件
                "/webjars/**",              // 静态资源
                "/favicon.ico"
        );
    }

    /**
     * 常规安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 禁用 CSRF
            .authorizeHttpRequests(auth -> auth
                // 公开接口（无需登录）
                .requestMatchers("/user/login", "/user/register").permitAll()
                .requestMatchers("/api/ai/**").permitAll() // 放行AI接口（开发环境）
                // 知识库接口（前端页面加载时需要，临时放行）
                .requestMatchers("/knowledge/**").permitAll()
                // 部门树接口（前端页面加载时需要，临时放行）
                .requestMatchers("/dept/tree").permitAll()
                // 合同类型和模板接口（前端页面加载时需要，临时放行）
                .requestMatchers("/contract-type/**", "/template/**").permitAll()
                // 开发环境：合同创建和更新接口（已登录即可，不检查具体权限）
                // 注意：生产环境应该移除这两行，恢复权限检查
                .requestMatchers("/contract/create", "/contract/update").authenticated()
                // 合同变更管理接口（已登录即可）
                .requestMatchers("/contract-change/**").authenticated()
                // 审批流程相关接口（开发环境临时放行，便于调试）
                .requestMatchers("/workflow/**").permitAll()
                // 合同相关接口（开发环境临时放行）
                .requestMatchers("/contract/**").permitAll()
                // 调试接口
                .requestMatchers("/debug/**").permitAll()
                // 其他所有接口都需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}