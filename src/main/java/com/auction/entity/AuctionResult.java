package com.auction.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AuctionResult {
    private Long id;
    private Long sessionId;
    private Long itemId;
    private Long winnerUserId;
    private BigDecimal finalPrice; // 元
    private Long highestBidId;
    private Integer resultStatus; // 0=流拍 1=成交 2=撤拍
    private Long orderId;
    private BigDecimal commissionFee; // 元
    private BigDecimal depositUsed;   // 元
    private Integer settleStatus; // 0 未结算 1 已结算
    private LocalDateTime settleTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


