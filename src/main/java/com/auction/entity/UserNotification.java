package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户通知实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class UserNotification {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知类型：1-中标通知，2-订单通知，3-支付通知，4-发货通知，5-系统通知
     */
    private Integer notificationType;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联ID（订单ID、拍品ID等）
     */
    private Long relatedId;

    /**
     * 关联类型（order-订单，item-拍品，session-拍卖会）
     */
    private String relatedType;

    /**
     * 跳转链接
     */
    private String linkUrl;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 读取时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志：0-未删除，1-已删除
     */
    private Integer deleted;
}

