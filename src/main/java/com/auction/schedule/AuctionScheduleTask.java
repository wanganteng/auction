package com.auction.schedule;

import com.auction.entity.AuctionItem;
import com.auction.entity.AuctionSession;
import com.auction.service.AuctionService;
import com.auction.service.AuctionOrderService;
import com.auction.service.SysConfigService;
import com.auction.websocket.AuctionWebSocketHandler;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拍卖定时任务
 * 处理拍卖状态更新、倒计时等定时任务
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class AuctionScheduleTask {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionWebSocketHandler webSocketHandler;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private AuctionOrderService auctionOrderService;

    @Autowired
    private com.auction.service.AuctionSettlementService auctionSettlementService;

    @Autowired
    private com.auction.service.AuctionSessionService auctionSessionService;

    /**
     * 每分钟检查拍卖状态
     * 自动开始和结束拍卖
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void checkAuctionStatus() {
        log.debug("检查拍卖状态...");
        
        try {
            // 检查需要开始的拍卖
            checkAndStartAuctions();
            
            // 检查需要结束的拍卖
            checkAndEndAuctions();
            
            // 检查拍卖会状态
            checkAuctionSessions();

            // 检查超时未支付订单并扣除保证金
            if (auctionOrderService instanceof com.auction.service.impl.AuctionOrderServiceImpl) {
                ((com.auction.service.impl.AuctionOrderServiceImpl) auctionOrderService).processOverdueUnpaidOrders();
            }
            
            // 检查已结束的拍卖会并触发结算
            checkAndSettleEndedSessions();
            
        } catch (Exception e) {
            log.error("检查拍卖状态时发生错误: {}", e.getMessage());
        }
    }

    /**
     * 发送拍卖倒计时
     */
    @Scheduled(fixedRate = 30000) // 每30秒执行一次
    public void sendAuctionCountdown() {
        log.debug("发送拍卖倒计时...");
        
        try {
            // 获取正在进行的拍卖
            PageInfo<AuctionItem> allAuctionsPageInfo = auctionService.getAuctionItems(1, 1000);
            List<AuctionItem> allAuctions = allAuctionsPageInfo.getList();
            List<AuctionItem> activeAuctions = new java.util.ArrayList<>();
            for (AuctionItem auction : allAuctions) {
                if (auction.getStatus().equals(4)) { // 4-拍卖中
                    activeAuctions.add(auction);
                }
            }
            
            for (AuctionItem auction : activeAuctions) {
                // 从系统配置获取倒计时时间
                Integer timeoutSeconds = sysConfigService.getIntConfigValue("auction.bidding.timeout_seconds", 300);
                long remainingSeconds = timeoutSeconds; // 从配置获取倒计时时间
                sendAuctionCountdownMessage(auction.getId(), remainingSeconds);
            }
            
        } catch (Exception e) {
            log.error("发送拍卖倒计时时发生错误: {}", e.getMessage());
        }
    }

    /**
     * 每天凌晨2点执行数据清理任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredData() {
        log.info("开始数据清理任务...");
        
        try {
            // 清理过期的拍卖数据
            cleanupExpiredAuctions();
            
            // 清理过期的会话数据
            cleanupExpiredSessions();
            
            log.info("数据清理任务完成");
            
        } catch (Exception e) {
            log.error("数据清理任务发生错误: {}", e.getMessage());
        }
    }

    /**
     * 每小时执行一次统计任务
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void updateAuctionStatistics() {
        log.debug("更新拍卖统计信息...");
        
        try {
            // 更新拍卖统计信息
            updateAuctionStats();
            
        } catch (Exception e) {
            log.error("更新拍卖统计信息时发生错误: {}", e.getMessage());
        }
    }

    /**
     * 检查并开始拍卖
     */
    private void checkAndStartAuctions() {
        try {
            // 获取已审核通过但未开始的拍卖
            PageInfo<AuctionItem> allAuctionsPageInfo = auctionService.getAuctionItems(1, 1000);
            List<AuctionItem> allAuctions = allAuctionsPageInfo.getList();
            List<AuctionItem> pendingAuctions = new java.util.ArrayList<>();
            for (AuctionItem auction : allAuctions) {
                if (auction.getStatus().equals(2)) { // 2-审核通过
                    pendingAuctions.add(auction);
                }
            }
            
            for (AuctionItem auction : pendingAuctions) {
                // 检查是否到了开始时间
                if (shouldStartAuction(auction)) {
                    log.info("自动开始拍卖: {}", auction.getId());
                    auctionService.startAuction(auction.getId());
                }
            }
            
        } catch (Exception e) {
            log.error("检查并开始拍卖时发生错误: {}", e.getMessage());
        }
    }

    /**
     * 检查并结束拍卖
     */
    private void checkAndEndAuctions() {
        try {
            // 获取所有拍卖会
            PageInfo<AuctionSession> allSessionsPageInfo = auctionService.getAuctionSessions(1, 1000);
            List<AuctionSession> allSessions = allSessionsPageInfo.getList();
            
            for (AuctionSession session : allSessions) {
                // 检查拍卖会是否应该结束
                if (shouldEndSession(session)) {
                    log.info("拍卖会时间已到，开始结束拍卖会: {}", session.getId());
                    
                    // 结束拍卖会
                    if (auctionService.endAuctionSession(session.getId())) {
                        log.info("拍卖会结束成功: {}", session.getId());
                        
                        // 立即进行结算
                        try {
                            log.info("开始结算拍卖会: {}", session.getId());
                            auctionSettlementService.settleSession(session.getId());
                            log.info("拍卖会结算完成: {}", session.getId());
                        } catch (Exception e) {
                            log.error("拍卖会结算失败: sessionId={}, error={}", session.getId(), e.getMessage(), e);
                        }
                    } else {
                        log.error("拍卖会结束失败: {}", session.getId());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("检查并结束拍卖时发生错误: {}", e.getMessage());
        }
    }

    /**
     * 检查拍卖会状态
     */
    private void checkAuctionSessions() {
        try {
            // 获取待开始的拍卖会
            PageInfo<AuctionSession> allSessionsPageInfo = auctionService.getAuctionSessions(1, 1000);
            List<AuctionSession> allSessions = allSessionsPageInfo.getList();
            List<AuctionSession> pendingSessions = new java.util.ArrayList<>();
            for (AuctionSession session : allSessions) {
                if (session.getStatus().equals(1)) { // 1-待开始
                    pendingSessions.add(session);
                }
            }
            
            for (AuctionSession session : pendingSessions) {
                if (shouldStartSession(session)) {
                    log.info("自动开始拍卖会: {}", session.getId());
                    auctionService.startAuctionSession(session.getId());
                }
            }
            
            // 获取进行中的拍卖会
            List<AuctionSession> activeSessions = new java.util.ArrayList<>();
            for (AuctionSession session : allSessions) {
                if (session.getStatus().equals(2)) { // 2-进行中
                    activeSessions.add(session);
                }
            }
            
            for (AuctionSession session : activeSessions) {
                if (shouldEndSession(session)) {
                    log.info("自动结束拍卖会: {}", session.getId());
                    auctionService.endAuctionSession(session.getId());
                }
            }
            
        } catch (Exception e) {
            log.error("检查拍卖会时发生错误: {}", e.getMessage());
        }
    }

    /**
     * 判断拍卖是否应该开始
     * 
     * @param auction 拍卖商品
     * @return 是否应该开始
     */
    private boolean shouldStartAuction(AuctionItem auction) {
        // 这里可以根据拍卖的配置时间来判断
        // 简化处理，返回true表示立即开始
        return true;
    }

    /**
     * 判断拍卖会是否应该结束
     * 
     * @param session 拍卖会
     * @return 是否应该结束
     */
    private boolean shouldEndSession(AuctionSession session) {
        // 检查拍卖会是否已结束
        if (session.getEndTime() != null && 
            session.getEndTime().isBefore(LocalDateTime.now()) &&
            session.getStatus() != null && session.getStatus() == 2) { // 2-进行中
            return true;
        }
        
        return false;
    }

    /**
     * 判断拍卖会是否应该开始
     * 
     * @param session 拍卖会
     * @return 是否应该开始
     */
    private boolean shouldStartSession(AuctionSession session) {
        return session.getStartTime() != null && 
               session.getStartTime().isBefore(LocalDateTime.now());
    }


    /**
     * 清理过期的拍卖数据
     */
    private void cleanupExpiredAuctions() {
        // 清理已结束超过30天的拍卖数据
        // 这里可以实现具体的清理逻辑
        log.info("清理过期的拍卖数据...");
    }

    /**
     * 清理过期的会话数据
     */
    private void cleanupExpiredSessions() {
        // 清理已结束超过30天的拍卖会数据
        // 这里可以实现具体的清理逻辑
        log.info("清理过期的拍卖会数据...");
    }

    /**
     * 检查并结算已结束的拍卖会
     */
    private void checkAndSettleEndedSessions() {
        try {
            // 获取已结束但未结算的拍卖会
            PageInfo<AuctionSession> allSessionsPageInfo = auctionService.getAuctionSessions(1, 1000);
            List<AuctionSession> allSessions = allSessionsPageInfo.getList();
            List<AuctionSession> endedSessions = new java.util.ArrayList<>();
            
            for (AuctionSession session : allSessions) {
                // 状态为已结束(3)或时间已过且状态为进行中(2)
                if (session.getStatus() != null && 
                    (session.getStatus() == 3 || 
                     (session.getStatus() == 2 && session.getEndTime() != null && 
                      session.getEndTime().isBefore(LocalDateTime.now())))) {
                    endedSessions.add(session);
                }
            }
            
            for (AuctionSession session : endedSessions) {
                try {
                    log.info("开始结算拍卖会: {}", session.getId());
                    auctionSettlementService.settleSession(session.getId());
                    log.info("拍卖会结算完成: {}", session.getId());
                } catch (Exception e) {
                    log.error("拍卖会结算失败: sessionId={}, error={}", session.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("检查已结束拍卖会时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新拍卖统计信息
     */
    private void updateAuctionStats() {
        // 更新各种统计信息
        // 这里可以实现具体的统计逻辑
        log.debug("更新拍卖统计信息...");
    }

    /**
     * 发送拍卖倒计时消息
     */
    private void sendAuctionCountdownMessage(Long auctionId, long remainingSeconds) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("auctionId", auctionId);
            data.put("remainingSeconds", remainingSeconds);
            
            Map<String, Object> message = new HashMap<>();
            message.put("type", "AUCTION_COUNTDOWN");
            message.put("content", "拍卖倒计时");
            message.put("timestamp", System.currentTimeMillis());
            message.put("data", data);
            
            webSocketHandler.broadcastToAll(message);
        } catch (Exception e) {
            log.error("发送拍卖倒计时消息失败: {}", e.getMessage(), e);
        }
    }
}
