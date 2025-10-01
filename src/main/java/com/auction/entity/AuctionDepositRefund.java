package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保证金退还实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class AuctionDepositRefund {

    /**
     * 退还ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 拍卖品ID
     */
    private Long auctionItemId;

    /**
     * 退还金额（分）
     */
    private Long refundAmount;

    /**
     * 退还原因
     */
    private String refundReason;

    /**
     * 状态：0-待处理，1-已处理，2-已拒绝
     */
    private Integer status;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime;

    /**
     * 处理备注
     */
    private String processRemark;

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
     * 获取退还金额（元）
     * 
     * @return 退还金额（元）
     */
    public BigDecimal getRefundAmountYuan() {
        return BigDecimal.valueOf(this.refundAmount).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getStatusDesc() {
        switch (this.status) {
            case 0: return "待处理";
            case 1: return "已处理";
            case 2: return "已拒绝";
            default: return "未知";
        }
    }
}