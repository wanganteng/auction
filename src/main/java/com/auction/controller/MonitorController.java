package com.auction.controller;

import com.auction.common.Result;
import com.auction.monitor.DepositFreezeMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控控制器
 * 提供系统监控和统计信息接口
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@Tag(name = "系统监控", description = "系统监控和统计信息接口")
public class MonitorController {

    @Autowired
    private DepositFreezeMonitor depositFreezeMonitor;

    /**
     * 获取保证金冻结监控统计
     */
    @GetMapping("/deposit-freeze/stats")
    @Operation(summary = "获取保证金冻结监控统计", description = "获取保证金冻结相关的监控统计信息")
    public Result<Map<String, Object>> getDepositFreezeStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("description", depositFreezeMonitor.getMonitorStats());
            stats.put("timestamp", System.currentTimeMillis());
            
            return Result.success("获取监控统计成功", stats);
        } catch (Exception e) {
            log.error("获取保证金冻结监控统计失败: {}", e.getMessage(), e);
            return Result.error("获取监控统计失败: " + e.getMessage());
        }
    }

    /**
     * 重置保证金冻结监控统计
     */
    @PostMapping("/deposit-freeze/reset")
    @Operation(summary = "重置保证金冻结监控统计", description = "重置保证金冻结监控统计数据")
    public Result<String> resetDepositFreezeStats() {
        try {
            depositFreezeMonitor.resetStats();
            return Result.success("重置监控统计成功");
        } catch (Exception e) {
            log.error("重置保证金冻结监控统计失败: {}", e.getMessage(), e);
            return Result.error("重置监控统计失败: " + e.getMessage());
        }
    }

    /**
     * 系统健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "系统健康检查", description = "检查系统各组件运行状态")
    public Result<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            health.put("depositFreezeMonitor", depositFreezeMonitor.getMonitorStats());
            
            return Result.success("系统运行正常", health);
        } catch (Exception e) {
            log.error("系统健康检查失败: {}", e.getMessage(), e);
            return Result.error("系统健康检查失败: " + e.getMessage());
        }
    }
}
