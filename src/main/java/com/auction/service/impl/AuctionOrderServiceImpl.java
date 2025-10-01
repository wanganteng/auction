package com.auction.service.impl;

import com.auction.entity.AuctionOrder;
import com.auction.mapper.AuctionOrderMapper;
import com.auction.service.AuctionOrderService;
import com.auction.service.UserDepositAccountService;
import com.auction.service.SysConfigService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import java.util.UUID;

/**
 * 订单服务实现类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class AuctionOrderServiceImpl implements AuctionOrderService {

    @Autowired
    private AuctionOrderMapper orderMapper;

    @Autowired
    private UserDepositAccountService depositAccountService;

    @Autowired
    private SysConfigService sysConfigService;

    @Override
    public Long createOrder(AuctionOrder order) {
        log.debug("创建订单: {}", order.getOrderNo());
        
        try {
            // 生成订单号
            if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
                order.setOrderNo("ORDER_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8));
            }
            
            // 设置默认值
            if (order.getStatus() == null) {
                order.setStatus(0); // 待支付
            }
            if (order.getCreateTime() == null) {
                order.setCreateTime(LocalDateTime.now());
            }
            if (order.getUpdateTime() == null) {
                order.setUpdateTime(LocalDateTime.now());
            }
            
            int result = orderMapper.insert(order);
            if (result > 0) {
                log.info("订单创建成功: {}", order.getOrderNo());
                return order.getId();
            } else {
                log.error("订单创建失败: {}", order.getOrderNo());
                return null;
            }
        } catch (Exception e) {
            log.error("创建订单时发生错误: {}", e.getMessage());
            return null;
        }
    }

    

    @Override
    public AuctionOrder getOrderById(Long orderId) {
        log.debug("根据ID获取订单: {}", orderId);
        return orderMapper.selectById(orderId);
    }

    @Override
    public AuctionOrder getOrderByOrderNo(String orderNo) {
        log.debug("根据订单号获取订单: {}", orderNo);
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageInfo<AuctionOrder> getUserOrders(Long userId, Integer pageNum, Integer pageSize) {
        log.debug("获取用户订单列表: 用户ID={}, 页码={}, 大小={}", userId, pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        
        // 创建查询条件
        AuctionOrder queryOrder = new AuctionOrder();
        queryOrder.setBuyerId(userId);
        List<AuctionOrder> orders = orderMapper.selectList(queryOrder);
        return new PageInfo<>(orders);
    }

    @Override
    public PageInfo<AuctionOrder> getAllOrders(Integer pageNum, Integer pageSize) {
        log.debug("获取所有订单列表: 页码={}, 大小={}", pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<AuctionOrder> orders = orderMapper.selectAll();
        return new PageInfo<>(orders);
    }

    public PageInfo<AuctionOrder> getOrdersByStatus(Integer status, Integer pageNum, Integer pageSize) {
        log.debug("根据状态获取订单列表: 状态={}, 页码={}, 大小={}", status, pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<AuctionOrder> orders = orderMapper.selectByStatus(status);
        return new PageInfo<>(orders);
    }

    @Override
    public boolean payOrder(Long orderId) {
        return payOrder(orderId, BigDecimal.ZERO);
    }
    
    @Override
    public boolean payOrder(Long orderId, BigDecimal shippingFee) {
        log.debug("支付订单: orderId={}, shippingFee={}", orderId, shippingFee);
        
        try {
            AuctionOrder order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            if (!order.getStatus().equals(1)) { // 状态 1-待付款
                log.error("订单状态不是待支付: 当前状态={}", order.getStatus());
                return false;
            }
            
            Long buyerId = order.getBuyerId();
            BigDecimal depositAmount = order.getDepositAmount();
            BigDecimal balanceAmount = order.getBalanceAmount();
            
            // 计算总支付金额（尾款 + 物流费）
            BigDecimal totalPayAmount = balanceAmount.add(shippingFee != null ? shippingFee : BigDecimal.ZERO);
            
            // 1. 从可用余额中扣除尾款和物流费
            if (totalPayAmount.compareTo(BigDecimal.ZERO) > 0) {
                String desc = String.format("支付订单（尾款¥%.2f + 物流费¥%.2f），订单号:%s", 
                    balanceAmount, shippingFee, order.getOrderNo());
                boolean deducted = depositAccountService.deductFromAvailable(buyerId, totalPayAmount, orderId, "order", desc);
                if (!deducted) {
                    log.error("扣除支付金额失败: orderId={}, amount={}", orderId, totalPayAmount);
                    return false;
                }
                log.info("扣除支付金额成功: userId={}, amount={}", buyerId, totalPayAmount);
            }
            
            // 2. 从冻结金额中扣除保证金（抵扣订单）
            if (depositAmount != null && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
                String desc = "保证金抵扣订单，订单号:" + order.getOrderNo();
                boolean deducted = depositAccountService.deductAmount(buyerId, depositAmount, orderId, "order", desc);
                if (!deducted) {
                    log.error("扣除保证金失败: orderId={}, amount={}", orderId, depositAmount);
                    // TODO: 这里应该回滚尾款扣除，建议使用事务管理
                    return false;
                }
                log.info("保证金抵扣成功: userId={}, amount={}", buyerId, depositAmount);
            }
            
            // 3. 更新订单状态为已支付
            order.setStatus(2); // 2-已支付
            order.setPaymentTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            
            int result = orderMapper.update(order);
            if (result > 0) {
                log.info("订单支付成功: orderId={}, 尾款={}, 物流费={}, 保证金={}", 
                    orderId, balanceAmount, shippingFee, depositAmount);
                return true;
            } else {
                log.error("更新订单状态失败: {}", orderId);
                return false;
            }
        } catch (Exception e) {
            log.error("支付订单时发生错误: orderId={}, error={}", orderId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean updateOrder(AuctionOrder order) {
        try {
            order.setUpdateTime(LocalDateTime.now());
            int result = orderMapper.update(order);
            return result > 0;
        } catch (Exception e) {
            log.error("更新订单失败: orderId={}, error={}", order.getId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean shipOrder(Long orderId) {
        log.debug("订单发货: {}", orderId);
        
        try {
            AuctionOrder order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            if (!order.getStatus().equals(2)) { // 2-已支付
                log.error("订单状态不是已支付: 当前状态={}", order.getStatus());
                return false;
            }
            
            order.setStatus(3); // 3-已发货
            order.setShipTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            
            int result = orderMapper.update(order);
            if (result > 0) {
                log.info("订单发货成功: {}", orderId);
                return true;
            } else {
                log.error("订单发货失败: {}", orderId);
                return false;
            }
        } catch (Exception e) {
            log.error("订单发货时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 拒绝发货并退款（管理员操作）
     * 原因：拍品丢失、卖家不愿意卖、联系不上卖家等
     */
    @Transactional
    public boolean rejectShipment(Long orderId, String reason) {
        log.debug("拒绝发货: orderId={}, reason={}", orderId, reason);
        
        try {
            AuctionOrder order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                throw new RuntimeException("订单不存在");
            }
            
            if (!order.getStatus().equals(2)) { // 必须是已支付状态
                log.error("订单状态不是已支付，无法拒绝发货: 当前状态={}", order.getStatus());
                throw new RuntimeException("订单状态错误，无法拒绝发货");
            }
            
            Long buyerId = order.getBuyerId();
            BigDecimal totalRefund = order.getTotalAmount(); // 退款总额 = 成交价 + 佣金
            
            // 1. 退还用户已支付的金额（尾款 + 保证金）
            // 尾款退还
            if (order.getBalanceAmount() != null && order.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
                String desc = "拒绝发货退款（尾款），订单号:" + order.getOrderNo() + "，原因:" + reason;
                depositAccountService.recharge(buyerId, order.getBalanceAmount(), desc);
                log.info("退还尾款: orderId={}, amount={}", orderId, order.getBalanceAmount());
            }
            
            // 保证金退还
            if (order.getDepositAmount() != null && order.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
                String desc = "拒绝发货退款（保证金），订单号:" + order.getOrderNo() + "，原因:" + reason;
                depositAccountService.recharge(buyerId, order.getDepositAmount(), desc);
                log.info("退还保证金: orderId={}, amount={}", orderId, order.getDepositAmount());
            }
            
            // 2. 更新订单状态为已取消
            order.setStatus(6); // 6-已取消
            order.setUpdateTime(LocalDateTime.now());
            
            // 注意：拒绝原因已通过审计日志记录，无需在订单表中重复存储
            
            int result = orderMapper.update(order);
            if (result > 0) {
                log.info("拒绝发货成功，已退款: orderId={}, buyerId={}, totalRefund={}", 
                    orderId, buyerId, totalRefund);
                return true;
            } else {
                log.error("更新订单状态失败: {}", orderId);
                throw new RuntimeException("更新订单状态失败");
            }
        } catch (Exception e) {
            log.error("拒绝发货失败: orderId={}, error={}", orderId, e.getMessage(), e);
            throw new RuntimeException("拒绝发货失败: " + e.getMessage());
        }
    }

    @Override
    public boolean confirmReceive(Long orderId) {
        log.debug("确认收货: {}", orderId);
        
        try {
            AuctionOrder order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            if (!order.getStatus().equals(2)) {
                log.error("订单状态不是已发货: {}", orderId);
                return false;
            }
            
            order.setStatus(4); // 已收货
            order.setReceiveTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            
            int result = orderMapper.update(order);
            if (result > 0) {
                log.info("订单确认收货成功: {}", orderId);
                return true;
            } else {
                log.error("订单确认收货失败: {}", orderId);
                return false;
            }
        } catch (Exception e) {
            log.error("确认收货时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        log.debug("取消订单: {}", orderId);
        
        try {
            AuctionOrder order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            if (order.getStatus() >= 3) {
                log.error("订单已完成，无法取消: {}", orderId);
                return false;
            }
            
            order.setStatus(6); // 已取消
            order.setUpdateTime(LocalDateTime.now());
            
            int result = orderMapper.update(order);
            if (result > 0) {
                log.info("订单取消成功: {}", orderId);
                return true;
            } else {
                log.error("订单取消失败: {}", orderId);
                return false;
            }
        } catch (Exception e) {
            log.error("取消订单时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 处理超时未支付订单：扣除保证金并取消订单
     * 超时阈值（分钟）从系统配置 order.pay.timeout_minutes 读取，默认30
     * 扣除金额=订单depositAmount（元）
     */
    public void processOverdueUnpaidOrders() {
        try {
            Integer timeoutMinutes = sysConfigService.getIntConfigValue("order.pay.timeout_minutes", 30);
            LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);

            // 获取待支付订单（状态=0）
            List<AuctionOrder> pending = orderMapper.selectByStatus(0);
            for (AuctionOrder order : pending) {
                if (order.getCreateTime() == null) {
                    continue;
                }
                if (order.getCreateTime().isAfter(deadline)) {
                    continue; // 未超时
                }

                try {
                    // 扣除保证金
                    Long buyerId = order.getBuyerId();
                    if (order.getDepositAmount() != null) {
                        String desc = "订单支付超时扣除保证金，订单号:" + order.getOrderNo();
                        boolean deducted = depositAccountService.deductAmount(buyerId, order.getDepositAmount(), order.getId(), "order", desc);
                        if (!deducted) {
                            log.warn("扣除保证金失败: orderId={}, buyerId={}", order.getId(), buyerId);
                        }
                    } else {
                        log.warn("订单未设置保证金: orderId={}", order.getId());
                    }

                    // 取消订单
                    order.setStatus(6); // 已取消
                    order.setUpdateTime(LocalDateTime.now());
                    orderMapper.update(order);
                    log.info("订单超时已取消并处理保证金: {}", order.getId());
                } catch (Exception ex) {
                    log.error("处理超时订单失败: orderId={}, err={}", order.getId(), ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            log.error("处理超时未支付订单失败: {}", e.getMessage(), e);
        }
    }

    public boolean refundOrder(Long orderId, String reason) {
        log.debug("订单退款: {}, 原因: {}", orderId, reason);
        
        try {
            AuctionOrder order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            if (order.getStatus() < 1) {
                log.error("订单未支付，无法退款: {}", orderId);
                return false;
            }
            
            order.setStatus(6); // 已取消
            order.setUpdateTime(LocalDateTime.now());
            
            int result = orderMapper.update(order);
            if (result > 0) {
                log.info("订单退款成功: {}", orderId);
                return true;
            } else {
                log.error("订单退款失败: {}", orderId);
                return false;
            }
        } catch (Exception e) {
            log.error("订单退款时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getOrderStats(Long userId) {
        log.debug("获取订单统计信息: 用户ID={}", userId);
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<AuctionOrder> orders;
            if (userId != null) {
                // 创建查询条件
                AuctionOrder queryOrder = new AuctionOrder();
                queryOrder.setBuyerId(userId);
                orders = orderMapper.selectList(queryOrder);
            } else {
                orders = orderMapper.selectAll();
            }
            
            long totalOrders = orders.size();
            long pendingOrders = orders.stream().filter(o -> o.getStatus() == 0).count();
            long paidOrders = orders.stream().filter(o -> o.getStatus() == 1).count();
            long shippedOrders = orders.stream().filter(o -> o.getStatus() == 2).count();
            long completedOrders = orders.stream().filter(o -> o.getStatus() == 3).count();
            long cancelledOrders = orders.stream().filter(o -> o.getStatus() == 4).count();
            long refundedOrders = orders.stream().filter(o -> o.getStatus() == 5).count();
            
            stats.put("totalOrders", totalOrders);
            stats.put("pendingOrders", pendingOrders);
            stats.put("paidOrders", paidOrders);
            stats.put("shippedOrders", shippedOrders);
            stats.put("completedOrders", completedOrders);
            stats.put("cancelledOrders", cancelledOrders);
            stats.put("refundedOrders", refundedOrders);
            
            return stats;
        } catch (Exception e) {
            log.error("获取订单统计信息时发生错误: {}", e.getMessage());
            return stats;
        }
    }

    @Override
    public boolean completeOrder(Long orderId) {
        log.debug("完成订单: {}", orderId);
        
        try {
            AuctionOrder order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            if (!order.getStatus().equals(4)) {
                log.error("订单状态不是已收货: {}", orderId);
                return false;
            }
            
            order.setStatus(5); // 已完成
            order.setUpdateTime(LocalDateTime.now());
            
            int result = orderMapper.update(order);
            if (result > 0) {
                log.info("订单完成成功: {}", orderId);
                return true;
            } else {
                log.error("订单完成失败: {}", orderId);
                return false;
            }
        } catch (Exception e) {
            log.error("完成订单时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<AuctionOrder> getOrderList(AuctionOrder order) {
        log.debug("查询订单列表");
        
        try {
            return orderMapper.selectList(order);
        } catch (Exception e) {
            log.error("查询订单列表时发生错误: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        log.debug("更新订单状态: {}, 状态: {}", orderId, status);
        
        try {
            AuctionOrder order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            order.setStatus(status);
            order.setUpdateTime(LocalDateTime.now());
            
            int result = orderMapper.update(order);
            if (result > 0) {
                log.info("订单状态更新成功: {}, 状态: {}", orderId, status);
                return true;
            } else {
                log.error("订单状态更新失败: {}, 状态: {}", orderId, status);
                return false;
            }
        } catch (Exception e) {
            log.error("更新订单状态时发生错误: {}", e.getMessage());
            return false;
        }
    }
}