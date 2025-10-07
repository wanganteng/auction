package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ========================================
 * 拍品实体类（AuctionItem）
 * ========================================
 * 功能说明：
 * 1. 表示拍卖系统中的拍品信息
 * 2. 包含拍品的基本信息（名称、描述、分类等）
 * 3. 包含价格信息（起拍价、保留价、当前价、估价）
 * 4. 包含拍卖规则（保证金比例、佣金比例等）
 * 5. 包含服务信息（是否保真、是否包邮、是否支持退货）
 * 6. 包含拍品详细属性（重量、尺寸、材质、年代等）
 * 
 * 数据库表：auction_item
 * 
 * 字段说明：
 * - 价格字段统一使用BigDecimal类型，确保金额精度
 * - 时间字段使用LocalDateTime类型，支持Java 8时间API
 * - 图片字段存储JSON格式的URL列表
 * - 部分字段为非数据库字段，用于前端展示
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter、toString等方法
public class AuctionItem {

    /* ========================= 基本信息字段 ========================= */
    
    /**
     * 拍品ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /**
     * 拍品名称
     * 例如："清代青花瓷花瓶"、"齐白石山水画"
     * 必填字段
     */
    private String itemName;

    /**
     * 拍品描述
     * 详细描述拍品的特征、历史、价值等信息
     * 支持长文本
     */
    private String description;

    /**
     * 拍品分类ID
     * 关联分类表，表示拍品所属类别
     * 例如：1-书画、2-陶瓷、3-玉器、4-珠宝、5-家具、6-杂项
     */
    private Long categoryId;

    /* ========================= 价格信息字段 ========================= */
    
    /**
     * 起拍价（元）
     * 拍卖的初始价格，竞拍从此价格开始
     * 使用BigDecimal确保金额精度
     * 必填字段
     */
    private BigDecimal startingPrice;

    /**
     * 保留价/底价（元）
     * 拍品的最低成交价格，低于此价格视为流拍
     * 可选字段，如果不设置则无底价限制
     */
    private BigDecimal reservePrice;

    /**
     * 当前最高价（元）
     * 实时更新的当前最高出价
     * 初始值等于起拍价
     * 每次有人出价后更新此字段
     */
    private BigDecimal currentPrice;

    /**
     * 拍品估价（元）
     * 专业评估的拍品价值
     * 用于参考，不影响实际竞拍
     */
    private BigDecimal estimatedPrice;

    /* ========================= 拍卖规则字段 ========================= */
    
    /**
     * 保证金比例（0-1之间的小数）
     * 例如：0.1 表示 10%
     * 用户需要缴纳拍品起拍价 × 保证金比例的金额才能参与竞拍
     */
    private BigDecimal depositRatio;

    /**
     * 佣金比例（0-1之间的小数）
     * 例如：0.05 表示 5%
     * 成交后买家需额外支付成交价 × 佣金比例的佣金
     */
    private BigDecimal commissionRatio;

    /* ========================= 服务保障字段 ========================= */
    
    /**
     * 是否保真
     * 0-否：不保证真品
     * 1-是：承诺为真品，假一赔十
     */
    private Integer isAuthentic;

    /**
     * 是否包邮
     * 0-否：需要买家承担运费
     * 1-是：卖家承担运费
     */
    private Integer isFreeShipping;

    /**
     * 运费（元）
     * 仅当is_free_shipping=0时有效
     * 如果包邮，此字段无效
     */
    private BigDecimal shippingFee;

    /**
     * 是否支持退货
     * 0-否：不支持退货
     * 1-是：支持7天无理由退货
     */
    private Integer isReturnable;

    /* ========================= 状态字段 ========================= */
    
    /**
     * 拍品状态
     * 0-下架：不在前台显示，不可参与拍卖
     * 1-上架：可以加入拍卖会进行拍卖
     */
    private Integer status;

    // 审核相关字段已移除（系统简化，无需审核流程）

    /* ========================= 关联信息字段 ========================= */
    
    /**
     * 上传人ID（超级管理员）
     * 关联sys_user表，记录是哪位管理员上传的拍品
     */
    private Long uploaderId;

    /* ========================= 图片信息字段 ========================= */
    
    /**
     * 拍品图片列表（JSON格式存储）
     * 存储格式：["url1", "url2", "url3"]
     * 用于在列表页和详情页展示拍品图片
     */
    private String images;

    /**
     * 拍品详情图片列表（JSON格式存储）
     * 存储更多角度、更高清晰度的图片
     * 用于详情页的详细展示
     */
    private String detailImages;

    /* ========================= 拍品属性字段 ========================= */
    
    /**
     * 拍品重量（克）
     * 用于计算运费和物流信息
     * 例如：500 表示500克
     */
    private BigDecimal weight;

    /**
     * 拍品尺寸（长x宽x高，单位：厘米）
     * 格式示例："30x20x10"
     * 用于描述拍品的物理尺寸
     */
    private String dimensions;

    /**
     * 拍品材质
     * 例如："景德镇陶瓷"、"和田玉"、"黄花梨木"
     * 帮助买家了解拍品材料
     */
    private String material;

    /**
     * 拍品年代
     * 例如："清代"、"民国"、"现代"
     * 对于古董类拍品很重要
     */
    private String era;

    /**
     * 拍品来源
     * 例如："私人收藏"、"博物馆"、"拍卖行"
     * 说明拍品的出处和流转历史
     */
    private String source;

    /**
     * 拍品证书
     * 例如："国家文物鉴定证书"、"珠宝鉴定证书"
     * 证明拍品真实性和价值的文件
     */
    private String certificate;

    /**
     * 拍品编号
     * 系统内部的拍品唯一编号
     * 例如："A2024001"
     */
    private String itemCode;

    /* ========================= 时间戳字段 ========================= */
    
    /**
     * 创建时间
     * 记录拍品首次创建的时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 记录拍品最后一次修改的时间
     * 每次更新拍品信息时自动更新
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /* ========================= 非数据库字段（用于前端展示） ========================= */

    /**
     * 上传人信息（非数据库字段）
     * 关联查询得到的上传人详细信息
     * 用于在前端显示是哪位管理员上传的拍品
     */
    private SysUser uploader;

    // 审核人信息已移除（系统简化，无审核流程）

    /**
     * 拍卖状态（非数据库字段）
     * 动态计算的拍卖状态，用于前端显示
     * 0-未拍卖/未开始：拍品未加入拍卖会或拍卖会未开始
     * 1-拍卖中：拍品正在拍卖
     * 2-已成交：拍卖结束且有人中标
     * 3-流拍：拍卖结束但无人出价或未达保留价
     */
    private Integer auctionStatus;

    /**
     * 拍品图片URL列表（非数据库字段）
     * 从images字段解析出来的URL数组
     * 用于前端直接使用，无需再次解析JSON
     */
    private List<String> imageList;

}