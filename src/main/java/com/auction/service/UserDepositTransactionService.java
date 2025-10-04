package com.auction.service;

import com.auction.entity.UserDepositTransaction;
import com.auction.mapper.UserDepositTransactionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 保证金交易流水服务类
 * 提供严谨的交易流水管理功能
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class UserDepositTransactionService {

    @Autowired
    private UserDepositTransactionMapper userDepositTransactionMapper;

    /**
     * 创建交易流水
     */
    @Transactional
    public Long createTransaction(Long accountId, Long userId, Integer transactionType, 
                                BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter,
                                Long relatedId, String relatedType, String description) {
        try {
            // 生成交易流水号
            String transactionNo = generateTransactionNo();

            // 创建交易流水
            UserDepositTransaction transaction = new UserDepositTransaction();
            transaction.setAccountId(accountId);
            transaction.setUserId(userId);
            transaction.setTransactionNo(transactionNo);
            transaction.setTransactionType(transactionType);
            transaction.setAmount(amount);
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(balanceAfter);
            transaction.setRelatedId(relatedId);
            transaction.setRelatedType(relatedType);
            transaction.setDescription(description);
            
            // 根据交易类型设置状态：充值(1)和提现(6)需要审核，其他类型直接成功
            if (transactionType == 1 || transactionType == 6) {
                transaction.setStatus(0); // 待审核
            } else {
                transaction.setStatus(1); // 成功
            }
            
            transaction.setCreateTime(LocalDateTime.now());
            transaction.setUpdateTime(LocalDateTime.now());
            transaction.setDeleted(0);

            userDepositTransactionMapper.insert(transaction);

            log.info("交易流水创建成功: 流水号={}, 用户ID={}, 类型={}, 金额={}", 
                transactionNo, userId, transactionType, amount);
            return transaction.getId();

        } catch (Exception e) {
            log.error("交易流水创建失败: 用户ID={}, 类型={}, 金额={}, 错误: {}", 
                userId, transactionType, amount, e.getMessage(), e);
            throw new RuntimeException("交易流水创建失败: " + e.getMessage());
        }
    }

    /**
     * 查询交易流水列表
     */
    public List<UserDepositTransaction> getTransactionList(UserDepositTransaction transaction) {
        try {
            return userDepositTransactionMapper.selectList(transaction);
        } catch (Exception e) {
            log.error("查询交易流水列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据用户ID查询交易流水
     */
    public List<UserDepositTransaction> getTransactionsByUserId(Long userId) {
        try {
            return userDepositTransactionMapper.selectByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户交易流水失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据账户ID查询交易流水
     */
    public List<UserDepositTransaction> getTransactionsByAccountId(Long accountId) {
        try {
            return userDepositTransactionMapper.selectByAccountId(accountId);
        } catch (Exception e) {
            log.error("查询账户交易流水失败: 账户ID={}, 错误: {}", accountId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据关联ID查询交易流水
     */
    public List<UserDepositTransaction> getTransactionsByRelatedId(Long relatedId, String relatedType) {
        try {
            return userDepositTransactionMapper.selectByRelatedId(relatedId, relatedType);
        } catch (Exception e) {
            log.error("查询关联交易流水失败: 关联ID={}, 类型={}, 错误: {}", 
                relatedId, relatedType, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据交易流水号查询
     */
    public UserDepositTransaction getTransactionByNo(String transactionNo) {
        try {
            return userDepositTransactionMapper.selectByTransactionNo(transactionNo);
        } catch (Exception e) {
            log.error("查询交易流水失败: 流水号={}, 错误: {}", transactionNo, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据ID查询交易
     */
    public UserDepositTransaction getById(Long id) {
        try {
            return userDepositTransactionMapper.selectById(id);
        } catch (Exception e) {
            log.error("查询交易失败: id={}, 错误: {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 更新交易状态（审核用）
     */
    @Transactional
    public boolean updateTransactionStatus(Long transactionId, Integer status, BigDecimal balanceAfter, 
                                          Long reviewerId, String reviewRemark) {
        try {
            UserDepositTransaction transaction = userDepositTransactionMapper.selectById(transactionId);
            if (transaction == null) {
                throw new RuntimeException("交易记录不存在");
            }

            transaction.setStatus(status);
            transaction.setBalanceAfter(balanceAfter);
            transaction.setReviewerId(reviewerId);
            transaction.setReviewTime(LocalDateTime.now());
            transaction.setReviewRemark(reviewRemark);
            transaction.setUpdateTime(LocalDateTime.now());

            int result = userDepositTransactionMapper.update(transaction);
            if (result > 0) {
                log.info("交易状态更新成功: transactionId={}, status={}", transactionId, status);
                return true;
            }
            return false;

        } catch (Exception e) {
            log.error("更新交易状态失败: transactionId={}, error={}", transactionId, e.getMessage(), e);
            throw new RuntimeException("更新交易状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取待审核的交易列表（管理员用）
     */
    public List<UserDepositTransaction> getPendingTransactions(Integer transactionType) {
        try {
            return userDepositTransactionMapper.selectPendingTransactions(transactionType);
        } catch (Exception e) {
            log.error("获取待审核交易列表失败: type={}, 错误: {}", transactionType, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询用户在特定拍品上的冻结保证金总额
     * @param userId 用户ID
     * @param itemId 拍品ID
     * @return 冻结保证金总额（元）
     */
    public BigDecimal getFrozenAmountByUserAndItem(Long userId, Long itemId) {
        try {
            UserDepositTransaction query = new UserDepositTransaction();
            query.setUserId(userId);
            query.setRelatedId(itemId);
            query.setRelatedType("item");
            query.setTransactionType(3); // 冻结类型
            query.setStatus(1); // 成功状态
            
            List<UserDepositTransaction> transactions = userDepositTransactionMapper.selectList(query);
            
            BigDecimal totalFrozen = BigDecimal.ZERO;
            for (UserDepositTransaction transaction : transactions) {
                if (transaction.getAmount() != null) {
                    totalFrozen = totalFrozen.add(transaction.getAmount());
                }
            }
            
            log.info("查询用户拍品冻结保证金: userId={}, itemId={}, totalFrozen={}", userId, itemId, totalFrozen);
            return totalFrozen;
            
        } catch (Exception e) {
            log.error("查询用户拍品冻结保证金失败: userId={}, itemId={}, error={}", userId, itemId, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 生成交易流水号
     */
    private String generateTransactionNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "TXN" + timestamp + random;
    }
}
