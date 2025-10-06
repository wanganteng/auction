package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拍品实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class AuctionItem {

    /**
     * 拍品ID
     */
    private Long id;

    /**
     * 拍品名称
     */
    private String itemName;

    /**
     * 拍品描述
     */
    private String description;

    /**
     * 拍品分类ID
     */
    private Long categoryId;

    /**
     * 起拍价
     */
    private BigDecimal startingPrice;

    /**
     * 保留价（底价）
     */
    private BigDecimal reservePrice;

    /**
     * 当前最高价
     */
    private BigDecimal currentPrice;

    /**
     * 拍品估价（元）
     */
    private BigDecimal estimatedPrice;

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
     * 运费（元），is_free_shipping=1时此字段无效
     */
    private BigDecimal shippingFee;

    /**
     * 是否支持退货：0-否，1-是
     */
    private Integer isReturnable;

    /**
     * 拍品状态：0-下架，1-上架
     */
    private Integer status;

    // 审核相关字段已移除

    /**
     * 上传人ID（超级管理员）
     */
    private Long uploaderId;

    /**
     * 拍品图片列表（JSON格式存储）
     */
    private String images;

    /**
     * 拍品详情图片列表（JSON格式存储）
     */
    private String detailImages;

    /**
     * 拍品重量（克）
     */
    private BigDecimal weight;

    /**
     * 拍品尺寸（长x宽x高，单位：厘米）
     */
    private String dimensions;

    /**
     * 拍品材质
     */
    private String material;

    /**
     * 拍品年代
     */
    private String era;

    /**
     * 拍品来源
     */
    private String source;

    /**
     * 拍品证书
     */
    private String certificate;

    /**
     * 拍品编号
     */
    private String itemCode;

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
     * 上传人信息（非数据库字段）
     */
    private SysUser uploader;

    // 审核人信息已移除

    /**
     * 拍卖态（非库字段）：0-未拍卖/未开始，1-拍卖中，2-已成交，3-流拍
     */
    private Integer auctionStatus;

    /**
     * 拍品图片列表（非数据库字段）
     */
    private List<String> imageList;

    /**
     * 拍品详情图片列表（非数据库字段）
     */
    private List<String> detailImageList;
}