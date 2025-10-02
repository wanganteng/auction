package com.auction.mapper;

import com.auction.entity.AuctionSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拍卖会Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface AuctionSessionMapper {

    /**
     * 插入拍卖会
     */
    int insert(AuctionSession session);

    /**
     * 更新拍卖会
     */
    int update(AuctionSession session);

    /**
     * 根据ID查询拍卖会
     */
    AuctionSession selectById(Long id);

    /**
     * 根据ID查询拍卖会（包含加价阶梯配置）
     */
    AuctionSession selectByIdWithBidIncrement(Long id);

    /**
     * 查询拍卖会列表
     */
    List<AuctionSession> selectList(AuctionSession session);

    /**
     * 根据ID删除拍卖会
     */
    int deleteById(Long id);

    /**
     * 查询所有拍卖会
     */
    List<AuctionSession> selectAll();

    /**
     * 根据ID更新拍卖会
     */
    int updateById(AuctionSession session);

    /**
     * 根据拍品ID查询关联的拍卖会
     */
    List<AuctionSession> selectSessionsByItemId(Long itemId);

    /**
     * 根据加价阶梯配置ID查询使用该配置的拍卖会列表
     */
    List<AuctionSession> selectSessionsByBidIncrementConfigId(@Param("configId") Long configId);
}