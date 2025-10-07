package com.auction.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ========================================
 * 保证金交易流水实体类（UserDepositTransaction）
 * ========================================
 * 功能说明：
 * 1. 记录用户保证金账户的每一笔交易
 * 2. 支持多种交易类型（充值、提现、冻结、解冻、扣除、退还）
 * 3. 记录交易前后的余额变化
 * 4. 提供完整的审计追踪
 * 5. 支持交易审核流程
 * 
 * 数据库表：user_deposit_transaction
 * 
 * 交易类型说明：
 * 1-充值：用户向账户充值保证金
 * 2-提现：用户从账户提现保证金（需审核）
 * 3-冻结：出价时冻结保证金
 * 4-解冻：拍卖结束未成交时解冻保证金
 * 5-扣除：拍卖成交后扣除保证金用于支付
 * 6-退还：其他情况的退款
 * 
 * 交易状态：
 * 0-待审核：充值和提现需要管理员审核
 * 1-成功：交易已完成
 * 2-失败：交易失败（余额不足等）
 * 3-处理中：正在处理的交易
 * 
 * 审计功能：
 * - 记录交易前后余额，便于对账
 * - 记录关联对象（拍卖会、订单等）
 * - 记录审核人和审核时间
 * - 支持追溯每一笔资金变动
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter等方法
public class UserDepositTransaction {

    /* ========================= 基本信息字段 ========================= */

    /**
     * 交易ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /**
     * 保证金账户ID
     * 关联user_deposit_account表
     * 表示这笔交易属于哪个账户
     */
    private Long accountId;

    /**
     * 用户ID
     * 关联sys_user表
     * 冗余字段，便于快速查询
     */
    private Long userId;

    /**
     * 交易流水号
     * 系统生成的唯一交易编号
     * 格式：TXN + 时间戳 + 随机数
     * 例如：TXN202401011234567890
     * 用于对账和查询
     */
    private String transactionNo;

    /* ========================= 交易信息字段 ========================= */

    /**
     * 交易类型
     * 1-充值：用户向账户充值保证金（需审核）
     * 2-提现：用户从账户提现保证金（需审核）
     * 3-冻结：出价时冻结保证金（自动）
     * 4-解冻：拍卖结束未成交时解冻（自动）
     * 5-扣除：拍卖成交后扣除保证金用于支付（自动）
     * 6-退还：其他情况的退款（手动/自动）
     */
    private Integer transactionType;

    /**
     * 交易金额（元）
     * 本次交易涉及的金额
     * 使用BigDecimal确保精度
     * 正数表示增加，负数表示减少
     */
    private BigDecimal amount;

    /**
     * 交易前余额（元）
     * 记录交易发生前的账户余额
     * 用于审计和对账
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后余额（元）
     * 记录交易完成后的账户余额
     * 计算公式：交易前余额 + 交易金额
     * 用于验证交易的正确性
     */
    private BigDecimal balanceAfter;

    /* ========================= 关联信息字段 ========================= */

    /**
     * 关联ID
     * 关联的业务对象ID
     * - 冻结/解冻：拍品ID
     * - 扣除：订单ID
     * - 充值/提现：申请ID
     */
    private Long relatedId;

    /**
     * 关联类型
     * 说明relatedId对应的业务对象类型
     * 可选值："item"、"order"、"session"等
     * 用于追溯交易的业务场景
     */
    private String relatedType;

    /**
     * 交易描述
     * 说明交易的详细信息
     * 例如："出价冻结保证金"、"用户充值"、"拍卖结束解冻"
     */
    private String description;

    /* ========================= 审核信息字段 ========================= */

    /**
     * 交易状态
     * 0-待审核：充值和提现申请等待管理员审核
     * 1-成功：交易已成功完成
     * 2-失败：交易失败（余额不足、审核拒绝等）
     * 3-处理中：正在处理的交易
     */
    private Integer status;

    /**
     * 审核人ID
     * 关联sys_user表
     * 审核充值和提现申请的管理员ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     * 管理员审核的时间
     * 仅当status变为成功或失败时设置
     */
    private LocalDateTime reviewTime;

    /**
     * 审核备注
     * 管理员审核时填写的备注信息
     * 例如：审核通过原因、拒绝原因等
     */
    private String reviewRemark;

    /* ========================= 时间戳字段 ========================= */

    /**
     * 创建时间
     * 交易记录创建的时间
     * 等于交易发生的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 交易记录最后修改的时间
     * 例如审核时更新
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（软删除）
     * 0-未删除：正常记录
     * 1-已删除：已删除的记录
     * 注意：交易流水一般不删除，用于永久审计
     */
    private Integer deleted;
}
