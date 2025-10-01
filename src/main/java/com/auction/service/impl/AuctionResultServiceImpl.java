package com.auction.service.impl;

import com.auction.entity.AuctionResult;
import com.auction.mapper.AuctionResultMapper;
import com.auction.service.AuctionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionResultServiceImpl implements AuctionResultService {

    @Autowired
    private AuctionResultMapper auctionResultMapper;

    @Override
    public Long saveResult(AuctionResult result) {
        auctionResultMapper.insert(result);
        return result.getId();
    }

    @Override
    public int updateResult(AuctionResult result) {
        return auctionResultMapper.update(result);
    }

    @Override
    public AuctionResult getBySessionAndItem(Long sessionId, Long itemId) {
        return auctionResultMapper.selectBySessionAndItem(sessionId, itemId);
    }

    @Override
    public List<AuctionResult> getBySessionId(Long sessionId) {
        return auctionResultMapper.selectBySessionId(sessionId);
    }
}


