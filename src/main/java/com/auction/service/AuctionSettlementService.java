package com.auction.service;

import com.auction.entity.*;
import com.auction.mapper.AuctionItemMapper;
import com.auction.mapper.AuctionSessionMapper;
import com.auction.service.impl.AuctionOrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 拍卖会结算服务
 * - 结束会场后，计算每个拍品的成交结果
 * - 为中标者创建订单并计算应付尾款（成交价 + 佣金 - 已冻结保证金）
 * - 为未中标的竞拍者解冻保证金
 * - 记录结果到 auction_result
 */
@Slf4j
@Service
public class AuctionSettlementService {

    @Autowired
    private AuctionItemMapper auctionItemMapper;

    @Autowired
    private AuctionBidService auctionBidService;

    @Autowired
    private AuctionResultService auctionResultService;

    @Autowired
    private AuctionOrderService auctionOrderService;

    @Autowired
    private AuctionSessionMapper auctionSessionMapper;

    @Autowired
    private UserDepositAccountService userDepositAccountService;

    @Autowired
    private UserNotificationService userNotificationService;

    @Autowired
    private com.auction.websocket.AuctionWebSocketHandler webSocketHandler;

    @Autowired
    private com.auction.service.SysUserService sysUserService;

    /**
     * 结算指定拍卖会
     */
    @Transactional
    public void settleSession(Long sessionId) {
        AuctionSession session = auctionSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("拍卖会不存在");
        }
        // 仅对已结束(或到时)的会场进行结算
        if (session.getEndTime() == null || session.getEndTime().isAfter(LocalDateTime.now())) {
            log.warn("拍卖会尚未结束，跳过结算: {}", sessionId);
            return;
        }

        BigDecimal commissionRatio = session.getCommissionRatio() != null ? session.getCommissionRatio() : BigDecimal.ZERO;
        BigDecimal depositRatio = session.getDepositRatio() != null ? session.getDepositRatio() : new BigDecimal("0.10");

        List<AuctionItem> items = auctionItemMapper.selectBySessionId(sessionId);
        if (items == null || items.isEmpty()) {
            log.info("会场无拍品可结算: {}", sessionId);
            return;
        }

