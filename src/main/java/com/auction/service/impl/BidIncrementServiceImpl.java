package com.auction.service.impl;

import com.auction.entity.AuctionSession;
import com.auction.entity.BidIncrementConfig;
import com.auction.entity.BidIncrementRule;
import com.auction.mapper.AuctionSessionMapper;
import com.auction.mapper.BidIncrementConfigMapper;
import com.auction.mapper.BidIncrementRuleMapper;
import com.auction.service.BidIncrementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 加价阶梯服务实现类
 *
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class BidIncrementServiceImpl implements BidIncrementService {

    @Autowired
    private BidIncrementConfigMapper configMapper;

    @Autowired
    private BidIncrementRuleMapper ruleMapper;

    @Autowired
    private AuctionSessionMapper auctionSessionMapper;

    @Override
    @Transactional
    public Long createConfig(BidIncrementConfig config, List<BidIncrementRule> rules) {
        try {
            // 设置创建时间
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            config.setDeleted(0);

            // 插入配置
            configMapper.insert(config);

            // 插入规则
            if (rules != null && !rules.isEmpty()) {
                for (BidIncrementRule rule : rules) {
                    rule.setConfigId(config.getId());
                    rule.setCreateTime(LocalDateTime.now());
                    rule.setUpdateTime(LocalDateTime.now());
                    rule.setDeleted(0);
                }
                ruleMapper.insertBatch(rules);
            }

            log.info("加价阶梯配置创建成功: configId={}, configName={}", config.getId(), config.getConfigName());
            return config.getId();

        } catch (Exception e) {
            log.error("加价阶梯配置创建失败: configName={}", config.getConfigName(), e);
            throw new RuntimeException("加价阶梯配置创建失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateConfig(BidIncrementConfig config, List<BidIncrementRule> rules) {
        try {
            // 校验是否有拍卖会正在使用该配置且已开始
            if (!canModifyConfigForSession(config.getId(), null)) {
                throw new RuntimeException("该加价阶梯配置已被拍卖会使用且拍卖会已开始，无法修改");
            }

            // 更新配置
            config.setUpdateTime(LocalDateTime.now());
            int configResult = configMapper.updateById(config);

            if (configResult <= 0) {
                return false;
            }

            // 删除原有规则
            ruleMapper.deleteByConfigId(config.getId());

            // 插入新规则
            if (rules != null && !rules.isEmpty()) {
                for (BidIncrementRule rule : rules) {
                    rule.setConfigId(config.getId());
                    rule.setCreateTime(LocalDateTime.now());
                    rule.setUpdateTime(LocalDateTime.now());
                    rule.setDeleted(0);
                }
                ruleMapper.insertBatch(rules);
            }

            log.info("加价阶梯配置更新成功: configId={}, configName={}", config.getId(), config.getConfigName());
            return true;

        } catch (Exception e) {
            log.error("加价阶梯配置更新失败: configId={}", config.getId(), e);
            throw new RuntimeException("加价阶梯配置更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteConfig(Long configId) {
        try {
            // 校验是否有拍卖会正在使用该配置且已开始
            if (!canModifyConfigForSession(configId, null)) {
                throw new RuntimeException("该加价阶梯配置已被拍卖会使用且拍卖会已开始，无法删除");
            }

            // 删除规则
            ruleMapper.deleteByConfigId(configId);

            // 删除配置
            BidIncrementConfig config = new BidIncrementConfig();
            config.setId(configId);
            config.setDeleted(1);
            config.setUpdateTime(LocalDateTime.now());

            int result = configMapper.updateById(config);

            if (result > 0) {
                log.info("加价阶梯配置删除成功: configId={}", configId);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("加价阶梯配置删除失败: configId={}", configId, e);
            throw new RuntimeException("加价阶梯配置删除失败: " + e.getMessage());
        }
    }

    @Override
    public BidIncrementConfig getConfigById(Long configId) {
        try {
            BidIncrementConfig config = configMapper.selectById(configId);
            if (config != null) {
                // 加载规则列表
                List<BidIncrementRule> rules = ruleMapper.selectListByConfigId(configId);
                config.setRules(rules);
            }
            return config;

        } catch (Exception e) {
            log.error("查询加价阶梯配置失败: configId={}", configId, e);
            return null;
        }
    }

    @Override
    public List<BidIncrementConfig> getEnabledConfigs() {
        try {
            return configMapper.selectEnabledList();
        } catch (Exception e) {
            log.error("查询启用的加价阶梯配置列表失败", e);
            return null;
        }
    }

    @Override
    public List<BidIncrementConfig> getAllConfigs() {
        try {
            return configMapper.selectList(null);
        } catch (Exception e) {
            log.error("查询所有加价阶梯配置列表失败", e);
            return null;
        }
    }

    @Override
    public BidIncrementConfig getConfigBySessionId(Long sessionId) {
        try {
            return configMapper.selectBySessionId(sessionId);
        } catch (Exception e) {
            log.error("根据拍卖会ID查询加价阶梯配置失败: sessionId={}", sessionId, e);
            return null;
        }
    }

    @Override
    public BigDecimal getNextMinimumBid(BigDecimal currentPrice, Long configId) {
        try {
            BidIncrementRule rule = getApplicableRule(currentPrice, configId);
            if (rule != null) {
                return currentPrice.add(rule.getIncrementAmount());
            }

            // 如果没有找到适用的规则，返回当前价格+1（兜底逻辑）
            log.warn("未找到适用的加价规则，使用兜底逻辑: currentPrice={}, configId={}", currentPrice, configId);
            return currentPrice.add(BigDecimal.ONE);

        } catch (Exception e) {
            log.error("获取下一次最低出价金额失败: currentPrice={}, configId={}", currentPrice, configId, e);
            return currentPrice.add(BigDecimal.ONE);
        }
    }

    @Override
    public boolean validateBidAmount(BigDecimal currentPrice, BigDecimal bidAmount, Long configId) {
        try {
            if (configId == null) {
                // 如果没有配置加价阶梯，默认校验通过
                log.debug("无加价阶梯配置，默认校验通过: currentPrice={}, bidAmount={}", currentPrice, bidAmount);
                return true;
            }

            BidIncrementRule rule = getApplicableRule(currentPrice, configId);
            if (rule == null) {
                // 如果没有找到适用的规则，默认校验通过
                log.warn("未找到适用的加价规则，默认校验通过: currentPrice={}, bidAmount={}", currentPrice, bidAmount);
                return true;
            }

            // 出价必须是当前价格 + 加价金额的倍数
            BigDecimal minBidAmount = currentPrice.add(rule.getIncrementAmount());
            boolean isValid = bidAmount.compareTo(minBidAmount) >= 0;

            log.debug("加价阶梯校验: currentPrice={}, bidAmount={}, minBidAmount={}, isValid={}",
                     currentPrice, bidAmount, minBidAmount, isValid);

            return isValid;

        } catch (Exception e) {
            log.error("校验出价金额失败: currentPrice={}, bidAmount={}, configId={}",
                     currentPrice, bidAmount, configId, e);
            // 出错时默认校验通过，避免影响正常业务
            return true;
        }
    }

    @Override
    public BidIncrementRule getApplicableRule(BigDecimal amount, Long configId) {
        try {
            return ruleMapper.selectByAmountAndConfigId(amount, configId);
        } catch (Exception e) {
            log.error("获取适用的加价规则失败: amount={}, configId={}", amount, configId, e);
            return null;
        }
    }

    @Override
    public List<BidIncrementRule> getRulesByConfigId(Long configId) {
        try {
            return ruleMapper.selectListByConfigId(configId);
        } catch (Exception e) {
            log.error("获取规则列表失败: configId={}", configId, e);
            return null;
        }
    }

    @Override
    public boolean canModifyConfigForSession(Long configId, Long sessionId) {
        try {
            // 查询使用该配置的所有拍卖会
            List<AuctionSession> sessions = auctionSessionMapper.selectSessionsByBidIncrementConfigId(configId);

            if (sessions == null || sessions.isEmpty()) {
                // 没有拍卖会使用该配置，可以修改
                return true;
            }

            for (AuctionSession session : sessions) {
                // 如果指定了特定的拍卖会ID，只检查这个拍卖会
                if (sessionId != null && !sessionId.equals(session.getId())) {
                    continue;
                }

                // 检查拍卖会是否已开始（进行中状态）
                if (isSessionStarted(session)) {
                    log.warn("拍卖会已开始，无法修改加价阶梯配置: sessionId={}, sessionName={}",
                            session.getId(), session.getSessionName());
                    return false;
                }
            }

            // 所有相关拍卖会都未开始，可以修改
            return true;

        } catch (Exception e) {
            log.error("校验加价阶梯配置修改权限失败: configId={}", configId, e);
            // 出错时保守处理，不允许修改
            return false;
        }
    }

    /**
     * 检查拍卖会是否已开始（进行中状态）
     *
     * @param session 拍卖会对象
     * @return 是否已开始
     */
    private boolean isSessionStarted(AuctionSession session) {
        try {
            if (session == null) {
                return false;
            }

            // 动态纠正状态
            recalculateSessionStatus(session);

            // 检查是否为进行中状态（2）
            return session.getStatus() != null && session.getStatus() == 2;

        } catch (Exception e) {
            log.error("检查拍卖会状态失败: sessionId={}, 错误: {}", session.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据当前时间动态纠正拍卖会状态：
     * 1-待开始；2-进行中；3-已结束（4-已取消维持不变）
     */
    private void recalculateSessionStatus(AuctionSession session) {
        try {
            if (session == null) return;
            Integer current = session.getStatus();
            if (current != null && current == 4) {
                return; // 已取消保持不变
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = session.getStartTime();
            LocalDateTime end = session.getEndTime();
            if (start == null || end == null) return;
            int newStatus;
            if (now.isBefore(start)) {
                newStatus = 1; // 待开始
            } else if (!now.isAfter(end)) {
                newStatus = 2; // 进行中
            } else {
                newStatus = 3; // 已结束
            }
            session.setStatus(newStatus);
        } catch (Exception e) {
            log.debug("动态计算拍卖会状态失败: {}", e.getMessage());
        }
    }
}
