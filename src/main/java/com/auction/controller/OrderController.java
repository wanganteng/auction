package com.auction.controller;

import com.auction.entity.AuctionOrder;
import com.auction.entity.SysUser;
import com.auction.service.AuctionOrderService;
import com.auction.common.Result;
import com.auction.util.SecurityUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 * 处理订单相关操作
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "订单相关接口")
public class OrderController {

    @Autowired
    private AuctionOrderService auctionOrderService;

    /**
     * 创建订单
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "拍卖成交后创建订单")
    public Result<Map<String, Object>> createOrder(@RequestBody AuctionOrder order) {
        try {
            // 获取当前用户
            SysUser currentUser = SecurityUtils.getCurrentUser();
            order.setBuyerId(currentUser.getId());

            // 创建订单
            Long orderId = auctionOrderService.createOrder(order);

            Map<String, Object> data = new HashMap<>();
            data.put("orderId", orderId);
            data.put("orderNo", order.getOrderNo());

            return Result.success("订单创建成功", data);

        } catch (Exception e) {
            log.error("订单创建失败: {}", e.getMessage(), e);
            return Result.error("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单列表
     */
    @GetMapping
    @Operation(summary = "查询订单列表", description = "查询当前用户的订单列表")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            
            // 创建查询条件
            AuctionOrder order = new AuctionOrder();
            order.setBuyerId(currentUser.getId());
            
            // 获取分页数据
            PageInfo<AuctionOrder> pageInfo = auctionOrderService.getUserOrders(currentUser.getId(), page, size);
            
            Map<String, Object> data = new HashMap<>();
            data.put("data", pageInfo.getList());
            data.put("total", pageInfo.getTotal());
            data.put("pageNum", pageInfo.getPageNum());
            data.put("pageSize", pageInfo.getPageSize());
            
            return Result.success("查询成功", data);

        } catch (Exception e) {
            log.error("查询订单列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询订单
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询订单详情", description = "根据ID查询订单详情")
    public Result<AuctionOrder> getOrderById(@PathVariable Long id) {
        try {
            AuctionOrder order = auctionOrderService.getOrderById(id);
            if (order != null) {
                return Result.success("查询成功", order);
            } else {
                return Result.error("订单不存在");
            }

        } catch (Exception e) {
            log.error("查询订单失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    @Operation(summary = "支付订单", description = "支付订单")
    public Result<String> payOrder(@PathVariable Long id) {
        try {
            boolean success = auctionOrderService.payOrder(id);
            
            if (success) {
                return Result.success("订单支付成功");
            } else {
                return Result.error("订单支付失败");
            }

        } catch (Exception e) {
            log.error("订单支付失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("订单支付失败: " + e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/{id}/receive")
    @Operation(summary = "确认收货", description = "确认收货")
    public Result<String> confirmReceive(@PathVariable Long id) {
        try {
            boolean success = auctionOrderService.confirmReceive(id);
            
            if (success) {
                return Result.success("确认收货成功");
            } else {
                return Result.error("确认收货失败");
            }

        } catch (Exception e) {
            log.error("确认收货失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消订单")
    public Result<String> cancelOrder(@PathVariable Long id) {
        try {
            boolean success = auctionOrderService.cancelOrder(id);
            
            if (success) {
                return Result.success("订单取消成功");
            } else {
                return Result.error("订单取消失败");
            }

        } catch (Exception e) {
            log.error("订单取消失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("订单取消失败: " + e.getMessage());
        }
    }
}