package com.auction.service;

import com.auction.entity.AuctionLogistics;
import com.auction.entity.AuctionOrder;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * 物流服务接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface LogisticsService {

    /**
     * 创建物流信息
     * 
     * @param logistics 物流信息
     * @return 是否成功
     */
    boolean createLogistics(AuctionLogistics logistics);

    /**
     * 更新物流信息
     * 
     * @param logistics 物流信息
     * @return 是否成功
     */
    boolean updateLogistics(AuctionLogistics logistics);

    /**
     * 根据ID获取物流信息
     * 
     * @param logisticsId 物流ID
     * @return 物流信息
     */
    AuctionLogistics getLogisticsById(Long logisticsId);

    /**
     * 根据订单ID获取物流信息
     * 
     * @param orderId 订单ID
     * @return 物流信息
     */
    AuctionLogistics getLogisticsByOrderId(Long orderId);

    /**
     * 根据物流单号获取物流信息
     * 
     * @param trackingNumber 物流单号
     * @return 物流信息
     */
    AuctionLogistics getLogisticsByTrackingNumber(String trackingNumber);

    /**
     * 获取所有物流信息
     * 
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 物流信息列表
     */
    PageInfo<AuctionLogistics> getAllLogistics(Integer pageNum, Integer pageSize);

    /**
     * 根据状态获取物流信息
     * 
     * @param status 物流状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 物流信息列表
     */
    PageInfo<AuctionLogistics> getLogisticsByStatus(Integer status, Integer pageNum, Integer pageSize);

    /**
     * 发货
     * 
     * @param orderId 订单ID
     * @param logisticsCompany 物流公司
     * @param trackingNumber 物流单号
     * @param senderAddress 发货地址
     * @param receiverAddress 收货地址
     * @param senderName 发货人姓名
     * @param senderPhone 发货人电话
     * @param receiverName 收货人姓名
     * @param receiverPhone 收货人电话
     * @return 是否成功
     */
    boolean shipOrder(Long orderId, String logisticsCompany, String trackingNumber,
                     String senderAddress, String receiverAddress,
                     String senderName, String senderPhone,
                     String receiverName, String receiverPhone);

    /**
     * 更新物流状态
     * 
     * @param logisticsId 物流ID
     * @param status 新状态
     * @param logisticsDetail 物流详情
     * @return 是否成功
     */
    boolean updateLogisticsStatus(Long logisticsId, Integer status, String logisticsDetail);

    /**
     * 确认收货
     * 
     * @param logisticsId 物流ID
     * @return 是否成功
     */
    boolean confirmDelivery(Long logisticsId);

    /**
     * 获取物流统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getLogisticsStats();
}