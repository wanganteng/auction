package com.auction.service.impl;

import com.auction.entity.AuctionBid;
import com.auction.entity.AuctionItem;
import com.auction.entity.AuctionSession;
import com.auction.entity.SysUser;
import com.auction.entity.UserDepositAccount;
import com.auction.mapper.AuctionBidMapper;
import com.auction.mapper.AuctionItemMapper;
import com.auction.mapper.AuctionSessionMapper;
import com.auction.service.*;
import com.auction.websocket.AuctionWebSocketHandler;
import com.auction.state.AuctionStateMachine;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 拍卖服务实现类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class AuctionServiceImpl implements AuctionService {

    @Autowired
    private AuctionItemMapper auctionItemMapper;

    @Autowired
    private AuctionBidMapper auctionBidMapper;

    @Autowired
    private AuctionSessionMapper auctionSessionMapper;

    @Autowired
    private AuctionStateMachine stateMachine;

    @Autowired
    private UserDepositAccountService depositAccountService;

    @Autowired
    private AuctionWebSocketHandler webSocketHandler;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private AuctionResultService auctionResultService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private AuctionSessionService auctionSessionService;

    @Override
    public boolean createAuctionItem(AuctionItem item) {
        log.debug("创建拍卖商品: {}", item.getItemName());
        
        try {
            // 生成商品编码
            // 拍品代码由ID自动生成，无需手动设置
            
            // 设置默认状态为待审核
            item.setStatus(1); // 1-待审核
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            
            int result = auctionItemMapper.insert(item);
            if (result > 0) {
                log.info("拍卖商品创建成功: {}", item.getItemName());
                return true;
            } else {
                log.error("拍卖商品创建失败: {}", item.getItemName());
                return false;
            }
        } catch (Exception e) {
            log.error("创建拍卖商品时发生错误: {}", e.getMessage());
            throw new RuntimeException("创建拍卖商品失败", e);
        }
    }

    @Override
    public boolean reviewAuctionItem(Long itemId, Integer status, String reason) {
        log.debug("审核拍卖商品: {}, 审核状态: {}, 原因: {}", itemId, status, reason);
        
        try {
            AuctionItem item = auctionItemMapper.selectById(itemId);
            if (item == null) {
                log.error("拍卖商品不存在: {}", itemId);
                return false;
            }
            
            if (!item.getStatus().equals(1)) { // 1-待审核
                log.error("拍卖商品状态不是待审核: {}", itemId);
                return false;
            }
            
            // 使用状态机进行状态转换
            String action = (status == 2) ? "APPROVE" : "REJECT";
            if (stateMachine.transitionItem(item, action, status)) {
                item.setUpdateTime(LocalDateTime.now());
                int result = auctionItemMapper.update(item);
                if (result > 0) {
                    log.info("拍卖商品审核完成: {}, 审核状态: {}", itemId, status);
                    return true;
                } else {
                    log.error("拍卖商品状态转换无效: {}", itemId);
                    return false;
                }
            } else {
                log.error("拍卖商品状态转换无效: {}", itemId);
                return false;
            }
        } catch (Exception e) {
            log.error("审核拍卖商品时发生错误: {}", e.getMessage());
            throw new RuntimeException("审核拍卖商品失败", e);
        }
    }

    @Override
    public boolean startAuction(Long itemId) {
        log.debug("开始拍卖商品: {}", itemId);
        
        try {
            AuctionItem item = auctionItemMapper.selectById(itemId);
            if (item == null) {
                log.error("拍卖商品不存在: {}", itemId);
                return false;
            }
            
            if (!stateMachine.canStartAuction(item)) {
                log.error("无法开始拍卖商品: {}", itemId);
                return false;
            }
            
            if (stateMachine.transitionItem(item, "START_AUCTION", 4)) { // 4-拍卖中
                item.setUpdateTime(LocalDateTime.now());
                int result = auctionItemMapper.update(item);
                if (result > 0) {
                    log.info("拍卖开始成功: {}", itemId);
                    // 发送拍卖开始消息
                    sendAuctionStartMessage(itemId, item);
                    return true;
                }
                return false;
            } else {
                log.error("开始拍卖状态转换无效: {}", itemId);
                return false;
            }
        } catch (Exception e) {
            log.error("开始拍卖时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean endAuction(Long itemId) {
        log.debug("结束拍卖商品: {}", itemId);
        
        try {
            AuctionItem item = auctionItemMapper.selectById(itemId);
            if (item == null) {
                log.error("拍卖商品不存在: {}", itemId);
                return false;
            }
            
            if (!stateMachine.canEndAuction(item)) {
                log.error("无法结束拍卖商品: {}", itemId);
                return false;
            }
            
            // 检查是否有出价记录
            List<AuctionBid> bids = auctionBidMapper.selectByAuctionId(itemId);
            Integer targetStatus;
            
            if (bids != null && !bids.isEmpty()) {
                // 有出价，标记为已成交
                targetStatus = 5; // 5-已成交
                // 更新当前价格为最高出价
                AuctionBid highestBid = bids.get(0); // 假设已按价格排序
                item.setCurrentPrice(highestBid.getBidAmountYuan()); // 直接使用元单位
            } else {
                // 无出价，标记为流拍
                targetStatus = 6; // 6-流拍
            }
            
            String action = (targetStatus == 5) ? "SOLD" : "UNSOLD";
            if (stateMachine.transitionItem(item, action, targetStatus)) {
                item.setUpdateTime(LocalDateTime.now());
                int result = auctionItemMapper.update(item);
                if (result > 0) {
                    log.info("拍卖结束成功: {}, 结果: {}", itemId, targetStatus);
                    // 记录拍卖结果
                    Long winnerId = null;
                    BigDecimal finalPrice = null;
                    Long highestBidId = null;
                    if (bids != null && !bids.isEmpty()) {
                        winnerId = bids.get(0).getUserId();
                        finalPrice = bids.get(0).getBidAmountYuan();
                        highestBidId = bids.get(0).getId();
                    }
                    persistAuctionResult(item, winnerId, finalPrice, highestBidId, targetStatus);
                    // 发送拍卖结束消息
                    sendAuctionEndMessage(itemId, item, winnerId, finalPrice);
                    return true;
                }
                return false;
            } else {
                log.error("结束拍卖状态转换无效: {}", itemId);
                return false;
            }
        } catch (Exception e) {
            log.error("结束拍卖时发生错误: {}", e.getMessage());
            return false;
        }
    }

    private void persistAuctionResult(AuctionItem item, Long winnerId, BigDecimal finalPrice, Long highestBidId, Integer targetStatus) {
        try {
            // sessionId 若与 item 绑定关系存在，请按你的结构获取；此处兼容为空
            Long sessionId = null;
            com.auction.entity.AuctionResult result = new com.auction.entity.AuctionResult();
            result.setSessionId(sessionId);
            result.setItemId(item.getId());
            result.setWinnerUserId(winnerId);
            result.setFinalPrice(finalPrice == null ? BigDecimal.ZERO : finalPrice);
            result.setHighestBidId(highestBidId);
            result.setResultStatus(targetStatus == 5 ? 1 : 0); // 5=已成交 -> 1 成交；6=流拍 -> 0 流拍
            result.setSettleStatus(0);
            auctionResultService.saveResult(result);
        } catch (Exception e) {
            log.error("记录拍卖结果失败: itemId={}, err= {}", item.getId(), e.getMessage());
        }
    }


    @Override
    public AuctionItem getAuctionItemDetail(Long itemId) {
        log.debug("获取拍卖商品详情: {}", itemId);
        return auctionItemMapper.selectById(itemId);
    }

    @Override
    public PageInfo<AuctionBid> getBidHistory(Long itemId, Integer pageNum, Integer pageSize) {
        log.debug("获取拍卖出价历史: {}, 页码={}, 大小={}", itemId, pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<AuctionBid> bids = auctionBidMapper.selectByAuctionId(itemId);
        return new PageInfo<>(bids);
    }

    @Override
    public AuctionBid getCurrentHighestBid(Long itemId) {
        log.debug("获取拍卖当前最高出价: {}", itemId);
        List<AuctionBid> bids = auctionBidMapper.selectByAuctionId(itemId);
        if (bids != null && !bids.isEmpty()) {
            return bids.get(0); // 假设已按价格降序排列
        }
        return null;
    }

    @Override
    public Map<String, Object> getAuctionStats(Long itemId) {
        log.debug("获取拍卖统计信息: {}", itemId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取商品信息
        AuctionItem item = auctionItemMapper.selectById(itemId);
        if (item != null) {
            stats.put("item", item);
            stats.put("currentPrice", item.getCurrentPrice());
            stats.put("bidCount", auctionBidMapper.countByAuctionId(itemId));
            stats.put("status", item.getStatus());
            stats.put("statusDescription", getItemStatusDescription(item.getStatus()));
        }
        
        return stats;
    }

    @Override
    public boolean createAuctionSession(AuctionSession session) {
        log.debug("创建拍卖会: {}", session.getSessionName());
        
        try {
            // 生成拍卖会编码
            // 拍卖会代码由ID自动生成，无需手动设置
            
            // 设置默认状态为待开始
            session.setStatus(1); // 1-待开始
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            session.setDeleted(0);
            
            int result = auctionSessionMapper.insert(session);
            if (result > 0) {
                log.info("拍卖会创建成功: {}", session.getSessionName());
                return true;
            } else {
                log.error("拍卖会创建失败: {}", session.getSessionName());
                return false;
            }
        } catch (Exception e) {
            log.error("创建拍卖会时发生错误: {}", e.getMessage());
            throw new RuntimeException("创建拍卖会失败", e);
        }
    }

    @Override
    public boolean startAuctionSession(Long sessionId) {
        log.debug("开始拍卖会: {}", sessionId);
        
        try {
            AuctionSession session = auctionSessionMapper.selectById(sessionId);
            if (session == null) {
                log.error("拍卖会不存在: {}", sessionId);
                return false;
            }
            
            if (!stateMachine.canStartSession(session)) {
                log.error("无法开始拍卖会: {}", sessionId);
                return false;
            }
            
            if (stateMachine.transitionSession(session, "START", 2)) { // 2-进行中
                session.setUpdateTime(LocalDateTime.now());
                int result = auctionSessionMapper.updateById(session);
                if (result > 0) {
                    log.info("拍卖会开始成功: {}", sessionId);
                    return true;
                }
                return false;
            } else {
                log.error("开始拍卖会状态转换无效: {}", sessionId);
                return false;
            }
        } catch (Exception e) {
            log.error("开始拍卖会时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean endAuctionSession(Long sessionId) {
        log.debug("结束拍卖会: {}", sessionId);
        
        try {
            AuctionSession session = auctionSessionMapper.selectById(sessionId);
            if (session == null) {
                log.error("拍卖会不存在: {}", sessionId);
                return false;
            }
            
            if (!stateMachine.canEndSession(session)) {
                log.error("无法结束拍卖会: {}", sessionId);
                return false;
            }
            
            if (stateMachine.transitionSession(session, "FINISH", 3)) { // 3-已结束
                session.setUpdateTime(LocalDateTime.now());
                int result = auctionSessionMapper.updateById(session);
                if (result > 0) {
                    log.info("拍卖会结束成功: {}", sessionId);
                    return true;
                }
                return false;
            } else {
                log.error("结束拍卖会状态转换无效: {}", sessionId);
                return false;
            }
        } catch (Exception e) {
            log.error("结束拍卖会时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public PageInfo<AuctionSession> getAuctionSessions(Integer pageNum, Integer pageSize) {
        log.debug("获取拍卖会列表: 页码={}, 大小={}", pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<AuctionSession> sessions = auctionSessionMapper.selectAll();
        return new PageInfo<>(sessions);
    }

    @Override
    public AuctionSession getAuctionSessionDetail(Long sessionId) {
        log.debug("获取拍卖会详情: {}", sessionId);
        return auctionSessionMapper.selectById(sessionId);
    }

    @Override
    public boolean canBid(Long itemId, Long userId) {
        log.debug("检查用户是否可以出价: 拍卖={}, 用户={}", itemId, userId);
        
        try {
            // 检查拍卖是否存在且状态正确
            AuctionItem item = auctionItemMapper.selectById(itemId);
            if (item == null || !item.getStatus().equals(4)) { // 4-拍卖中
                return false;
            }
            
            // 检查用户保证金是否足够
            // 计算保证金金额 = 起拍价 * 保证金比例
            BigDecimal depositAmount = item.getStartingPrice() != null && item.getDepositRatio() != null ? 
                item.getStartingPrice().multiply(item.getDepositRatio()) : BigDecimal.ZERO;
            Long depositAmountCents = depositAmount.multiply(new BigDecimal("100")).longValue(); // 转换为分
            
            // 检查是否满足拍卖会的最小保证金要求
            // 这里需要从拍卖会获取最小保证金要求，暂时使用系统默认值
            // 最小保证金校验已移至加价阶梯规则中统一处理
            
            if (!depositAccountService.hasEnoughBalance(userId, depositAmountCents)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("检查用户出价权限时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isValidBid(Long itemId, Long bidAmount) {
        log.debug("检查出价是否有效: 拍卖={}, 金额={}", itemId, bidAmount);
        
        try {
            AuctionItem item = auctionItemMapper.selectById(itemId);
            if (item == null || !item.getStatus().equals(4)) { // 4-拍卖中
                return false;
            }
            
            // 检查出价是否高于当前价格
            BigDecimal bidAmountYuan = new BigDecimal(bidAmount).divide(new BigDecimal("100")); // 转换为元
            if (bidAmountYuan.compareTo(item.getCurrentPrice()) <= 0) {
                return false;
            }
            
            // 注意：最大出价限制和加价幅度校验已移至加价阶梯规则中统一处理
            
            return true;
        } catch (Exception e) {
            log.error("检查出价有效性时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public PageInfo<AuctionItem> getAuctionItems(Integer pageNum, Integer pageSize) {
        log.debug("获取拍卖商品列表: 页码={}, 大小={}", pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<AuctionItem> items = auctionItemMapper.selectAll();
        return new PageInfo<>(items);
    }

    @Override
    public PageInfo<AuctionItem> getAuctionItemsByStatus(Integer status, Integer pageNum, Integer pageSize) {
        log.debug("根据状态获取拍卖商品列表: 状态={}, 页码={}, 大小={}", status, pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<AuctionItem> items = auctionItemMapper.selectByStatus(status);
        return new PageInfo<>(items);
    }

    @Override
    public PageInfo<AuctionItem> getAuctionItemsByUserId(Long userId, Integer pageNum, Integer pageSize) {
        log.debug("根据用户ID获取拍卖商品列表: 用户ID={}, 页码={}, 大小={}", userId, pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<AuctionItem> items = auctionItemMapper.selectByUserId(userId);
        return new PageInfo<>(items);
    }

    /**
     * 发送拍卖开始消息
     */
    private void sendAuctionStartMessage(Long itemId, AuctionItem item) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "AUCTION_START");
            message.put("content", "拍卖开始");
            message.put("timestamp", System.currentTimeMillis());
            
            Map<String, Object> data = new HashMap<>();
            data.put("itemId", itemId);
            data.put("itemName", item.getItemName());
            data.put("startingPrice", item.getStartingPrice());
            data.put("startingPriceYuan", item.getStartingPrice() != null ? 
                item.getStartingPrice().divide(new BigDecimal("100")) : BigDecimal.ZERO);
            
            message.put("data", data);
            
            // 广播到所有连接
            webSocketHandler.broadcastToAll(message);
        } catch (Exception e) {
            log.error("发送拍卖开始消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送拍卖结束消息
     */
    private void sendAuctionEndMessage(Long itemId, AuctionItem item, Long winnerId, BigDecimal finalPrice) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "AUCTION_END");
            message.put("content", "拍卖结束");
            message.put("timestamp", System.currentTimeMillis());
            
            Map<String, Object> data = new HashMap<>();
            data.put("itemId", itemId);
            data.put("itemName", item.getItemName());
            data.put("winnerId", winnerId);
            data.put("finalPrice", finalPrice);
            data.put("finalPriceYuan", finalPrice != null ? finalPrice : BigDecimal.ZERO);
            
            // 获取中拍者信息
            if (winnerId != null) {
                try {
                    SysUser winner = sysUserService.getById(winnerId);
                    if (winner != null) {
                        String winnerName = (winner.getNickname() != null && !winner.getNickname().trim().isEmpty()) 
                            ? winner.getNickname() 
                            : winner.getUsername();
                        data.put("winnerName", winnerName);
                    }
                } catch (Exception e) {
                    log.warn("获取中拍者信息失败: winnerId={}, error={}", winnerId, e.getMessage());
                }
            }
            
            message.put("data", data);
            
            // 查找拍品所属的拍卖会ID
            Long sessionId = findSessionIdByItemId(itemId);
            if (sessionId != null) {
                // 广播到拍卖会
                webSocketHandler.broadcastToAuction(sessionId, message, null);
            } else {
                // 如果没有找到拍卖会ID，则广播到所有连接
                webSocketHandler.broadcastToAll(message);
            }
        } catch (Exception e) {
            log.error("发送拍卖结束消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据拍品ID查找所属的拍卖会ID
     */
    private Long findSessionIdByItemId(Long itemId) {
        try {
            // 获取所有拍卖会
            PageInfo<AuctionSession> allSessionsPageInfo = getAuctionSessions(1, 1000);
            List<AuctionSession> allSessions = allSessionsPageInfo.getList();
            
            for (AuctionSession session : allSessions) {
                // 检查该拍品是否属于这个拍卖会
                List<AuctionItem> sessionItems = auctionSessionService.getSessionItems(session.getId());
                boolean belongsToSession = sessionItems.stream()
                    .anyMatch(item -> item.getId().equals(itemId));
                
                if (belongsToSession) {
                    return session.getId();
                }
            }
        } catch (Exception e) {
            log.warn("查找拍品所属拍卖会失败: itemId={}, error={}", itemId, e.getMessage());
        }
        
        return null;
    }

    /**
     * 获取拍品状态描述
     */
    private String getItemStatusDescription(Integer status) {
        if (status == null) return "未知";
        
        switch (status) {
            case 1: return "待审核";
            case 2: return "审核通过";
            case 3: return "审核拒绝";
            case 4: return "拍卖中";
            case 5: return "已成交";
            case 6: return "流拍";
            case 7: return "已下架";
            default: return "未知";
        }
    }
}