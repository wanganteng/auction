package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ========================================
 * 拍卖会实体类（AuctionSession）
 * ========================================
 * 功能说明：
 * 1. 表示拍卖系统中的拍卖会/专场信息
 * 2. 一个拍卖会可以包含多个拍品
 * 3. 包含拍卖会的基本信息（名称、描述、类型、时间）
 * 4. 包含拍卖规则（保证金、佣金、加价规则等）
 * 5. 包含服务保障（保真、包邮、退货）
 * 6. 包含防狙击拍卖设置（延时拍卖）
 * 7. 支持拍卖会的完整生命周期管理
 * 
 * 数据库表：auction_session
 * 
 * 状态流转：
 * 草稿(0) -> 待开始(1) -> 进行中(2) -> 已结束(3)
 *                     \-> 已取消(4)
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter、toString等方法
public class AuctionSession {

    /* ========================= 基本信息字段 ========================= */

    /**
     * 拍卖会ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /**
     * 拍卖会名称
     * 例如："2024春季艺术品专场"、"名家书画精品拍卖"
     * 必填字段
     */
    private String sessionName;

    /**
     * 拍卖会描述
     * 详细介绍本次拍卖会的主题、特色、亮点等
     * 支持长文本
     */
    private String description;

    /**
     * 拍卖会类型
     * 1-书画专场：主要拍卖书法和绘画作品
     * 2-陶瓷专场：主要拍卖瓷器和陶器
     * 3-玉器专场：主要拍卖玉石制品
     * 4-珠宝专场：主要拍卖珠宝首饰
     * 5-综合专场：包含多种类别拍品
     */
    private Integer sessionType;
    
    /**
     * 会场图片列表（JSON数组字符串）
     * 存储格式：["url1", "url2", "url3"]
     * 第一张图片作为封面图
     * 用于在前台展示拍卖会场景
     */
    private String images;

    /* ========================= 状态和时间字段 ========================= */

    /**
     * 拍卖会状态
     * 0-草稿：刚创建，未发布
     * 1-待开始：已发布，等待开始时间到达
     * 2-进行中：正在进行拍卖
     * 3-已结束：拍卖时间结束
     * 4-已取消：拍卖会被取消
     */
    private Integer status;

    /**
     * 开始时间
     * 拍卖会开始的时间，到达此时间后状态自动变为"进行中"
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     * 拍卖会结束的时间，到达此时间后状态自动变为"已结束"
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /* ========================= 关联信息字段 ========================= */

    /**
     * 创建人ID（超级管理员）
     * 关联sys_user表，记录是哪位管理员创建的拍卖会
     */
    private Long creatorId;

    /* ========================= 统计信息字段 ========================= */

    /**
     * 拍品总数
     * 本次拍卖会包含的拍品数量
     * 创建拍卖会时自动计算
     */
    private Integer totalItems;

    /**
     * 已成交拍品数
     * 拍卖结束后统计的成交拍品数量
     * 实时更新
     */
    private Integer soldItems;

    /**
     * 围观人数
     * 访问过本次拍卖会详情页的用户数量
     * 通过Redis实时统计
     */
    private Integer viewCount;

    /* ========================= 拍卖规则字段 ========================= */

    /**
     * 保证金比例（0-1之间的小数）
     * 例如：0.1 表示10%
     * 用户需要缴纳拍品起拍价 × 保证金比例的金额才能参与竞拍
     * 拍卖结束后未成交则退还保证金
     */
    private BigDecimal depositRatio;

    /**
     * 佣金比例（0-1之间的小数）
     * 例如：0.05 表示5%
     * 成交后买家需额外支付成交价 × 佣金比例的佣金给平台
     * 这是平台的主要收入来源
     */
    private BigDecimal commissionRatio;

    /* ========================= 服务保障字段 ========================= */

    /**
     * 是否保真
     * 0-否：不保证真品，买家需自行鉴别
     * 1-是：平台承诺为真品，假一赔十
     */
    private Integer isAuthentic;

    /**
     * 是否包邮
     * 0-否：买家需承担运费
     * 1-是：卖家/平台承担运费
     */
    private Integer isFreeShipping;

    /**
     * 是否支持退货
     * 0-否：成交后不支持退货
     * 1-是：支持7天无理由退货
     */
    private Integer isReturnable;

    /* ========================= 加价规则字段 ========================= */

    /**
     * 加价阶梯配置ID
     * 关联bid_increment_config表
     * 定义不同价格区间的最小加价幅度
     * 例如：0-1000元每次加10元，1000-5000元每次加50元
     */
    private Long bidIncrementConfigId;

    /**
     * 加价阶梯配置信息（非数据库字段）
     * 关联查询得到的完整配置对象
     * 包含所有加价规则详情
     */
    private BidIncrementConfig bidIncrementConfig;

    /* ========================= 展示信息字段 ========================= */

    /**
     * 拍卖会封面图片
     * 单张封面图的URL，用于列表页展示
     * 如果有多张图片，取images字段的第一张
     */
    private String coverImage;

    /**
     * 拍卖会规则说明
     * 详细说明本次拍卖会的特殊规则
     * 例如：出价规则、违约处理、退款政策等
     */
    private String rules;

    /**
     * 是否对用户可见
     * 0-隐藏：仅管理员可见，用户端不显示
     * 1-展示：在用户端拍卖会列表中显示
     * 用于控制拍卖会的发布状态
     */
    private Integer isVisible;

    /* ========================= 防狙击拍卖设置 ========================= */

    /**
     * 是否启用防狙击拍卖（延时拍卖）
     * 0-否：拍卖会严格按结束时间结束
     * 1-是：临近结束时有出价则自动延时
     * 防止用户在最后几秒突然出价狙击成交
     */
    private Integer antiSnipingEnabled;
    
    /**
     * 临近结束触发延时的阈值（秒）
     * 例如：60 表示结束前60秒内有出价则触发延时
     * 仅当antiSnipingEnabled=1时有效
     */
    private Integer extendThresholdSec;
    
    /**
     * 每次顺延的秒数
     * 例如：300 表示每次延时5分钟
     * 触发延时后，结束时间自动延后这么多秒
     */
    private Integer extendSeconds;
    
    /**
     * 最大顺延次数
     * 例如：5 表示最多延时5次
     * 0表示不限制延时次数
     * 防止无限延时
     */
    private Integer extendMaxTimes;

    /* ========================= 时间戳字段 ========================= */

    /**
     * 创建时间
     * 记录拍卖会首次创建的时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 记录拍卖会最后一次修改的时间
     * 每次更新拍卖会信息时自动更新
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志（软删除）
     * 0-未删除：正常数据
     * 1-已删除：已删除，查询时默认过滤
     * 使用软删除可以保留历史数据，便于追溯
     */
    private Integer deleted;

    /* ========================= 非数据库字段（用于关联查询和前端展示） ========================= */

    /**
     * 创建人信息（非数据库字段）
     * 关联查询得到的创建人详细信息
     * 用于在前端显示是哪位管理员创建的拍卖会
     */
    private SysUser creator;

    /**
     * 拍卖会拍品列表（非数据库字段）
     * 通过auction_session_item关联表查询得到的拍品列表
     * 用于在详情页展示拍卖会包含的所有拍品
     */
    private List<AuctionItem> items;

    /**
     * 拍卖会拍品ID列表（非数据库字段）
     * 从items列表提取出的ID数组
     * 用于创建和更新拍卖会时传递拍品关联关系
     */
    private List<Long> itemIds;
}