package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ========================================
 * 用户收货地址实体类（UserAddress）
 * ========================================
 * 功能说明：
 * 1. 存储用户的收货地址信息
 * 2. 支持多个地址，可设置默认地址
 * 3. 用于订单发货时选择收货地址
 * 4. 支持地址标签（家、公司、学校等）
 * 
 * 数据库表：user_address
 * 
 * 应用场景：
 * - 用户在个人中心管理收货地址
 * - 下单时选择收货地址
 * - 支持省市区三级联动选择
 * 
 * 地址结构：
 * - 省市区：三级行政区划
 * - 详细地址：街道、门牌号等
 * - 完整地址：省市区 + 详细地址（自动拼接）
 * 
 * 默认地址：
 * - 每个用户只能有一个默认地址
 * - 设置新默认地址时，自动取消其他地址的默认状态
 * - 下单时默认选中默认地址
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter等方法
public class UserAddress {

    /* ========================= 基本信息字段 ========================= */

    /**
     * 地址ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /**
     * 用户ID
     * 关联sys_user表
     * 表示该地址属于哪个用户
     * 一个用户可以有多个地址
     */
    private Long userId;

    /* ========================= 收货人信息字段 ========================= */

    /**
     * 收货人姓名
     * 收货人的真实姓名
     * 用于快递单和签收
     */
    private String receiverName;

    /**
     * 收货人电话
     * 11位中国大陆手机号
     * 用于快递员联系收货人
     * 必填字段
     */
    private String receiverPhone;

    /* ========================= 地址信息字段 ========================= */

    /**
     * 省份
     * 一级行政区划
     * 例如："北京市"、"广东省"
     */
    private String province;

    /**
     * 城市
     * 二级行政区划
     * 例如："北京市"、"深圳市"
     */
    private String city;

    /**
     * 区县
     * 三级行政区划
     * 例如："朝阳区"、"南山区"
     */
    private String district;

    /**
     * 详细地址
     * 街道、门牌号等具体信息
     * 例如："建国路88号XX大厦10层1001室"
     */
    private String detailAddress;

    /**
     * 完整地址
     * 省市区 + 详细地址的拼接
     * 例如："北京市朝阳区建国路88号XX大厦10层1001室"
     * 可以自动生成，也可以手动填写
     */
    private String fullAddress;

    /* ========================= 地址属性字段 ========================= */

    /**
     * 是否默认地址
     * 0-否：普通地址
     * 1-是：默认地址（每个用户只能有一个）
     * 
     * 默认地址的作用：
     * - 下单时自动选中
     * - 列表中置顶显示
     * - 有特殊标记（蓝色边框等）
     */
    private Integer isDefault;

    /**
     * 地址标签
     * 快速识别地址用途
     * 常用标签："家"、"公司"、"学校"
     * 可选字段，由用户自定义
     */
    private String tag;

    /* ========================= 时间戳字段 ========================= */

    /**
     * 创建时间
     * 地址添加的时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 地址最后修改的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志（软删除）
     * 0-未删除：正常地址
     * 1-已删除：已删除的地址
     */
    private Integer deleted;
}

