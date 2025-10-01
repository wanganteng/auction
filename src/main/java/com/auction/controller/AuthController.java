package com.auction.controller;

import com.auction.common.Result;
import com.auction.dto.LoginRequest;
import com.auction.dto.RegisterRequest;
import com.auction.entity.SysUser;
import com.auction.security.JwtTokenProvider;
import com.auction.service.SysUserService;
import com.auction.service.RedisService;
import com.auction.service.SysConfigService;
import com.auction.service.UserOnlineStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、登出等认证相关操作
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private SysUserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private UserOnlineStatusService userOnlineStatusService;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @param request HTTP请求
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户通过用户名和密码登录系统")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest, 
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        try {
            log.info("用户登录尝试: {}", loginRequest.getUsername());
            
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // 设置认证信息到Security上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息
            SysUser user = userService.getByUsername(loginRequest.getUsername());
            
            // 更新最后登录信息
            String clientIp = getClientIpAddress(request);
            userService.updateLastLoginTime(user.getId(), clientIp);

            // 生成JWT Token（包含基本信息）
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user);
            
            // 在Redis中存储会话信息（混合模式）
            String sessionKey = "user:session:" + user.getId();
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", user.getId());
            sessionData.put("username", user.getUsername());
            sessionData.put("userType", user.getUserType());
            sessionData.put("isOnline", true);
            sessionData.put("lastActivity", System.currentTimeMillis());
            sessionData.put("ipAddress", clientIp);
            sessionData.put("userAgent", request.getHeader("User-Agent"));
            sessionData.put("loginTime", System.currentTimeMillis());
            sessionData.put("accessToken", accessToken);
            sessionData.put("refreshToken", refreshToken);
            
            // 设置会话过期时间（7天）
            redisService.setHash(sessionKey, sessionData);
            redisService.expire(sessionKey, 7 * 24 * 3600);
            
            // 存储到用户在线状态表
            userOnlineStatusService.updateUserLogin(
                user.getId(), 
                user.getUsername(), 
                clientIp, 
                request.getHeader("User-Agent"), 
                sessionKey
            );

            String redirectUrl = user.getUserType() == 1 ? "/auction/admin/" : "/auction/user/";
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);
            data.put("tokenType", "Bearer");
            data.put("expiresIn", 3600); // 1小时
            data.put("user", user);
            data.put("redirectUrl", redirectUrl);
            data.put("sessionKey", sessionKey);

            log.info("用户登录成功: {}", loginRequest.getUsername());
            return Result.success("登录成功", data);
            
        } catch (Exception e) {
            log.error("用户登录失败: {}, 错误: {}", loginRequest.getUsername(), e.getMessage());
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public Result<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            log.info("用户注册尝试: {}", registerRequest.getUsername());
            
            // 检查用户名是否已存在
            if (userService.existsByUsername(registerRequest.getUsername())) {
                return Result.error("用户名已存在");
            }
            
            // 检查手机号是否已存在
            if (registerRequest.getPhone() != null && userService.existsByPhone(registerRequest.getPhone())) {
                return Result.error("手机号已存在");
            }
            
            // 检查邮箱是否已存在
            if (registerRequest.getEmail() != null && userService.existsByEmail(registerRequest.getEmail())) {
                return Result.error("邮箱已存在");
            }
            
            // 创建用户
            SysUser user = new SysUser();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setNickname(registerRequest.getNickname());
            user.setPhone(registerRequest.getPhone());
            user.setEmail(registerRequest.getEmail());
            user.setUserType(1); // 普通用户
            
            if (userService.save(user)) {
                log.info("用户注册成功: {}", registerRequest.getUsername());
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败，请重试");
            }
            
        } catch (Exception e) {
            log.error("用户注册失败: {}, 错误: {}", registerRequest.getUsername(), e.getMessage());
            return Result.error("注册失败：" + e.getMessage());
        }
    }


    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<SysUser> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Result.error("未登录");
            }
            
            String username = authentication.getName();
            SysUser user = userService.getByUsername(username);
            
            if (user != null) {
                // 清除敏感信息
                user.setPassword(null);
                return Result.success("获取成功", user);
            } else {
                return Result.error("用户不存在");
            }
            
        } catch (Exception e) {
            log.error("获取当前用户信息失败: {}", e.getMessage());
            return Result.error("获取用户信息失败");
        }
    }


    /**
     * 刷新Token
     * 
     * @param refreshTokenRequest 刷新Token请求
     * @return 新的Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用Refresh Token获取新的Access Token")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.get("refreshToken");
            
            if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
                return Result.error("无效的Refresh Token");
            }
            
            // 从Refresh Token中获取用户信息
            String username = tokenProvider.getUsernameFromToken(refreshToken);
            Long userId = tokenProvider.getUserIdFromToken(refreshToken);
            Integer userType = tokenProvider.getUserTypeFromToken(refreshToken);
            
            // 检查Redis中的会话是否存在
            String sessionKey = "user:session:" + userId;
            if (!redisService.exists(sessionKey)) {
                return Result.error("会话已过期，请重新登录");
            }
            
            // 生成新的Access Token
            String newAccessToken = tokenProvider.generateToken(username, userId, userType);
            
            // 更新Redis中的Access Token
            redisService.hset(sessionKey, "accessToken", newAccessToken);
            redisService.hset(sessionKey, "lastActivity", System.currentTimeMillis());
            
            // 更新用户活动时间
            userOnlineStatusService.updateLastActivity(userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            data.put("tokenType", "Bearer");
            data.put("expiresIn", 3600); // 1小时
            
            return Result.success("Token刷新成功", data);
            
        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            return Result.error("Token刷新失败");
        }
    }

    /**
     * 用户退出登录
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 退出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户退出登录", description = "用户退出登录并清理会话")
    public Result<String> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 从请求头获取Token
            String token = getTokenFromRequest(request);
            if (token != null) {
                Long userId = tokenProvider.getUserIdFromToken(token);
                
                // 清除Redis中的用户会话
                String sessionKey = "user:session:" + userId;
                redisService.del(sessionKey);
                
                // 设置用户离线
                userOnlineStatusService.setUserOffline(userId);
            }
            
            // 清理Security上下文
            SecurityContextHolder.clearContext();
            
            log.info("用户退出登录成功");
            return Result.success("退出登录成功");
            
        } catch (Exception e) {
            log.error("用户退出登录失败: {}", e.getMessage());
            return Result.error("退出登录失败");
        }
    }

    /**
     * 从请求中获取Token
     * 
     * @param request HTTP请求
     * @return Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}
