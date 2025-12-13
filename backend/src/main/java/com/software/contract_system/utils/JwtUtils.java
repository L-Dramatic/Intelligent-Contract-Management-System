package com.software.contract_system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // 1. 定义密钥 (这就像是你的私章，绝对不能泄露给前端)
    // 必须足够长，否则报错
    private static final String SECRET = "MySecretKeyForContractSystemShouldBeLongEnough123456";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 2. 定义过期时间 (比如 24小时)
    private static final long EXPIRATION = 1000 * 60 * 60 * 24;

    /**
     * 生成 Token (已修改：增加 role 参数)
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色 (如 ADMIN, MANAGER)
     * @return 加密后的字符串
     */
    public String generateToken(Long userId, String username, String role, java.util.List<String> permissions) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("perms", permissions) // ★★★ 存入权限列表
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token (验证真伪)
     * @param token 加密字符串
     * @return 包含的信息
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}