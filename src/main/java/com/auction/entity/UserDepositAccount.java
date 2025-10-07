package com.auction.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ========================================
 * 用户保证金账户实体类（UserDepositAccount）
 * ========================================
 * 功能说明：
 * 1. 表示用户在拍卖系统中的保证金账户
 * 2. 记录用户的保证金余额（总额、可用、冻结、退还）
 * 3. 支持保证金的充值、冻结、解冻、扣除、退还等操作
 * 4. 用于管理用户参与竞拍的资金
 * 
 * 数据库表：user_deposit_account
 * 
 * 保证金说明：
 * - 用户参与竞拍前需要充值保证金
 * - 出价时自动冻结相应金额
 * - 拍卖结束后，成交则扣除，未成交则解冻
 * - 可以申请提现可用余额
 * 
 * 金额关系：
 * 总金额 = 可用金额 + 冻结金额
 * 
 * 账户状态：
 * - 正常(1)：可以正常使用
 * - 冻结(2)：账户被冻结，无法充值和竞拍
 * - 注销(3)：账户已注销
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter等方法
public class UserDepositAccount {

    /* ========================= 基本信息字段 ========================= */

    /**
     * 账户ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /**
     * 用户ID
     * 关联sys_user表
     * 一个用户对应一个保证金账户
     */
    private Long userId;

    /* ========================= 金额信息字段 ========================= */

    /**
     * 总保证金金额（元）
     * 账户的总金额
     * 计算公式：可用金额 + 冻结金额
     * 使用BigDecimal确保金额精度
     */
    private BigDecimal totalAmount;

    /**
     * 可用保证金金额（元）
     * 可以自由使用的金额
     * 可以用于：
     * - 参与新的竞拍（会被冻结）
     * - 申请提现
     * - 支付订单
     */
    private BigDecimal availableAmount;

    /**
     * 冻结保证金金额（元）
     * 已被冻结的金额
     * 冻结场景：
     * - 用户出价时冻结（出价金额 × 保证金比例）
     * - 拍卖结束后解冻（如果未中标）
     * - 成交后扣除（如果中标）
     */
    private BigDecimal frozenAmount;

    /**
     * 已退还保证金金额（元）
     * 历史累计退还给用户的金额
     * 用于统计和审计
     */
    private BigDecimal refundedAmount;

    /* ========================= 状态字段 ========================= */

    /**
     * 账户状态
     * 1-正常：可以正常充值、竞拍、提现
     * 2-冻结：账户被管理员冻结，无法操作（违规用户）
     * 3-注销：账户已注销，无法使用
     */
    private Integer status;

    /* ========================= 时间戳字段 ========================= */

    /**
     * 创建时间
     * 账户首次创建的时间（通常是用户首次充值时）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 账户最后一次变动的时间
     * 每次金额变化都会更新
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（软删除）
     * 0-未删除：正常账户
     * 1-已删除：已删除的账户
     */
    private Integer deleted;
}
