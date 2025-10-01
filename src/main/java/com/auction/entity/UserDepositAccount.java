package com.auction.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户保证金账户实体
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class UserDepositAccount {

    /**
     * 账户ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 总保证金金额
     */
    private BigDecimal totalAmount;

    /**
     * 可用保证金金额
     */
    private BigDecimal availableAmount;

    /**
     * 冻结保证金金额
     */
    private BigDecimal frozenAmount;

    /**
     * 已退还保证金金额
     */
    private BigDecimal refundedAmount;

    /**
     * 账户状态：1-正常，2-冻结，3-注销
     */
    private Integer status;

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
