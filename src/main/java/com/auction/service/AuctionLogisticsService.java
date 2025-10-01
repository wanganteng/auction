package com.auction.service;

import com.auction.entity.AuctionLogistics;
import com.auction.mapper.AuctionLogisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流信息服务类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class AuctionLogisticsService {

    @Autowired
    private AuctionLogisticsMapper auctionLogisticsMapper;

    /**
     * 创建物流信息
     */
    @Transactional
    public Long createLogistics(AuctionLogistics logistics) {
        try {
            // 设置创建时间
            logistics.setCreateTime(LocalDateTime.now());
            logistics.setUpdateTime(LocalDateTime.now());

            // 插入物流信息
            auctionLogisticsMapper.insert(logistics);

            log.info("物流信息创建成功: 订单ID={}, 运单号={}", 
                logistics.getOrderId(), logistics.getTrackingNumber());
            return logistics.getId();

        } catch (Exception e) {
            log.error("物流信息创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("物流信息创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新物流信息
     */
    @Transactional
    public boolean updateLogistics(AuctionLogistics logistics) {
        try {
            // 设置更新时间
            logistics.setUpdateTime(LocalDateTime.now());

            // 更新物流信息
            int result = auctionLogisticsMapper.update(logistics);
            
            if (result > 0) {
                log.info("物流信息更新成功: ID={}, 运单号={}", 
                    logistics.getId(), logistics.getTrackingNumber());
                return true;
            } else {
                log.warn("物流信息更新失败: ID={}", logistics.getId());
                return false;
            }

        } catch (Exception e) {
            log.error("物流信息更新失败: {}", e.getMessage(), e);
            throw new RuntimeException("物流信息更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新物流状态
     */
    @Transactional
    public boolean updateLogisticsStatus(Long logisticsId, Integer status) {
        try {
            AuctionLogistics logistics = auctionLogisticsMapper.selectById(logisticsId);
            if (logistics == null) {
                throw new RuntimeException("物流信息不存在");
            }

            logistics.setLogisticsStatus(status);
            logistics.setUpdateTime(LocalDateTime.now());

            int result = auctionLogisticsMapper.update(logistics);
            
            if (result > 0) {
                log.info("物流状态更新成功: ID={}, 状态={}", logisticsId, status);
                return true;
            } else {
                log.warn("物流状态更新失败: ID={}", logisticsId);
                return false;
            }

        } catch (Exception e) {
            log.error("物流状态更新失败: ID={}, 错误: {}", logisticsId, e.getMessage(), e);
            throw new RuntimeException("物流状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 查询物流信息列表
     */
    public List<AuctionLogistics> getLogisticsList(AuctionLogistics logistics) {
        try {
            return auctionLogisticsMapper.selectList(logistics);
        } catch (Exception e) {
            log.error("查询物流信息列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据ID查询物流信息
     */
    public AuctionLogistics getLogisticsById(Long id) {
        try {
            return auctionLogisticsMapper.selectById(id);
        } catch (Exception e) {
            log.error("查询物流信息失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据订单ID查询物流信息
     */
    public AuctionLogistics getLogisticsByOrderId(Long orderId) {
        try {
            return auctionLogisticsMapper.selectByOrderId(orderId);
        } catch (Exception e) {
            log.error("查询物流信息失败: 订单ID={}, 错误: {}", orderId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据运单号查询物流信息
     */
    public AuctionLogistics getLogisticsByTrackingNumber(String trackingNumber) {
        try {
            return auctionLogisticsMapper.selectByTrackingNumber(trackingNumber);
        } catch (Exception e) {
            log.error("查询物流信息失败: 运单号={}, 错误: {}", trackingNumber, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除物流信息
     */
    @Transactional
    public boolean deleteLogistics(Long id) {
        try {
            int result = auctionLogisticsMapper.deleteById(id);
            
            if (result > 0) {
                log.info("物流信息删除成功: ID={}", id);
                return true;
            } else {
                log.warn("物流信息删除失败: ID={}", id);
                return false;
            }

        } catch (Exception e) {
            log.error("物流信息删除失败: ID={}, 错误: {}", id, e.getMessage(), e);
            throw new RuntimeException("物流信息删除失败: " + e.getMessage());
        }
    }
}
