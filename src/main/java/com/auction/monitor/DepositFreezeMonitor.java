package com.auction.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 保证金冻结监控组件
 * 用于监控和告警保证金冻结异常情况
 */
@Slf4j
@Component
public class DepositFreezeMonitor {

    // 监控统计
    private final ConcurrentHashMap<String, AtomicLong> freezeStats = new ConcurrentHashMap<>();
    private final AtomicLong totalFreezeCount = new AtomicLong(0);
    private final AtomicLong zeroFreezeCount = new AtomicLong(0);

    /**
     * 记录保证金冻结操作
     * 
     * @param userId 用户ID
     * @param itemId 拍品ID
     * @param sessionId 拍卖会ID
     * @param bidAmount 出价金额（分）
     * @param freezeAmount 冻结金额（元）
     * @param oldRequiredCents 历史需保证金（分）
     * @param newRequiredCents 新需保证金（分）
     */
    public void recordFreezeOperation(Long userId, Long itemId, Long sessionId, 
                                    BigDecimal bidAmount, BigDecimal freezeAmount,
                                    BigDecimal oldRequiredDeposit, BigDecimal newRequiredDeposit) {
        
        String key = String.format("user_%d_item_%d_session_%d", userId, itemId, sessionId);
        freezeStats.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        totalFreezeCount.incrementAndGet();
        
        // 记录详细日志
        log.info("保证金冻结监控: 用户ID={}, 拍品ID={}, 拍卖会ID={}, 出价={}元, 冻结={}元, 历史需={}元, 新需={}元", 
            userId, itemId, sessionId, bidAmount, freezeAmount, oldRequiredDeposit, newRequiredDeposit);
        
        // 检查异常情况
        checkAnomalies(userId, itemId, sessionId, bidAmount, freezeAmount, oldRequiredDeposit, newRequiredDeposit);
    }

    /**
     * 检查异常情况
     */
    private void checkAnomalies(Long userId, Long itemId, Long sessionId, 
                              BigDecimal bidAmount, BigDecimal freezeAmount,
                              BigDecimal oldRequiredDeposit, BigDecimal newRequiredDeposit) {
        
        // 1. 检查冻结金额为0的情况
        if (freezeAmount.compareTo(BigDecimal.ZERO) == 0) {
            zeroFreezeCount.incrementAndGet();
            
            // 如果新出价大于历史最高出价但冻结金额为0，这是异常情况
            if (newRequiredDeposit.compareTo(oldRequiredDeposit) > 0) {
                log.error("【告警】保证金冻结异常: 新出价大于历史最高出价但冻结金额为0! " +
                    "用户ID={}, 拍品ID={}, 拍卖会ID={}, 出价={}元, 历史需={}元, 新需={}元", 
                    userId, itemId, sessionId, bidAmount, oldRequiredDeposit, newRequiredDeposit);
                
                // 这里可以添加告警通知逻辑，如发送邮件、短信等
                sendAlert("保证金冻结异常", String.format(
                    "用户ID=%d, 拍品ID=%d, 拍卖会ID=%d, 出价=%s元, 历史需=%s元, 新需=%s元", 
                    userId, itemId, sessionId, bidAmount, oldRequiredDeposit, newRequiredDeposit));
            } else {
                log.info("正常情况: 出价未超过历史最高，无需冻结保证金");
            }
        }
        
        // 2. 检查冻结金额异常大的情况
        BigDecimal expectedMaxFreeze = bidAmount.multiply(new BigDecimal("0.1")); // 假设最大10%保证金
        if (freezeAmount.compareTo(expectedMaxFreeze) > 0) {
            log.warn("【警告】保证金冻结金额异常大: 用户ID={}, 拍品ID={}, 冻结={}元, 出价={}元", 
                userId, itemId, freezeAmount, bidAmount);
        }
        
        // 3. 检查用户出价频率异常
        String userKey = "user_" + userId;
        long userBidCount = freezeStats.getOrDefault(userKey, new AtomicLong(0)).get();
        if (userBidCount > 100) { // 假设单用户出价超过100次为异常
            log.warn("【警告】用户出价频率异常: 用户ID={}, 出价次数={}", userId, userBidCount);
        }
    }

    /**
     * 发送告警通知
     */
    private void sendAlert(String title, String message) {
        // 这里可以实现具体的告警逻辑，如：
        // 1. 发送邮件
        // 2. 发送短信
        // 3. 推送到监控系统
        // 4. 记录到告警表
        
        log.error("【告警】{}: {}", title, message);
        
        // 示例：记录到告警表（需要实现告警服务）
        // alertService.recordAlert("DEPOSIT_FREEZE_ANOMALY", title, message);
    }

    /**
     * 获取监控统计信息
     */
    public String getMonitorStats() {
        long totalFreezes = totalFreezeCount.get();
        long zeroFreezes = zeroFreezeCount.get();
        double zeroFreezeRate = totalFreezes > 0 ? (double) zeroFreezes / totalFreezes * 100 : 0;
        
        return String.format("保证金冻结监控统计: 总冻结次数=%d, 零冻结次数=%d, 零冻结率=%.2f%%", 
            totalFreezes, zeroFreezes, zeroFreezeRate);
    }

    /**
     * 重置监控统计
     */
    public void resetStats() {
        freezeStats.clear();
        totalFreezeCount.set(0);
        zeroFreezeCount.set(0);
        log.info("保证金冻结监控统计已重置");
    }
}
