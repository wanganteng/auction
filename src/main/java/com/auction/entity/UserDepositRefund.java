package com.auction.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保证金退款申请实体
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class UserDepositRefund {

    /**
     * 退款申请ID
     */
    private Long id;

    /**
     * 保证金账户ID
     */
    private Long accountId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款申请单号
     */
    private String refundNo;

    /**
     * 申请退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 当前可用金额
     */
    private BigDecimal availableAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 退款状态：1-待审核，2-审核通过，3-审核拒绝，4-退款成功，5-退款失败
     */
    private Integer status;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款交易流水ID
     */
    private Long refundTransactionId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志：0-未删除，1-已删除
     */
    private Integer deleted;
}
