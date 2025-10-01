package com.auction.service;

import com.auction.entity.UserOnlineStatus;
import com.github.pagehelper.PageInfo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户在线状态服务接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface UserOnlineStatusService {

    /**
     * 根据ID获取状态
     */
    UserOnlineStatus getById(Long id);

    /**
     * 根据用户ID获取状态
     */
    UserOnlineStatus getByUserId(Long userId);

    /**
     * 获取所有在线用户
     */
    List<UserOnlineStatus> getOnlineUsers();

    /**
     * 获取所有用户状态（分页）
     */
    PageInfo<UserOnlineStatus> getAllUsers(int pageNum, int pageSize);

    /**
     * 统计在线用户数
     */
    int getOnlineUserCount();

    /**
     * 统计总用户数
     */
    int getTotalUserCount();

    /**
     * 用户登录时更新状态
     */
    boolean updateUserLogin(Long userId, String username, String ipAddress, String userAgent, String sessionId);

    /**
     * 更新用户最后活动时间
     */
    boolean updateLastActivity(Long userId);

    /**
     * 设置用户离线
     */
    boolean setUserOffline(Long userId);

    /**
     * 设置所有用户离线
     */
    boolean setAllUsersOffline();

    /**
     * 清理过期记录
     */
    boolean cleanExpiredRecords(int days);

    /**
     * 检查用户是否在线
     */
    boolean isUserOnline(Long userId);

    /**
     * 获取用户最后活动时间
     */
    LocalDateTime getLastActivity(Long userId);
}
