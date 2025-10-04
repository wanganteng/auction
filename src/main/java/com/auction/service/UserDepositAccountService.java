package com.auction.service;

import com.auction.entity.UserDepositAccount;
import com.auction.entity.UserDepositTransaction;
import com.auction.mapper.UserDepositAccountMapper;
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
 * 用户保证金账户服务类
 * 提供严谨的保证金账户管理功能
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class UserDepositAccountService {

    @Autowired
    private UserDepositAccountMapper userDepositAccountMapper;

    @Autowired
    private UserDepositTransactionService userDepositTransactionService;

    /**
     * 创建用户保证金账户
     */
    @Transactional
    public Long createAccount(Long userId) {
        try {
            // 检查是否已存在账户
            UserDepositAccount existingAccount = userDepositAccountMapper.selectByUserId(userId);
            if (existingAccount != null) {
                throw new RuntimeException("用户保证金账户已存在");
            }

            // 创建新账户
            UserDepositAccount account = new UserDepositAccount();
            account.setUserId(userId);
            account.setTotalAmount(BigDecimal.ZERO);
            account.setAvailableAmount(BigDecimal.ZERO);
            account.setFrozenAmount(BigDecimal.ZERO);
            account.setRefundedAmount(BigDecimal.ZERO);
            account.setStatus(1); // 正常状态
            account.setCreateTime(LocalDateTime.now());
            account.setUpdateTime(LocalDateTime.now());
            account.setDeleted(0);

            userDepositAccountMapper.insert(account);

            log.info("用户保证金账户创建成功: 用户ID={}, 账户ID={}", userId, account.getId());
            return account.getId();

        } catch (Exception e) {
            log.error("用户保证金账户创建失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("用户保证金账户创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取或创建用户保证金账户
     */
    @Transactional
    public UserDepositAccount getOrCreateAccount(Long userId) {
        try {
            UserDepositAccount account = userDepositAccountMapper.selectByUserId(userId);
            if (account == null) {
                Long accountId = createAccount(userId);
                account = userDepositAccountMapper.selectById(accountId);
            }
            return account;

        } catch (Exception e) {
            log.error("获取用户保证金账户失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("获取用户保证金账户失败: " + e.getMessage());
        }
    }

    /**
     * 充值保证金（提交审核申请）
     */
    @Transactional
    public boolean recharge(Long userId, BigDecimal amount, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("充值金额必须大于0");
            }

            // 获取或创建账户
            UserDepositAccount account = getOrCreateAccount(userId);

            // 创建待审核的充值交易记录（不立即变更余额）
            userDepositTransactionService.createTransaction(
                account.getId(),
                userId,
                1, // 充值
                amount,
                account.getAvailableAmount(),
                account.getAvailableAmount(), // 待审核时余额不变
                null,
                null,
                description != null ? description : "保证金充值申请"
            );

            log.info("保证金充值申请已提交，等待审核: 用户ID={}, 金额={}", userId, amount);
            return true;

        } catch (Exception e) {
            log.error("保证金充值申请失败: 用户ID={}, 金额={}, 错误: {}", userId, amount, e.getMessage(), e);
            throw new RuntimeException("保证金充值申请失败: " + e.getMessage());
        }
    }

    /**
     * 冻结保证金
     */
    @Transactional
    public boolean freezeAmount(Long userId, BigDecimal amount, Long relatedId, String relatedType, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("冻结金额必须大于0");
            }

            // 获取账户
            UserDepositAccount account = getOrCreateAccount(userId);

            // 检查可用余额是否足够
            if (account.getAvailableAmount().compareTo(amount) < 0) {
                throw new RuntimeException("可用保证金不足");
            }

            // 计算新余额
            BigDecimal newAvailableAmount = account.getAvailableAmount().subtract(amount);
            BigDecimal newFrozenAmount = account.getFrozenAmount().add(amount);

            // 更新账户余额
            userDepositAccountMapper.updateAmount(
                account.getId(),
                account.getTotalAmount(),
                newAvailableAmount,
                newFrozenAmount,
                account.getRefundedAmount()
            );

            // 记录交易流水
            userDepositTransactionService.createTransaction(
                account.getId(),
                userId,
                3, // 冻结
                amount,
                account.getAvailableAmount(),
                newAvailableAmount,
                relatedId,
                relatedType,
                description != null ? description : "保证金冻结"
            );

            log.info("保证金冻结成功: 用户ID={}, 金额={}, 关联ID={}", userId, amount, relatedId);
            return true;

        } catch (Exception e) {
            log.error("保证金冻结失败: 用户ID={}, 金额={}, 错误: {}", userId, amount, e.getMessage(), e);
            throw new RuntimeException("保证金冻结失败: " + e.getMessage());
        }
    }

    /**
     * 解冻保证金
     */
    @Transactional
    public boolean unfreezeAmount(Long userId, BigDecimal amount, Long relatedId, String relatedType, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("解冻金额必须大于0");
            }

            // 获取账户
            UserDepositAccount account = getOrCreateAccount(userId);

            // 检查冻结余额是否足够
            if (account.getFrozenAmount().compareTo(amount) < 0) {
                throw new RuntimeException("冻结保证金不足");
            }

            // 计算新余额
            BigDecimal newAvailableAmount = account.getAvailableAmount().add(amount);
            BigDecimal newFrozenAmount = account.getFrozenAmount().subtract(amount);

            // 更新账户余额
            userDepositAccountMapper.updateAmount(
                account.getId(),
                account.getTotalAmount(),
                newAvailableAmount,
                newFrozenAmount,
                account.getRefundedAmount()
            );

            // 记录交易流水
            userDepositTransactionService.createTransaction(
                account.getId(),
                userId,
                4, // 解冻
                amount,
                account.getAvailableAmount(),
                newAvailableAmount,
                relatedId,
                relatedType,
                description != null ? description : "保证金解冻"
            );

            log.info("保证金解冻成功: 用户ID={}, 金额={}, 关联ID={}", userId, amount, relatedId);
            return true;

        } catch (Exception e) {
            log.error("保证金解冻失败: 用户ID={}, 金额={}, 错误: {}", userId, amount, e.getMessage(), e);
            throw new RuntimeException("保证金解冻失败: " + e.getMessage());
        }
    }

    /**
     * 从可用余额中扣除金额（用于支付尾款等）
     */
    @Transactional
    public boolean deductFromAvailable(Long userId, BigDecimal amount, Long relatedId, String relatedType, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("扣除金额必须大于0");
            }

            // 获取账户
            UserDepositAccount account = getOrCreateAccount(userId);

            // 检查可用余额是否足够
            if (account.getAvailableAmount().compareTo(amount) < 0) {
                throw new RuntimeException("可用余额不足");
            }

            // 计算新余额
            BigDecimal newTotalAmount = account.getTotalAmount().subtract(amount);
            BigDecimal newAvailableAmount = account.getAvailableAmount().subtract(amount);

            // 更新账户余额
            userDepositAccountMapper.updateAmount(
                account.getId(),
                newTotalAmount,
                newAvailableAmount,
                account.getFrozenAmount(),
                account.getRefundedAmount()
            );

            // 记录交易流水（类型7-支付）
            userDepositTransactionService.createTransaction(
                account.getId(),
                userId,
                7, // 支付
                amount,
                account.getAvailableAmount(),
                newAvailableAmount,
                relatedId,
                relatedType,
                description != null ? description : "余额支付"
            );

            log.info("从可用余额扣除成功: 用户ID={}, 金额={}, 关联ID={}", userId, amount, relatedId);
            return true;

        } catch (Exception e) {
            log.error("从可用余额扣除失败: 用户ID={}, 金额={}, 错误: {}", userId, amount, e.getMessage(), e);
            throw new RuntimeException("从可用余额扣除失败: " + e.getMessage());
        }
    }

    /**
     * 从冻结金额中扣除保证金（用于订单支付、违约扣除等）
     */
    @Transactional
    public boolean deductAmount(Long userId, BigDecimal amount, Long relatedId, String relatedType, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("扣除金额必须大于0");
            }

            // 获取账户
            UserDepositAccount account = getOrCreateAccount(userId);

            // 检查冻结余额是否足够
            if (account.getFrozenAmount().compareTo(amount) < 0) {
                throw new RuntimeException("冻结保证金不足");
            }

            // 计算新余额
            BigDecimal newTotalAmount = account.getTotalAmount().subtract(amount);
            BigDecimal newFrozenAmount = account.getFrozenAmount().subtract(amount);

            // 更新账户余额
            userDepositAccountMapper.updateAmount(
                account.getId(),
                newTotalAmount,
                account.getAvailableAmount(),
                newFrozenAmount,
                account.getRefundedAmount()
            );

            // 记录交易流水（类型5-扣除）
            userDepositTransactionService.createTransaction(
                account.getId(),
                userId,
                5, // 扣除
                amount,
                account.getFrozenAmount(),
                newFrozenAmount,
                relatedId,
                relatedType,
                description != null ? description : "保证金扣除"
            );

            log.info("保证金扣除成功: 用户ID={}, 金额={}, 关联ID={}", userId, amount, relatedId);
            return true;

        } catch (Exception e) {
            log.error("保证金扣除失败: 用户ID={}, 金额={}, 错误: {}", userId, amount, e.getMessage(), e);
            throw new RuntimeException("保证金扣除失败: " + e.getMessage());
        }
    }

    /**
     * 提现保证金（提交审核申请）
     */
    @Transactional
    public boolean withdraw(Long userId, BigDecimal amount, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("提现金额必须大于0");
            }

            // 获取账户
            UserDepositAccount account = getOrCreateAccount(userId);

            // 检查可用余额是否足够
            if (account.getAvailableAmount().compareTo(amount) < 0) {
                throw new RuntimeException("可用保证金不足");
            }

            // 创建待审核的提现交易记录（不立即变更余额）
            userDepositTransactionService.createTransaction(
                account.getId(),
                userId,
                2, // 提现
                amount,
                account.getAvailableAmount(),
                account.getAvailableAmount(), // 待审核时余额不变
                null,
                null,
                description != null ? description : "保证金提现申请"
            );

            log.info("保证金提现申请已提交，等待审核: 用户ID={}, 金额={}", userId, amount);
            return true;

        } catch (Exception e) {
            log.error("保证金提现申请失败: 用户ID={}, 金额={}, 错误: {}", userId, amount, e.getMessage(), e);
            throw new RuntimeException("保证金提现申请失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过充值申请（管理员操作）
     */
    @Transactional
    public boolean approveRecharge(Long transactionId, Long reviewerId, String remark) {
        try {
            UserDepositTransaction transaction = userDepositTransactionService.getById(transactionId);
            if (transaction == null) {
                throw new RuntimeException("交易记录不存在");
            }
            
            if (transaction.getStatus() != 0) {
                throw new RuntimeException("该交易已审核，无法重复审核");
            }
            
            if (transaction.getTransactionType() != 1) {
                throw new RuntimeException("该交易不是充值申请");
            }

            // 获取账户
            UserDepositAccount account = getOrCreateAccount(transaction.getUserId());

            // 计算新余额
            BigDecimal newTotalAmount = account.getTotalAmount().add(transaction.getAmount());
            BigDecimal newAvailableAmount = account.getAvailableAmount().add(transaction.getAmount());

            // 更新账户余额
            userDepositAccountMapper.updateAmount(
                account.getId(),
                newTotalAmount,
                newAvailableAmount,
                account.getFrozenAmount(),
                account.getRefundedAmount()
            );

            // 更新交易状态为成功
            userDepositTransactionService.updateTransactionStatus(
                transactionId, 
                1, // 成功
                newAvailableAmount,
                reviewerId,
                remark
            );

            log.info("充值审核通过: transactionId={}, userId={}, amount={}", 
                transactionId, transaction.getUserId(), transaction.getAmount());
            return true;

        } catch (Exception e) {
            log.error("充值审核失败: transactionId={}, error={}", transactionId, e.getMessage(), e);
            throw new RuntimeException("充值审核失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过提现申请（管理员操作）
     */
    @Transactional
    public boolean approveWithdraw(Long transactionId, Long reviewerId, String remark) {
        try {
            UserDepositTransaction transaction = userDepositTransactionService.getById(transactionId);
            if (transaction == null) {
                throw new RuntimeException("交易记录不存在");
            }
            
            if (transaction.getStatus() != 0) {
                throw new RuntimeException("该交易已审核，无法重复审核");
            }
            
            if (transaction.getTransactionType() != 6) {
                throw new RuntimeException("该交易不是提现申请");
            }

            // 获取账户
            UserDepositAccount account = getOrCreateAccount(transaction.getUserId());

            // 再次检查余额是否足够（防止期间余额变化）
            if (account.getAvailableAmount().compareTo(transaction.getAmount()) < 0) {
                throw new RuntimeException("用户可用余额不足");
            }

            // 计算新余额
            BigDecimal newTotalAmount = account.getTotalAmount().subtract(transaction.getAmount());
            BigDecimal newAvailableAmount = account.getAvailableAmount().subtract(transaction.getAmount());

            // 更新账户余额
            userDepositAccountMapper.updateAmount(
                account.getId(),
                newTotalAmount,
                newAvailableAmount,
                account.getFrozenAmount(),
                account.getRefundedAmount()
            );

            // 更新交易状态为成功
            userDepositTransactionService.updateTransactionStatus(
                transactionId, 
                1, // 成功
                newAvailableAmount,
                reviewerId,
                remark
            );

            log.info("提现审核通过: transactionId={}, userId={}, amount={}", 
                transactionId, transaction.getUserId(), transaction.getAmount());
            return true;

        } catch (Exception e) {
            log.error("提现审核失败: transactionId={}, error={}", transactionId, e.getMessage(), e);
            throw new RuntimeException("提现审核失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝充值/提现申请（管理员操作）
     */
    @Transactional
    public boolean rejectTransaction(Long transactionId, Long reviewerId, String remark) {
        try {
            UserDepositTransaction transaction = userDepositTransactionService.getById(transactionId);
            if (transaction == null) {
                throw new RuntimeException("交易记录不存在");
            }
            
            if (transaction.getStatus() != 0) {
                throw new RuntimeException("该交易已审核，无法重复审核");
            }

            // 更新交易状态为失败
            userDepositTransactionService.updateTransactionStatus(
                transactionId, 
                2, // 失败
                transaction.getBalanceBefore(), // 余额不变
                reviewerId,
                remark
            );

            log.info("交易审核拒绝: transactionId={}, userId={}", 
                transactionId, transaction.getUserId());
            return true;

        } catch (Exception e) {
            log.error("拒绝交易失败: transactionId={}, error={}", transactionId, e.getMessage(), e);
            throw new RuntimeException("拒绝交易失败: " + e.getMessage());
        }
    }

    /**
     * 退还保证金
     */
    @Transactional
    public boolean refundAmount(Long userId, BigDecimal amount, Long relatedId, String relatedType, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("退还金额必须大于0");
            }

            // 获取账户
            UserDepositAccount account = getOrCreateAccount(userId);

            // 计算新余额
            BigDecimal newRefundedAmount = account.getRefundedAmount().add(amount);

            // 更新账户余额
            userDepositAccountMapper.updateAmount(
                account.getId(),
                account.getTotalAmount(),
                account.getAvailableAmount(),
                account.getFrozenAmount(),
                newRefundedAmount
            );

            // 记录交易流水
            userDepositTransactionService.createTransaction(
                account.getId(),
                userId,
                6, // 退还
                amount,
                account.getRefundedAmount(),
                newRefundedAmount,
                relatedId,
                relatedType,
                description != null ? description : "保证金退还"
            );

            log.info("保证金退还成功: 用户ID={}, 金额={}, 关联ID={}", userId, amount, relatedId);
            return true;

        } catch (Exception e) {
            log.error("保证金退还失败: 用户ID={}, 金额={}, 错误: {}", userId, amount, e.getMessage(), e);
            throw new RuntimeException("保证金退还失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户保证金余额是否足够
     * 
     * @param userId 用户ID
     * @param amount 需要的金额（分）
     * @return 是否足够
     */
    public boolean hasEnoughBalance(Long userId, Long amount) {
        try {
            UserDepositAccount account = getAccountByUserId(userId);
            if (account == null) {
                return false;
            }
            
            // 将分转换为元
            BigDecimal amountYuan = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100));
            
            // 检查可用余额是否足够
            return account.getAvailableAmount().compareTo(amountYuan) >= 0;
        } catch (Exception e) {
            log.error("检查用户保证金余额失败: 用户ID={}, 金额={}, 错误: {}", userId, amount, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 查询用户保证金账户
     */
    public UserDepositAccount getAccountByUserId(Long userId) {
        try {
            return userDepositAccountMapper.selectByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户保证金账户失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查询保证金账户列表
     */
    public List<UserDepositAccount> getAccountList(UserDepositAccount account) {
        try {
            return userDepositAccountMapper.selectList(account);
        } catch (Exception e) {
            log.error("查询保证金账户列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 冻结保证金账户（管理员操作）
     * 
     * @param accountId 账户ID
     * @param reason 冻结原因
     * @return 是否成功
     */
    @Transactional
    public boolean freezeAccount(Long accountId, String reason) {
        try {
            UserDepositAccount account = userDepositAccountMapper.selectById(accountId);
            if (account == null) {
                throw new RuntimeException("保证金账户不存在");
            }
            
            if (account.getStatus() == 2) {
                throw new RuntimeException("该账户已被冻结");
            }
            
            // 更新账户状态为冻结（2）
            int result = userDepositAccountMapper.updateStatus(accountId, 2);
            
            if (result > 0) {
                log.info("保证金账户已冻结: 账户ID={}, 用户ID={}, 原因={}", 
                    accountId, account.getUserId(), reason);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("冻结保证金账户失败: 账户ID={}, 错误: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("冻结账户失败: " + e.getMessage());
        }
    }

    /**
     * 解冻保证金账户（管理员操作）
     * 
     * @param accountId 账户ID
     * @param reason 解冻原因
     * @return 是否成功
     */
    @Transactional
    public boolean unfreezeAccount(Long accountId, String reason) {
        try {
            UserDepositAccount account = userDepositAccountMapper.selectById(accountId);
            if (account == null) {
                throw new RuntimeException("保证金账户不存在");
            }
            
            if (account.getStatus() == 1) {
                throw new RuntimeException("该账户未被冻结");
            }
            
            // 更新账户状态为正常（1）
            int result = userDepositAccountMapper.updateStatus(accountId, 1);
            
            if (result > 0) {
                log.info("保证金账户已解冻: 账户ID={}, 用户ID={}, 原因={}", 
                    accountId, account.getUserId(), reason);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("解冻保证金账户失败: 账户ID={}, 错误: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("解冻账户失败: " + e.getMessage());
        }
    }
}
