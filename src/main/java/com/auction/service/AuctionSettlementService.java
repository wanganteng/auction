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

        // 计算每个用户的最高出价（分）
        Map<Long, Long> userMaxBidCents = new HashMap<>();
        for (AuctionBid b : bids) {
            if (b.getStatus() != null && b.getStatus() == 0 && b.getBidAmount() != null) {
                long cents = b.getBidAmount();
                userMaxBidCents.merge(b.getUserId(), cents, Math::max);
            }
        }

        // 找到最高出价及中标者
        Long winnerUserId = null;
        Long finalPriceCents = 0L;
        Long highestBidId = null;
        for (AuctionBid b : bids) {
            if (b.getStatus() != null && b.getStatus() == 0 && b.getBidAmount() != null) {
                if (b.getBidAmount() > finalPriceCents) {
                    finalPriceCents = b.getBidAmount();
                    winnerUserId = b.getUserId();
                    highestBidId = b.getId();
                }
            }
        }

        // 判断是否流拍（无人出价或不满足保留价）
        boolean sold;
        if (finalPriceCents == 0L) {
            sold = false;
        } else if (item.getReservePrice() != null) {
            sold = BigDecimal.valueOf(finalPriceCents).divide(new BigDecimal("100"))
                    .compareTo(item.getReservePrice()) >= 0;
        } else {
            sold = true;
        }

        AuctionResult result = new AuctionResult();
        result.setSessionId(sessionId);
        result.setItemId(itemId);
        result.setWinnerUserId(sold ? winnerUserId : null);
        result.setFinalPrice(finalPriceCents);
        result.setHighestBidId(highestBidId);
        result.setResultStatus(sold ? 1 : 0);
        result.setSettleStatus(0);
        result.setCreateTime(LocalDateTime.now());
        result.setUpdateTime(LocalDateTime.now());

        Long orderId = null;

        if (sold && winnerUserId != null) {
            // 佣金（分）= 成交价 * 佣金比例，四舍五入到分
            long commissionCents = commissionRatio.multiply(BigDecimal.valueOf(finalPriceCents))
                    .setScale(0, BigDecimal.ROUND_HALF_UP).longValue();

            // 中标者需冻结的保证金（分）= 最高出价 * 保证金比例（向上取整到分，保持与出价阶段一致）
            long winnerDepositCents = depositRatio.multiply(BigDecimal.valueOf(finalPriceCents))
                    .setScale(0, BigDecimal.ROUND_CEILING).longValue();

            // 创建订单（金额单位：元）
            BigDecimal finalPriceYuan = BigDecimal.valueOf(finalPriceCents).divide(new BigDecimal("100"));
            BigDecimal commissionYuan = BigDecimal.valueOf(commissionCents).divide(new BigDecimal("100"));
            BigDecimal depositYuan = BigDecimal.valueOf(winnerDepositCents).divide(new BigDecimal("100"));
            BigDecimal balanceYuan = finalPriceYuan.add(commissionYuan).subtract(depositYuan);
            if (balanceYuan.compareTo(BigDecimal.ZERO) < 0) balanceYuan = BigDecimal.ZERO;

            AuctionOrder order = new AuctionOrder();
            order.setSessionId(sessionId);
            order.setItemId(itemId);
            order.setBuyerId(winnerUserId);
            order.setSellerId(1L); // 超级管理员/平台
            order.setTotalAmount(finalPriceYuan);
            order.setDepositAmount(depositYuan);
            order.setBalanceAmount(balanceYuan);
            order.setStatus(1); // 待付款
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            orderId = auctionOrderService.createOrder(order);

            // 记录到结果
            result.setOrderId(orderId);
            result.setCommissionFee(commissionCents);
            result.setDepositUsed(winnerDepositCents);

            // 发送中标通知
            if (orderId != null) {
                try {
                    userNotificationService.createWinNotification(
                        winnerUserId, 
                        orderId, 
                        itemId, 
                        item.getItemName(), 
                        order.getOrderNo()
                    );
                    log.info("中标通知已发送: userId={}, orderId={}, itemId={}", winnerUserId, orderId, itemId);
                } catch (Exception e) {
                    log.error("发送中标通知失败: userId={}, orderId={}, error={}", winnerUserId, orderId, e.getMessage());
                }
            }

            // 未中标者解冻保证金
            for (Map.Entry<Long, Long> entry : userMaxBidCents.entrySet()) {
                Long userId = entry.getKey();
                if (!userId.equals(winnerUserId)) {
                    long userDepositCents = depositRatio.multiply(BigDecimal.valueOf(entry.getValue()))
                            .setScale(0, BigDecimal.ROUND_CEILING).longValue();
                    BigDecimal unfreezeYuan = BigDecimal.valueOf(userDepositCents).divide(new BigDecimal("100"));
                    try {
                        userDepositAccountService.unfreezeAmount(userId, unfreezeYuan, itemId, "item", "未中标解冻");
                    } catch (Exception e) {
                        log.warn("解冻未中标保证金失败: userId={}, itemId={}, err={}", userId, itemId, e.getMessage());
                    }
                }
            }
        } else {
            // 流拍：所有参与者解冻保证金
            for (Map.Entry<Long, Long> entry : userMaxBidCents.entrySet()) {
                Long userId = entry.getKey();
                long userDepositCents = depositRatio.multiply(BigDecimal.valueOf(entry.getValue()))
                        .setScale(0, BigDecimal.ROUND_CEILING).longValue();
                BigDecimal unfreezeYuan = BigDecimal.valueOf(userDepositCents).divide(new BigDecimal("100"));
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
    }
}


