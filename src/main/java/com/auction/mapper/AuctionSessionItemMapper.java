package com.auction.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuctionSessionItemMapper {

    int deleteBySessionId(@Param("sessionId") Long sessionId);

    int batchInsert(@Param("sessionId") Long sessionId,
                    @Param("itemIds") List<Long> itemIds);

    List<Long> findConflictingItemIds(@Param("itemIds") List<Long> itemIds);
}


