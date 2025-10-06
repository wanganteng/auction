package com.auction.service;

import com.auction.entity.AuctionBid;
import com.auction.entity.AuctionItem;
import com.auction.entity.AuctionSession;
import com.auction.mapper.AuctionBidMapper;
import com.auction.mapper.AuctionItemMapper;
import com.auction.mapper.AuctionSessionMapper;
import com.auction.service.RedisService;
import com.auction.service.UserDepositAccountService;
import com.auction.entity.UserDepositAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拍卖出价服务类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class AuctionBidService {

    @Autowired
    private AuctionBidMapper auctionBidMapper;

    @Autowired
    private AuctionItemMapper auctionItemMapper;

    @Autowired
    private AuctionSessionMapper auctionSessionMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserDepositAccountService depositAccountService;

    @Autowired
    private BidIncrementService bidIncrementService;


    /**
     * 出价
     */
    @Transactional
    public Long placeBid(AuctionBid bid) {
        try {
            // 验证出价（包含差额冻结所需校验）
            validateBid(bid);

            // 设置出价时间
            bid.setBidTime(LocalDateTime.now());
            bid.setStatus(0); // 有效
            bid.setCreateTime(LocalDateTime.now());
            bid.setUpdateTime(LocalDateTime.now());
            bid.setDeleted(0); // 未删除

            // 在插入出价记录之前，先计算历史最高出价（避免包含当前出价）
            BigDecimal oldRequiredDeposit = calculateHistoricalDepositRequirement(bid);

            // 插入出价记录
            auctionBidMapper.insert(bid);

            // 更新拍品当前价格
            updateItemCurrentPrice(bid);

            // 冻结保证金：按会场比例，仅冻结相较于该用户历史最高有效出价的差额
            freezeDepositAmount(bid, oldRequiredDeposit);

            // 延时拍卖：在结束前阈值内出价则顺延结束时间
            try {
                AuctionSession session = auctionSessionMapper.selectById(bid.getSessionId());
                if (session != null && session.getAntiSnipingEnabled() != null && session.getAntiSnipingEnabled() == 1) {
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    java.time.Duration toEnd = java.time.Duration.between(now, session.getEndTime());
                    int threshold = session.getExtendThresholdSec() != null ? session.getExtendThresholdSec() : 60;
                    int extendSec = session.getExtendSeconds() != null ? session.getExtendSeconds() : 60;
                    int maxTimes = session.getExtendMaxTimes() != null ? session.getExtendMaxTimes() : 5;
                    if (toEnd.getSeconds() <= threshold && toEnd.getSeconds() > 0) {
                        String key = "auction:extend:count:" + session.getId();
                        Long times = redisService.incrementAuctionBidCount(session.getId()); // 复用计数或改为独立键
                        // 如果需要独立键可改为 redisService.increment(key)
                        long used = times != null ? times : 0L;
                        if (maxTimes == 0 || used < maxTimes) {
                            session.setEndTime(session.getEndTime().plusSeconds(extendSec));
                            session.setUpdateTime(java.time.LocalDateTime.now());
                            auctionSessionMapper.updateById(session);
                        }
                    }
                }
            } catch (Exception ignore) {}

            log.info("出价成功: 用户ID={}, 拍品ID={}, 出价={}", 
                bid.getUserId(), bid.getItemId(), bid.getBidAmountYuan());
            return bid.getId();

        } catch (Exception e) {
            log.error("出价失败: {}", e.getMessage(), e);
            throw new RuntimeException("出价失败: " + e.getMessage());
        }
    }

    /**
     * 查询出价记录
     */
    public List<AuctionBid> getBidList(AuctionBid bid) {
        try {
            return auctionBidMapper.selectList(bid);
        } catch (Exception e) {
            log.error("查询出价记录失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询拍品出价记录
     */
    public List<AuctionBid> getItemBidList(Long itemId) {
        try {
            AuctionBid bid = new AuctionBid();
            bid.setItemId(itemId);
            bid.setStatus(0); // 只查询有效出价
            return auctionBidMapper.selectList(bid);
        } catch (Exception e) {
            log.error("查询拍品出价记录失败: itemId={}, 错误: {}", itemId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取拍品最高出价
     */
    public AuctionBid getHighestBid(Long itemId) {
        try {
            return auctionBidMapper.selectHighestBid(itemId);
        } catch (Exception e) {
            log.error("查询拍品最高出价失败: itemId={}, 错误: {}", itemId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 验证出价
     */
    private void validateBid(AuctionBid bid) {
        // 获取拍品信息
        AuctionItem item = auctionItemMapper.selectById(bid.getItemId());
        if (item == null) {
            throw new RuntimeException("拍品不存在");
        }

        // 检查拍品状态：0-下架，1-上架（允许上架状态下竞价）
        if (item.getStatus() == null || item.getStatus() != 1) {
            throw new RuntimeException("拍品不在上架状态");
        }

        // 检查出价金额
        if (bid.getBidAmountYuan().compareTo(item.getCurrentPrice()) <= 0) {
            throw new RuntimeException("出价必须高于当前价格");
        }

        // 获取拍卖会信息进行加价阶梯校验
        AuctionSession auctionSession = auctionSessionMapper.selectById(bid.getSessionId());
        if (auctionSession != null && auctionSession.getBidIncrementConfigId() != null) {
            boolean isValidBid = bidIncrementService.validateBidAmount(
                item.getCurrentPrice(),
                bid.getBidAmountYuan(),
                auctionSession.getBidIncrementConfigId()
            );

            if (!isValidBid) {
                throw new RuntimeException("出价不符合加价阶梯规则，请按照规定加价");
            }
        }

        // 检查起拍价
        if (bid.getBidAmountYuan().compareTo(item.getStartingPrice()) < 0) {
            throw new RuntimeException("出价不能低于起拍价");
        }

        // 检查保留价
        // 保留价仅用于结果判定，不在出价环节校验
        // if (item.getReservePrice() != null &&
        //     bid.getBidAmountYuan().compareTo(item.getReservePrice()) < 0) {
        //     throw new RuntimeException("出价不能低于保留价");
        // }

        // 校验保证金充足（基于会场保证金比例）
        AuctionSession session = auctionSessionMapper.selectById(bid.getSessionId());
        if (session == null) {
            throw new RuntimeException("拍卖会不存在");
        }
        java.math.BigDecimal ratio = session.getDepositRatio() != null ? session.getDepositRatio() : new java.math.BigDecimal("0.10");
        // 差额校验：只校验新增冻结部分所需（向上取整到元）
        BigDecimal newRequiredDeposit = bid.getBidAmountYuan().multiply(ratio)
                .setScale(0, java.math.RoundingMode.CEILING);

        BigDecimal oldRequiredDeposit = BigDecimal.ZERO;
        try {
            AuctionBid query = new AuctionBid();
            query.setItemId(bid.getItemId());
            query.setSessionId(bid.getSessionId());
            query.setUserId(bid.getUserId());
            query.setStatus(0);
            List<AuctionBid> userBids = auctionBidMapper.selectList(query);
            BigDecimal maxBidAmount = BigDecimal.ZERO;
            if (userBids != null) {
                for (AuctionBid b : userBids) {
                    if (b.getBidAmountYuan() != null && b.getBidAmountYuan().compareTo(maxBidAmount) > 0) {
                        maxBidAmount = b.getBidAmountYuan();
                    }
                }
            }
            if (maxBidAmount.compareTo(BigDecimal.ZERO) > 0) {
                oldRequiredDeposit = maxBidAmount.multiply(ratio)
                        .setScale(0, java.math.RoundingMode.CEILING);
            }
        } catch (Exception ignore) {}

        BigDecimal deltaFreeze = newRequiredDeposit.subtract(oldRequiredDeposit);
        if (deltaFreeze.compareTo(BigDecimal.ZERO) < 0) {
            deltaFreeze = BigDecimal.ZERO;
        }

        UserDepositAccount account = depositAccountService.getAccountByUserId(bid.getUserId());
        if (account == null) {
            throw new RuntimeException("保证金账户不存在，请先充值");
        }
        BigDecimal available = account.getAvailableAmount();
        if (available.compareTo(deltaFreeze) < 0) {
            throw new RuntimeException("可用保证金不足，需新增：" + deltaFreeze + " 元");
        }
    }

    /**
     * 计算历史最高出价所需的保证金（不包含当前出价）
     * 
     * @param bid 当前出价
     * @return 历史最高出价所需保证金（元）
     */
    private BigDecimal calculateHistoricalDepositRequirement(AuctionBid bid) {
        try {
            AuctionSession session = auctionSessionMapper.selectById(bid.getSessionId());
            if (session == null) {
                log.warn("拍卖会不存在: sessionId={}", bid.getSessionId());
                return BigDecimal.ZERO;
            }
            
            java.math.BigDecimal ratio = session.getDepositRatio() != null ? 
                session.getDepositRatio() : new java.math.BigDecimal("0.10");
            
            // 查询该用户在该拍品（同一会场）下的历史最高有效出价（不包含当前出价）
            AuctionBid query = new AuctionBid();
            query.setItemId(bid.getItemId());
            query.setSessionId(bid.getSessionId());
            query.setUserId(bid.getUserId());
            query.setStatus(0);
            List<AuctionBid> userBids = auctionBidMapper.selectList(query);
            
            BigDecimal maxBidAmount = BigDecimal.ZERO;
            if (userBids != null) {
                for (AuctionBid b : userBids) {
                    if (b.getBidAmountYuan() != null && b.getBidAmountYuan().compareTo(maxBidAmount) > 0) {
                        maxBidAmount = b.getBidAmountYuan();
                    }
                }
            }
            
            if (maxBidAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal oldRequiredDeposit = maxBidAmount.multiply(ratio)
                        .setScale(0, java.math.RoundingMode.CEILING);
                
                log.debug("历史最高出价: {}元, 所需保证金: {}元", maxBidAmount, oldRequiredDeposit);
                return oldRequiredDeposit;
            }
            
            log.debug("用户无历史出价记录");
            return BigDecimal.ZERO;
            
        } catch (Exception e) {
            log.error("计算历史保证金需求失败: userId={}, itemId={}, sessionId={}, error={}", 
                bid.getUserId(), bid.getItemId(), bid.getSessionId(), e.getMessage(), e);
            return BigDecimal.ZERO; // 出错时返回0，确保不会过度冻结
        }
    }

    /**
     * 冻结保证金金额
     * 
     * @param bid 出价记录
     * @param oldRequiredDeposit 历史最高出价所需保证金（元）
     */
    private void freezeDepositAmount(AuctionBid bid, BigDecimal oldRequiredDeposit) {
        try {
            AuctionSession session = auctionSessionMapper.selectById(bid.getSessionId());
            if (session == null) {
                log.error("拍卖会不存在，无法冻结保证金: sessionId={}", bid.getSessionId());
                throw new RuntimeException("拍卖会不存在");
            }
            
            java.math.BigDecimal ratio = session.getDepositRatio() != null ? 
                session.getDepositRatio() : new java.math.BigDecimal("0.10");
            
            // 新出价所需保证金（元，向上取整）
            BigDecimal newRequiredDeposit = bid.getBidAmountYuan().multiply(ratio)
                    .setScale(0, java.math.RoundingMode.CEILING);

            BigDecimal deltaFreeze = newRequiredDeposit.subtract(oldRequiredDeposit);
            if (deltaFreeze.compareTo(BigDecimal.ZERO) < 0) {
                deltaFreeze = BigDecimal.ZERO;
            }
            
            // 记录详细的冻结信息
            log.info("保证金冻结计算: 用户ID={}, 拍品ID={}, 出价={}元, 新需保证金={}元, 历史需保证金={}元, 差额冻结={}元", 
                bid.getUserId(), bid.getItemId(), bid.getBidAmountYuan(), newRequiredDeposit, oldRequiredDeposit, deltaFreeze);
            
            // 冻结金额（元）
            BigDecimal freezeAmount = deltaFreeze;

            // 安全检查：如果差额为0但新出价大于历史最高出价，因为计算目前计算保证才用向上取整，如果相邻两次加价价格较近则会出现这种情况
            if (deltaFreeze.compareTo(BigDecimal.ZERO) == 0 && newRequiredDeposit.compareTo(oldRequiredDeposit) > 0) {
                log.error("保证金计算标点: 新出价大于历史最高出价但差额为0, userId={}, itemId={}, newRequired={}, oldRequired={}",
                    bid.getUserId(), bid.getItemId(), newRequiredDeposit, oldRequiredDeposit);
                //throw new RuntimeException("保证金计算异常，请检查历史出价记录");
            }

            UserDepositAccount account = depositAccountService.getAccountByUserId(bid.getUserId());
            if (account == null) {
                log.error("用户保证金账户不存在: userId={}", bid.getUserId());
                throw new RuntimeException("保证金账户不存在");
            }
            
            if (deltaFreeze.compareTo(BigDecimal.ZERO) > 0) {
                
                // 再次验证可用余额
                if (account.getAvailableAmount().compareTo(freezeAmount) < 0) {
                    log.error("可用保证金不足: userId={}, 需要={}, 可用={}", 
                        bid.getUserId(), freezeAmount, account.getAvailableAmount());
                    throw new RuntimeException("可用保证金不足，需新增：" + freezeAmount + " 元");
                }
                
                boolean success = depositAccountService.freezeAmount(
                    bid.getUserId(), freezeAmount, bid.getItemId(), "item", "出价冻结保证金");
                
                if (!success) {
                    throw new RuntimeException("保证金冻结操作失败");
                }
                
                log.info("保证金冻结成功: 用户ID={}, 金额={}元, 拍品ID={}", 
                    bid.getUserId(), freezeAmount, bid.getItemId());
            } else {
                log.info("无需冻结保证金: 用户ID={}, 出价ID={}, 差额={}元", 
                    bid.getUserId(), bid.getId(), deltaFreeze);
            }
            
        } catch (Exception e) {
            log.error("冻结保证金失败: userId={}, itemId={}, sessionId={}, error={}", 
                bid.getUserId(), bid.getItemId(), bid.getSessionId(), e.getMessage(), e);
            throw new RuntimeException("冻结保证金失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍品当前价格
     */
    private void updateItemCurrentPrice(AuctionBid bid) {
        try {
            AuctionItem item = new AuctionItem();
            item.setId(bid.getItemId());
            item.setCurrentPrice(bid.getBidAmountYuan());
            item.setUpdateTime(LocalDateTime.now());
            
            auctionItemMapper.updateById(item);
        } catch (Exception e) {
            log.error("更新拍品当前价格失败: {}", e.getMessage(), e);
        }
    }

}
