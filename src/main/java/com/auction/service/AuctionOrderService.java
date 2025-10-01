package com.auction.service;

import com.auction.entity.AuctionOrder;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * 拍卖订单服务接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface AuctionOrderService {

    /**
     * 创建订单
     */
    Long createOrder(AuctionOrder order);

    /**
     * 支付订单
     */
    boolean payOrder(Long orderId);

    /**
     * 支付订单（带物流费）
     */
    boolean payOrder(Long orderId, java.math.BigDecimal shippingFee);

    /**
     * 更新订单
     */
    boolean updateOrder(AuctionOrder order);

    /**
     * 发货
     */
    boolean shipOrder(Long orderId);

    /**
     * 确认收货
     */
    boolean confirmReceive(Long orderId);

    /**
     * 完成订单
     */
    boolean completeOrder(Long orderId);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId);

    /**
     * 查询订单列表
     */
    List<AuctionOrder> getOrderList(AuctionOrder order);

    /**
     * 根据ID查询订单
     */
    AuctionOrder getOrderById(Long id);

    /**
     * 根据订单号查询订单
     */
    AuctionOrder getOrderByOrderNo(String orderNo);

    /**
     * 获取用户订单列表（分页）
     */
    PageInfo<AuctionOrder> getUserOrders(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取所有订单列表（分页）
     */
    PageInfo<AuctionOrder> getAllOrders(Integer pageNum, Integer pageSize);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long orderId, Integer status);

    /**
     * 获取订单统计信息
     */
    Map<String, Object> getOrderStats(Long userId);
}