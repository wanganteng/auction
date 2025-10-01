package com.auction.controller;

import com.auction.entity.AuctionLogistics;
import com.auction.service.AuctionLogisticsService;
import com.auction.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流管理控制器
 * 处理物流相关操作
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/logistics")
@Tag(name = "物流管理", description = "物流相关接口")
public class LogisticsController {

    @Autowired
    private AuctionLogisticsService auctionLogisticsService;

    /**
     * 创建物流信息
     */
    @PostMapping
    @Operation(summary = "创建物流信息", description = "为订单创建物流信息")
    public Result<Map<String, Object>> createLogistics(@RequestBody AuctionLogistics logistics) {
        try {
            // 创建物流信息
            Long logisticsId = auctionLogisticsService.createLogistics(logistics);

            Map<String, Object> data = new HashMap<>();
            data.put("logisticsId", logisticsId);

            return Result.success("物流信息创建成功", data);

        } catch (Exception e) {
            log.error("物流信息创建失败: {}", e.getMessage(), e);
            return Result.error("物流信息创建失败: " + e.getMessage());
        }
    }

    /**
     * 查询物流信息列表
     */
    @GetMapping
    @Operation(summary = "查询物流信息列表", description = "查询物流信息列表")
    public Result<List<AuctionLogistics>> getLogisticsList(AuctionLogistics logistics) {
        try {
            List<AuctionLogistics> logisticsList = auctionLogisticsService.getLogisticsList(logistics);
            return Result.success("查询成功", logisticsList);

        } catch (Exception e) {
            log.error("查询物流信息列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询物流信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询物流信息详情", description = "根据ID查询物流信息详情")
    public Result<AuctionLogistics> getLogisticsById(@PathVariable Long id) {
        try {
            AuctionLogistics logistics = auctionLogisticsService.getLogisticsById(id);
            if (logistics != null) {
                return Result.success("查询成功", logistics);
            } else {
                return Result.error("物流信息不存在");
            }

        } catch (Exception e) {
            log.error("查询物流信息失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单ID查询物流信息
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "根据订单ID查询物流信息", description = "根据订单ID查询物流信息")
    public Result<AuctionLogistics> getLogisticsByOrderId(@PathVariable Long orderId) {
        try {
            AuctionLogistics logistics = auctionLogisticsService.getLogisticsByOrderId(orderId);
            if (logistics != null) {
                return Result.success("查询成功", logistics);
            } else {
                return Result.error("物流信息不存在");
            }

        } catch (Exception e) {
            log.error("查询物流信息失败: 订单ID={}, 错误: {}", orderId, e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据运单号查询物流信息
     */
    @GetMapping("/tracking/{trackingNumber}")
    @Operation(summary = "根据运单号查询物流信息", description = "根据运单号查询物流信息")
    public Result<AuctionLogistics> getLogisticsByTrackingNumber(@PathVariable String trackingNumber) {
        try {
            AuctionLogistics logistics = auctionLogisticsService.getLogisticsByTrackingNumber(trackingNumber);
            if (logistics != null) {
                return Result.success("查询成功", logistics);
            } else {
                return Result.error("物流信息不存在");
            }

        } catch (Exception e) {
            log.error("查询物流信息失败: 运单号={}, 错误: {}", trackingNumber, e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新物流信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新物流信息", description = "更新物流信息")
    public Result<String> updateLogistics(@PathVariable Long id, @RequestBody AuctionLogistics logistics) {
        try {
            logistics.setId(id);
            boolean success = auctionLogisticsService.updateLogistics(logistics);
            
            if (success) {
                return Result.success("物流信息更新成功");
            } else {
                return Result.error("物流信息更新失败");
            }

        } catch (Exception e) {
            log.error("物流信息更新失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("物流信息更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新物流状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新物流状态", description = "更新物流状态")
    public Result<String> updateLogisticsStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = auctionLogisticsService.updateLogisticsStatus(id, status);
            
            if (success) {
                return Result.success("物流状态更新成功");
            } else {
                return Result.error("物流状态更新失败");
            }

        } catch (Exception e) {
            log.error("物流状态更新失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("物流状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除物流信息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除物流信息", description = "删除物流信息")
    public Result<String> deleteLogistics(@PathVariable Long id) {
        try {
            boolean success = auctionLogisticsService.deleteLogistics(id);
            
            if (success) {
                return Result.success("物流信息删除成功");
            } else {
                return Result.error("物流信息删除失败");
            }

        } catch (Exception e) {
            log.error("物流信息删除失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("物流信息删除失败: " + e.getMessage());
        }
    }
}