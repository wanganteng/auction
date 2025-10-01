package com.auction.mapper;

import com.auction.entity.AuctionBid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拍卖出价Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface AuctionBidMapper {

    /**
     * 插入出价记录
     */
    int insert(AuctionBid bid);

    /**
     * 更新出价记录
     */
    int update(AuctionBid bid);

    /**
     * 根据ID查询出价记录
     */
    AuctionBid selectById(Long id);

    /**
     * 查询出价记录列表
     */
    List<AuctionBid> selectList(AuctionBid bid);

    /**
     * 查询拍品最高出价
     */
    AuctionBid selectHighestBid(@Param("itemId") Long itemId);

    /**
     * 根据拍卖会ID查询出价记录
     */
    List<AuctionBid> selectByAuctionId(@Param("auctionId") Long auctionId);

    /**
     * 根据拍卖会ID统计出价数量
     */
    int countByAuctionId(@Param("auctionId") Long auctionId);
}