package com.auction.controller;

import com.auction.common.Result;
import com.auction.dto.BidRequest;
import com.auction.dto.CreateAuctionItemRequest;
import com.auction.dto.CreateAuctionSessionRequest;
import com.auction.entity.AuctionBid;
import com.auction.entity.AuctionItem;
import com.auction.entity.AuctionSession;
import com.github.pagehelper.PageInfo;
import com.auction.security.CustomUserDetailsService;
import com.auction.service.AuctionService;
import com.auction.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 拍卖控制器
 * 处理拍卖相关的操作
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/auction")
@Tag(name = "拍卖管理", description = "拍卖相关接口")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 创建拍卖商品
     * 
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/item")
    @Operation(summary = "创建拍卖商品", description = "创建新的拍卖商品")
    public Result<String> createAuctionItem(@Valid @RequestBody CreateAuctionItemRequest request) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            AuctionItem item = new AuctionItem();
            item.setItemName(request.getItemName());
            item.setCategoryId(request.getCategoryId());
            item.setDescription(request.getDescription());
            item.setImages(request.getImages());
            item.setStartingPrice(new BigDecimal(request.getStartPrice() * 100)); // 转换为分
            item.setReservePrice(request.getReservePrice() != null ? new BigDecimal(request.getReservePrice() * 100) : null);
            // 保证金比例由拍卖会设置，这里不设置
            // item.setDepositRatio() 将在加入拍卖会时设置
            // 加价幅度暂时设为起拍价的5%
            item.setCurrentPrice(new BigDecimal(request.getStartPrice() * 100)); // 初始当前价格等于起拍价
            item.setUploaderId(userId);
            
            if (auctionService.createAuctionItem(item)) {
                return Result.success("商品创建成功，等待审核");
            } else {
                return Result.error("商品创建失败");
            }
            
        } catch (Exception e) {
            log.error("创建拍卖商品失败: {}", e.getMessage());
            return Result.error("创建商品失败：" + e.getMessage());
        }
    }

    /**
     * 获取拍卖商品列表
     * 
     * @param status 状态
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 商品列表
     */
    @GetMapping("/items")
    @Operation(summary = "获取拍卖商品列表", description = "分页获取拍卖商品列表")
    public Result<Map<String, Object>> getAuctionItems(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageInfo<AuctionItem> itemsPageInfo;
            if (status != null) {
                itemsPageInfo = auctionService.getAuctionItemsByStatus(status, pageNum, pageSize);
            } else {
                itemsPageInfo = auctionService.getAuctionItems(pageNum, pageSize);
            }
            List<AuctionItem> items = itemsPageInfo.getList();
            
            // 如果有分类过滤，在服务层过滤
            if (categoryId != null) {
                items = items.stream()
                    .filter(item -> categoryId.equals(item.getCategoryId()))
                    .collect(Collectors.toList());
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", items);
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);
            data.put("total", itemsPageInfo.getTotal());
            
            return Result.success("获取成功", data);
            
        } catch (Exception e) {
            log.error("获取拍卖商品列表失败: {}", e.getMessage());
            return Result.error("获取商品列表失败");
        }
    }

    /**
     * 获取拍卖商品详情
     * 
     * @param itemId 商品ID
     * @return 商品详情
     */
    @GetMapping("/item/{itemId}")
    @Operation(summary = "获取拍卖商品详情", description = "根据商品ID获取拍卖商品详细信息")
    public Result<AuctionItem> getAuctionItemDetail(@PathVariable Long itemId) {
        try {
            AuctionItem item = auctionService.getAuctionItemDetail(itemId);
            if (item != null) {
                return Result.success("获取成功", item);
            } else {
                return Result.error("商品不存在");
            }
            
        } catch (Exception e) {
            log.error("获取拍卖商品详情失败: {}", e.getMessage());
            return Result.error("获取商品详情失败");
        }
    }

    /**
     * 出价
     * 
     * @param request 出价请求
     * @param request Http请求
     * @return 出价结果
     */
    @PostMapping("/bid")
    @Operation(summary = "出价", description = "对拍卖商品进行出价")
    public Result<String> placeBid(@Valid @RequestBody BidRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 将元转换为分
            Long bidAmount = request.getBidAmount() * 100;
            
            String clientIp = getClientIpAddress(httpRequest);
            
            // 创建出价对象
            AuctionBid bid = new AuctionBid();
            bid.setSessionId(request.getAuctionId());
            bid.setUserId(userId);
            bid.setBidAmount(bidAmount);
            bid.setBidAmountYuan(new BigDecimal(request.getBidAmount()));
            bid.setClientIp(clientIp);
            bid.setSource(1); // 1-手动出价，2-自动出价
            bid.setIsAuto(0); // 0-否，1-是
            bid.setStatus(0); // 0-有效，1-无效，2-被超越
            
            if (auctionService.placeBid(bid)) {
                return Result.success("出价成功");
            } else {
                return Result.error("出价失败，请检查出价金额和拍卖状态");
            }
            
        } catch (Exception e) {
            log.error("出价失败: {}", e.getMessage());
            return Result.error("出价失败：" + e.getMessage());
        }
    }

    /**
     * 获取出价记录
     * 
     * @param auctionId 拍卖ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 出价记录列表
     */
    @GetMapping("/bids/{auctionId}")
    @Operation(summary = "获取出价记录", description = "获取指定拍卖商品的出价历史记录")
    public Result<Map<String, Object>> getBidHistory(
            @PathVariable Long auctionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageInfo<AuctionBid> bidsPageInfo = auctionService.getBidHistory(auctionId, pageNum, pageSize);
            List<AuctionBid> bids = bidsPageInfo.getList();
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", bids);
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);
            data.put("total", bidsPageInfo.getTotal());
            
            return Result.success("获取成功", data);
            
        } catch (Exception e) {
            log.error("获取出价记录失败: {}", e.getMessage());
            return Result.error("获取出价记录失败");
        }
    }

    /**
     * 获取当前最高出价
     * 
     * @param auctionId 拍卖ID
     * @return 最高出价
     */
    @GetMapping("/highest-bid/{auctionId}")
    @Operation(summary = "获取当前最高出价", description = "获取指定拍卖商品的当前最高出价")
    public Result<AuctionBid> getCurrentHighestBid(@PathVariable Long auctionId) {
        try {
            AuctionBid highestBid = auctionService.getCurrentHighestBid(auctionId);
            return Result.success("获取成功", highestBid);
            
        } catch (Exception e) {
            log.error("获取当前最高出价失败: {}", e.getMessage());
            return Result.error("获取最高出价失败");
        }
    }

    /**
     * 获取拍卖统计信息
     * 
     * @param auctionId 拍卖ID
     * @return 统计信息
     */
    @GetMapping("/stats/{auctionId}")
    @Operation(summary = "获取拍卖统计信息", description = "获取指定拍卖商品的统计信息")
    public Result<Map<String, Object>> getAuctionStats(@PathVariable Long auctionId) {
        try {
            Map<String, Object> stats = auctionService.getAuctionStats(auctionId);
            return Result.success("获取成功", stats);
            
        } catch (Exception e) {
            log.error("获取拍卖统计信息失败: {}", e.getMessage());
            return Result.error("获取统计信息失败");
        }
    }

    /**
     * 创建拍卖会
     * 
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/session")
    @Operation(summary = "创建拍卖会", description = "创建新的拍卖会")
    public Result<String> createAuctionSession(@Valid @RequestBody CreateAuctionSessionRequest request) {
        try {
            AuctionSession session = new AuctionSession();
            session.setSessionName(request.getSessionName());
            session.setDescription(request.getDescription());
            session.setStartTime(request.getStartTime());
            session.setEndTime(request.getEndTime());
            
            if (auctionService.createAuctionSession(session)) {
                return Result.success("拍卖会创建成功");
            } else {
                return Result.error("拍卖会创建失败");
            }
            
        } catch (Exception e) {
            log.error("创建拍卖会失败: {}", e.getMessage());
            return Result.error("创建拍卖会失败：" + e.getMessage());
        }
    }

    /**
     * 获取拍卖会列表
     * 
     * @param status 状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 拍卖会列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取拍卖会列表", description = "分页获取拍卖会列表")
    public Result<Map<String, Object>> getAuctionSessions(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageInfo<AuctionSession> sessionsPageInfo = auctionService.getAuctionSessions(pageNum, pageSize);
            List<AuctionSession> sessions = sessionsPageInfo.getList();
            
            // 如果有状态过滤，在服务层过滤
            if (status != null) {
                sessions = sessions.stream()
                    .filter(session -> status.equals(session.getStatus()))
                    .collect(Collectors.toList());
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", sessions);
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);
            data.put("total", sessionsPageInfo.getTotal());
            
            return Result.success("获取成功", data);
            
        } catch (Exception e) {
            log.error("获取拍卖会列表失败: {}", e.getMessage());
            return Result.error("获取拍卖会列表失败");
        }
    }

    /**
     * 获取拍卖会详情
     * 
     * @param sessionId 拍卖会ID
     * @return 拍卖会详情
     */
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "获取拍卖会详情", description = "根据拍卖会ID获取详细信息")
    public Result<AuctionSession> getAuctionSessionDetail(@PathVariable Long sessionId) {
        try {
            AuctionSession session = auctionService.getAuctionSessionDetail(sessionId);
            if (session != null) {
                return Result.success("获取成功", session);
            } else {
                return Result.error("拍卖会不存在");
            }
            
        } catch (Exception e) {
            log.error("获取拍卖会详情失败: {}", e.getMessage());
            return Result.error("获取拍卖会详情失败");
        }
    }

    /**
     * 检查用户是否可以出价
     * 
     * @param auctionId 拍卖ID
     * @return 检查结果
     */
    @GetMapping("/can-bid/{auctionId}")
    @Operation(summary = "检查用户是否可以出价", description = "检查指定用户是否可以对指定拍卖商品出价")
    public Result<Map<String, Object>> canUserBid(@PathVariable Long auctionId) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean canBid = auctionService.canBid(auctionId, userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("canBid", canBid);
            data.put("auctionId", auctionId);
            data.put("userId", userId);
            
            return Result.success("检查完成", data);
            
        } catch (Exception e) {
            log.error("检查出价权限失败: {}", e.getMessage());
            return Result.error("检查出价权限失败");
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

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}
