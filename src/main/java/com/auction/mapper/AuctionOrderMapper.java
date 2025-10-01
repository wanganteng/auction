package com.auction.mapper;

import com.auction.entity.AuctionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拍卖订单Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface AuctionOrderMapper {

    /**
     * 插入订单
     */
    int insert(AuctionOrder order);

    /**
     * 更新订单
     */
    int update(AuctionOrder order);

    /**
     * 根据ID查询订单
     */
    AuctionOrder selectById(Long id);

    /**
     * 根据订单号查询订单
     */
    AuctionOrder selectByOrderNo(String orderNo);

    /**
     * 查询订单列表
     */
    List<AuctionOrder> selectList(AuctionOrder order);

    /**
     * 根据ID删除订单
     */
    int deleteById(Long id);

    /**
     * 根据拍卖会ID查询订单
     */
    List<AuctionOrder> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据拍品ID查询订单
     */
    List<AuctionOrder> selectByItemId(@Param("itemId") Long itemId);

    /**
     * 查询所有订单
     */
    List<AuctionOrder> selectAll();

    /**
     * 根据状态查询订单
     */
    List<AuctionOrder> selectByStatus(@Param("status") Integer status);
}