package com.auction.service;

import com.auction.entity.UserNotification;
import com.auction.mapper.UserNotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户通知服务类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class UserNotificationService {

    @Autowired
    private UserNotificationMapper notificationMapper;

    /**
     * 创建中标通知
     */
    @Transactional
    public boolean createWinNotification(Long userId, Long orderId, Long itemId, String itemName, String orderNo) {
        return createWinNotification(userId, orderId, itemId, itemName, orderNo, null, false, null);
    }
    
    /**
     * 创建中标通知（带余额检查）
     */
    @Transactional
    public boolean createWinNotification(Long userId, Long orderId, Long itemId, String itemName, String orderNo,
                                        java.math.BigDecimal balanceAmount, boolean needRecharge, java.math.BigDecimal rechargeAmount) {
        try {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setNotificationType(1); // 中标通知
            notification.setTitle("🎉 恭喜您中标了！");
            
            // 构建通知内容
            StringBuilder content = new StringBuilder();
            content.append(String.format("恭喜您成功竞得拍品【%s】！\n\n", itemName));
            content.append(String.format("订单号：%s\n", orderNo));
            
            if (balanceAmount != null) {
                content.append(String.format("需支付尾款：¥%.2f\n", balanceAmount));
            }
            
            if (needRecharge && rechargeAmount != null && rechargeAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                content.append(String.format("\n⚠️ 温馨提示：\n"));
                content.append(String.format("您的保证金余额不足，还需充值 ¥%.2f 才能完成支付。\n", rechargeAmount));
                content.append("请尽快前往【保证金管理】页面充值，以免超时未付款导致订单取消。");
            } else {
                content.append("\n您的保证金余额充足，请前往【我的订单】完成支付。");
            }
            
            notification.setContent(content.toString());
            notification.setRelatedId(orderId);
            notification.setRelatedType("order");
            notification.setLinkUrl("/user/orders/" + orderId);
            notification.setIsRead(0);
            
            int result = notificationMapper.insert(notification);
            if (result > 0) {
                log.info("中标通知创建成功: userId={}, orderId={}, needRecharge={}", userId, orderId, needRecharge);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("创建中标通知失败: userId={}, orderId={}, error={}", userId, orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建订单通知
     */
    @Transactional
    public boolean createOrderNotification(Long userId, Long orderId, String title, String content) {
        try {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setNotificationType(2); // 订单通知
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedId(orderId);
            notification.setRelatedType("order");
            notification.setLinkUrl("/user/orders/" + orderId);
            notification.setIsRead(0);
            
            int result = notificationMapper.insert(notification);
            return result > 0;
        } catch (Exception e) {
            log.error("创建订单通知失败: userId={}, orderId={}, error={}", userId, orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建系统通知
     */
    @Transactional
    public boolean createSystemNotification(Long userId, String title, String content, String linkUrl) {
        try {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setNotificationType(5); // 系统通知
            notification.setTitle(title);
            notification.setContent(content);
            notification.setLinkUrl(linkUrl);
            notification.setIsRead(0);
            
            int result = notificationMapper.insert(notification);
            return result > 0;
        } catch (Exception e) {
            log.error("创建系统通知失败: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取用户通知列表（分页）
     */
    public Map<String, Object> getUserNotifications(Long userId, Integer pageNum, Integer pageSize) {
        try {
            int offset = (pageNum - 1) * pageSize;
            List<UserNotification> notifications = notificationMapper.selectByUserId(userId, offset, pageSize);
            int unreadCount = notificationMapper.countUnread(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", notifications);
            result.put("unreadCount", unreadCount);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            
            return result;
        } catch (Exception e) {
            log.error("获取用户通知列表失败: userId={}, error={}", userId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取未读通知数量
     */
    public int getUnreadCount(Long userId) {
        try {
            return notificationMapper.countUnread(userId);
        } catch (Exception e) {
            log.error("获取未读通知数量失败: userId={}, error={}", userId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public boolean markAsRead(Long notificationId) {
        try {
            int result = notificationMapper.markAsRead(notificationId, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("标记通知为已读失败: notificationId={}, error={}", notificationId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public boolean markAllAsRead(Long userId) {
        try {
            int result = notificationMapper.markAllAsRead(userId);
            log.info("标记所有通知为已读: userId={}, count={}", userId, result);
            return true;
        } catch (Exception e) {
            log.error("标记所有通知为已读失败: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除通知
     */
    @Transactional
    public boolean deleteNotification(Long notificationId) {
        try {
            int result = notificationMapper.deleteById(notificationId);
            return result > 0;
        } catch (Exception e) {
            log.error("删除通知失败: notificationId={}, error={}", notificationId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据ID获取通知
     */
    public UserNotification getById(Long id) {
        try {
            return notificationMapper.selectById(id);
        } catch (Exception e) {
            log.error("获取通知失败: id={}, error={}", id, e.getMessage(), e);
            return null;
        }
    }
}

