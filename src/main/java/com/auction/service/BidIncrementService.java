package com.auction.service;

import com.auction.entity.BidIncrementConfig;
import com.auction.entity.BidIncrementRule;

import java.math.BigDecimal;
import java.util.List;

/**
 * 加价阶梯服务接口
 *
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface BidIncrementService {

    /**
     * 创建加价阶梯配置
     */
    Long createConfig(BidIncrementConfig config, List<BidIncrementRule> rules);

    /**
     * 更新加价阶梯配置
     */
    boolean updateConfig(BidIncrementConfig config, List<BidIncrementRule> rules);

    /**
     * 删除加价阶梯配置
     */
    boolean deleteConfig(Long configId);

    /**
     * 根据ID查询加价阶梯配置
     */
    BidIncrementConfig getConfigById(Long configId);

    /**
     * 查询启用的加价阶梯配置列表
     */
    List<BidIncrementConfig> getEnabledConfigs();

    /**
     * 查询所有加价阶梯配置列表
     */
    List<BidIncrementConfig> getAllConfigs();

    /**
     * 根据拍卖会ID获取适用的加价阶梯配置
     */
    BidIncrementConfig getConfigBySessionId(Long sessionId);

    /**
     * 根据当前价格和配置ID获取下一次最低出价金额
     */
    BigDecimal getNextMinimumBid(BigDecimal currentPrice, Long configId);

    /**
     * 校验出价是否符合加价阶梯规则
     */
    boolean validateBidAmount(BigDecimal currentPrice, BigDecimal bidAmount, Long configId);

    /**
     * 根据价格区间获取适用的加价规则
     */
    BidIncrementRule getApplicableRule(BigDecimal amount, Long configId);

    /**
     * 根据配置ID获取所有规则（按排序号排序）
     */
    List<BidIncrementRule> getRulesByConfigId(Long configId);

    /**
     * 校验拍卖会是否可以使用该加价阶梯配置（拍卖会开始后不能修改）
     */
    boolean canModifyConfigForSession(Long configId, Long sessionId);
}
