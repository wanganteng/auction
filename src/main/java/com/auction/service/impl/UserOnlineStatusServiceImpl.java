package com.auction.service.impl;

import com.auction.entity.UserOnlineStatus;
import com.auction.mapper.UserOnlineStatusMapper;
import com.auction.service.UserOnlineStatusService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户在线状态服务实现类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class UserOnlineStatusServiceImpl implements UserOnlineStatusService {

    @Autowired
    private UserOnlineStatusMapper userOnlineStatusMapper;

    @Override
    public UserOnlineStatus getById(Long id) {
        return userOnlineStatusMapper.selectById(id);
    }

    @Override
    public UserOnlineStatus getByUserId(Long userId) {
        return userOnlineStatusMapper.selectByUserId(userId);
    }

    @Override
    public List<UserOnlineStatus> getOnlineUsers() {
        return userOnlineStatusMapper.selectOnlineUsers();
    }

    @Override
    public PageInfo<UserOnlineStatus> getAllUsers(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserOnlineStatus> users = userOnlineStatusMapper.selectAllUsers();
        return new PageInfo<>(users);
    }

    @Override
    public int getOnlineUserCount() {
        return userOnlineStatusMapper.countOnlineUsers();
    }

    @Override
    public int getTotalUserCount() {
        return userOnlineStatusMapper.countAllUsers();
    }

    @Override
    public boolean updateUserLogin(Long userId, String username, String ipAddress, String userAgent, String sessionId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            UserOnlineStatus existingStatus = userOnlineStatusMapper.selectByUserId(userId);
            
            if (existingStatus != null) {
                // 更新现有记录
                existingStatus.setUsername(username);
                existingStatus.setLoginTime(now);
                existingStatus.setLastActivity(now);
                existingStatus.setIpAddress(ipAddress);
                existingStatus.setUserAgent(userAgent);
                existingStatus.setSessionId(sessionId);
                existingStatus.setIsOnline(1);
                existingStatus.setUpdateTime(now);
                
                int result = userOnlineStatusMapper.updateById(existingStatus);
                return result > 0;
            } else {
                // 创建新记录
                UserOnlineStatus status = new UserOnlineStatus();
                status.setUserId(userId);
                status.setUsername(username);
                status.setLoginTime(now);
                status.setLastActivity(now);
                status.setIpAddress(ipAddress);
                status.setUserAgent(userAgent);
                status.setSessionId(sessionId);
                status.setIsOnline(1);
                status.setCreateTime(now);
                status.setUpdateTime(now);
                
                int result = userOnlineStatusMapper.insert(status);
                return result > 0;
            }
        } catch (Exception e) {
            log.error("更新用户登录状态失败: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateLastActivity(Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int result = userOnlineStatusMapper.updateLastActivity(userId, now);
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户最后活动时间失败: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean setUserOffline(Long userId) {
        try {
            int result = userOnlineStatusMapper.setUserOffline(userId);
            return result > 0;
        } catch (Exception e) {
            log.error("设置用户离线失败: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean setAllUsersOffline() {
        try {
            int result = userOnlineStatusMapper.setAllUsersOffline();
            log.info("设置所有用户离线，影响记录数: {}", result);
            return true;
        } catch (Exception e) {
            log.error("设置所有用户离线失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean cleanExpiredRecords(int days) {
        try {
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(days);
            int result = userOnlineStatusMapper.deleteExpiredRecords(expiredTime);
            log.info("清理过期记录完成，删除记录数: {}", result);
            return true;
        } catch (Exception e) {
            log.error("清理过期记录失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isUserOnline(Long userId) {
        UserOnlineStatus status = userOnlineStatusMapper.selectByUserId(userId);
        return status != null && status.getIsOnline() == 1;
    }

    @Override
    public LocalDateTime getLastActivity(Long userId) {
        UserOnlineStatus status = userOnlineStatusMapper.selectByUserId(userId);
        return status != null ? status.getLastActivity() : null;
    }
}
