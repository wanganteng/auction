package com.auction.service;

import com.auction.entity.AuctionResult;

import java.util.List;

public interface AuctionResultService {
    Long saveResult(AuctionResult result);
    int updateResult(AuctionResult result);
    AuctionResult getBySessionAndItem(Long sessionId, Long itemId);
    List<AuctionResult> getBySessionId(Long sessionId);
}


