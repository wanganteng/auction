package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拍卖会实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class AuctionSession {

    /**
     * 拍卖会ID
     */
    private Long id;

    /**
     * 拍卖会名称
     */
    private String sessionName;

    /**
     * 拍卖会描述
     */
    private String description;

    /**
     * 拍卖会类型：1-艺术品专场，2-珠宝专场，3-古董专场，4-综合专场
     */
    private Integer sessionType;
        /**
         * 会场图片列表（JSON数组字符串），第一张作为封面
         */
        private String images;

    /**
     * 拍卖会状态：0-草稿，1-待开始，2-进行中，3-已结束，4-已取消
     */
    private Integer status;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 创建人ID（超级管理员）
     */
    private Long creatorId;

    /**
     * 拍品总数
     */
    private Integer totalItems;

    /**
     * 已成交拍品数
     */
    private Integer soldItems;

    /**
     * 围观人数
     */
    private Integer viewCount;

    /**
     * 保证金比例（0-1之间的小数）
     */
    private BigDecimal depositRatio;

    /**
     * 佣金比例（0-1之间的小数）
     */
    private BigDecimal commissionRatio;

    /**
     * 是否保真：0-否，1-是
     */
    private Integer isAuthentic;

    /**
     * 是否包邮：0-否，1-是
     */
    private Integer isFreeShipping;

    /**
     * 是否支持退货：0-否，1-是
     */
    private Integer isReturnable;

    /**
     * 最小保证金金额（分）
     */
    private Long minDepositAmount;

    /**
     * 最大出价金额（分）
     */
    private Long maxBidAmount;

    /**
     * 最小加价幅度（分）
     */
    private Long minIncrementAmount;

    /**
     * 拍卖会封面图片
     */
    private String coverImage;

    /**
     * 拍卖会规则
     */
    private String rules;

    /**
     * 是否对用户可见：0-隐藏，1-展示
     */
    private Integer isVisible;

    /** 是否启用延时拍卖：0-否，1-是 */
    private Integer antiSnipingEnabled;
    /** 临近结束触发延时的阈值（秒） */
    private Integer extendThresholdSec;
    /** 每次顺延的秒数 */
    private Integer extendSeconds;
    /** 最大顺延次数（0为不限制） */
    private Integer extendMaxTimes;

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
     * 创建人信息（非数据库字段）
     */
    private SysUser creator;

    /**
     * 拍卖会拍品列表（非数据库字段）
     */
    private List<AuctionItem> items;

    /**
     * 拍卖会拍品ID列表（非数据库字段）
     */
    private List<Long> itemIds;
}