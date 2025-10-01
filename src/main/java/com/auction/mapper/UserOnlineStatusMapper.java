package com.auction.mapper;

import com.auction.entity.UserOnlineStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户在线状态Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface UserOnlineStatusMapper {

    /**
     * 根据ID查询
     */
    UserOnlineStatus selectById(Long id);

    /**
     * 根据用户ID查询
     */
    UserOnlineStatus selectByUserId(Long userId);

    /**
     * 查询所有在线用户
     */
    List<UserOnlineStatus> selectOnlineUsers();

    /**
     * 查询所有用户状态（分页）
     */
    List<UserOnlineStatus> selectAllUsers();

    /**
     * 统计在线用户数
     */
    int countOnlineUsers();

    /**
     * 统计总用户数
     */
    int countAllUsers();

    /**
     * 插入用户状态
     */
    int insert(UserOnlineStatus status);

    /**
     * 更新用户状态
     */
    int updateById(UserOnlineStatus status);

    /**
     * 更新用户最后活动时间
     */
    int updateLastActivity(@Param("userId") Long userId, @Param("lastActivity") LocalDateTime lastActivity);

    /**
     * 设置用户离线
     */
    int setUserOffline(@Param("userId") Long userId);

    /**
     * 设置所有用户离线（系统维护时使用）
     */
    int setAllUsersOffline();

    /**
     * 删除过期记录
     */
    int deleteExpiredRecords(@Param("expiredTime") LocalDateTime expiredTime);
}