        for (AuctionItem item : items) {
            try {
                settleSingleItem(session, item, commissionRatio, depositRatio);
            } catch (Exception e) {
                log.error("结算拍品失败: sessionId={}, itemId={}, err={}", sessionId, item.getId(), e.getMessage(), e);
            }
        }
    }

    private void settleSingleItem(AuctionSession session,
                                  AuctionItem item,
                                  BigDecimal commissionRatio,
                                  BigDecimal depositRatio) {
        Long sessionId = session.getId();
        Long itemId = item.getId();

        // 取该拍品所有有效出价，计算最高价与每个用户的最高价
        List<AuctionBid> bids = auctionBidService.getItemBidList(itemId);
        if (bids == null) bids = Collections.emptyList();

        // 计算每个用户的最高出价（元）
        Map<Long, BigDecimal> userMaxBidYuan = new HashMap<>();
        for (AuctionBid b : bids) {
            if (b.getStatus() != null && b.getStatus() == 0 && b.getBidAmountYuan() != null) {
                BigDecimal yuan = b.getBidAmountYuan();
                userMaxBidYuan.merge(b.getUserId(), yuan, BigDecimal::max);
            }
        }

        // 找到最高出价及中标者
        Long winnerUserId = null;
        BigDecimal finalPriceYuan = BigDecimal.ZERO;
        Long highestBidId = null;
        for (AuctionBid b : bids) {
            if (b.getStatus() != null && b.getStatus() == 0 && b.getBidAmountYuan() != null) {
                if (b.getBidAmountYuan().compareTo(finalPriceYuan) > 0) {
                    finalPriceYuan = b.getBidAmountYuan();
                    winnerUserId = b.getUserId();
                    highestBidId = b.getId();
                }
            }
        }

        // 判断是否流拍（无人出价或不满足保留价）
        boolean sold;
        if (finalPriceYuan.compareTo(BigDecimal.ZERO) == 0) {
            sold = false;
        } else if (item.getReservePrice() != null) {
            sold = finalPriceYuan.compareTo(item.getReservePrice()) >= 0;
        } else {
            sold = true;
        }

        AuctionResult result = new AuctionResult();
        result.setSessionId(sessionId);
        result.setItemId(itemId);
        result.setWinnerUserId(sold ? winnerUserId : null);
        result.setFinalPrice(finalPriceYuan);
        result.setHighestBidId(highestBidId);
        result.setResultStatus(sold ? 1 : 0);
        result.setSettleStatus(0);
        result.setCreateTime(LocalDateTime.now());
        result.setUpdateTime(LocalDateTime.now());

        Long orderId = null;

        if (sold && winnerUserId != null) {
            // 佣金（元）= 成交价 * 佣金比例，四舍五入到元
            BigDecimal commissionYuan = commissionRatio.multiply(finalPriceYuan)
                    .setScale(0, BigDecimal.ROUND_HALF_UP);

            // 中标者需冻结的保证金（元）= 最高出价 * 保证金比例（向上取整到元，保持与出价阶段一致）
            BigDecimal winnerDepositYuan = depositRatio.multiply(finalPriceYuan)
                    .setScale(0, BigDecimal.ROUND_CEILING);

            // 创建订单（金额单位：元）
            BigDecimal balanceYuan = finalPriceYuan.add(commissionYuan).subtract(winnerDepositYuan);
            if (balanceYuan.compareTo(BigDecimal.ZERO) < 0) balanceYuan = BigDecimal.ZERO;

            AuctionOrder order = new AuctionOrder();
            order.setSessionId(sessionId);
            order.setItemId(itemId);
            order.setBuyerId(winnerUserId);
            order.setSellerId(1L); // 超级管理员/平台
            order.setTotalAmount(finalPriceYuan);
            order.setDepositAmount(winnerDepositYuan);
            order.setBalanceAmount(balanceYuan);
            order.setStatus(1); // 待付款
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            orderId = auctionOrderService.createOrder(order);

            // 记录到结果
            result.setOrderId(orderId);
            result.setCommissionFee(commissionYuan);
            result.setDepositUsed(winnerDepositYuan);

            // 发送中标通知（包含充值提醒）
            if (orderId != null) {
                try {
                    // 检查用户保证金余额是否足够支付尾款
                    com.auction.entity.UserDepositAccount account = 
                        userDepositAccountService.getAccountByUserId(winnerUserId);
                    BigDecimal userAvailable = account != null ? account.getAvailableAmount() : BigDecimal.ZERO;
                    boolean needRecharge = userAvailable.compareTo(balanceYuan) < 0;
                    BigDecimal rechargeAmount = needRecharge ? balanceYuan.subtract(userAvailable) : BigDecimal.ZERO;
                    
                    userNotificationService.createWinNotification(
                        winnerUserId, 
                        orderId, 
                        itemId, 
                        item.getItemName(), 
                        order.getOrderNo(),
                        balanceYuan,
                        needRecharge,
                        rechargeAmount
                    );
                    log.info("中标通知已发送: userId={}, orderId={}, itemId={}, needRecharge={}", 
                        winnerUserId, orderId, itemId, needRecharge);
                } catch (Exception e) {
                    log.error("发送中标通知失败: userId={}, orderId={}, error={}", winnerUserId, orderId, e.getMessage());
                }
            }

            // 未中标者解冻保证金
            for (Map.Entry<Long, BigDecimal> entry : userMaxBidYuan.entrySet()) {
                Long userId = entry.getKey();
                if (!userId.equals(winnerUserId)) {
                    BigDecimal userDepositYuan = depositRatio.multiply(entry.getValue())
                            .setScale(0, BigDecimal.ROUND_CEILING);
                    BigDecimal unfreezeYuan = userDepositYuan;
                    try {
                        userDepositAccountService.unfreezeAmount(userId, unfreezeYuan, itemId, "item", "未中标解冻");
                    } catch (Exception e) {
                        log.warn("解冻未中标保证金失败: userId={}, itemId={}, err={}", userId, itemId, e.getMessage());
                    }
                }
            }
        } else {
            // 流拍：所有参与者解冻保证金
            for (Map.Entry<Long, BigDecimal> entry : userMaxBidYuan.entrySet()) {
                Long userId = entry.getKey();
                BigDecimal userDepositYuan = depositRatio.multiply(entry.getValue())
                        .setScale(0, BigDecimal.ROUND_CEILING);
                BigDecimal unfreezeYuan = userDepositYuan;
                try {
                    userDepositAccountService.unfreezeAmount(userId, unfreezeYuan, itemId, "item", "流拍解冻");
                } catch (Exception e) {
                    log.warn("流拍解冻保证金失败: userId={}, itemId={}, err={}", userId, itemId, e.getMessage());
                }
            }
        }

        // 写入结果表
        auctionResultService.saveResult(result);
        // 更新拍品状态为已成交/流拍
        AuctionItem update = new AuctionItem();
        update.setId(itemId);
        update.setStatus(sold ? 5 : 6); // 5-已成交 6-流拍
        update.setUpdateTime(LocalDateTime.now());
        auctionItemMapper.update(update);

        // 发送拍卖结束消息到竞价房间
        sendAuctionEndMessage(sessionId, itemId, item, winnerUserId, finalPriceYuan);
    }

    /**
     * 发送拍卖结束消息到竞价房间
     */
    private void sendAuctionEndMessage(Long sessionId, Long itemId, AuctionItem item, Long winnerUserId, BigDecimal finalPriceYuan) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "AUCTION_END");
            message.put("content", "拍卖结束");
            message.put("timestamp", System.currentTimeMillis());
            
            Map<String, Object> data = new HashMap<>();
            data.put("itemId", itemId);
            data.put("itemName", item.getItemName());
            data.put("winnerId", winnerUserId);
            data.put("finalPrice", finalPriceYuan);
            data.put("finalPriceYuan", finalPriceYuan);
            
            // 获取中拍者信息
            if (winnerUserId != null) {
                try {
                    SysUser winner = sysUserService.getById(winnerUserId);
                    if (winner != null) {
                        String winnerName = (winner.getNickname() != null && !winner.getNickname().trim().isEmpty()) 
                            ? winner.getNickname() 
                            : winner.getUsername();
                        data.put("winnerName", winnerName);
                    }
                } catch (Exception e) {
                    log.warn("获取中拍者信息失败: winnerId={}, error={}", winnerUserId, e.getMessage());
                }
            }
            
            message.put("data", data);
            
            // 广播到拍卖会
            webSocketHandler.broadcastToAuction(sessionId, message, null);
            
            log.info("拍卖结束消息已发送: sessionId={}, itemId={}, winnerId={}, finalPrice={}", 
                sessionId, itemId, winnerUserId, finalPriceYuan);
                
        } catch (Exception e) {
            log.error("发送拍卖结束消息失败: sessionId={}, itemId={}, error={}", sessionId, itemId, e.getMessage(), e);
        }
    }
}


