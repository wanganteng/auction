package com.auction.util;

import com.auction.entity.SysUser;
import com.auction.security.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 提供统一的用户认证相关方法
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户
     * 
     * @return 当前登录的用户对象
     * @throws RuntimeException 如果用户未登录
     */
    public static SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal principal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            return principal.getUser();
        }
        throw new RuntimeException("用户未登录");
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 当前登录用户的ID
     * @throws RuntimeException 如果用户未登录
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * 获取当前登录用户名
     * 
     * @return 当前登录用户的用户名
     * @throws RuntimeException 如果用户未登录
     */
    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * 判断当前用户是否为管理员
     * 
     * @return true-是管理员，false-不是管理员
     */
    public static boolean isAdmin() {
        try {
            SysUser user = getCurrentUser();
            return user != null && user.getUserType() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断当前用户是否已登录
     * 
     * @return true-已登录，false-未登录
     */
    public static boolean isAuthenticated() {
        try {
            getCurrentUser();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

