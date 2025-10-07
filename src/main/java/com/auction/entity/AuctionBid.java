package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ========================================
 * 拍卖出价实体类（AuctionBid）
 * ========================================
 * 功能说明：
 * 1. 表示用户在拍卖会中的每一次出价记录
 * 2. 记录出价的详细信息（金额、时间、来源）
 * 3. 用于显示出价历史和排行
 * 4. 支持手动出价和自动出价
 * 5. 记录出价的状态（有效、无效、被超越）
 * 
 * 数据库表：auction_bid
 * 
 * 出价规则：
 * - 每次出价必须高于当前最高价
 * - 必须符合加价阶梯规则
 * - 出价时自动冻结保证金
 * - 被他人超越后状态变为"被超越"
 * 
 * 出价类型：
 * - 手动出价：用户主动在页面点击出价按钮
 * - 自动出价：用户设置最高价，系统自动跟价（未实现）
 * 
 * 状态说明：
 * - 有效(0)：当前最高出价
 * - 无效(1)：出价被撤销或取消
 * - 被超越(2)：被其他用户的更高出价超越
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter等方法
public class AuctionBid {

    /* ========================= 基本信息字段 ========================= */

    /**
     * 出价ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /* ========================= 关联信息字段 ========================= */

    /**
     * 拍卖会ID
     * 关联auction_session表
     * 表示出价发生在哪个拍卖会
     */
    private Long sessionId;

    /**
     * 拍品ID
     * 关联auction_item表
     * 表示对哪个拍品出价
     */
    private Long itemId;

    /**
     * 出价用户ID
     * 关联sys_user表
     * 表示谁出的价
     */
    private Long userId;

    /* ========================= 出价信息字段 ========================= */

    /**
     * 出价金额（元，整数）
     * 用户的出价金额，必须是整数
     * 使用BigDecimal确保精度
     * 必须高于当前最高价且符合加价规则
     */
    private BigDecimal bidAmountYuan;

    /**
     * 出价时间
     * 记录用户出价的精确时间
     * 用于排序和判断出价先后
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bidTime;

    /* ========================= 出价属性字段 ========================= */

    /**
     * 出价来源
     * 1-手动出价：用户在页面上主动点击出价按钮
     * 2-自动出价：系统根据用户设置的自动出价规则自动出价
     */
    private Integer source;

    /**
     * 是否自动出价
     * 0-否：手动出价
     * 1-是：自动出价（代理出价功能）
     */
    private Integer isAuto;

    /**
     * 出价状态
     * 0-有效：当前的有效出价
     * 1-无效：被撤销或取消的出价
     * 2-被超越：被其他用户的更高出价超越
     */
    private Integer status;

    /* ========================= 审计信息字段 ========================= */

    /**
     * 客户端IP地址
     * 记录出价时的IP地址
     * 用于安全审计和防作弊
     * 格式：xxx.xxx.xxx.xxx
     */
    private String clientIp;

    /**
     * 用户代理（浏览器信息）
     * 记录出价时使用的浏览器和设备信息
     * 用于统计分析和安全审计
     * 例如："Mozilla/5.0 (Windows NT 10.0; Win64; x64) ..."
     */
    private String userAgent;

    /* ========================= 时间戳字段 ========================= */

    /**
     * 创建时间
     * 记录出价记录创建的时间（通常等于bidTime）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 记录出价记录最后修改的时间
     * 例如状态变更时更新
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志（软删除）
     * 0-未删除：正常记录
     * 1-已删除：已删除的记录
     */
    private Integer deleted;

    /* ========================= 非数据库字段（用于关联查询） ========================= */

    /**
     * 用户信息（非数据库字段）
     * 关联查询得到的出价用户详细信息
     * 用于在出价列表中显示用户昵称
     */
    private SysUser user;

    /**
     * 拍品信息（非数据库字段）
     * 关联查询得到的拍品详细信息
     * 用于在出价记录中显示拍品名称和图片
     */
    private AuctionItem item;

    /**
     * 拍卖会信息（非数据库字段）
     * 关联查询得到的拍卖会详细信息
     * 用于在出价记录中显示拍卖会名称
     */
    private AuctionSession session;
}