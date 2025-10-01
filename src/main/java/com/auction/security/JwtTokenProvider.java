package com.auction.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 提供者
 * 负责JWT Token的生成、解析和验证
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${auction.jwt.secret}")
    private String jwtSecret;

    @Value("${auction.jwt.expiration}")
    private int jwtExpirationInMs;

    @Value("${jwt.header:Authorization}")
    private String jwtHeader;

    @Value("${jwt.prefix:Bearer }")
    private String jwtPrefix;

    /**
     * 生成JWT Token
     * 
     * @param authentication 认证信息
     * @return JWT Token
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userPrincipal.getUsername());
        claims.put("authorities", userPrincipal.getAuthorities());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成JWT Token（带额外信息）
     * 
     * @param username 用户名
     * @param userId 用户ID
     * @param userType 用户类型
     * @return JWT Token
     */
    public String generateToken(String username, Long userId, Integer userType) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成Refresh Token
     * 
     * @param user 用户信息
     * @return Refresh Token
     */
    public String generateRefreshToken(com.auction.entity.SysUser user) {
        Date expiryDate = new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000L); // 7天

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("userType", user.getUserType());
        claims.put("tokenType", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户类型
     * 
     * @param token JWT Token
     * @return 用户类型
     */
    public Integer getUserTypeFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userType", Integer.class);
    }

    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("无效的JWT令牌: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT令牌已过期: 过期时间={}, 当前时间={}", 
                ex.getClaims().getExpiration(), new Date());
        } catch (UnsupportedJwtException ex) {
            log.error("不支持的JWT令牌: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT声明字符串为空: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("JWT令牌验证失败", ex);
        }
        return false;
    }

    /**
     * 检查Token是否过期
     * 
     * @param token JWT Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * 获取Token过期时间
     * 
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }

    /**
     * 获取签名密钥
     * 
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取JWT Header名称
     * 
     * @return Header名称
     */
    public String getJwtHeader() {
        return jwtHeader;
    }

    /**
     * 获取JWT前缀
     * 
     * @return JWT前缀
     */
    public String getJwtPrefix() {
        return jwtPrefix;
    }
}
