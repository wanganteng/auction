package com.auction.security;

import com.auction.service.SysUserService;
import com.auction.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 在每个请求中检查JWT Token并设置认证信息
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private SysUserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 跳过不需要认证的API路径
        String requestPath = request.getServletPath();
        if (requestPath.startsWith("/api/auth/login") || 
            requestPath.startsWith("/api/auth/register") ||
            requestPath.startsWith("/api/auth/logout") ||
            requestPath.startsWith("/auction/api/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 从请求头或Cookie中获取JWT Token（按路径优先级选择）
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    // Token有效，进行认证
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    
                    // 加载用户详情
                    SysUser user = userService.getByUsername(username);
                    if (user == null) {
                        log.warn("用户不存在: {}", username);
                        // 清理无效token的Cookie
                        clearTokenCookie(response);
                        return;
                    }
                    
                    // 创建UserDetails对象
                    UserDetails userDetails = new CustomUserDetailsService.CustomUserPrincipal(user);
                    
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Set Authentication to security context for '{}', uri: {}", 
                        username, request.getRequestURI());
                } else {
                    // Token无效或过期，清理Cookie
                    log.debug("Token无效或过期，清理Cookie");
                    clearTokenCookie(response);
                }
            }
        } catch (Exception ex) {
            log.error("无法在安全上下文中设置用户认证", ex);
            // 发生异常时也清理Cookie
            clearTokenCookie(response);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头或Cookie中获取JWT Token
     * 
     * @param request HTTP请求
     * @return JWT Token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 仅从 Authorization 头读取
        String bearerToken = request.getHeader(tokenProvider.getJwtHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenProvider.getJwtPrefix())) {
            return bearerToken.substring(tokenProvider.getJwtPrefix().length());
        }
        return null;
    }
    
    /**
     * 清理Token Cookie
     * 
     * @param response HTTP响应
     */
    private void clearTokenCookie(HttpServletResponse response) {
        javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("adminToken", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        log.debug("已清理过期的Token Cookie");
    }
}
