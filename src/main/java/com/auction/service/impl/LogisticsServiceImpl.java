package com.auction.service.impl;

import com.auction.entity.AuctionLogistics;
import com.auction.entity.AuctionOrder;
import com.auction.mapper.AuctionLogisticsMapper;
import com.auction.service.LogisticsService;
import com.auction.service.AuctionOrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流服务实现类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class LogisticsServiceImpl implements LogisticsService {

    @Autowired
    private AuctionLogisticsMapper logisticsMapper;

    @Autowired
    private AuctionOrderService orderService;

    @Override
    public boolean createLogistics(AuctionLogistics logistics) {
        log.debug("创建物流信息: 订单ID={}", logistics.getOrderId());
        
        try {
            // 设置默认值
            if (logistics.getLogisticsStatus() == null) {
                logistics.setLogisticsStatus(0); // 待发货
            }
            if (logistics.getCreateTime() == null) {
                logistics.setCreateTime(LocalDateTime.now());
            }
            if (logistics.getUpdateTime() == null) {
                logistics.setUpdateTime(LocalDateTime.now());
            }
            
            int result = logisticsMapper.insert(logistics);
            if (result > 0) {
                log.info("物流信息创建成功: 订单ID={}", logistics.getOrderId());
                return true;
            } else {
                log.error("物流信息创建失败: 订单ID={}", logistics.getOrderId());
                return false;
            }
        } catch (Exception e) {
            log.error("创建物流信息时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateLogistics(AuctionLogistics logistics) {
        log.debug("更新物流信息: {}", logistics.getId());
        
        try {
            logistics.setUpdateTime(LocalDateTime.now());
            int result = logisticsMapper.updateById(logistics);
            if (result > 0) {
                log.info("物流信息更新成功: {}", logistics.getId());
                return true;
            } else {
                log.error("物流信息更新失败: {}", logistics.getId());
                return false;
            }
        } catch (Exception e) {
            log.error("更新物流信息时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public AuctionLogistics getLogisticsById(Long logisticsId) {
        log.debug("根据ID获取物流信息: {}", logisticsId);
        return logisticsMapper.selectById(logisticsId);
    }

    @Override
    public AuctionLogistics getLogisticsByOrderId(Long orderId) {
        log.debug("根据订单ID获取物流信息: {}", orderId);
        return logisticsMapper.selectByOrderId(orderId);
    }

    @Override
    public AuctionLogistics getLogisticsByTrackingNumber(String trackingNumber) {
        log.debug("根据物流单号获取物流信息: {}", trackingNumber);
        return logisticsMapper.selectByTrackingNumber(trackingNumber);
    }

    @Override
    public PageInfo<AuctionLogistics> getAllLogistics(Integer pageNum, Integer pageSize) {
        log.debug("获取所有物流信息: 页码={}, 大小={}", pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<AuctionLogistics> logisticsList = logisticsMapper.selectAll();
        return new PageInfo<>(logisticsList);
    }

    @Override
    public PageInfo<AuctionLogistics> getLogisticsByStatus(Integer status, Integer pageNum, Integer pageSize) {
        log.debug("根据状态获取物流信息: 状态={}, 页码={}, 大小={}", status, pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<AuctionLogistics> logisticsList = logisticsMapper.selectByStatus(status);
        return new PageInfo<>(logisticsList);
    }

    @Override
    public boolean shipOrder(Long orderId, String logisticsCompany, String trackingNumber,
                            String senderAddress, String receiverAddress,
                            String senderName, String senderPhone,
                            String receiverName, String receiverPhone) {
        log.debug("订单发货: 订单ID={}, 物流公司={}", orderId, logisticsCompany);
        
        try {
            // 检查订单是否存在
            AuctionOrder order = orderService.getOrderById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            // 检查订单状态
            if (!order.getStatus().equals(1)) {
                log.error("订单状态不是已支付: {}", orderId);
                return false;
            }
            
            // 创建物流信息
            AuctionLogistics logistics = new AuctionLogistics();
            logistics.setOrderId(orderId);
            logistics.setLogisticsCompany(logisticsCompany);
            logistics.setTrackingNumber(trackingNumber);
            logistics.setLogisticsStatus(1); // 已发货
            logistics.setSenderAddress(senderAddress);
            logistics.setReceiverAddress(receiverAddress);
            logistics.setSenderName(senderName);
            logistics.setSenderPhone(senderPhone);
            logistics.setReceiverName(receiverName);
            logistics.setReceiverPhone(receiverPhone);
            logistics.setShipTime(LocalDateTime.now());
            logistics.setCreateTime(LocalDateTime.now());
            logistics.setUpdateTime(LocalDateTime.now());
            
            boolean result = createLogistics(logistics);
            if (result) {
                // 更新订单状态为已发货
                orderService.shipOrder(orderId);
                log.info("订单发货成功: 订单ID={}", orderId);
                return true;
            } else {
                log.error("订单发货失败: 订单ID={}", orderId);
                return false;
            }
        } catch (Exception e) {
            log.error("订单发货时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateLogisticsStatus(Long logisticsId, Integer status, String logisticsDetail) {
        log.debug("更新物流状态: 物流ID={}, 状态={}", logisticsId, status);
        
        try {
            AuctionLogistics logistics = logisticsMapper.selectById(logisticsId);
            if (logistics == null) {
                log.error("物流信息不存在: {}", logisticsId);
                return false;
            }
            
            logistics.setLogisticsStatus(status);
            if (logisticsDetail != null && !logisticsDetail.isEmpty()) {
                logistics.setLogisticsDetail(logisticsDetail);
            }
            logistics.setUpdateTime(LocalDateTime.now());
            
            int result = logisticsMapper.updateById(logistics);
            if (result > 0) {
                log.info("物流状态更新成功: 物流ID={}, 状态={}", logisticsId, status);
                return true;
            } else {
                log.error("物流状态更新失败: 物流ID={}", logisticsId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新物流状态时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean confirmDelivery(Long logisticsId) {
        log.debug("确认收货: 物流ID={}", logisticsId);
        
        try {
            AuctionLogistics logistics = logisticsMapper.selectById(logisticsId);
            if (logistics == null) {
                log.error("物流信息不存在: {}", logisticsId);
                return false;
            }
            
            logistics.setLogisticsStatus(3); // 已签收
            logistics.setReceiveTime(LocalDateTime.now());
            logistics.setUpdateTime(LocalDateTime.now());
            
            int result = logisticsMapper.updateById(logistics);
            if (result > 0) {
                // 更新订单状态为已完成
                orderService.confirmReceive(logistics.getOrderId());
                log.info("确认收货成功: 物流ID={}", logisticsId);
                return true;
            } else {
                log.error("确认收货失败: 物流ID={}", logisticsId);
                return false;
            }
        } catch (Exception e) {
            log.error("确认收货时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getLogisticsStats() {
        log.debug("获取物流统计信息");
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<AuctionLogistics> allLogistics = logisticsMapper.selectAll();
            
            long totalLogistics = allLogistics.size();
            long pendingLogistics = allLogistics.stream().filter(l -> l.getLogisticsStatus() == 0).count();
            long shippedLogistics = allLogistics.stream().filter(l -> l.getLogisticsStatus() == 1).count();
            long inTransitLogistics = allLogistics.stream().filter(l -> l.getLogisticsStatus() == 2).count();
            long deliveredLogistics = allLogistics.stream().filter(l -> l.getLogisticsStatus() == 3).count();
            long exceptionLogistics = allLogistics.stream().filter(l -> l.getLogisticsStatus() == 4).count();
            
            stats.put("totalLogistics", totalLogistics);
            stats.put("pendingLogistics", pendingLogistics);
            stats.put("shippedLogistics", shippedLogistics);
            stats.put("inTransitLogistics", inTransitLogistics);
            stats.put("deliveredLogistics", deliveredLogistics);
            stats.put("exceptionLogistics", exceptionLogistics);
            
            return stats;
        } catch (Exception e) {
            log.error("获取物流统计信息时发生错误: {}", e.getMessage());
            return stats;
        }
    }
}