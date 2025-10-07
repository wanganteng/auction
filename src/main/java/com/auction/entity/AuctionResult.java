package com.auction.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ========================================
 * 拍卖结果实体类（AuctionResult）
 * ========================================
 * 功能说明：
 * 1. 记录拍品的最终拍卖结果
 * 2. 包含成交信息（中标人、成交价、佣金）
 * 3. 关联订单和出价记录
 * 4. 支持结算状态跟踪
 * 5. 区分成交、流拍、撤拍三种结果
 * 
 * 数据库表：auction_result
 * 
 * 生成时机：
 * - 拍卖会结束后，系统自动为每个拍品生成结果记录
 * - 记录拍卖的最终状态和相关信息
 * 
 * 结果类型：
 * - 成交(1)：有人出价且达到保留价
 * - 流拍(0)：无人出价或未达保留价
 * - 撤拍(2)：拍品被撤回
 * 
 * 结算说明：
 * - 未结算(0)：结果已生成，但还未完成财务结算
 * - 已结算(1)：已完成保证金扣除、解冻等财务操作
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter等方法
public class AuctionResult {
    
    /* ========================= 基本信息字段 ========================= */
    
    /**
     * 结果记录ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;
    
    /* ========================= 关联信息字段 ========================= */
    
    /**
     * 拍卖会ID
     * 关联auction_session表
     * 表示该结果属于哪个拍卖会
     */
    private Long sessionId;
    
    /**
     * 拍品ID
     * 关联auction_item表
     * 表示该结果对应的拍品
     */
    private Long itemId;
    
    /**
     * 中标人用户ID
     * 关联sys_user表
     * 最高出价者的用户ID
     * 流拍时为null
     */
    private Long winnerUserId;
    
    /* ========================= 成交信息字段 ========================= */
    
    /**
     * 最终成交价（元）
     * 拍卖结束时的最高出价
     * 流拍时记录当前价格或起拍价
     * 使用BigDecimal确保精度
     */
    private BigDecimal finalPrice;
    
    /**
     * 最高出价记录ID
     * 关联auction_bid表
     * 指向最终的最高出价记录
     * 用于追溯成交详情
     */
    private Long highestBidId;
    
    /**
     * 结果状态
     * 0-流拍：无人出价或未达保留价
     * 1-成交：有人中标
     * 2-撤拍：拍品被撤回（异常情况）
     */
    private Integer resultStatus;
    
    /**
     * 订单ID
     * 关联auction_order表
     * 如果成交，自动生成订单并记录ID
     * 流拍时为null
     */
    private Long orderId;
    
    /* ========================= 费用信息字段 ========================= */
    
    /**
     * 佣金费用（元）
     * 计算公式：最终成交价 × 佣金比例
     * 平台收取的服务费
     * 流拍时为0
     */
    private BigDecimal commissionFee;
    
    /**
     * 保证金使用金额（元）
     * 用户已冻结的保证金金额
     * 成交后用于抵扣订单金额
     * 流拍后全额退还
     */
    private BigDecimal depositUsed;
    
    /* ========================= 结算信息字段 ========================= */
    
    /**
     * 结算状态
     * 0-未结算：结果已生成，但财务操作未完成
     * 1-已结算：已完成保证金扣除、解冻等操作
     * 
     * 结算操作：
     * - 成交：扣除中标人保证金，退还其他人保证金
     * - 流拍：退还所有人保证金
     */
    private Integer settleStatus;
    
    /**
     * 结算时间
     * 完成财务结算的时间
     * 仅当settleStatus=1时有值
     */
    private LocalDateTime settleTime;
    
    /**
     * 备注
     * 记录特殊情况说明
     * 例如：撤拍原因、流拍原因等
     */
    private String remark;
    
    /* ========================= 时间戳字段 ========================= */
    
    /**
     * 创建时间
     * 结果记录创建的时间
     * 通常是拍卖会结束的时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     * 结果记录最后修改的时间
     * 例如结算时更新
     */
    private LocalDateTime updateTime;
}


