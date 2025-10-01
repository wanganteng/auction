package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流信息实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class AuctionLogistics {

    /**
     * 物流ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 运单号
     */
    private String trackingNumber;

    /**
     * 物流状态：1-已发货，2-运输中，3-已到达，4-已签收
     */
    private Integer logisticsStatus;

    /**
     * 发货人姓名
     */
    private String senderName;

    /**
     * 发货人电话
     */
    private String senderPhone;

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
     * 发货地址
     */
    private String senderAddress;

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
     * 物流详情
     */
    private String logisticsDetail;

    /**
     * 备注
     */
    private String remark;

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
     * 订单信息（非数据库字段）
     */
    private AuctionOrder order;
}