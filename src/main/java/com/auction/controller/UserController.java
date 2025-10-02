package com.auction.controller;

import com.auction.entity.*;
import com.auction.service.*;
import com.auction.websocket.AuctionWebSocketHandler;
import com.auction.common.Result;
import com.auction.util.SecurityUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.auction.security.CustomUserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 买家控制器
 * 包含保证金管理、拍卖会列表、竞拍等功能
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "买家功能", description = "买家相关接口")
public class UserController {

    @Autowired
    private AuctionSessionService auctionSessionService;

    @Autowired
    private UserDepositAccountService userDepositAccountService;

    @Autowired
    private UserDepositTransactionService userDepositTransactionService;

    @Autowired
    private UserDepositRefundService userDepositRefundService;

    @Autowired
    private AuctionBidService auctionBidService;

    @Autowired
    private AuctionOrderService auctionOrderService;

    @Autowired
    private com.auction.mapper.AuctionItemMapper auctionItemMapper;

    @Autowired
    private AuctionWebSocketHandler auctionWebSocketHandler;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AuctionItemService auctionItemService;

    @Autowired
    private SysUserService sysUserService;

    // ==================== 拍卖会管理 ====================

    /**
     * 获取拍卖会列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取拍卖会列表", description = "获取所有可用的拍卖会列表")
    public Result<Map<String, Object>> getSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 仅返回"已展示"的拍卖会
            AuctionSession query = new AuctionSession();
            query.setIsVisible(1);
            List<AuctionSession> sessions = auctionSessionService.getSessionList(query);
            
            // 为每个拍卖会添加拍品数量和围观人数
            List<Map<String, Object>> sessionList = new ArrayList<>();
            for (AuctionSession session : sessions) {
                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("id", session.getId());
                sessionData.put("sessionName", session.getSessionName());
                sessionData.put("description", session.getDescription());
                sessionData.put("startTime", session.getStartTime());
                sessionData.put("endTime", session.getEndTime());
                sessionData.put("status", session.getStatus());
                sessionData.put("coverImage", session.getCoverImage());
                sessionData.put("sessionType", session.getSessionType());
                
                // 获取拍品数量
                List<com.auction.entity.AuctionItem> items = auctionItemMapper.selectBySessionId(session.getId());
                sessionData.put("totalItems", items != null ? items.size() : 0);
                
                // 获取围观人数（从Redis获取实时数据）
                try {
                    Long viewCount = redisService.getAuctionViewCount(session.getId());
                    sessionData.put("viewCount", viewCount != null ? viewCount : 0);
                } catch (Exception e) {
                    log.warn("获取围观人数失败: sessionId={}, error={}", session.getId(), e.getMessage());
                    sessionData.put("viewCount", 0);
                }
                
                sessionList.add(sessionData);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", sessionList);
            result.put("total", sessionList.size());
            
            return Result.success("查询成功", result);

        } catch (Exception e) {
            log.error("获取拍卖会列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取拍卖会详情
     */
    @GetMapping("/sessions/{id}")
    @Operation(summary = "获取拍卖会详情", description = "获取指定拍卖会的详细信息")
    public Result<Map<String, Object>> getSessionDetail(@PathVariable Long id) {
        try {
            // 获取详情后校验是否可见
            AuctionSession session = auctionSessionService.getSessionById(id);
            if (session == null) {
                return Result.error("拍卖会不存在");
            }
            if (session.getIsVisible() == null || session.getIsVisible() != 1) {
                return Result.error("拍卖会暂未对外展示");
            }
            
            Map<String, Object> sessionDetail = new HashMap<>();
            sessionDetail.put("id", session.getId());
            sessionDetail.put("sessionName", session.getSessionName());
            sessionDetail.put("description", session.getDescription());
            sessionDetail.put("startTime", session.getStartTime());
            sessionDetail.put("endTime", session.getEndTime());
            sessionDetail.put("status", session.getStatus());
            sessionDetail.put("coverImage", session.getCoverImage());
            sessionDetail.put("depositRatio", session.getDepositRatio());
            sessionDetail.put("commissionRatio", session.getCommissionRatio());
            sessionDetail.put("minDepositAmount", session.getMinDepositAmount());
            sessionDetail.put("maxBidAmount", session.getMaxBidAmount());
            sessionDetail.put("minIncrementAmount", session.getMinIncrementAmount());
            sessionDetail.put("totalItems", session.getTotalItems());
            sessionDetail.put("soldItems", session.getSoldItems());
            sessionDetail.put("viewCount", session.getViewCount());
            
            // 加载该拍卖会的拍品列表
            List<com.auction.entity.AuctionItem> items = auctionItemMapper.selectBySessionId(id);
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (com.auction.entity.AuctionItem item : items) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", item.getId());
                m.put("itemName", item.getItemName());
                m.put("description", item.getDescription());
                m.put("itemCode", item.getItemCode());
                m.put("estimatedPrice", item.getEstimatedPrice());
                m.put("dimensions", item.getDimensions());
                m.put("material", item.getMaterial());
                m.put("startingPrice", item.getStartingPrice());
                m.put("currentPrice", item.getCurrentPrice());
                m.put("images", item.getImages());
                m.put("status", item.getStatus());
                itemList.add(m);
            }
            sessionDetail.put("items", itemList);
            
            return Result.success("查询成功", sessionDetail);

        } catch (Exception e) {
            log.error("获取拍卖会详情失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 记录一次围观（浏览）并返回最新围观人数
     */
    @PostMapping("/sessions/{id}/view")
    @Operation(summary = "增加围观人数", description = "用户访问拍卖会详情即计一次围观")
    public Result<Long> addSessionView(@PathVariable Long id) {
        try {
            Long count = redisService.incrementAuctionViewCount(id);
            return Result.success("记录成功", count);
        } catch (Exception e) {
            log.error("记录围观人数失败: sessionId={}, error={}", id, e.getMessage(), e);
            return Result.error("记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取拍卖会出价记录
     */
    @GetMapping("/sessions/{id}/bids")
    @Operation(summary = "获取出价记录", description = "获取指定拍卖会的出价记录")
    public Result<List<Map<String, Object>>> getSessionBids(@PathVariable Long id) {
        try {
            // 使用现有的方法获取出价记录
            AuctionBid bidQuery = new AuctionBid();
            bidQuery.setSessionId(id);
            bidQuery.setStatus(0); // 只查询有效出价
            List<AuctionBid> bids = auctionBidService.getBidList(bidQuery);
            
            List<Map<String, Object>> bidList = new ArrayList<>();
            for (AuctionBid bid : bids) {
                Map<String, Object> bidMap = new HashMap<>();
                bidMap.put("id", bid.getId());
                bidMap.put("bidAmount", bid.getBidAmountYuan());
                bidMap.put("bidTime", bid.getBidTime());
                
                // 获取用户真实信息
                SysUser user = sysUserService.getById(bid.getUserId());
                String displayName = "未知用户";
                if (user != null) {
                    // 优先使用昵称，如果没有昵称则使用用户名
                    displayName = (user.getNickname() != null && !user.getNickname().trim().isEmpty()) 
                        ? user.getNickname() 
                        : user.getUsername();
                }
                bidMap.put("username", displayName);
                bidMap.put("source", bid.getSource());
                bidMap.put("isAuto", bid.getIsAuto());
                bidList.add(bidMap);
            }
            
            return Result.success("查询成功", bidList);

        } catch (Exception e) {
            log.error("获取出价记录失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }


    /**
     * 参与竞拍
     */
    @PostMapping("/sessions/{id}/bid")
    @Operation(summary = "参与竞拍", description = "用户参与拍卖会竞拍")
    public Result<String> placeBid(@PathVariable Long id, @RequestBody Map<String, Object> bidData) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            
            // 检查保证金账户状态
            UserDepositAccount account = userDepositAccountService.getAccountByUserId(currentUser.getId());
            if (account != null && account.getStatus() == 2) {
                return Result.error("您的保证金账户已被冻结，无法参与竞拍，请联系管理员");
            }
            
            BigDecimal bidAmount = new BigDecimal(bidData.get("bidAmount").toString());
            // 读取拍品ID
            Object itemIdObj = bidData.get("itemId");
            if (itemIdObj == null) {
                return Result.error("参数错误：缺少itemId");
            }
            Long itemId;
            if (itemIdObj instanceof Number) {
                itemId = ((Number) itemIdObj).longValue();
            } else {
                itemId = Long.parseLong(itemIdObj.toString());
            }
            
            // 创建出价记录
            AuctionBid bid = new AuctionBid();
            bid.setSessionId(id);
            bid.setItemId(itemId);
            bid.setUserId(currentUser.getId());
            // 验证出价金额为整数
            if (bidAmount.scale() > 0) {
                return Result.error("出价金额必须为整数元");
            }
            
            // 直接设置出价金额（元）
            bid.setBidAmountYuan(bidAmount);
            bid.setBidTime(LocalDateTime.now());
            bid.setClientIp("127.0.0.1"); // 使用正确的字段名
            bid.setSource(1); // 1-手动出价
            bid.setIsAuto(0); // 0-否
            bid.setStatus(0); // 0-有效
            // 默认字段，避免NULL
            bid.setDeleted(0);
            bid.setCreateTime(LocalDateTime.now());
            bid.setUpdateTime(LocalDateTime.now());

            Long bidId = auctionBidService.placeBid(bid);
            
            if (bidId != null) {
                // 出价成功后通过WebSocket广播（兼容REST出价路径）
                try {
                    Long userBidCount = redisService.incrementUserBidCount(currentUser.getId(), id);
                    Long auctionBidCount = redisService.incrementAuctionBidCount(id);
                    auctionWebSocketHandler.sendBidMessage(id, bid, userBidCount, auctionBidCount);
                } catch (Exception ignore) {}
                return Result.success("出价成功");
            } else {
                return Result.error("出价失败");
            }

        } catch (Exception e) {
            log.error("参与竞拍失败: {}", e.getMessage(), e);
            return Result.error("出价失败: " + e.getMessage());
        }
    }

    // ==================== 订单管理 ====================

    /**
     * 获取用户订单列表
     */
    @GetMapping("/orders")
    @Operation(summary = "获取订单列表", description = "获取当前用户的订单列表")
    public Result<Map<String, Object>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            // 使用现有的方法获取用户订单列表
            PageInfo<AuctionOrder> pageInfo = auctionOrderService.getUserOrders(currentUser.getId(), page, size);
            
            // 为每个订单关联拍品信息（获取运费和包邮信息）
            for (AuctionOrder order : pageInfo.getList()) {
                if (order.getItem() == null && order.getItemId() != null) {
                    try {
                        AuctionItem item = auctionItemService.getItemById(order.getItemId());
                        order.setItem(item);
                    } catch (Exception e) {
                        log.warn("关联拍品信息失败: orderId={}, itemId={}", order.getId(), order.getItemId());
                    }
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", pageInfo.getList());
            result.put("total", pageInfo.getTotal());
            result.put("pageNum", pageInfo.getPageNum());
            result.put("pageSize", pageInfo.getPageSize());
            result.put("pages", pageInfo.getPages());
            
            return Result.success("查询成功", result);

        } catch (Exception e) {
            log.error("获取订单列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/orders/pay")
    @Operation(summary = "支付订单", description = "用户支付订单")
    public Result<String> payOrder(@RequestBody Map<String, Object> paymentData) {
        try {
            Long orderId = Long.valueOf(paymentData.get("orderId").toString());
            String paymentMethod = paymentData.get("paymentMethod").toString();
            
            SysUser currentUser = SecurityUtils.getCurrentUser();
            // 先验证订单是否属于当前用户
            AuctionOrder order = auctionOrderService.getOrderById(orderId);
            if (order == null || !order.getBuyerId().equals(currentUser.getId())) {
                return Result.error("订单不存在或无权限");
            }
            
            // 提取配送信息
            Integer deliveryMethod = paymentData.containsKey("deliveryMethod") ? 
                Integer.valueOf(paymentData.get("deliveryMethod").toString()) : 1;
            BigDecimal shippingFee = paymentData.containsKey("shippingFee") ? 
                new BigDecimal(paymentData.get("shippingFee").toString()) : BigDecimal.ZERO;
            String receiverName = paymentData.containsKey("receiverName") ? 
                paymentData.get("receiverName").toString() : null;
            String receiverPhone = paymentData.containsKey("receiverPhone") ? 
                paymentData.get("receiverPhone").toString() : null;
            String receiverAddress = paymentData.containsKey("receiverAddress") ? 
                paymentData.get("receiverAddress").toString() : null;
            
            // 更新订单的配送信息
            order.setDeliveryMethod(deliveryMethod);
            order.setShippingFee(shippingFee);
            order.setReceiverName(receiverName);
            order.setReceiverPhone(receiverPhone);
            order.setReceiverAddress(receiverAddress);
            if (deliveryMethod == 2) {
                // 线下自提时设置自提地址（可以从系统配置读取）
                order.setPickupAddress("北京市朝阳区建国路XX号XX大厦10层");
            }
            auctionOrderService.updateOrder(order);
            
            // 执行支付（包含物流费）
            boolean success = auctionOrderService.payOrder(orderId, shippingFee);
            
            if (success) {
                return Result.success("支付成功");
            } else {
                return Result.error("支付失败");
            }

        } catch (Exception e) {
            log.error("支付订单失败: {}", e.getMessage(), e);
            return Result.error("支付失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/orders/{id}/cancel")
    @Operation(summary = "取消订单", description = "用户取消订单")
    public Result<String> cancelOrder(@PathVariable Long id) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            // 先验证订单是否属于当前用户
            AuctionOrder order = auctionOrderService.getOrderById(id);
            if (order == null || !order.getBuyerId().equals(currentUser.getId())) {
                return Result.error("订单不存在或无权限");
            }
            
            boolean success = auctionOrderService.cancelOrder(id);
            
            if (success) {
                return Result.success("订单已取消");
            } else {
                return Result.error("取消失败");
            }

        } catch (Exception e) {
            log.error("取消订单失败: {}", e.getMessage(), e);
            return Result.error("取消失败: " + e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/orders/{id}/confirm")
    @Operation(summary = "确认收货", description = "用户确认收货")
    public Result<String> confirmReceive(@PathVariable Long id) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            // 先验证订单是否属于当前用户
            AuctionOrder order = auctionOrderService.getOrderById(id);
            if (order == null || !order.getBuyerId().equals(currentUser.getId())) {
                return Result.error("订单不存在或无权限");
            }
            
            boolean success = auctionOrderService.confirmReceive(id);
            
            if (success) {
                return Result.success("确认收货成功");
            } else {
                return Result.error("确认收货失败");
            }

        } catch (Exception e) {
            log.error("确认收货失败: {}", e.getMessage(), e);
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }

    // ==================== 保证金管理 ====================


    /**
     * 获取保证金状态
     */
    @GetMapping("/deposits/status")
    @Operation(summary = "获取保证金状态", description = "检查用户是否有足够的保证金")
    public Result<Map<String, Object>> getDepositStatus() {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            UserDepositAccount account = userDepositAccountService.getAccountByUserId(currentUser.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasDeposit", account != null && account.getAvailableAmount().compareTo(BigDecimal.ZERO) > 0);
            result.put("balance", account != null ? account.getAvailableAmount() : BigDecimal.ZERO);
            
            return Result.success("查询成功", result);

        } catch (Exception e) {
            log.error("获取保证金状态失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取保证金交易记录
     */
    @GetMapping("/deposits/transactions")
    @Operation(summary = "获取保证金交易记录", description = "获取用户的保证金交易记录")
    public Result<List<Map<String, Object>>> getDepositTransactions() {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            // 获取交易记录列表
            List<UserDepositTransaction> transactionList = userDepositTransactionService.getTransactionsByUserId(currentUser.getId());
            
            // 转换为Map格式
            List<Map<String, Object>> transactions = new ArrayList<>();
            for (UserDepositTransaction transaction : transactionList) {
                Map<String, Object> transactionMap = new HashMap<>();
                transactionMap.put("id", transaction.getId());
                transactionMap.put("transactionNo", transaction.getTransactionNo());
                transactionMap.put("transactionType", transaction.getTransactionType());
                transactionMap.put("amount", transaction.getAmount());
                transactionMap.put("balanceBefore", transaction.getBalanceBefore());
                transactionMap.put("balanceAfter", transaction.getBalanceAfter());
                transactionMap.put("status", transaction.getStatus());
                transactionMap.put("description", transaction.getDescription());
                transactionMap.put("createTime", transaction.getCreateTime());
                transactionMap.put("relatedId", transaction.getRelatedId());
                transactionMap.put("relatedType", transaction.getRelatedType());
                transactions.add(transactionMap);
            }
            
            return Result.success("查询成功", transactions);

        } catch (Exception e) {
            log.error("获取保证金交易记录失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 充值保证金
     */
    @PostMapping("/deposits/deposit")
    @Operation(summary = "充值保证金", description = "充值保证金到账户")
    public Result<String> rechargeDeposit(@RequestParam BigDecimal amount, 
                                        @RequestParam(required = false) String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("充值金额必须大于0");
            }

            SysUser currentUser = SecurityUtils.getCurrentUser();
            
            boolean success = userDepositAccountService.recharge(
                currentUser.getId(), 
                amount, 
                description != null ? description : "用户充值"
            );

            if (success) {
                return Result.success("保证金充值成功");
            } else {
                return Result.error("保证金充值失败");
            }

        } catch (Exception e) {
            log.error("保证金充值失败: {}", e.getMessage(), e);
            return Result.error("保证金充值失败: " + e.getMessage());
        }
    }

    /**
     * 提现保证金
     */
    @PostMapping("/deposits/withdraw")
    @Operation(summary = "提现保证金", description = "从保证金账户提现")
    public Result<String> withdrawDeposit(@RequestBody Map<String, Object> withdrawData) {
        try {
            BigDecimal amount = new BigDecimal(withdrawData.get("amount").toString());
            String withdrawMethod = withdrawData.get("withdrawMethod").toString();
            String account = withdrawData.get("account").toString();
            String remark = withdrawData.get("remark") != null ? withdrawData.get("remark").toString() : "";
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("提现金额必须大于0");
            }

            SysUser currentUser = SecurityUtils.getCurrentUser();
            
            // 简化处理：暂时不支持提现功能
            // 实际项目中应该实现完整的提现流程
            boolean success = false;
            
            // 这里可以添加提现逻辑，比如：
            // 1. 检查账户余额
            // 2. 创建提现申请
            // 3. 等待管理员审核
            // 4. 审核通过后扣除余额
            
            // 暂时返回失败，提示功能暂未开放
            return Result.error("提现功能暂未开放，请联系管理员");

        } catch (Exception e) {
            log.error("保证金提现失败: {}", e.getMessage(), e);
            return Result.error("提现失败: " + e.getMessage());
        }
    }

    /**
     * 查询交易流水
     */
    @GetMapping("/deposit/transactions")
    @Operation(summary = "查询交易流水", description = "查询当前用户的保证金交易流水")
    public Result<List<UserDepositTransaction>> getDepositTransactions(@RequestParam(required = false) Integer transactionType) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            
            UserDepositTransaction transaction = new UserDepositTransaction();
            transaction.setUserId(currentUser.getId());
            if (transactionType != null) {
                transaction.setTransactionType(transactionType);
            }
            
            List<UserDepositTransaction> transactions = userDepositTransactionService.getTransactionList(transaction);
            return Result.success("查询成功", transactions);

        } catch (Exception e) {
            log.error("查询交易流水失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 申请退还保证金
     */
    @PostMapping("/deposit/refund")
    @Operation(summary = "申请退还保证金", description = "申请退还保证金")
    public Result<Map<String, Object>> applyRefund(@RequestParam BigDecimal amount, 
                                                  @RequestParam(required = false) String reason) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("退款金额必须大于0");
            }

            SysUser currentUser = SecurityUtils.getCurrentUser();
            
            Long refundId = userDepositRefundService.createRefundApplication(
                currentUser.getId(), 
                amount, 
                reason != null ? reason : "用户申请退款"
            );

            Map<String, Object> data = new HashMap<>();
            data.put("refundId", refundId);

            return Result.success("退款申请提交成功", data);

        } catch (Exception e) {
            log.error("退款申请失败: {}", e.getMessage(), e);
            return Result.error("退款申请失败: " + e.getMessage());
        }
    }

    /**
     * 查询退款申请列表
     */
    @GetMapping("/deposit/refunds")
    @Operation(summary = "查询退款申请列表", description = "查询当前用户的退款申请列表")
    public Result<List<UserDepositRefund>> getDepositRefunds(@RequestParam(required = false) Integer status) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            
            UserDepositRefund refund = new UserDepositRefund();
            refund.setUserId(currentUser.getId());
            if (status != null) {
                refund.setStatus(status);
            }
            
            List<UserDepositRefund> refunds = userDepositRefundService.getRefundList(refund);
            return Result.success("查询成功", refunds);

        } catch (Exception e) {
            log.error("查询退款申请失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // ==================== 竞拍功能 ====================




    /**
     * 获取拍卖会统计信息
     * 
     * @param sessionId 拍卖会ID
     * @return 统计信息
     */
    @GetMapping("/sessions/{sessionId}/statistics")
    @Operation(summary = "获取拍卖会统计信息", description = "获取指定拍卖会的围观人数、出价次数等统计信息")
    public Result<Map<String, Object>> getSessionStatistics(@PathVariable Long sessionId) {
        try {
            Map<String, Object> statistics = auctionSessionService.getSessionStatistics(sessionId);
            return Result.success("获取统计信息成功", statistics);
        } catch (Exception e) {
            log.error("获取拍卖会统计信息失败: 拍卖会ID={}, 错误: {}", sessionId, e.getMessage(), e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的出价统计
     * 
     * @param sessionId 拍卖会ID
     * @return 我的出价统计
     */
    @GetMapping("/sessions/{sessionId}/my-bid-statistics")
    @Operation(summary = "获取我的出价统计", description = "获取当前用户在指定拍卖会的出价统计信息")
    public Result<Map<String, Object>> getMyBidStatistics(@PathVariable Long sessionId) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            Map<String, Object> statistics = auctionSessionService.getUserBidStatistics(currentUser.getId(), sessionId);
            return Result.success("获取我的出价统计成功", statistics);
        } catch (Exception e) {
            log.error("获取我的出价统计失败: 拍卖会ID={}, 错误: {}", sessionId, e.getMessage(), e);
            return Result.error("获取我的出价统计失败: " + e.getMessage());
        }
    }

}