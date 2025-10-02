package com.auction.controller;

import com.auction.common.Result;
import com.auction.entity.BidIncrementConfig;
import com.auction.entity.BidIncrementRule;
import com.auction.service.BidIncrementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 加价阶梯管理控制器
 *
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/bid-increment")
@Tag(name = "加价阶梯管理", description = "加价阶梯配置相关接口")
public class BidIncrementController {

    @Autowired
    private BidIncrementService bidIncrementService;

    @PostMapping("/config")
    @Operation(summary = "创建加价阶梯配置", description = "创建新的加价阶梯配置及规则")
    public Result<String> createConfig(
            @RequestBody CreateConfigRequest request) {
        try {
            Long configId = bidIncrementService.createConfig(request.getConfig(), request.getRules());
            return Result.success("加价阶梯配置创建成功，ID: " + configId);
        } catch (Exception e) {
            log.error("创建加价阶梯配置失败", e);
            return Result.error("创建加价阶梯配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/config")
    @Operation(summary = "更新加价阶梯配置", description = "更新加价阶梯配置及规则")
    public Result<String> updateConfig(
            @RequestBody UpdateConfigRequest request) {
        try {
            // 前端校验：检查是否有拍卖会正在使用该配置且已开始
            if (request.getConfig() != null && request.getConfig().getId() != null) {
                boolean canModify = bidIncrementService.canModifyConfigForSession(request.getConfig().getId(), null);
                if (!canModify) {
                    return Result.error("该加价阶梯配置已被拍卖会使用且拍卖会已开始，无法修改。请等待拍卖会结束后再进行修改。");
                }
            }

            boolean success = bidIncrementService.updateConfig(request.getConfig(), request.getRules());
            if (success) {
                return Result.success("加价阶梯配置更新成功");
            } else {
                return Result.error("加价阶梯配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新加价阶梯配置失败", e);
            return Result.error("更新加价阶梯配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/config/{configId}")
    @Operation(summary = "删除加价阶梯配置", description = "删除指定的加价阶梯配置")
    public Result<String> deleteConfig(
            @Parameter(description = "配置ID") @PathVariable Long configId) {
        try {
            // 前端校验：检查是否有拍卖会正在使用该配置且已开始
            boolean canModify = bidIncrementService.canModifyConfigForSession(configId, null);
            if (!canModify) {
                return Result.error("该加价阶梯配置已被拍卖会使用且拍卖会已开始，无法删除。请等待拍卖会结束后再进行删除。");
            }

            boolean success = bidIncrementService.deleteConfig(configId);
            if (success) {
                return Result.success("加价阶梯配置删除成功");
            } else {
                return Result.error("加价阶梯配置删除失败");
            }
        } catch (Exception e) {
            log.error("删除加价阶梯配置失败", e);
            return Result.error("删除加价阶梯配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/config/{configId}")
    @Operation(summary = "查询加价阶梯配置详情", description = "根据ID查询加价阶梯配置及规则详情")
    public Result<String> getConfig(
            @Parameter(description = "配置ID") @PathVariable Long configId) {
        try {
            BidIncrementConfig config = bidIncrementService.getConfigById(configId);
            if (config != null) {
                return Result.success("查询成功，配置名称: " + config.getConfigName());
            } else {
                return Result.error("加价阶梯配置不存在");
            }
        } catch (Exception e) {
            log.error("查询加价阶梯配置失败", e);
            return Result.error("查询加价阶梯配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/configs")
    @Operation(summary = "查询加价阶梯配置列表", description = "查询所有加价阶梯配置列表")
    public Result<String> getAllConfigs() {
        try {
            List<BidIncrementConfig> configs = bidIncrementService.getAllConfigs();
            return Result.success("查询成功，共找到 " + configs.size() + " 个配置");
        } catch (Exception e) {
            log.error("查询加价阶梯配置列表失败", e);
            return Result.error("查询加价阶梯配置列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/configs/enabled")
    @Operation(summary = "查询启用的加价阶梯配置", description = "查询所有启用的加价阶梯配置列表")
    public Result<String> getEnabledConfigs() {
        try {
            List<BidIncrementConfig> configs = bidIncrementService.getEnabledConfigs();
            return Result.success("查询成功，共找到 " + configs.size() + " 个启用的配置");
        } catch (Exception e) {
            log.error("查询启用的加价阶梯配置失败", e);
            return Result.error("查询启用的加价阶梯配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/next-bid")
    @Operation(summary = "获取下一次最低出价金额", description = "根据当前价格和配置ID计算下一次最低出价金额")
    public Result<String> getNextMinimumBid(@RequestBody NextBidRequest request) {
        try {
            BigDecimal nextBid = bidIncrementService.getNextMinimumBid(
                request.getCurrentPrice(),
                request.getConfigId()
            );
            return Result.success("下一次最低出价: " + nextBid + "元");
        } catch (Exception e) {
            log.error("获取下一次最低出价金额失败", e);
            return Result.error("获取下一次最低出价金额失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate-bid")
    @Operation(summary = "校验出价金额", description = "校验出价金额是否符合加价阶梯规则")
    public Result<String> validateBidAmount(@RequestBody ValidateBidRequest request) {
        try {
            boolean isValid = bidIncrementService.validateBidAmount(
                request.getCurrentPrice(),
                request.getBidAmount(),
                request.getConfigId()
            );
            return Result.success(isValid ? "出价金额符合规则" : "出价金额不符合规则");
        } catch (Exception e) {
            log.error("校验出价金额失败", e);
            return Result.error("校验出价金额失败: " + e.getMessage());
        }
    }

    /**
     * 前端集成指南：
     *
     * 1. 出价校验：在用户输入出价金额时，调用 /api/bid-increment/validate-bid 接口校验
     * 2. 获取下一次最低出价：调用 /api/bid-increment/next-bid 接口获取建议出价金额
     * 3. WebSocket实时推送：竞价房间内会自动推送加价规则和下一次最低出价金额
     * 4. 配置管理限制：拍卖会开始后，加价阶梯配置无法修改或删除
     *
     * WebSocket消息类型：
     * - BID_INCREMENT_RULES: 加价规则推送（用户加入拍卖时）
     * - NEXT_MINIMUM_BID: 下一次最低出价推送（每次出价后）
     *
     * 前端最佳实践：
     * 1. 用户加入拍卖时，接收并缓存加价规则
     * 2. 当前端接收到价格更新时，根据缓存的规则自动计算下一次最低出价
     * 3. 用户输入出价金额时，先用缓存的规则校验，校验失败时调用后端接口
     * 4. 出价成功后，自动更新出价框为新的下一次最低出价
     * 5. 配置管理页面需检查拍卖会状态，已开始的拍卖会对应的配置应禁用编辑/删除按钮
     */

    // 请求DTO类
    public static class CreateConfigRequest {
        private BidIncrementConfig config;
        private List<BidIncrementRule> rules;

        public BidIncrementConfig getConfig() { return config; }
        public void setConfig(BidIncrementConfig config) { this.config = config; }
        public List<BidIncrementRule> getRules() { return rules; }
        public void setRules(List<BidIncrementRule> rules) { this.rules = rules; }
    }

    public static class UpdateConfigRequest {
        private BidIncrementConfig config;
        private List<BidIncrementRule> rules;

        public BidIncrementConfig getConfig() { return config; }
        public void setConfig(BidIncrementConfig config) { this.config = config; }
        public List<BidIncrementRule> getRules() { return rules; }
        public void setRules(List<BidIncrementRule> rules) { this.rules = rules; }
    }

    public static class NextBidRequest {
        private BigDecimal currentPrice;
        private Long configId;

        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public Long getConfigId() { return configId; }
        public void setConfigId(Long configId) { this.configId = configId; }
    }

    public static class ValidateBidRequest {
        private BigDecimal currentPrice;
        private BigDecimal bidAmount;
        private Long configId;

        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getBidAmount() { return bidAmount; }
        public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
        public Long getConfigId() { return configId; }
        public void setConfigId(Long configId) { this.configId = configId; }
    }
}
