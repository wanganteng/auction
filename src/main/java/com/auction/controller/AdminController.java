package com.auction.controller;

import com.auction.entity.AuctionItem;
import com.auction.entity.AuctionSession;
import com.auction.entity.AuctionOrder;
import com.auction.entity.AuctionLogistics;
import com.auction.entity.UserDepositAccount;
import com.auction.entity.UserDepositTransaction;
import com.auction.entity.UserDepositRefund;
import com.auction.entity.SysUser;
import com.auction.entity.SysConfig;
import com.auction.util.SecurityUtils;
import com.auction.service.AuctionItemService;
import com.auction.service.AuctionSessionService;
import com.auction.service.AuctionOrderService;
import com.auction.service.AuctionLogisticsService;
import com.auction.service.UserDepositAccountService;
import com.auction.service.UserDepositTransactionService;
import com.auction.service.UserDepositRefundService;
import com.auction.service.SysConfigService;
import com.auction.service.MinioService;
import com.auction.service.BidIncrementService;
import com.auction.entity.BidIncrementConfig;
import com.auction.entity.BidIncrementRule;
import com.auction.common.Result;
import com.auction.service.impl.AuctionOrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 超级管理员控制器
 * 包含拍品管理、拍卖会管理等功能
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@Tag(name = "超级管理", description = "超级管理员相关接口")
public class AdminController {

    @Autowired
    private AuctionItemService auctionItemService;

    @Autowired
    private AuctionSessionService auctionSessionService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private AuctionOrderService auctionOrderService;

    @Autowired
    private com.auction.mapper.AuctionSessionMapper auctionSessionMapper;

    @Autowired
    private com.auction.mapper.AuctionItemMapper auctionItemMapper;

    @Autowired
    private com.auction.mapper.AuctionOrderMapper auctionOrderMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuctionLogisticsService auctionLogisticsService;

    @Autowired
    private UserDepositAccountService userDepositAccountService;

    @Autowired
    private UserDepositTransactionService userDepositTransactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.auction.service.AuditLogService auditLogService;

    @Autowired
    private UserDepositRefundService userDepositRefundService;

    @Autowired
    private com.auction.service.AuctionSettlementService auctionSettlementService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private BidIncrementService bidIncrementService;

    // ==================== 拍品管理 ====================

