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
 * ========================================
 * JWT Token提供者（JwtTokenProvider）
 * ========================================
 * 功能说明：
 * 1. 负责JWT Token的生成、解析和验证
 * 2. 支持Access Token和Refresh Token
 * 3. 在Token中存储用户信息（用户名、ID、类型、权限）
 * 4. 提供Token的安全验证
 * 
 * JWT说明：
 * - JWT：JSON Web Token，一种基于JSON的开放标准
 * - 用于在网络应用间安全传输信息
 * - 由三部分组成：Header（头部）、Payload（负载）、Signature（签名）
 * - 格式：xxxxx.yyyyy.zzzzz
 * 
 * Token类型：
 * 1. Access Token：短期有效（1小时），用于API调用
 * 2. Refresh Token：长期有效（7天），用于刷新Access Token
 * 
 * 使用流程：
 * 1. 用户登录，生成Access Token和Refresh Token
 * 2. 前端每次请求携带Access Token
 * 3. Access Token过期后，使用Refresh Token获取新的Access Token
 * 4. Refresh Token过期后，需要重新登录
 * 
 * 安全特性：
 * - 使用HS512算法签名，防止篡改
 * - Token包含过期时间，自动失效
 * - 密钥从配置文件读取，不在代码中硬编码
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j       // Lombok注解：自动生成log对象
@Component   // Spring注解：注册为Spring组件
public class JwtTokenProvider {

    /* ========================= 配置属性 ========================= */

    @Value("${auction.jwt.secret}")
    private String jwtSecret;  // JWT签名密钥，从配置文件读取

    @Value("${auction.jwt.expiration}")
    private int jwtExpirationInMs;  // Access Token过期时间（毫秒）

    @Value("${jwt.header:Authorization}")
    private String jwtHeader;  // HTTP请求头名称，默认"Authorization"

    @Value("${jwt.prefix:Bearer }")
    private String jwtPrefix;  // Token前缀，默认"Bearer "

    /**
     * 生成JWT Token（基于Spring Security Authentication）
     * 
     * 功能说明：
     * 1. 从Authentication对象提取用户信息
     * 2. 将用户名和权限存入Token
     * 3. 设置Token过期时间
     * 4. 使用密钥签名生成最终Token
     * 
     * Token内容：
     * - subject：用户名
     * - claims.username：用户名（冗余）
     * - claims.authorities：用户权限列表
     * - iat：签发时间
     * - exp：过期时间
     * 
     * @param authentication Spring Security认证对象
     * @return JWT Token字符串
     */
    public String generateToken(Authentication authentication) {
        // 从认证对象中获取用户详情
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        // 计算过期时间
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

        // 构建Claims（负载数据）
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userPrincipal.getUsername());
        claims.put("authorities", userPrincipal.getAuthorities());

        // 构建并签名Token
        return Jwts.builder()
                .setClaims(claims)                                    // 设置负载数据
                .setSubject(userPrincipal.getUsername())              // 设置主题（用户名）
                .setIssuedAt(new Date())                              // 设置签发时间
                .setExpiration(expiryDate)                            // 设置过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // 签名算法和密钥
                .compact();                                           // 生成Token字符串
    }

    /**
     * 生成JWT Token（带额外信息）
     * 
     * 功能说明：
     * 直接使用用户名、ID和类型生成Token
     * 适用于刷新Token等场景
     * 
     * Token内容：
     * - subject：用户名
     * - claims.userId：用户ID
     * - claims.userType：用户类型（0-买家，1-管理员）
     * 
     * @param username 用户名
     * @param userId 用户ID
     * @param userType 用户类型
     * @return JWT Token字符串
     */
    public String generateToken(String username, Long userId, Integer userType) {
        // 计算过期时间
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

        // 构建Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);

        // 构建并签名Token
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
     * 功能说明：
     * 1. 生成有效期更长的Refresh Token（7天）
     * 2. 用于刷新Access Token，避免频繁登录
     * 3. 标记tokenType为"refresh"，用于区分
     * 
     * Refresh Token特点：
     * - 有效期更长（7天 vs 1小时）
     * - 只能用于刷新Access Token
     * - 不能直接用于API调用
     * 
     * @param user 用户信息对象
     * @return Refresh Token字符串
     */
    public String generateRefreshToken(com.auction.entity.SysUser user) {
        // 计算过期时间（7天）
        Date expiryDate = new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000L);

        // 构建Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("userType", user.getUserType());
        claims.put("tokenType", "refresh");  // 标记为Refresh Token

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
