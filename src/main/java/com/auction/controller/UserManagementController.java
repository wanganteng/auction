package com.auction.controller;

import com.auction.common.Result;
import com.auction.entity.UserOnlineStatus;
import com.auction.service.UserOnlineStatusService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/user-management")
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserManagementController {

    @Autowired
    private UserOnlineStatusService userOnlineStatusService;

    @GetMapping("/list")
    @Operation(summary = "获取用户列表", description = "获取所有用户状态列表")
    public Result<PageInfo<UserOnlineStatus>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            PageInfo<UserOnlineStatus> users = userOnlineStatusService.getAllUsers(pageNum, pageSize);
            return Result.success(users);
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage(), e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/online")
    @Operation(summary = "获取在线用户", description = "获取当前在线用户列表")
    public Result<List<UserOnlineStatus>> getOnlineUsers() {
        try {
            List<UserOnlineStatus> onlineUsers = userOnlineStatusService.getOnlineUsers();
            return Result.success(onlineUsers);
        } catch (Exception e) {
            log.error("获取在线用户失败: {}", e.getMessage(), e);
            return Result.error("获取在线用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取用户统计", description = "获取用户统计信息")
    public Result<Map<String, Object>> getUserStats() {
        try {
            int onlineCount = userOnlineStatusService.getOnlineUserCount();
            int totalCount = userOnlineStatusService.getTotalUserCount();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("onlineCount", onlineCount);
            stats.put("totalCount", totalCount);
            stats.put("offlineCount", totalCount - onlineCount);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计失败: {}", e.getMessage(), e);
            return Result.error("获取用户统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户状态详情")
    public Result<UserOnlineStatus> getUserById(@PathVariable Long userId) {
        try {
            UserOnlineStatus user = userOnlineStatusService.getByUserId(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户详情失败: {}", e.getMessage(), e);
            return Result.error("获取用户详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/offline")
    @Operation(summary = "强制用户离线", description = "强制指定用户离线")
    public Result<String> forceUserOffline(@PathVariable Long userId) {
        try {
            boolean success = userOnlineStatusService.setUserOffline(userId);
            return success ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            log.error("强制用户离线失败: {}", e.getMessage(), e);
            return Result.error("强制用户离线失败: " + e.getMessage());
        }
    }

    @PostMapping("/offline-all")
    @Operation(summary = "强制所有用户离线", description = "强制所有用户离线（系统维护时使用）")
    public Result<String> forceAllUsersOffline() {
        try {
            boolean success = userOnlineStatusService.setAllUsersOffline();
            return success ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            log.error("强制所有用户离线失败: {}", e.getMessage(), e);
            return Result.error("强制所有用户离线失败: " + e.getMessage());
        }
    }

    @PostMapping("/clean-expired")
    @Operation(summary = "清理过期记录", description = "清理过期的用户状态记录")
    public Result<String> cleanExpiredRecords(@RequestParam(defaultValue = "30") int days) {
        try {
            boolean success = userOnlineStatusService.cleanExpiredRecords(days);
            return success ? Result.success("清理完成") : Result.error("清理失败");
        } catch (Exception e) {
            log.error("清理过期记录失败: {}", e.getMessage(), e);
            return Result.error("清理过期记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/online-status")
    @Operation(summary = "检查用户在线状态", description = "检查指定用户是否在线")
    public Result<Map<String, Object>> checkUserOnlineStatus(@PathVariable Long userId) {
        try {
            boolean isOnline = userOnlineStatusService.isUserOnline(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("isOnline", isOnline);
            result.put("userId", userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查用户在线状态失败: {}", e.getMessage(), e);
            return Result.error("检查用户在线状态失败: " + e.getMessage());
        }
    }
}