    /**
     * 上传拍品
     */
    @PostMapping("/items/upload")
    @Operation(summary = "上传拍品", description = "超级管理员上传拍品")
    public Result<Map<String, Object>> uploadItem(
            @ModelAttribute AuctionItem item,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        try {
            // 获取当前用户
            SysUser currentUser = SecurityUtils.getCurrentUser();
            item.setUploaderId(currentUser.getId());

            // 创建拍品
            Long itemId = auctionItemService.createItem(item, imageFiles);

            Map<String, Object> data = new HashMap<>();
            data.put("itemId", itemId);

            return Result.success("拍品上传成功", data);
        } catch (Exception e) {
            log.error("拍品上传失败: {}", e.getMessage(), e);
            return Result.error("拍品上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取管理后台统计数据
     */
    @GetMapping("/sessions/stats")
    @Operation(summary = "获取管理后台统计数据", description = "获取拍品数、拍卖会数、订单数、出价数等")
    public Result<Map<String, Object>> getAdminStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            // 使用数据库COUNT，避免大结果集
            Integer totalItems = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auction_item WHERE deleted=0", Integer.class);
            Integer totalSessions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auction_session WHERE deleted=0", Integer.class);
            Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE deleted=0", Integer.class);
            Integer totalBids = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auction_bid WHERE deleted=0", Integer.class);

            Integer ongoingSessions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auction_session WHERE deleted=0 AND status=2", Integer.class);
            Integer endedSessions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auction_session WHERE deleted=0 AND status=3", Integer.class);

            stats.put("totalItems", totalItems);
            stats.put("totalSessions", totalSessions);
            stats.put("totalUsers", totalUsers);
            stats.put("totalBids", totalBids);
            stats.put("ongoingSessions", ongoingSessions);
            stats.put("endedSessions", endedSessions);
            // 可扩展：订单、营收等
            return Result.success("获取统计数据成功", stats);
        } catch (Exception e) {
            log.error("获取管理后台统计数据失败: {}", e.getMessage(), e);
            return Result.success("获取统计数据成功", Collections.emptyMap());
        }
    }

    /**
     * 仪表盘总览统计（拍品数/拍卖会数/用户数/出价数）
     */
    @GetMapping("/dashboard/overview")
    @Operation(summary = "仪表盘总览统计", description = "拍品总数、拍卖会总数、用户总数、出价总数")
    public Result<Map<String, Object>> getDashboardOverview() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalItems", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auction_item WHERE deleted=0", Integer.class));
            stats.put("totalSessions", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auction_session WHERE deleted=0", Integer.class));
            stats.put("totalUsers", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE deleted=0", Integer.class));
            stats.put("totalBids", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auction_bid WHERE deleted=0", Integer.class));
            return Result.success("获取总览统计成功", stats);
        } catch (Exception e) {
            log.error("获取总览统计失败: {}", e.getMessage(), e);
            return Result.success("获取总览统计成功", Collections.emptyMap());
        }
    }

    

    /**
     * 查询拍品列表
     */
    @GetMapping("/items")
    @Operation(summary = "查询拍品列表", description = "查询所有拍品")
    public Result<List<AuctionItem>> getItemList(AuctionItem item) {
        try {
            List<AuctionItem> items = auctionItemService.getItemList(item);
            return Result.success("查询成功", items);

        } catch (Exception e) {
            log.error("查询拍品列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询拍品
     */
    @GetMapping("/items/{id}")
    @Operation(summary = "查询拍品详情", description = "根据ID查询拍品详情")
    public Result<AuctionItem> getItemById(@PathVariable Long id) {
        try {
            AuctionItem item = auctionItemService.getItemById(id);
            if (item != null) {
                return Result.success("查询成功", item);
            } else {
                return Result.error("拍品不存在");
            }

        } catch (Exception e) {
            log.error("查询拍品失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除拍品
     */
    @DeleteMapping("/items/{id}")
    @Operation(summary = "删除拍品", description = "删除拍品")
    public Result<String> deleteItem(@PathVariable Long id) {
        try {
            boolean success = auctionItemService.deleteItem(id);
            if (success) {
                return Result.success("拍品删除成功");
            } else {
                return Result.error("拍品删除失败");
            }

        } catch (Exception e) {
            log.error("删除拍品失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/items/{id}/status")
    @Operation(summary = "更新拍品状态", description = "更新拍品上下架状态")
    public Result<String> updateItemStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            // 检查拍品是否关联到未开始或进行中的拍卖会
            if (status == 0) { // 下架操作
                boolean hasActiveSessions = auctionSessionService.hasActiveSessionsForItem(id);
                if (hasActiveSessions) {
                    return Result.error("该拍品已关联到未开始或进行中的拍卖会，请先从拍卖会中移除后再下架");
                }
            }
            
            boolean success = auctionItemService.updateItemStatus(id, status);
            return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
        } catch (Exception e) {
            log.error("更新拍品状态失败: {}", e.getMessage(), e);
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍品（JSON格式）
     */
    @PutMapping("/items/{id}")
    @Operation(summary = "更新拍品", description = "更新拍品信息")
    public Result<String> updateItem(@PathVariable Long id, @RequestBody AuctionItem item) {
        try {
            item.setId(id);
            
            // 如果状态变更为下架，检查拍卖会关联
            if (item.getStatus() != null && item.getStatus() == 0) {
                boolean hasActiveSessions = auctionSessionService.hasActiveSessionsForItem(id);
                if (hasActiveSessions) {
                    return Result.error("该拍品已关联到未开始或进行中的拍卖会，请先从拍卖会中移除后再下架");
                }
            }
            
            boolean success = auctionItemService.updateItem(item);
            
            if (success) {
                return Result.success("拍品更新成功");
            } else {
                return Result.error("拍品更新失败");
            }

        } catch (Exception e) {
            log.error("拍品更新失败: {}", e.getMessage(), e);
            return Result.error("拍品更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍品（支持图片上传）
     */
    @PutMapping("/items/{id}/with-images")
    @Operation(summary = "更新拍品（含图片）", description = "更新拍品信息并处理图片上传")
    public Result<String> updateItemWithImages(
            @PathVariable Long id,
            @RequestParam("itemName") String itemName,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "itemCode", required = false) String itemCode,
            @RequestParam("startingPrice") Double startingPrice,
            @RequestParam(value = "estimatedPrice", required = false) Double estimatedPrice,
            @RequestParam(value = "dimensions", required = false) String dimensions,
            @RequestParam(value = "material", required = false) String material,
            @RequestParam("description") String description,
            @RequestParam("status") Integer status,
            @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "currentImages", required = false) String currentImages) {
        try {
            // 如果状态变更为下架，检查拍卖会关联
            if (status == 0) {
                boolean hasActiveSessions = auctionSessionService.hasActiveSessionsForItem(id);
                if (hasActiveSessions) {
                    return Result.error("该拍品已关联到未开始或进行中的拍卖会，请先从拍卖会中移除后再下架");
                }
            }
            
            boolean success = auctionItemService.updateItemWithImages(id, itemName, categoryId, itemCode, startingPrice, estimatedPrice, dimensions, material, description, status, imageFiles, currentImages);
            
            if (success) {
                return Result.success("拍品更新成功");
            } else {
                return Result.error("拍品更新失败");
            }

        } catch (Exception e) {
            log.error("拍品更新失败: {}", e.getMessage(), e);
            return Result.error("拍品更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除拍品（重复定义已移除）
     */

    // ==================== 拍卖会管理 ====================

    /**
     * 创建拍卖会
     */
    @PostMapping("/sessions")
    @Operation(summary = "创建拍卖会", description = "创建新的拍卖会")
    public Result<Map<String, Object>> createSession(@RequestBody Map<String, Object> sessionData) {
        try {
            // 解析拍卖会数据
            String sessionName = sessionData.get("sessionName").toString();
            String sessionType = sessionData.get("sessionType").toString();
            String description = sessionData.get("description").toString();
            String startTimeStr = sessionData.get("startTime").toString();
            String endTimeStr = sessionData.get("endTime").toString();
            
            // 解析拍卖参数
            BigDecimal depositRatio = new BigDecimal(sessionData.get("depositRatio").toString());
            BigDecimal commissionRatio = new BigDecimal(sessionData.get("commissionRatio").toString());
            Long bidIncrementConfigId = sessionData.get("bidIncrementConfigId") == null ? null : Long.valueOf(sessionData.get("bidIncrementConfigId").toString());
            Integer depositRefundDays = Integer.valueOf(sessionData.get("depositRefundDays").toString());
            
            // 解析拍品ID列表
            @SuppressWarnings("unchecked")
            List<Integer> itemIds = (List<Integer>) sessionData.get("itemIds");
            
            // 创建拍卖会对象
            AuctionSession session = new AuctionSession();
            session.setSessionName(sessionName);
            session.setDescription(description);
            // 保存拍卖会类型
            try {
                session.setSessionType(Integer.valueOf(sessionType));
            } catch (Exception ignore) {}
            session.setStartTime(LocalDateTime.parse(startTimeStr.replace("Z", "")));
            session.setEndTime(LocalDateTime.parse(endTimeStr.replace("Z", "")));
            session.setDepositRatio(depositRatio);
            session.setCommissionRatio(commissionRatio);
            session.setBidIncrementConfigId(bidIncrementConfigId);
            
            // 设置默认值
            session.setStatus(1); // 1-待开始
            session.setIsAuthentic(1); // 默认保真
            session.setIsFreeShipping(1); // 默认包邮
            session.setIsReturnable(1); // 默认支持退货
            session.setTotalItems(0);
            session.setSoldItems(0);
            session.setViewCount(0);
            session.setDeleted(0);
            
            // 转换itemIds为Long类型
            List<Long> longItemIds = itemIds.stream().map(Integer::longValue).collect(Collectors.toList());
            
            // 创建拍卖会
            Long sessionId = auctionSessionService.createSession(session, longItemIds, null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            
            return Result.success("拍卖会创建成功", result);

        } catch (Exception e) {
            log.error("创建拍卖会失败: {}", e.getMessage(), e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 创建拍卖会（含多图）
     */
    @PostMapping(value = "/sessions/with-images")
    @Operation(summary = "创建拍卖会（含多图）", description = "multipart提交，支持会场多张图片，第一张为封面")
    public Result<Map<String, Object>> createSessionWithImages(
            @RequestParam("sessionName") String sessionName,
            @RequestParam(value = "sessionType", required = false) Integer sessionType,
            @RequestParam("description") String description,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam("depositRatio") BigDecimal depositRatio,
            @RequestParam("commissionRatio") BigDecimal commissionRatio,
            @RequestParam(value = "isAuthentic", required = false) Integer isAuthentic,
            @RequestParam(value = "isFreeShipping", required = false) Integer isFreeShipping,
            @RequestParam(value = "isReturnable", required = false) Integer isReturnable,
            @RequestParam(value = "antiSnipingEnabled", required = false) Integer antiSnipingEnabled,
            @RequestParam(value = "extendThresholdSec", required = false) Integer extendThresholdSec,
            @RequestParam(value = "extendSeconds", required = false) Integer extendSeconds,
            @RequestParam(value = "extendMaxTimes", required = false) Integer extendMaxTimes,
            @RequestParam(value = "rules", required = false) String rules,
            @RequestParam(value = "itemIds", required = false) List<Long> itemIds,
            @RequestParam(value = "currentImages", required = false) String currentImages,
            @RequestParam(value = "coverImages", required = false) List<MultipartFile> coverImages
    ) {
        try {
            AuctionSession session = new AuctionSession();
            session.setSessionName(sessionName);
            session.setSessionType(sessionType);
            session.setDescription(description);
            session.setStartTime(parseDateTimeFlexible(startTime));
            session.setEndTime(parseDateTimeFlexible(endTime));
            session.setDepositRatio(depositRatio);
            session.setCommissionRatio(commissionRatio);
            if (isAuthentic != null) session.setIsAuthentic(isAuthentic);
            if (isFreeShipping != null) session.setIsFreeShipping(isFreeShipping);
            if (isReturnable != null) session.setIsReturnable(isReturnable);
            if (antiSnipingEnabled != null) session.setAntiSnipingEnabled(antiSnipingEnabled);
            session.setExtendThresholdSec(extendThresholdSec);
            session.setExtendSeconds(extendSeconds);
            session.setExtendMaxTimes(extendMaxTimes);
            session.setRules(rules);

            Long sessionId = auctionSessionService.createSessionWithImages(session, coverImages, currentImages, itemIds);
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            return Result.success("拍卖会创建成功", result);
        } catch (Exception e) {
            log.error("创建拍卖会(含多图)失败: {}", e.getMessage(), e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }


    /**
     * 查询拍卖会列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "查询拍卖会列表", description = "查询所有拍卖会")
    public Result<List<AuctionSession>> getSessionList(AuctionSession session) {
        try {
            List<AuctionSession> sessions = auctionSessionService.getSessionList(session);
            return Result.success("查询成功", sessions);

        } catch (Exception e) {
            log.error("查询拍卖会列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询拍卖会
     */
    @GetMapping("/sessions/{id}")
    @Operation(summary = "查询拍卖会详情", description = "根据ID查询拍卖会详情")
    public Result<AuctionSession> getSessionById(@PathVariable Long id) {
        try {
            AuctionSession session = auctionSessionService.getSessionById(id);
            if (session != null) {
                return Result.success("查询成功", session);
            } else {
                return Result.error("拍卖会不存在");
            }

        } catch (Exception e) {
            log.error("查询拍卖会失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍卖会
     */
    @PutMapping("/sessions/{id}")
    @Operation(summary = "更新拍卖会", description = "更新拍卖会信息")
    public Result<String> updateSession(@PathVariable Long id, @RequestBody AuctionSession session) {
        try {
            session.setId(id);
            boolean success = auctionSessionService.updateSession(session, null, null);
            
            if (success) {
                return Result.success("拍卖会更新成功");
            } else {
                return Result.error("拍卖会更新失败");
            }

        } catch (Exception e) {
            log.error("拍卖会更新失败: {}", e.getMessage(), e);
            return Result.error("拍卖会更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍卖会（含封面图）
     */
    @PutMapping(value = "/sessions/{id}/with-cover")
    @Operation(summary = "更新拍卖会（含多图）", description = "表单提交，支持多图，第一张为封面")
    public Result<String> updateSessionWithCover(
            @PathVariable Long id,
            @RequestParam(value = "sessionName", required = false) String sessionName,
            @RequestParam(value = "sessionType", required = false) Integer sessionType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "depositRatio", required = false) BigDecimal depositRatio,
            @RequestParam(value = "commissionRatio", required = false) BigDecimal commissionRatio,
            @RequestParam(value = "isAuthentic", required = false) Integer isAuthentic,
            @RequestParam(value = "isFreeShipping", required = false) Integer isFreeShipping,
            @RequestParam(value = "isReturnable", required = false) Integer isReturnable,
            @RequestParam(value = "antiSnipingEnabled", required = false) Integer antiSnipingEnabled,
            @RequestParam(value = "extendThresholdSec", required = false) Integer extendThresholdSec,
            @RequestParam(value = "extendSeconds", required = false) Integer extendSeconds,
            @RequestParam(value = "extendMaxTimes", required = false) Integer extendMaxTimes,
            @RequestParam(value = "rules", required = false) String rules,
            @RequestParam(value = "bidIncrementConfigId", required = false) Long bidIncrementConfigId,
            @RequestParam(value = "currentImages", required = false) String currentImages,
            @RequestParam(value = "itemIds", required = false) String itemIds,
            @RequestParam(value = "coverImages", required = false) List<MultipartFile> coverImages
    ) {
        try {
            AuctionSession session = new AuctionSession();
            session.setId(id);
            session.setSessionName(sessionName);
            session.setSessionType(sessionType);
            session.setDescription(description);
            if (startTime != null && !startTime.isEmpty()) {
                session.setStartTime(parseDateTimeFlexible(startTime));
            }
            if (endTime != null && !endTime.isEmpty()) {
                session.setEndTime(parseDateTimeFlexible(endTime));
            }
            session.setDepositRatio(depositRatio);
            session.setCommissionRatio(commissionRatio);
            if (isAuthentic != null) session.setIsAuthentic(isAuthentic);
            if (isFreeShipping != null) session.setIsFreeShipping(isFreeShipping);
            if (isReturnable != null) session.setIsReturnable(isReturnable);
            if (antiSnipingEnabled != null) session.setAntiSnipingEnabled(antiSnipingEnabled);
            session.setExtendThresholdSec(extendThresholdSec);
            session.setExtendSeconds(extendSeconds);
            session.setExtendMaxTimes(extendMaxTimes);
            session.setRules(rules);
            if (bidIncrementConfigId != null) session.setBidIncrementConfigId(bidIncrementConfigId);

            // 解析拍品ID列表
            List<Long> itemIdList = null;
            if (itemIds != null && !itemIds.isEmpty()) {
                try {
                    itemIdList = objectMapper.readValue(itemIds, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
                } catch (Exception e) {
                    log.warn("解析拍品ID列表失败: {}", e.getMessage());
                }
            }

            // 使用含多图的统一更新
            boolean success = auctionSessionService.updateSessionWithImages(session, coverImages, currentImages, itemIdList);
            return success ? Result.success("拍卖会更新成功") : Result.error("拍卖会更新失败");
        } catch (Exception e) {
            log.error("拍卖会更新失败(含封面图): {}", e.getMessage(), e);
            return Result.error("拍卖会更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取拍卖会的拍品列表
     */
    @GetMapping("/sessions/{id}/items")
    @Operation(summary = "获取拍卖会的拍品列表", description = "获取指定拍卖会关联的所有拍品")
    public Result<List<AuctionItem>> getSessionItems(@PathVariable Long id) {
        try {
            List<AuctionItem> items = auctionSessionService.getSessionItems(id);
            return Result.success("获取成功", items);
        } catch (Exception e) {
            log.error("获取拍卖会拍品列表失败: sessionId={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("获取拍卖会拍品列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除拍卖会
     */
    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "删除拍卖会", description = "删除拍卖会")
    public Result<String> deleteSession(@PathVariable Long id) {
        try {
            boolean success = auctionSessionService.deleteSession(id);
            
            if (success) {
                return Result.success("拍卖会删除成功");
            } else {
                return Result.error("拍卖会删除失败");
            }

        } catch (Exception e) {
            log.error("拍卖会删除失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("拍卖会删除失败: " + e.getMessage());
        }
    }

    /**
     * 切换拍卖会可见性
     */
    @PostMapping("/sessions/{id}/visible")
    @Operation(summary = "切换拍卖会可见性", description = "value=1 展示，value=0 隐藏")
    public Result<String> toggleSessionVisible(@PathVariable Long id, @RequestParam Integer value) {
        try {
            AuctionSession session = new AuctionSession();
            session.setId(id);
            session.setIsVisible(value != null && value == 1 ? 1 : 0);
            session.setUpdateTime(java.time.LocalDateTime.now());
            int updated = auctionSessionMapper.updateById(session);
            return updated > 0 ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新可见性失败: {}", e.getMessage(), e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }


    /**
     * 获取可用的拍品列表
     */
    @GetMapping("/items/available")
    @Operation(summary = "获取可用拍品", description = "获取已上架的拍品列表")
    public Result<List<AuctionItem>> getAvailableItems() {
        try {
            // 仅返回上架且未被未开始/进行中的会场占用的拍品
            List<AuctionItem> items = auctionSessionService.getAvailableItems();
            return Result.success("查询成功", items);

        } catch (Exception e) {
            log.error("查询可用拍品失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // ==================== 保证金审核管理 ====================

    /**
     * 获取待审核的保证金交易列表
     */
    @GetMapping("/deposits/pending")
    @Operation(summary = "获取待审核列表", description = "获取待审核的充值和提现申请")
    public Result<List<UserDepositTransaction>> getPendingDeposits(
            @RequestParam(required = false) Integer type) {
        try {
            List<UserDepositTransaction> transactions = userDepositTransactionService.getPendingTransactions(type);
            return Result.success("查询成功", transactions);
        } catch (Exception e) {
            log.error("获取待审核列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过充值申请
     */
    @PostMapping("/deposits/{id}/approve-recharge")
    @Operation(summary = "审核通过充值", description = "审核通过用户充值申请")
    public Result<String> approveRecharge(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> params) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            String remark = params != null ? params.get("remark") : null;
            
            boolean success = userDepositAccountService.approveRecharge(id, currentUser.getId(), remark);
            return success ? Result.success("审核通过") : Result.error("审核失败");
        } catch (Exception e) {
            log.error("审核充值失败: id={}, error={}", id, e.getMessage(), e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过提现申请
     */
    @PostMapping("/deposits/{id}/approve-withdraw")
    @Operation(summary = "审核通过提现", description = "审核通过用户提现申请")
    public Result<String> approveWithdraw(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> params) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            String remark = params != null ? params.get("remark") : null;
            
            boolean success = userDepositAccountService.approveWithdraw(id, currentUser.getId(), remark);
            return success ? Result.success("审核通过") : Result.error("审核失败");
        } catch (Exception e) {
            log.error("审核提现失败: id={}, error={}", id, e.getMessage(), e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝充值/提现申请
     */
    @PostMapping("/deposits/{id}/reject")
    @Operation(summary = "拒绝申请", description = "拒绝用户充值或提现申请")
    public Result<String> rejectDeposit(
            @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            String remark = params.get("remark");
            
            if (remark == null || remark.trim().isEmpty()) {
                return Result.error("请填写拒绝原因");
            }
            
            boolean success = userDepositAccountService.rejectTransaction(id, currentUser.getId(), remark);
            return success ? Result.success("已拒绝") : Result.error("操作失败");
        } catch (Exception e) {
            log.error("拒绝申请失败: id={}, error={}", id, e.getMessage(), e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    // ==================== 订单管理 ====================

    /**
     * 查询订单列表
     */
    @GetMapping("/orders")
    @Operation(summary = "查询订单列表", description = "查询所有订单")
    public Result<List<AuctionOrder>> getOrderList(AuctionOrder order) {
        try {
            List<AuctionOrder> orders = auctionOrderService.getOrderList(order);
            return Result.success("查询成功", orders);

        } catch (Exception e) {
            log.error("查询订单列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取待发货订单列表
     */
    @GetMapping("/orders/pending-ship")
    @Operation(summary = "获取待发货订单", description = "获取所有已支付待发货的订单")
    public Result<List<AuctionOrder>> getPendingShipOrders() {
        try {
            // 查询状态为2（已支付）的订单
            List<AuctionOrder> allOrders = auctionOrderService.getOrderList(new AuctionOrder());
            List<AuctionOrder> pendingOrders = allOrders.stream()
                .filter(order -> order.getStatus() != null && order.getStatus() == 2)
                .collect(Collectors.toList());
            
            return Result.success("查询成功", pendingOrders);
        } catch (Exception e) {
            log.error("获取待发货订单失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝发货（含退款）
     */
    @PostMapping("/orders/{id}/reject-shipment")
    @Operation(summary = "拒绝发货", description = "拒绝发货并全额退款给买家")
    public Result<String> rejectShipment(
            @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        try {
            String reason = params.get("reason");
            
            if (reason == null || reason.trim().isEmpty()) {
                return Result.error("请填写拒绝发货原因");
            }
            
            if (auctionOrderService instanceof AuctionOrderServiceImpl) {
                boolean success = ((AuctionOrderServiceImpl) auctionOrderService).rejectShipment(id, reason);
                return success ? Result.success("已拒绝发货并退款") : Result.error("操作失败");
            } else {
                return Result.error("服务不可用");
            }
        } catch (Exception e) {
            log.error("拒绝发货失败: orderId={}, error={}", id, e.getMessage(), e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 发货
     */
    @PostMapping("/orders/{id}/ship")
    @Operation(summary = "发货", description = "订单发货")
    public Result<String> shipOrder(@PathVariable Long id) {
        try {
            boolean success = auctionOrderService.shipOrder(id);
            
            if (success) {
                return Result.success("发货成功");
            } else {
                return Result.error("发货失败");
            }

        } catch (Exception e) {
            log.error("发货失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("发货失败: " + e.getMessage());
        }
    }

    /**
     * 完成订单
     */
    @PostMapping("/orders/{id}/complete")
    @Operation(summary = "完成订单", description = "完成订单")
    public Result<String> completeOrder(@PathVariable Long id) {
        try {
            boolean success = auctionOrderService.completeOrder(id);
            
            if (success) {
                return Result.success("订单完成成功");
            } else {
                return Result.error("订单完成失败");
            }

        } catch (Exception e) {
            log.error("订单完成失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("订单完成失败: " + e.getMessage());
        }
    }

    // ==================== 审计日志管理 ====================

    /**
     * 查询审计日志
     */
    @GetMapping("/audit-logs")
    @Operation(summary = "查询审计日志", description = "查询系统审计日志")
    public Result<Map<String, Object>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer success,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime.replace(" ", "T")) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime.replace(" ", "T")) : null;
            
            Map<String, Object> result = auditLogService.queryLogs(
                userId, operationType, module, success, start, end, pageNum, pageSize
            );
            
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询审计日志失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // ==================== 物流公司配置管理 ====================

    @Autowired
    private com.auction.service.LogisticsCompanyService logisticsCompanyService;

    /**
     * 创建物流公司配置
     */
    @PostMapping("/logistics-companies")
    @Operation(summary = "创建物流公司配置", description = "创建新的物流公司配置")
    public Result<String> createLogisticsCompany(@RequestBody com.auction.entity.LogisticsCompany company) {
        try {
            boolean success = logisticsCompanyService.createCompany(company);
            if (success) {
                return Result.success("物流公司配置创建成功");
            } else {
                return Result.error("物流公司配置创建失败");
            }
        } catch (Exception e) {
            log.error("创建物流公司配置失败: {}", e.getMessage(), e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新物流公司配置
     */
    @PutMapping("/logistics-companies/{id}")
    @Operation(summary = "更新物流公司配置", description = "更新物流公司配置")
    public Result<String> updateLogisticsCompany(@PathVariable Long id, @RequestBody com.auction.entity.LogisticsCompany company) {
        try {
            company.setId(id);
            boolean success = logisticsCompanyService.updateCompany(company);
            if (success) {
                return Result.success("物流公司配置更新成功");
            } else {
                return Result.error("物流公司配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新物流公司配置失败: {}", e.getMessage(), e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除物流公司配置
     */
    @DeleteMapping("/logistics-companies/{id}")
    @Operation(summary = "删除物流公司配置", description = "删除物流公司配置")
    public Result<String> deleteLogisticsCompany(@PathVariable Long id) {
        try {
            boolean success = logisticsCompanyService.deleteCompany(id);
            if (success) {
                return Result.success("物流公司配置删除成功");
            } else {
                return Result.error("物流公司配置删除失败");
            }
        } catch (Exception e) {
            log.error("删除物流公司配置失败: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询物流公司配置列表
     */
    @GetMapping("/logistics-companies")
    @Operation(summary = "查询物流公司配置列表", description = "查询物流公司配置列表")
    public Result<com.github.pagehelper.PageInfo<com.auction.entity.LogisticsCompany>> getLogisticsCompanies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            com.github.pagehelper.PageInfo<com.auction.entity.LogisticsCompany> pageInfo = 
                logisticsCompanyService.getAllCompanies(page, size);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            log.error("查询物流公司配置列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取启用的物流公司列表
     */
    @GetMapping("/logistics-companies/enabled")
    @Operation(summary = "获取启用的物流公司列表", description = "获取启用的物流公司列表")
    public Result<List<com.auction.entity.LogisticsCompany>> getEnabledLogisticsCompanies() {
        try {
            List<com.auction.entity.LogisticsCompany> companies = logisticsCompanyService.getEnabledCompanies();
            return Result.success("查询成功", companies);
        } catch (Exception e) {
            log.error("获取启用的物流公司列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 计算物流费用
     */
    @PostMapping("/logistics-companies/calculate-fee")
    @Operation(summary = "计算物流费用", description = "根据物流公司和订单金额计算物流费用")
    public Result<java.math.BigDecimal> calculateLogisticsFee(@RequestBody Map<String, Object> params) {
        try {
            Long companyId = Long.valueOf(params.get("companyId").toString());
            java.math.BigDecimal orderAmount = new java.math.BigDecimal(params.get("orderAmount").toString());
            
            java.math.BigDecimal fee = logisticsCompanyService.calculateShippingFee(companyId, orderAmount);
            return Result.success("计算成功", fee);
        } catch (Exception e) {
            log.error("计算物流费用失败: {}", e.getMessage(), e);
            return Result.error("计算失败: " + e.getMessage());
        }
    }

    // ==================== 物流管理 ====================

    /**
     * 创建物流信息
     */
    @PostMapping("/logistics")
    @Operation(summary = "创建物流信息", description = "为订单创建物流信息")
    public Result<Map<String, Object>> createLogistics(@RequestBody AuctionLogistics logistics) {
        try {
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
    @GetMapping("/logistics")
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
     * 更新物流状态
     */
    @PutMapping("/logistics/{id}/status")
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

    // ==================== 保证金管理 ====================

    /**
     * 查询保证金账户列表
     */
    @GetMapping("/deposit/accounts")
    @Operation(summary = "查询保证金账户列表", description = "查询所有用户的保证金账户")
    public Result<List<UserDepositAccount>> getDepositAccountList(UserDepositAccount account) {
        try {
            List<UserDepositAccount> accounts = userDepositAccountService.getAccountList(account);
            return Result.success("查询成功", accounts);

        } catch (Exception e) {
            log.error("查询保证金账户列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询交易流水列表
     */
    @GetMapping("/deposit/transactions")
    @Operation(summary = "查询交易流水列表", description = "查询所有用户的保证金交易流水")
    public Result<List<UserDepositTransaction>> getDepositTransactionList(UserDepositTransaction transaction) {
        try {
            List<UserDepositTransaction> transactions = userDepositTransactionService.getTransactionList(transaction);
            return Result.success("查询成功", transactions);

        } catch (Exception e) {
            log.error("查询交易流水列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询退款申请列表
     */
    @GetMapping("/deposit/refunds")
    @Operation(summary = "查询退款申请列表", description = "查询所有用户的退款申请")
    public Result<List<UserDepositRefund>> getDepositRefundList(UserDepositRefund refund) {
        try {
            List<UserDepositRefund> refunds = userDepositRefundService.getRefundList(refund);
            return Result.success("查询成功", refunds);

        } catch (Exception e) {
            log.error("查询退款申请列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 审核退款申请
     */
    @PostMapping("/deposit/refunds/{id}/audit")
    @Operation(summary = "审核退款申请", description = "审核用户的退款申请")
    public Result<String> auditRefundApplication(@PathVariable Long id, 
                                               @RequestParam Integer status, 
                                               @RequestParam(required = false) String auditComment) {
        try {
            SysUser currentUser = SecurityUtils.getCurrentUser();
            
            boolean success = userDepositRefundService.auditRefundApplication(
                id, 
                currentUser.getId(), 
                status, 
                auditComment
            );

            if (success) {
                return Result.success("退款申请审核完成");
            } else {
                return Result.error("退款申请审核失败");
            }

        } catch (Exception e) {
            log.error("退款申请审核失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("退款申请审核失败: " + e.getMessage());
        }
    }

    /**
     * 执行退款
     */
    @PostMapping("/deposit/refunds/{id}/execute")
    @Operation(summary = "执行退款", description = "执行退款操作")
    public Result<String> executeRefund(@PathVariable Long id) {
        try {
            boolean success = userDepositRefundService.executeRefund(id);

            if (success) {
                return Result.success("退款执行成功");
            } else {
                return Result.error("退款执行失败");
            }

        } catch (Exception e) {
            log.error("退款执行失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("退款执行失败: " + e.getMessage());
        }
    }

    // ==================== 拍卖会结算 ====================

    @PostMapping("/sessions/{id}/settle")
    @Operation(summary = "手动结算拍卖会", description = "手动触发拍卖会结算，计算成交结果、创建订单、解冻保证金")
    public Result<String> settleSession(@PathVariable Long id) {
        try {
            auctionSettlementService.settleSession(id);
            return Result.success("拍卖会结算成功");
        } catch (Exception e) {
            log.error("拍卖会结算失败: sessionId={}, error={}", id, e.getMessage(), e);
            return Result.error("拍卖会结算失败: " + e.getMessage());
        }
    }

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
     * 获取用户出价统计
     * 
     * @param userId 用户ID
     * @param sessionId 拍卖会ID
     * @return 用户出价统计
     */
    @GetMapping("/users/{userId}/sessions/{sessionId}/bid-statistics")
    @Operation(summary = "获取用户出价统计", description = "获取指定用户在指定拍卖会的出价统计信息")
    public Result<Map<String, Object>> getUserBidStatistics(@PathVariable Long userId, @PathVariable Long sessionId) {
        try {
            Map<String, Object> statistics = auctionSessionService.getUserBidStatistics(userId, sessionId);
            return Result.success("获取用户出价统计成功", statistics);
        } catch (Exception e) {
            log.error("获取用户出价统计失败: 用户ID={}, 拍卖会ID={}, 错误: {}", userId, sessionId, e.getMessage(), e);
            return Result.error("获取用户出价统计失败: " + e.getMessage());
        }
    }

    // ==================== 系统配置管理 ====================

    /**
     * 获取所有系统配置
     */
    @GetMapping("/configs")
    @Operation(summary = "获取所有系统配置", description = "获取系统中所有的配置项，支持按类型和关键词搜索")
    public Result<List<SysConfig>> getAllConfigs(
            @RequestParam(value = "configType", required = false) String configType,
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            List<SysConfig> configs;
            if (configType != null && !configType.trim().isEmpty()) {
                configs = sysConfigService.getConfigsByType(configType);
            } else {
                configs = sysConfigService.getAllConfigs();
            }
            
            // 如果有关键词搜索，进行过滤
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                configs = configs.stream()
                    .filter(config -> 
                        (config.getConfigKey() != null && config.getConfigKey().toLowerCase().contains(lowerKeyword)) ||
                        (config.getDescription() != null && config.getDescription().toLowerCase().contains(lowerKeyword))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            return Result.success("获取系统配置成功", configs);
        } catch (Exception e) {
            log.error("获取系统配置失败: {}", e.getMessage(), e);
            return Result.error("获取系统配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统配置
     */
    @GetMapping("/configs/system")
    @Operation(summary = "获取系统配置", description = "获取系统级别的配置项")
    public Result<List<SysConfig>> getSystemConfigs() {
        try {
            List<SysConfig> configs = sysConfigService.getSystemConfigs();
            return Result.success("获取系统配置成功", configs);
        } catch (Exception e) {
            log.error("获取系统配置失败: {}", e.getMessage(), e);
            return Result.error("获取系统配置失败: " + e.getMessage());
        }
    }

    /**
     * 根据配置键获取配置值
     */
    @GetMapping("/configs/{configKey}")
    @Operation(summary = "获取配置值", description = "根据配置键获取配置值")
    public Result<String> getConfigValue(@PathVariable String configKey) {
        try {
            String value = sysConfigService.getConfigValue(configKey);
            return Result.success("获取配置值成功", value);
        } catch (Exception e) {
            log.error("获取配置值失败: configKey={}, 错误: {}", configKey, e.getMessage(), e);
            return Result.error("获取配置值失败: " + e.getMessage());
        }
    }

    /**
     * 更新配置值
     */
    @PutMapping("/configs/{configKey}")
    @Operation(summary = "更新配置值", description = "根据配置键更新配置值")
    public Result<Boolean> updateConfigValue(@PathVariable String configKey, @RequestBody Map<String, String> request) {
        try {
            String configValue = request.get("configValue");
            if (configValue == null) {
                return Result.error("配置值不能为空");
            }
            
            boolean success = sysConfigService.setConfigValue(configKey, configValue);
            if (success) {
                return Result.success("更新配置值成功", true);
            } else {
                return Result.error("更新配置值失败");
            }
        } catch (Exception e) {
            log.error("更新配置值失败: configKey={}, 错误: {}", configKey, e.getMessage(), e);
            return Result.error("更新配置值失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/configs/batch")
    @Operation(summary = "批量更新配置", description = "批量更新多个配置项")
    public Result<Boolean> batchUpdateConfigs(@RequestBody Map<String, String> configs) {
        try {
            boolean success = sysConfigService.batchUpdateConfigs(configs);
            if (success) {
                return Result.success("批量更新配置成功", true);
            } else {
                return Result.error("批量更新配置失败");
            }
        } catch (Exception e) {
            log.error("批量更新配置失败: 错误: {}", e.getMessage(), e);
            return Result.error("批量更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 创建新配置
     */
    @PostMapping("/configs")
    @Operation(summary = "创建配置", description = "创建新的配置项")
    public Result<Boolean> createConfig(@RequestBody SysConfig config) {
        try {
            boolean success = sysConfigService.createConfig(config);
            if (success) {
                return Result.success("创建配置成功", true);
            } else {
                return Result.error("创建配置失败");
            }
        } catch (Exception e) {
            log.error("创建配置失败: 错误: {}", e.getMessage(), e);
            return Result.error("创建配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新配置
     */
    @PutMapping("/configs/update")
    @Operation(summary = "更新配置", description = "更新配置项")
    public Result<Boolean> updateConfig(@RequestBody SysConfig config) {
        try {
            boolean success = sysConfigService.updateConfig(config);
            if (success) {
                return Result.success("更新配置成功", true);
            } else {
                return Result.error("更新配置失败");
            }
        } catch (Exception e) {
            log.error("更新配置失败: 错误: {}", e.getMessage(), e);
            return Result.error("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/configs/{id}")
    @Operation(summary = "删除配置", description = "根据ID删除配置项")
    public Result<Boolean> deleteConfig(@PathVariable Long id) {
        try {
            boolean success = sysConfigService.deleteConfigById(id);
            if (success) {
                return Result.success("删除配置成功", true);
            } else {
                return Result.error("删除配置失败");
            }
        } catch (Exception e) {
            log.error("删除配置失败: id={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("删除配置失败: " + e.getMessage());
        }
    }

    /**
     * 重新加载配置缓存
     */
    @PostMapping("/configs/reload")
    @Operation(summary = "重新加载配置缓存", description = "重新加载所有配置到缓存中")
    public Result<String> reloadConfigCache() {
        try {
            sysConfigService.reloadConfigCache();
            return Result.success("重新加载配置缓存成功", "配置缓存已重新加载");
        } catch (Exception e) {
            log.error("重新加载配置缓存失败: 错误: {}", e.getMessage(), e);
            return Result.error("重新加载配置缓存失败: " + e.getMessage());
        }
    }

    // ==================== 加价阶梯配置管理 ====================

    /**
     * 创建加价阶梯配置
     */
    @PostMapping("/bid-increment-configs")
    @Operation(summary = "创建加价阶梯配置", description = "创建新的加价阶梯配置")
    public Result<Map<String, Object>> createBidIncrementConfig(@RequestBody Map<String, Object> configData) {
        try {
            // 解析配置数据
            String configName = configData.get("configName").toString();
            String description = configData.get("description").toString();
            Integer status = Integer.valueOf(configData.get("status").toString());
            
            // 解析规则数据
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rulesData = (List<Map<String, Object>>) configData.get("rules");
            
            // 创建配置对象
            BidIncrementConfig config = new BidIncrementConfig();
            config.setConfigName(configName);
            config.setDescription(description);
            config.setStatus(status);
            
            // 创建规则对象列表
            List<BidIncrementRule> rules = new ArrayList<>();
            if (rulesData != null) {
                for (Map<String, Object> ruleData : rulesData) {
                    BidIncrementRule rule = new BidIncrementRule();
                    rule.setMinAmount(new BigDecimal(ruleData.get("minAmount").toString()));
                    rule.setMaxAmount(new BigDecimal(ruleData.get("maxAmount").toString()));
                    rule.setIncrementAmount(new BigDecimal(ruleData.get("incrementAmount").toString()));
                    rule.setSortOrder(Integer.valueOf(ruleData.get("sortOrder").toString()));
                    rules.add(rule);
                }
            }
            
            // 创建配置
            Long configId = bidIncrementService.createConfig(config, rules);
            
            Map<String, Object> result = new HashMap<>();
            result.put("configId", configId);
            
            return Result.success("加价阶梯配置创建成功", result);
            
        } catch (Exception e) {
            log.error("创建加价阶梯配置失败: {}", e.getMessage(), e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 查询加价阶梯配置列表
     */
    @GetMapping("/bid-increment-configs")
    @Operation(summary = "查询加价阶梯配置列表", description = "查询所有加价阶梯配置")
    public Result<List<BidIncrementConfig>> getBidIncrementConfigList() {
        try {
            List<BidIncrementConfig> configs = bidIncrementService.getAllConfigs();
            return Result.success("查询成功", configs);
        } catch (Exception e) {
            log.error("查询加价阶梯配置列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询加价阶梯配置
     */
    @GetMapping("/bid-increment-configs/{id}")
    @Operation(summary = "查询加价阶梯配置详情", description = "根据ID查询加价阶梯配置详情")
    public Result<BidIncrementConfig> getBidIncrementConfigById(@PathVariable Long id) {
        try {
            BidIncrementConfig config = bidIncrementService.getConfigById(id);
            if (config != null) {
                return Result.success("查询成功", config);
            } else {
                return Result.error("加价阶梯配置不存在");
            }
        } catch (Exception e) {
            log.error("查询加价阶梯配置失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新加价阶梯配置
     */
    @PutMapping("/bid-increment-configs/{id}")
    @Operation(summary = "更新加价阶梯配置", description = "更新加价阶梯配置信息")
    public Result<String> updateBidIncrementConfig(@PathVariable Long id, @RequestBody Map<String, Object> configData) {
        try {
            // 解析配置数据
            String configName = configData.get("configName").toString();
            String description = configData.get("description").toString();
            Integer status = Integer.valueOf(configData.get("status").toString());
            
            // 解析规则数据
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rulesData = (List<Map<String, Object>>) configData.get("rules");
            
            // 创建配置对象
            BidIncrementConfig config = new BidIncrementConfig();
            config.setId(id);
            config.setConfigName(configName);
            config.setDescription(description);
            config.setStatus(status);
            
            // 创建规则对象列表
            List<BidIncrementRule> rules = new ArrayList<>();
            if (rulesData != null) {
                for (Map<String, Object> ruleData : rulesData) {
                    BidIncrementRule rule = new BidIncrementRule();
                    rule.setMinAmount(new BigDecimal(ruleData.get("minAmount").toString()));
                    rule.setMaxAmount(new BigDecimal(ruleData.get("maxAmount").toString()));
                    rule.setIncrementAmount(new BigDecimal(ruleData.get("incrementAmount").toString()));
                    rule.setSortOrder(Integer.valueOf(ruleData.get("sortOrder").toString()));
                    rules.add(rule);
                }
            }
            
            boolean success = bidIncrementService.updateConfig(config, rules);
            return success ? Result.success("加价阶梯配置更新成功") : Result.error("加价阶梯配置更新失败");
            
        } catch (Exception e) {
            log.error("更新加价阶梯配置失败: {}", e.getMessage(), e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除加价阶梯配置
     */
    @DeleteMapping("/bid-increment-configs/{id}")
    @Operation(summary = "删除加价阶梯配置", description = "删除加价阶梯配置")
    public Result<String> deleteBidIncrementConfig(@PathVariable Long id) {
        try {
            // 先校验是否被进行中拍卖会使用
            boolean canModify = bidIncrementService.canModifyConfigForSession(id, null);
            if (!canModify) {
                return Result.error("已被拍卖会使用且拍卖会已开始");
            }

            boolean success = bidIncrementService.deleteConfig(id);
            return success ? Result.success("加价阶梯配置删除成功") : Result.error("加价阶梯配置删除失败");
        } catch (Exception e) {
            log.error("删除加价阶梯配置失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取启用的加价阶梯配置列表
     */
    @GetMapping("/bid-increment-configs/enabled")
    @Operation(summary = "获取启用的加价阶梯配置", description = "获取所有启用的加价阶梯配置")
    public Result<List<BidIncrementConfig>> getEnabledBidIncrementConfigs() {
        try {
            List<BidIncrementConfig> configs = bidIncrementService.getEnabledConfigs();
            return Result.success("查询成功", configs);
        } catch (Exception e) {
            log.error("查询启用的加价阶梯配置失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    private java.time.LocalDateTime parseDateTimeFlexible(String input) {
        try {
            // 优先处理 "yyyy-MM-dd HH:mm:ss"
            java.time.format.DateTimeFormatter f1 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (input.length() == 19 && input.charAt(10) == ' ') {
                return java.time.LocalDateTime.parse(input, f1);
            }
            // 处理 ISO8601，允许末尾有Z或小数秒
            String normalized = input.replace("Z", "");
            if (normalized.endsWith(".000")) {
                normalized = normalized.substring(0, normalized.length() - 4);
            }
            // 尝试多种ISO格式
            java.time.format.DateTimeFormatter f2 = java.time.format.DateTimeFormatter.ISO_DATE_TIME;
            return java.time.LocalDateTime.parse(normalized, f2);
        } catch (Exception e) {
            throw new RuntimeException("时间格式不正确: " + input);
        }
    }
}