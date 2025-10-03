package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拍卖订单实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class AuctionOrder {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 拍卖会ID
     */
    private Long sessionId;

    /**
     * 拍品ID
     */
    private Long itemId;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID（超级管理员）
     */
    private Long sellerId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 保证金金额
     */
    private BigDecimal depositAmount;

    /**
     * 尾款金额
     */
    private BigDecimal balanceAmount;

    /**
     * 配送方式：1-物流配送，2-线下自提
     */
    private Integer deliveryMethod;

    /**
     * 物流费用
     */
    private BigDecimal shippingFee;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 自提地址
     */
    private String pickupAddress;

    /**
     * 订单状态：1-待付款，2-已付款，3-已发货，4-已收货，5-已完成，6-已取消
     */
    private Integer status;

    /**
     * 付款时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 发货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shipTime;

    /**
     * 收货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

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
     * 买家信息（非数据库字段）
     */
    private SysUser buyer;

    /**
     * 卖家信息（非数据库字段）
     */
    private SysUser seller;

    /**
     * 拍品信息（非数据库字段）
     */
    private AuctionItem item;

    /**
     * 拍卖会信息（非数据库字段）
     */
    private AuctionSession session;

    /**
     * 物流公司ID
     */
    private Long logisticsCompanyId;

    /**
     * 物流信息（非数据库字段）
     */
    private AuctionLogistics logistics;
}