package com.auction.mapper;

import com.auction.entity.AuctionSession;
import org.apache.ibatis.annotations.Mapper;

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
}