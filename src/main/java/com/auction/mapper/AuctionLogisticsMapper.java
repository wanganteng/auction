package com.auction.mapper;

import com.auction.entity.AuctionLogistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物流信息Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface AuctionLogisticsMapper {

    /**
     * 插入物流信息
     */
    int insert(AuctionLogistics logistics);

    /**
     * 更新物流信息
     */
    int update(AuctionLogistics logistics);

    /**
     * 根据ID查询物流信息
     */
    AuctionLogistics selectById(Long id);

    /**
     * 根据订单ID查询物流信息
     */
    AuctionLogistics selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据运单号查询物流信息
     */
    AuctionLogistics selectByTrackingNumber(@Param("trackingNumber") String trackingNumber);

    /**
     * 查询物流信息列表
     */
    List<AuctionLogistics> selectList(AuctionLogistics logistics);

    /**
     * 根据ID删除物流信息
     */
    int deleteById(Long id);

    /**
     * 查询所有物流信息
     */
    List<AuctionLogistics> selectAll();

    /**
     * 根据状态查询物流信息
     */
    List<AuctionLogistics> selectByStatus(@Param("status") Integer status);

    /**
     * 根据ID更新物流信息
     */
    int updateById(AuctionLogistics logistics);
}