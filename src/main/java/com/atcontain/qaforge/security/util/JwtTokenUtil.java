package com.atcontain.qaforge.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class JwtTokenUtil {

    // ★ 程序启动时自动生成一个安全的 HS256 密钥
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    public static String getToken(String username, Integer id) {
        String jwtToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .claim("username", username)
                .claim("Id", id)
                .setSubject("qa-forge")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .setId(UUID.randomUUID().toString())
                .signWith(SECRET_KEY)  // ← 使用 Key 对象
                .compact();
        log.info("生成 Token 成功，用户：{}，ID：{}", username, id);
        Claims claims = getClaims(jwtToken);
        Integer userId = (Integer) claims.get("Id");
        System.out.println(userId);
        return jwtToken;
    }

    public static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)  // ← 解析时也用同一个 Key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String getJti(String token) {
        Claims claims = getClaims(token);
        return claims.getId();
    }

    public static long getRemainingExpiration(String token) {
        Claims claims = getClaims(token);
        Date expiration = claims.getExpiration();
        long remaining =  expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}