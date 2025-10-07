package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ========================================
 * 拍卖订单实体类（AuctionOrder）
 * ========================================
 * 功能说明：
 * 1. 表示拍卖成交后生成的订单信息
 * 2. 包含订单的基本信息（订单号、金额、状态）
 * 3. 包含买卖双方信息
 * 4. 包含配送信息（物流/自提）
 * 5. 记录订单的完整生命周期
 * 
 * 数据库表：auction_order
 * 
 * 订单生成时机：
 * - 拍卖会结束后，系统自动为成交的拍品生成订单
 * - 买家需要支付尾款（成交价 - 已冻结保证金 + 佣金 + 运费）
 * 
 * 订单状态流转：
 * 待付款(1) -> 已付款(2) -> 已发货(3) -> 已收货(4) -> 已完成(5)
 *          \-> 已取消(6)
 * 
 * 金额计算：
 * - 订单总金额 = 成交价 + 佣金 + 运费
 * - 保证金金额 = 已冻结的保证金（可抵扣部分订单金额）
 * - 尾款金额 = 订单总金额 - 保证金金额
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter等方法
public class AuctionOrder {

    /* ========================= 基本信息字段 ========================= */

    /**
     * 订单ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /**
     * 订单号
     * 系统生成的唯一订单编号
     * 格式：ORD + 时间戳 + 随机数
     * 例如：ORD202401011234567890
     * 用于对外展示和查询
     */
    private String orderNo;

    /* ========================= 关联信息字段 ========================= */

    /**
     * 拍卖会ID
     * 关联auction_session表
     * 表示该订单来自哪个拍卖会
     */
    private Long sessionId;

    /**
     * 拍品ID
     * 关联auction_item表
     * 表示订单对应的拍品
     */
    private Long itemId;

    /**
     * 买家ID
     * 关联sys_user表
     * 拍卖成交的买家（最高出价者）
     */
    private Long buyerId;

    /**
     * 卖家ID（超级管理员）
     * 关联sys_user表
     * 拍品的所有者（通常是平台管理员）
     */
    private Long sellerId;

    /* ========================= 金额信息字段 ========================= */

    /**
     * 订单总金额（元）
     * 计算公式：成交价 + 佣金 + 运费
     * 这是买家需要支付的总金额
     */
    private BigDecimal totalAmount;

    /**
     * 保证金金额（元）
     * 用户参与竞拍时冻结的保证金
     * 可以抵扣部分订单金额
     */
    private BigDecimal depositAmount;

    /**
     * 尾款金额（元）
     * 计算公式：订单总金额 - 保证金金额
     * 这是买家需要额外支付的金额
     */
    private BigDecimal balanceAmount;

    /* ========================= 配送信息字段 ========================= */

    /**
     * 配送方式
     * 1-物流配送：卖家通过快递发货，买家收货
     * 2-线下自提：买家到指定地点自行提货
     */
    private Integer deliveryMethod;

    /**
     * 物流费用（元）
     * 仅当deliveryMethod=1时有效
     * 根据物流公司和订单金额计算
     */
    private BigDecimal shippingFee;

    /**
     * 收货人姓名
     * 物流配送时必填
     */
    private String receiverName;

    /**
     * 收货人电话
     * 物流配送时必填
     * 格式：11位手机号
     */
    private String receiverPhone;

    /**
     * 收货地址
     * 物流配送时必填
     * 完整的收货地址（省市区街道门牌号）
     */
    private String receiverAddress;

    /**
     * 自提地址
     * 线下自提时有效
     * 卖家提供的自提点地址
     * 例如："北京市朝阳区XX路XX号XX大厦10层"
     */
    private String pickupAddress;

    /* ========================= 状态和时间字段 ========================= */

    /**
     * 订单状态
     * 1-待付款：订单已生成，等待买家支付尾款
     * 2-已付款：买家已支付，等待卖家发货
     * 3-已发货：卖家已发货，等待买家确认收货
     * 4-已收货：买家已确认收货
     * 5-已完成：订单完成，保证金已解冻/扣除
     * 6-已取消：订单被取消（买家违约或其他原因）
     */
    private Integer status;

    /**
     * 付款时间
     * 记录买家支付尾款的时间
     * 状态变为"已付款"时设置
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 发货时间
     * 记录卖家发货的时间
     * 状态变为"已发货"时设置
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shipTime;

    /**
     * 收货时间
     * 记录买家确认收货的时间
     * 状态变为"已收货"时设置
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    /* ========================= 时间戳字段 ========================= */

    /**
     * 创建时间
     * 订单生成的时间（通常是拍卖会结束后）
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 订单最后一次修改的时间
     * 每次状态变更都会更新此字段
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志（软删除）
     * 0-未删除：正常订单
     * 1-已删除：已删除的订单
     */
    private Integer deleted;

    /* ========================= 物流信息字段 ========================= */

    /**
     * 物流公司ID
     * 关联logistics_company表
     * 买家选择的物流公司
     * 仅当deliveryMethod=1时有效
     */
    private Long logisticsCompanyId;

    /* ========================= 非数据库字段（用于关联查询） ========================= */

    /**
     * 买家信息（非数据库字段）
     * 关联查询得到的买家详细信息
     * 用于在订单详情页显示买家姓名、联系方式等
     */
    private SysUser buyer;

    /**
     * 卖家信息（非数据库字段）
     * 关联查询得到的卖家详细信息
     * 用于在订单详情页显示卖家信息
     */
    private SysUser seller;

    /**
     * 拍品信息（非数据库字段）
     * 关联查询得到的拍品详细信息
     * 包括拍品名称、图片、描述等
     * 用于在订单列表和详情页显示拍品信息
     */
    private AuctionItem item;

    /**
     * 拍卖会信息（非数据库字段）
     * 关联查询得到的拍卖会详细信息
     * 包括拍卖会名称、规则等
     * 用于在订单详情页显示拍卖会信息
     */
    private AuctionSession session;

    /**
     * 物流信息（非数据库字段）
     * 关联查询得到的物流跟踪信息
     * 包括物流单号、当前状态、物流轨迹等
     * 用于在订单详情页显示物流进度
     */
    private AuctionLogistics logistics;
}