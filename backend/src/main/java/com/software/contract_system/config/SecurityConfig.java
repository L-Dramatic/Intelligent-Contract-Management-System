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
                // 这里只配置业务接口的权限
                .requestMatchers("/user/login", "/user/register").permitAll()
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