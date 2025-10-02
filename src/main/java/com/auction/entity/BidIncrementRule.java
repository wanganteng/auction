package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加价阶梯规则实体类
 *
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class BidIncrementRule {

    /**
     * 规则ID
     */
    private Long id;

    /**
     * 加价阶梯配置ID
     */
    private Long configId;

    /**
     * 价格下限（包含）
     */
    private BigDecimal minAmount;

    /**
     * 价格上限（不包含）
     */
    private BigDecimal maxAmount;

    /**
     * 加价金额
     */
    private BigDecimal incrementAmount;

    /**
     * 排序号（数字越小越靠前）
     */
    private Integer sortOrder;

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
     * 加价阶梯配置信息（非数据库字段）
     */
    private BidIncrementConfig config;
}
