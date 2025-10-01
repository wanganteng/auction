package com.auction.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证入口点
 * 处理未认证的请求
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        String method = request.getMethod();
        String requestPath = request.getServletPath();
        String queryString = request.getQueryString();
        String authHeader = request.getHeader("Authorization");
        String referer = request.getHeader("Referer");
        String userAgent = request.getHeader("User-Agent");
        String xrw = request.getHeader("X-Requested-With");
        String authPreview = (authHeader == null ? "null" : (authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader));

        log.error("未授权错误: {}, method={}, path={}, query={}, auth={}, referer={}, xrw={}, ua={}",
            authException.getMessage(), method, requestPath, queryString, authPreview, referer, xrw, userAgent);
        
        // 检查是否是token过期导致的认证失败
        String errorMessage = authException.getMessage();
        if (errorMessage != null && errorMessage.contains("JWT令牌已过期")) {
            log.info("检测到JWT令牌过期，清理Cookie并重定向到登录页面");
            // 清理过期的token cookie
            clearExpiredTokenCookie(response);
        }
        
        // 如果是静态资源，直接返回404，不应该到这里
        if (requestPath.startsWith("/images/") || 
            requestPath.startsWith("/admin/css/") || requestPath.startsWith("/admin/js/") || 
            requestPath.startsWith("/admin/images/") || requestPath.startsWith("/admin/fonts/") ||
            requestPath.startsWith("/user/css/") || requestPath.startsWith("/user/js/") || 
            requestPath.startsWith("/user/images/")
            ) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // 如果是管理后台页面请求，重定向到统一登录页面
        if (requestPath.startsWith("/auction/admin") && !requestPath.equals("/auction/admin/login") && !requestPath.startsWith("/auction/admin/static")) {
            response.sendRedirect("/auction/login");
            return;
        }

        // 如果是用户页面请求，重定向到统一登录页面
        if (requestPath.startsWith("/auction/user") && !requestPath.equals("/auction/user/login") && !requestPath.equals("/auction/user/register") && !requestPath.startsWith("/auction/user/static")) {
            response.sendRedirect("/auction/login");
            return;
        }
        
        // 如果是API请求，返回JSON错误信息
        if (requestPath.startsWith("/api/")) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            final Map<String, Object> body = new HashMap<>();
            body.put("code", HttpServletResponse.SC_UNAUTHORIZED);
            body.put("message", "未授权访问，请先登录");
            body.put("data", null);
            body.put("timestamp", System.currentTimeMillis());
            body.put("path", requestPath);

            final ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), body);
            return;
        }
        
        // 其他情况，重定向到统一登录页面
        response.sendRedirect("/auction/login");
    }
    
    /**
     * 清理过期的Token Cookie
     * 
     * @param response HTTP响应
     */
    private void clearExpiredTokenCookie(HttpServletResponse response) {
        javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("adminToken", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        log.debug("已清理过期的Token Cookie");
    }
}
