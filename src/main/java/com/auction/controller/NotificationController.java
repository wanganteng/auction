package com.auction.controller;

import com.auction.common.Result;
import com.auction.entity.SysUser;
import com.auction.entity.UserNotification;
import com.auction.security.CustomUserDetailsService;
import com.auction.service.UserNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "通知管理", description = "用户通知相关接口")
public class NotificationController {

    @Autowired
    private UserNotificationService notificationService;

    /**
     * 获取当前用户通知列表
     */
    @GetMapping
    @Operation(summary = "获取通知列表", description = "获取当前用户的通知列表")
    public Result<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> data = notificationService.getUserNotifications(currentUser.getId(), pageNum, pageSize);
            return Result.success("获取成功", data);
        } catch (Exception e) {
            log.error("获取通知列表失败: {}", e.getMessage(), e);
            return Result.error("获取通知列表失败");
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读数量", description = "获取当前用户的未读通知数量")
    public Result<Integer> getUnreadCount() {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            int count = notificationService.getUnreadCount(currentUser.getId());
            return Result.success("获取成功", count);
        } catch (Exception e) {
            log.error("获取未读通知数量失败: {}", e.getMessage(), e);
            return Result.error("获取未读通知数量失败");
        }
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "标记指定通知为已读")
    public Result<String> markAsRead(@PathVariable Long id) {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 验证通知是否属于当前用户
            UserNotification notification = notificationService.getById(id);
            if (notification == null) {
                return Result.error("通知不存在");
            }
            if (!notification.getUserId().equals(currentUser.getId())) {
                return Result.error("无权操作此通知");
            }
            
            boolean success = notificationService.markAsRead(id);
            return success ? Result.success("标记成功") : Result.error("标记失败");
        } catch (Exception e) {
            log.error("标记通知已读失败: id={}, error={}", id, e.getMessage(), e);
            return Result.error("标记失败");
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "全部已读", description = "标记当前用户所有通知为已读")
    public Result<String> markAllAsRead() {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = notificationService.markAllAsRead(currentUser.getId());
            return success ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            log.error("标记所有通知已读失败: {}", e.getMessage(), e);
            return Result.error("操作失败");
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public Result<String> deleteNotification(@PathVariable Long id) {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 验证通知是否属于当前用户
            UserNotification notification = notificationService.getById(id);
            if (notification == null) {
                return Result.error("通知不存在");
            }
            if (!notification.getUserId().equals(currentUser.getId())) {
                return Result.error("无权操作此通知");
            }
            
            boolean success = notificationService.deleteNotification(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除通知失败: id={}, error={}", id, e.getMessage(), e);
            return Result.error("删除失败");
        }
    }

    /**
     * 获取当前用户
     */
    /**
     * 获取当前用户
     * @deprecated 使用 SecurityUtils.getCurrentUser() 代替
     */
    @Deprecated
    private SysUser getCurrentUser() {
        try {
            return com.auction.util.SecurityUtils.getCurrentUser();
        } catch (Exception e) {
            log.error("获取当前用户失败: {}", e.getMessage());
            return null;
        }
    }
}

