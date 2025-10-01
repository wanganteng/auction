package com.auction.mapper;

import com.auction.entity.UserNotification;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户通知Mapper
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface UserNotificationMapper {

    /**
     * 插入通知
     */
    @Insert("INSERT INTO user_notification(user_id, notification_type, title, content, related_id, related_type, link_url, is_read, create_time, update_time) " +
            "VALUES(#{userId}, #{notificationType}, #{title}, #{content}, #{relatedId}, #{relatedType}, #{linkUrl}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserNotification notification);

    /**
     * 根据用户ID查询通知列表
     */
    @Select("SELECT * FROM user_notification WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<UserNotification> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM user_notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    int countUnread(@Param("userId") Long userId);

    /**
     * 根据ID查询通知
     */
    @Select("SELECT * FROM user_notification WHERE id = #{id} AND deleted = 0")
    UserNotification selectById(@Param("id") Long id);

    /**
     * 标记为已读
     */
    @Update("UPDATE user_notification SET is_read = 1, read_time = #{readTime}, update_time = NOW() WHERE id = #{id}")
    int markAsRead(@Param("id") Long id, @Param("readTime") LocalDateTime readTime);

    /**
     * 标记用户所有通知为已读
     */
    @Update("UPDATE user_notification SET is_read = 1, read_time = NOW(), update_time = NOW() WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 删除通知（软删除）
     */
    @Update("UPDATE user_notification SET deleted = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据类型和关联ID查询通知
     */
    @Select("SELECT * FROM user_notification WHERE user_id = #{userId} AND notification_type = #{notificationType} AND related_id = #{relatedId} AND deleted = 0")
    List<UserNotification> selectByTypeAndRelatedId(@Param("userId") Long userId, @Param("notificationType") Integer notificationType, @Param("relatedId") Long relatedId);
}

