package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拍卖出价实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class AuctionBid {

    /**
     * 出价ID
     */
    private Long id;

    /**
     * 拍卖会ID
     */
    private Long sessionId;

    /**
     * 拍品ID
     */
    private Long itemId;

    /**
     * 出价用户ID
     */
    private Long userId;

    /**
     * 出价金额（元，整数）
     */
    private BigDecimal bidAmountYuan;

    /**
     * 出价时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bidTime;

    /**
     * 出价来源：1-手动出价，2-自动出价
     */
    private Integer source;

    /**
     * 是否自动出价：0-否，1-是
     */
    private Integer isAuto;

    /**
     * 出价状态：0-有效，1-无效，2-被超越
     */
    private Integer status;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

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

    /**
     * 用户信息（非数据库字段）
     */
    private SysUser user;

    /**
     * 拍品信息（非数据库字段）
     */
    private AuctionItem item;

    /**
     * 拍卖会信息（非数据库字段）
     */
    private AuctionSession session;
}