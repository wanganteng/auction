package com.auction.controller;

import com.auction.common.Result;
import com.auction.dto.DepositRequest;
import com.auction.dto.WithdrawRequest;
import com.auction.entity.UserDepositAccount;
import com.auction.entity.UserDepositTransaction;
import com.auction.security.CustomUserDetailsService;
import com.auction.service.UserDepositAccountService;
import com.auction.service.UserDepositTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保证金控制器
 * 处理保证金相关的操作
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/deposit")
@Tag(name = "保证金管理", description = "保证金相关接口")
public class DepositController {

    @Autowired
    private UserDepositAccountService depositAccountService;

    @Autowired
    private UserDepositTransactionService transactionService;

    /**
     * 获取保证金账户信息
     * 
     * @return 账户信息
     */
    @GetMapping("/account")
    @Operation(summary = "获取保证金账户信息", description = "获取当前用户的保证金账户信息")
    public Result<UserDepositAccount> getAccount() {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取或创建账户
            UserDepositAccount account = depositAccountService.getOrCreateAccount(userId);
            
            return Result.success("获取成功", account);
            
        } catch (Exception e) {
            log.error("获取保证金账户失败: {}", e.getMessage());
            return Result.error("获取账户信息失败");
        }
    }

    /**
     * 充值保证金
     * 
     * @param request 充值请求
     * @return 充值结果
     */
    @PostMapping("/deposit")
    @Operation(summary = "充值保证金", description = "向保证金账户充值")
    public Result<String> deposit(@Valid @RequestBody DepositRequest request) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 检查账户状态
            UserDepositAccount account = depositAccountService.getAccountByUserId(userId);
            if (account != null && account.getStatus() == 2) {
                return Result.error("您的保证金账户已被冻结，无法进行充值操作，请联系管理员");
            }
            
            // 直接使用元为单位
            BigDecimal amount = BigDecimal.valueOf(request.getAmount());
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("充值金额必须大于0");
            }
            
            String description = "用户充值：" + request.getAmount() + "元";
            if (request.getDescription() != null) {
                description += "，" + request.getDescription();
            }
            
            if (depositAccountService.recharge(userId, amount, description)) {
                return Result.success("充值申请已提交，请等待管理员审核");
            } else {
                return Result.error("充值申请提交失败");
            }
            
        } catch (Exception e) {
            log.error("充值失败: {}", e.getMessage());
            return Result.error("充值失败：" + e.getMessage());
        }
    }

    /**
     * 提现保证金
     * 
     * @param request 提现请求
     * @return 提现结果
     */
    @PostMapping("/withdraw")
    @Operation(summary = "提现保证金", description = "从保证金账户提现")
    public Result<String> withdraw(@Valid @RequestBody WithdrawRequest request) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 检查账户状态
            UserDepositAccount account = depositAccountService.getAccountByUserId(userId);
            if (account != null && account.getStatus() == 2) {
                return Result.error("您的保证金账户已被冻结，无法进行提现操作，请联系管理员");
            }
            
            // 直接使用元为单位
            BigDecimal amount = BigDecimal.valueOf(request.getAmount());
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("提现金额必须大于0");
            }
            
            String description = "用户提现：" + request.getAmount() + "元";
            if (request.getDescription() != null) {
                description += "，" + request.getDescription();
            }
            
            if (depositAccountService.withdraw(userId, amount, description)) {
                return Result.success("提现申请已提交，请等待管理员审核");
            } else {
                return Result.error("提现申请提交失败");
            }
            
        } catch (Exception e) {
            log.error("提现失败: {}", e.getMessage());
            return Result.error("提现失败：" + e.getMessage());
        }
    }

    /**
     * 获取交易流水
     * 
     * @param pageNum 页码（暂时不使用，保留接口兼容性）
     * @param pageSize 页大小（暂时不使用，保留接口兼容性）
     * @param type 交易类型（暂时不使用，保留接口兼容性）
     * @return 交易流水列表
     */
    @GetMapping("/transactions")
    @Operation(summary = "获取交易流水", description = "获取保证金账户的交易流水记录")
    public Result<Map<String, Object>> getTransactions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取所有交易流水
            List<UserDepositTransaction> allTransactions = transactionService.getTransactionsByUserId(userId);
            
            // 如果指定了类型，进行过滤
            List<UserDepositTransaction> filteredTransactions = new ArrayList<>();
            if (type != null) {
                for (UserDepositTransaction transaction : allTransactions) {
                    if (transaction.getTransactionType().equals(type)) {
                        filteredTransactions.add(transaction);
                    }
                }
            } else {
                filteredTransactions = allTransactions;
            }
            
            // 手动分页
            int total = filteredTransactions.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            
            List<UserDepositTransaction> pagedTransactions;
            if (start < total) {
                pagedTransactions = filteredTransactions.subList(start, end);
            } else {
                pagedTransactions = new ArrayList<>();
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", pagedTransactions);
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);
            data.put("total", total);
            
            return Result.success("获取成功", data);
            
        } catch (Exception e) {
            log.error("获取交易流水失败: {}", e.getMessage());
            return Result.error("获取交易流水失败");
        }
    }

    /**
     * 获取交易统计
     * 
     * @return 交易统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取交易统计", description = "获取保证金账户的交易统计信息")
    public Result<Map<String, Object>> getStats() {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<UserDepositTransaction> transactions = transactionService.getTransactionsByUserId(userId);
            
            Map<String, Object> stats = new HashMap<>();
            BigDecimal totalRecharge = BigDecimal.ZERO;      // 总充值
            BigDecimal totalWithdraw = BigDecimal.ZERO;      // 总提现
            BigDecimal totalFrozen = BigDecimal.ZERO;        // 总冻结
            BigDecimal totalUnfrozen = BigDecimal.ZERO;      // 总解冻
            BigDecimal totalDeducted = BigDecimal.ZERO;      // 总扣除
            BigDecimal totalRefunded = BigDecimal.ZERO;      // 总退还
            
            for (UserDepositTransaction transaction : transactions) {
                switch (transaction.getTransactionType()) {
                    case 1: // 充值
                        totalRecharge = totalRecharge.add(transaction.getAmount());
                        break;
                    case 2: // 冻结
                        totalFrozen = totalFrozen.add(transaction.getAmount());
                        break;
                    case 3: // 解冻
                        totalUnfrozen = totalUnfrozen.add(transaction.getAmount());
                        break;
                    case 4: // 扣除
                        totalDeducted = totalDeducted.add(transaction.getAmount());
                        break;
                    case 5: // 退还
                        totalRefunded = totalRefunded.add(transaction.getAmount());
                        break;
                    case 6: // 提现
                        totalWithdraw = totalWithdraw.add(transaction.getAmount());
                        break;
                }
            }
            
            stats.put("totalRecharge", totalRecharge);
            stats.put("totalWithdraw", totalWithdraw);
            stats.put("totalFrozen", totalFrozen);
            stats.put("totalUnfrozen", totalUnfrozen);
            stats.put("totalDeducted", totalDeducted);
            stats.put("totalRefunded", totalRefunded);
            
            return Result.success("获取成功", stats);
            
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage());
            return Result.error("获取统计信息失败");
        }
    }

    /**
     * 检查余额
     * 
     * @param amount 需要金额（元）
     * @return 检查结果
     */
    @GetMapping("/check-balance")
    @Operation(summary = "检查余额", description = "检查保证金账户余额是否足够")
    public Result<Map<String, Object>> checkBalance(@RequestParam Long amount) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 使用元为单位
            BigDecimal amountDecimal = BigDecimal.valueOf(amount);
            
            // 获取账户
            UserDepositAccount account = depositAccountService.getOrCreateAccount(userId);
            
            boolean sufficient = account.getAvailableAmount().compareTo(amountDecimal) >= 0;
            
            Map<String, Object> data = new HashMap<>();
            data.put("sufficient", sufficient);
            data.put("required", amountDecimal);
            data.put("available", account.getAvailableAmount());
            data.put("frozen", account.getFrozenAmount());
            data.put("total", account.getTotalAmount());
            
            return Result.success("检查完成", data);
            
        } catch (Exception e) {
            log.error("检查余额失败: {}", e.getMessage());
            return Result.error("检查余额失败");
        }
    }

    /**
     * 获取当前用户ID
     * 
     * @return 用户ID
     */
    /**
     * 获取当前用户ID
     * @deprecated 使用 SecurityUtils.getCurrentUserId() 代替
     */
    @Deprecated
    private Long getCurrentUserId() {
        try {
            return com.auction.util.SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            log.error("获取当前用户ID失败: {}", e.getMessage());
            return null;
        }
    }
}
