package com.auction.service;

import com.auction.entity.UserDepositAccount;
import com.auction.entity.UserDepositRefund;
import com.auction.mapper.UserDepositRefundMapper;
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
 * 保证金退款申请服务类
 * 提供严谨的退款申请管理功能
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class UserDepositRefundService {

    @Autowired
    private UserDepositRefundMapper userDepositRefundMapper;

    @Autowired
    private UserDepositAccountService userDepositAccountService;

    @Autowired
    private UserDepositTransactionService userDepositTransactionService;

    /**
     * 创建退款申请
     */
    @Transactional
    public Long createRefundApplication(Long userId, BigDecimal refundAmount, String reason) {
        try {
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("退款金额必须大于0");
            }

            // 获取用户保证金账户
            UserDepositAccount account = userDepositAccountService.getOrCreateAccount(userId);
            if (account == null) {
                throw new RuntimeException("用户保证金账户不存在");
            }

            // 检查可用金额是否足够
            if (account.getAvailableAmount().compareTo(refundAmount) < 0) {
                throw new RuntimeException("可用保证金不足");
            }

            // 生成退款申请单号
            String refundNo = generateRefundNo();

            // 创建退款申请
            UserDepositRefund refund = new UserDepositRefund();
            refund.setAccountId(account.getId());
            refund.setUserId(userId);
            refund.setRefundNo(refundNo);
            refund.setRefundAmount(refundAmount);
            refund.setAvailableAmount(account.getAvailableAmount());
            refund.setReason(reason);
            refund.setStatus(1); // 待审核
            refund.setCreateTime(LocalDateTime.now());
            refund.setUpdateTime(LocalDateTime.now());
            refund.setDeleted(0);

            userDepositRefundMapper.insert(refund);

            log.info("退款申请创建成功: 申请单号={}, 用户ID={}, 金额={}", 
                refundNo, userId, refundAmount);
            return refund.getId();

        } catch (Exception e) {
            log.error("退款申请创建失败: 用户ID={}, 金额={}, 错误: {}", 
                userId, refundAmount, e.getMessage(), e);
            throw new RuntimeException("退款申请创建失败: " + e.getMessage());
        }
    }

    /**
     * 审核退款申请
     */
    @Transactional
    public boolean auditRefundApplication(Long refundId, Long auditorId, Integer status, String auditComment) {
        try {
            // 查询退款申请
            UserDepositRefund refund = userDepositRefundMapper.selectById(refundId);
            if (refund == null) {
                throw new RuntimeException("退款申请不存在");
            }

            if (refund.getStatus() != 1) {
                throw new RuntimeException("退款申请状态不正确");
            }

            // 更新审核信息
            refund.setStatus(status);
            refund.setAuditorId(auditorId);
            refund.setAuditTime(LocalDateTime.now());
            refund.setAuditComment(auditComment);
            refund.setUpdateTime(LocalDateTime.now());

            userDepositRefundMapper.update(refund);

            // 如果审核通过，执行退款
            if (status == 2) { // 审核通过
                executeRefund(refundId);
            }

            log.info("退款申请审核完成: 申请ID={}, 审核结果={}", refundId, status);
            return true;

        } catch (Exception e) {
            log.error("退款申请审核失败: 申请ID={}, 错误: {}", refundId, e.getMessage(), e);
            throw new RuntimeException("退款申请审核失败: " + e.getMessage());
        }
    }

    /**
     * 执行退款
     */
    @Transactional
    public boolean executeRefund(Long refundId) {
        try {
            // 查询退款申请
            UserDepositRefund refund = userDepositRefundMapper.selectById(refundId);
            if (refund == null) {
                throw new RuntimeException("退款申请不存在");
            }

            if (refund.getStatus() != 2) {
                throw new RuntimeException("退款申请未通过审核");
            }

            // 执行退款操作
            boolean success = userDepositAccountService.refundAmount(
                refund.getUserId(),
                refund.getRefundAmount(),
                refundId,
                "refund",
                "保证金退款"
            );

            if (success) {
                // 更新退款申请状态
                refund.setStatus(4); // 退款成功
                refund.setRefundTime(LocalDateTime.now());
                refund.setUpdateTime(LocalDateTime.now());

                userDepositRefundMapper.update(refund);

                log.info("退款执行成功: 申请ID={}, 金额={}", refundId, refund.getRefundAmount());
                return true;
            } else {
                // 更新退款申请状态为失败
                refund.setStatus(5); // 退款失败
                refund.setUpdateTime(LocalDateTime.now());

                userDepositRefundMapper.update(refund);

                log.warn("退款执行失败: 申请ID={}, 金额={}", refundId, refund.getRefundAmount());
                return false;
            }

        } catch (Exception e) {
            log.error("退款执行失败: 申请ID={}, 错误: {}", refundId, e.getMessage(), e);
            throw new RuntimeException("退款执行失败: " + e.getMessage());
        }
    }

    /**
     * 查询退款申请列表
     */
    public List<UserDepositRefund> getRefundList(UserDepositRefund refund) {
        try {
            return userDepositRefundMapper.selectList(refund);
        } catch (Exception e) {
            log.error("查询退款申请列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据用户ID查询退款申请
     */
    public List<UserDepositRefund> getRefundsByUserId(Long userId) {
        try {
            return userDepositRefundMapper.selectByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户退款申请失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据状态查询退款申请
     */
    public List<UserDepositRefund> getRefundsByStatus(Integer status) {
        try {
            return userDepositRefundMapper.selectByStatus(status);
        } catch (Exception e) {
            log.error("查询退款申请失败: 状态={}, 错误: {}", status, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据退款申请单号查询
     */
    public UserDepositRefund getRefundByNo(String refundNo) {
        try {
            return userDepositRefundMapper.selectByRefundNo(refundNo);
        } catch (Exception e) {
            log.error("查询退款申请失败: 申请单号={}, 错误: {}", refundNo, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成退款申请单号
     */
    private String generateRefundNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "REF" + timestamp + random;
    }
}
