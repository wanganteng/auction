package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户地址实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class UserAddress {

    /**
     * 地址ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 完整地址（省市区+详细地址）
     */
    private String fullAddress;

    /**
     * 是否默认地址：0-否，1-是
     */
    private Integer isDefault;

    /**
     * 地址标签：家、公司、学校等
     */
    private String tag;

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
}

