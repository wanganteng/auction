package com.auction.service;

import com.auction.entity.AuctionBid;
import com.auction.entity.AuctionItem;
import com.auction.entity.AuctionSession;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * 拍卖服务接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface AuctionService {

    /**
     * 创建拍卖商品
     * 
     * @param item 商品信息
     * @return 是否成功
     */
    boolean createAuctionItem(AuctionItem item);

    /**
     * 审核拍卖商品
     * 
     * @param itemId 商品ID
     * @param status 审核状态
     * @param reason 审核原因
     * @return 是否成功
     */
    boolean reviewAuctionItem(Long itemId, Integer status, String reason);

    /**
     * 开始拍卖
     * 
     * @param itemId 商品ID
     * @return 是否成功
     */
    boolean startAuction(Long itemId);

    /**
     * 结束拍卖
     * 
     * @param itemId 商品ID
     * @return 是否成功
     */
    boolean endAuction(Long itemId);


    /**
     * 获取拍卖商品详情
     * 
     * @param itemId 商品ID
     * @return 商品详情
     */
    AuctionItem getAuctionItemDetail(Long itemId);

    /**
     * 获取出价历史
     * 
     * @param itemId 商品ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 出价历史
     */
    PageInfo<AuctionBid> getBidHistory(Long itemId, Integer pageNum, Integer pageSize);

    /**
     * 获取当前最高出价
     * 
     * @param itemId 商品ID
     * @return 最高出价
     */
    AuctionBid getCurrentHighestBid(Long itemId);

    /**
     * 获取拍卖统计信息
     * 
     * @param itemId 商品ID
     * @return 统计信息
     */
    Map<String, Object> getAuctionStats(Long itemId);

    /**
     * 创建拍卖场次
     * 
     * @param session 场次信息
     * @return 是否成功
     */
    boolean createAuctionSession(AuctionSession session);

    /**
     * 开始拍卖场次
     * 
     * @param sessionId 场次ID
     * @return 是否成功
     */
    boolean startAuctionSession(Long sessionId);

    /**
     * 结束拍卖场次
     * 
     * @param sessionId 场次ID
     * @return 是否成功
     */
    boolean endAuctionSession(Long sessionId);

    /**
     * 获取拍卖场次列表
     * 
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 场次列表
     */
    PageInfo<AuctionSession> getAuctionSessions(Integer pageNum, Integer pageSize);

    /**
     * 获取拍卖场次详情
     * 
     * @param sessionId 场次ID
     * @return 场次详情
     */
    AuctionSession getAuctionSessionDetail(Long sessionId);

    /**
     * 检查是否可以出价
     * 
     * @param itemId 商品ID
     * @param userId 用户ID
     * @return 是否可以出价
     */
    boolean canBid(Long itemId, Long userId);

    /**
     * 验证出价是否有效
     * 
     * @param itemId 商品ID
     * @param bidAmount 出价金额
     * @return 是否有效
     */
    boolean isValidBid(Long itemId, Long bidAmount);

    /**
     * 获取拍卖商品列表
     * 
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 商品列表
     */
    PageInfo<AuctionItem> getAuctionItems(Integer pageNum, Integer pageSize);

    /**
     * 根据状态获取拍卖商品列表
     * 
     * @param status 状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 商品列表
     */
    PageInfo<AuctionItem> getAuctionItemsByStatus(Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据用户ID获取拍卖商品列表
     * 
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 商品列表
     */
    PageInfo<AuctionItem> getAuctionItemsByUserId(Long userId, Integer pageNum, Integer pageSize);
}