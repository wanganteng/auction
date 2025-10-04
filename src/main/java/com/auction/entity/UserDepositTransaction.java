package com.auction.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保证金交易流水实体
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class UserDepositTransaction {

    /**
     * 交易ID
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
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 交易类型：1-充值，2-提现，3-冻结，4-解冻，5-扣除，6-退还
     */
    private Integer transactionType;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联ID（拍卖会ID、订单ID等）
     */
    private Long relatedId;

    /**
     * 关联类型：auction_session、order等
     */
    private String relatedType;

    /**
     * 交易描述
     */
    private String description;

    /**
     * 交易状态：0-待审核，1-成功，2-失败，3-处理中
     */
    private Integer status;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核备注
     */
    private String reviewRemark;

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
