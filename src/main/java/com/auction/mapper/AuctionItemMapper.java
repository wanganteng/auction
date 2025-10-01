package com.auction.mapper;

import com.auction.entity.AuctionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拍品Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface AuctionItemMapper {

    /**
     * 插入拍品
     */
    int insert(AuctionItem item);

    /**
     * 更新拍品
     */
    int update(AuctionItem item);

    /**
     * 根据ID查询拍品
     */
    AuctionItem selectById(Long id);

    /**
     * 查询拍品列表
     */
    List<AuctionItem> selectList(AuctionItem item);

    /**
     * 根据ID删除拍品
     */
    int deleteById(Long id);

    /**
     * 根据拍卖会ID更新拍品状态
     */
    int updateBySessionId(@Param("sessionId") Long sessionId, @Param("item") AuctionItem item);

    /**
     * 根据状态查询拍品
     */
    List<AuctionItem> selectByStatus(@Param("status") Integer status);

    /**
     * 查询所有拍品
     */
    List<AuctionItem> selectAll();

    /**
     * 根据用户ID查询拍品
     */
    List<AuctionItem> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据ID更新拍品
     */
    int updateById(AuctionItem item);

    /**
     * 根据拍卖会ID查询拍品
     */
    List<AuctionItem> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询可分配到拍卖会的拍品：仅返回上架，且未被未开始/进行中的拍卖会占用的拍品
     */
    List<AuctionItem> selectAvailableForAssignment();
}