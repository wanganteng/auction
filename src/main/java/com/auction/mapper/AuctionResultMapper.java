package com.auction.mapper;

import com.auction.entity.AuctionResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AuctionResultMapper {
    int insert(AuctionResult result);
    int update(AuctionResult result);
    AuctionResult selectBySessionAndItem(Long sessionId, Long itemId);
    List<AuctionResult> selectBySessionId(Long sessionId);
}


