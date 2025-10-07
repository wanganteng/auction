package com.auction.websocket;

import com.auction.entity.AuctionBid;
import com.auction.service.AuctionBidService;
import com.auction.service.SysUserService;
import com.auction.entity.SysUser;
import com.auction.service.RedisService;
import com.auction.service.BidIncrementService;
import com.auction.entity.BidIncrementConfig;
import com.auction.entity.BidIncrementRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.auction.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ========================================
 * 拍卖WebSocket处理器（AuctionWebSocketHandler）
 * ========================================
 * 功能说明：
 * 1. 处理拍卖相关的WebSocket实时通信
 * 2. 实现双向通信，服务器可以主动推送消息给客户端
 * 3. 管理所有WebSocket连接（按拍卖会和用户分组）
 * 4. 处理实时出价消息的接收和广播
 * 5. 支持JWT认证，确保连接安全
 * 
 * WebSocket说明：
 * - WebSocket是一种在单个TCP连接上进行全双工通讯的协议
 * - 与HTTP不同，WebSocket支持服务器主动推送消息
 * - 适合实时性要求高的场景（聊天、直播、拍卖等）
 * 
 * 连接管理：
 * - auctionSessions：按拍卖会ID分组存储连接
 * - userSessions：按用户ID存储连接
 * - 使用ConcurrentHashMap保证线程安全
 * 
 * 消息类型：
 * 1. bid：用户出价消息
 * 2. bidUpdate：出价更新广播
 * 3. statistics：统计信息更新
 * 4. ruleInfo：加价规则信息
 * 
 * 应用场景：
 * - 用户A出价后，立即通知所有在线用户
 * - 实时更新当前价格和出价记录
 * - 显示实时统计（围观人数、出价次数）
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j       // Lombok注解：自动生成log对象
@Component   // Spring注解：注册为Spring组件
public class AuctionWebSocketHandler implements WebSocketHandler {

    /* ========================= 连接存储 ========================= */

    /**
     * 存储所有WebSocket连接（按拍卖会分组）
     * 
     * 数据结构：Map<拍卖会ID, Map<会话ID, WebSocket会话>>
     * 
     * 作用：
     * - 当有人出价时，可以向同一拍卖会的所有用户广播
     * - 实现同一拍卖会内的实时更新
     * 
     * 线程安全：
     * - 使用ConcurrentHashMap保证多线程安全
     */
    private final Map<Long, Map<String, WebSocketSession>> auctionSessions = new ConcurrentHashMap<>();

    /**
     * 存储用户连接（按用户ID索引）
     * 
     * 数据结构：Map<用户ID, WebSocket会话>
     * 
     * 作用：
     * - 可以直接给指定用户发送消息
     * - 例如：出价成功/失败的个人通知
     * 
     * 注意：
     * - 一个用户同时只能有一个WebSocket连接
     * - 新连接会覆盖旧连接
     */
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /* ========================= 依赖注入 ========================= */

    @Autowired
    private ObjectMapper objectMapper;  // JSON序列化工具

    @Autowired
    private AuctionBidService auctionBidService;  // 出价服务

    @Autowired
    private RedisService redisService;  // Redis服务（统计数据）

    @Autowired
    private SysUserService sysUserService;  // 用户服务

    @Autowired
    private JwtTokenProvider tokenProvider;  // JWT Token提供者

    @Autowired
    private BidIncrementService bidIncrementService;  // 加价阶梯服务

    /**
     * WebSocket连接建立后的回调方法
     * 
     * 功能说明：
     * 1. 当客户端成功连接WebSocket时触发
     * 2. 从URL参数中提取JWT Token
     * 3. 验证Token并解析用户身份
     * 4. 将连接加入对应的拍卖会和用户连接池
     * 5. 发送连接成功消息
     * 
     * 连接URL示例：
     * ws://localhost:8080/ws/auction?token=xxx&auctionId=123
     * 
     * @param session WebSocket会话对象
     * @throws Exception 连接建立失败时抛出异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接已建立: {}", session.getId());
        // 从token中解析用户身份并绑定到会话
        try {
            String query = session.getUri() != null ? session.getUri().getQuery() : null;
            if (query != null) {
                String token = null;
                for (String p : query.split("&")) {
                    if (p.startsWith("token=")) {
                        token = p.substring(6);
                        break;
                    }
                }
                if (token != null && !token.isEmpty()) {
                    try {
                        if (tokenProvider.validateToken(token)) {
                            Long resolvedUserId = null;
                            String resolvedUsername = null;

                            // 优先取userId
                            try { resolvedUserId = tokenProvider.getUserIdFromToken(token); } catch (Exception ignore) {}
                            // 用户名
                            try { resolvedUsername = tokenProvider.getUsernameFromToken(token); } catch (Exception ignore) {}

                            // 如果只有用户名没有ID，查库补ID
                            if (resolvedUserId == null && resolvedUsername != null) {
                                SysUser user = sysUserService.getByUsername(resolvedUsername);
                                if (user != null && user.getId() != null) {
                                    resolvedUserId = user.getId();
                                }
                            }

                            if (resolvedUserId != null) {
                                session.getAttributes().put("userId", resolvedUserId);
                            }
                            if (resolvedUsername != null) {
                                session.getAttributes().put("username", resolvedUsername);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("[WS] 解析token失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[WS] 握手token解析异常: {}", e.getMessage());
        }

        // 发送连接成功消息
        sendMessage(session, createMessage("CONNECTED", "连接成功", null));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.debug("收到WebSocket消息: {}", message.getPayload());
        
        try {
            String payload = (String) message.getPayload();
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            
            String type = (String) messageData.get("type");
            Long auctionId = messageData.get("auctionId") != null ? 
                Long.valueOf(messageData.get("auctionId").toString()) : null;
            Long userId = getUserIdFromSession(session);
            
            switch (type) {
                case "JOIN_AUCTION":
                    // 仅使用后端在握手阶段解析到的用户身份
                    handleJoinAuction(session, auctionId, null);
                    break;
                case "LEAVE_AUCTION":
                    handleLeaveAuction(session, auctionId, userId);
                    break;
                case "BID_UPDATE":
                    handleBidUpdate(session, auctionId, messageData);
                    break;
                case "PING":
                    handlePing(session);
                    break;
                default:
                    log.warn("未知消息类型: {}", type);
                    sendMessage(session, createMessage("ERROR", "未知消息类型", null));
            }
            
        } catch (Exception e) {
            log.error("处理WebSocket消息时发生错误: {}", e.getMessage());
            sendMessage(session, createMessage("ERROR", "消息处理失败", null));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", exception.getMessage());
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket连接已关闭: {}, 状态: {}", session.getId(), closeStatus);
        removeSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 处理加入拍卖
     * 
     * @param session WebSocket连接
     * @param auctionId 拍卖ID
     * @param userId 用户ID
     */
    private void handleJoinAuction(WebSocketSession session, Long auctionId, Long userId) {
        if (auctionId == null) {
            sendMessage(session, createMessage("ERROR", "拍卖ID不能为空", null));
            return;
        }
        Long uid = userId != null ? userId : getUserIdFromSession(session);
        log.info("用户 {} (ID: {}) 加入拍卖 {}", 
            session.getAttributes().get("username") != null ? session.getAttributes().get("username") : "匿名", 
            uid, auctionId);
        
        // 添加到拍卖会话
        auctionSessions.computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
        
        // 如果提供了用户ID，添加到用户会话
        if (uid != null) {
            userSessions.put(uid, session);
        }
        // 连接统计

        // 增加围观人数
        Long viewCount = redisService.incrementAuctionViewCount(auctionId);
        
        // 发送加入成功消息
        Map<String, Object> joinData = new java.util.HashMap<>();
        joinData.put("auctionId", auctionId);
        joinData.put("viewCount", viewCount);
        sendMessage(session, createMessage("JOINED_AUCTION", "成功加入拍卖", joinData));

        // 推送加价规则
        pushBidIncrementRules(session, auctionId);
        
        // 通知其他用户有新用户加入
        Map<String, Object> userJoinData = new java.util.HashMap<>();
        userJoinData.put("auctionId", auctionId);
        userJoinData.put("userId", userId);
        userJoinData.put("viewCount", viewCount);
        broadcastToAuction(auctionId, createMessage("USER_JOINED", "有用户加入拍卖", userJoinData), session.getId());
    }

    /**
     * 处理离开拍卖
     * 
     * @param session WebSocket连接
     * @param auctionId 拍卖ID
     * @param userId 用户ID
     */
    private void handleLeaveAuction(WebSocketSession session, Long auctionId, Long userId) {
        if (auctionId == null) {
            sendMessage(session, createMessage("ERROR", "拍卖ID不能为空", null));
            return;
        }
        
        log.info("用户 {} 离开拍卖 {}", userId, auctionId);
        
        // 从拍卖会话中移除
        Map<String, WebSocketSession> sessions = auctionSessions.get(auctionId);
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                auctionSessions.remove(auctionId);
            }
        }
        
        // 从用户会话中移除
        if (userId != null) {
            userSessions.remove(userId);
        }
        
        // 减少围观人数
        Long viewCount = redisService.decrementAuctionViewCount(auctionId);
        
        // 发送离开成功消息
        Map<String, Object> leaveData = new java.util.HashMap<>();
        leaveData.put("auctionId", auctionId);
        leaveData.put("viewCount", viewCount);
        sendMessage(session, createMessage("LEFT_AUCTION", "成功离开拍卖", leaveData));
        
        // 通知其他用户有用户离开
        Map<String, Object> userLeaveData = new java.util.HashMap<>();
        userLeaveData.put("auctionId", auctionId);
        userLeaveData.put("userId", userId);
        userLeaveData.put("viewCount", viewCount);
        broadcastToAuction(auctionId, createMessage("USER_LEFT", "有用户离开拍卖", userLeaveData), session.getId());
    }

    /**
     * 处理出价更新
     * 
     * @param session WebSocket连接
     * @param auctionId 拍卖ID
     * @param messageData 消息数据
     */
    private void handleBidUpdate(WebSocketSession session, Long auctionId, Map<String, Object> messageData) {
        if (auctionId == null) {
            sendMessage(session, createMessage("ERROR", "拍卖ID不能为空", null));
            return;
        }
        
        try {
            // 从消息数据中提取出价信息
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                sendMessage(session, createMessage("ERROR", "用户未登录", null));
                return;
            }
            
            Long itemId = (Long) messageData.get("itemId");
            BigDecimal bidAmount = new BigDecimal(messageData.get("bidAmount").toString());
            String source = (String) messageData.getOrDefault("source", "websocket");
            Boolean isAuto = (Boolean) messageData.getOrDefault("isAuto", false);
            
            if (itemId == null || bidAmount == null) {
                sendMessage(session, createMessage("ERROR", "出价信息不完整", null));
                return;
            }
            
            // 创建出价记录
            AuctionBid bid = new AuctionBid();
            bid.setSessionId(auctionId);
            bid.setItemId(itemId);
            bid.setUserId(userId);
            bid.setBidAmountYuan(bidAmount);
            bid.setSource("websocket".equals(source) ? 1 : 2); // 1-手动出价，2-自动出价
            bid.setIsAuto((isAuto != null && isAuto) ? 1 : 0); // 0-否，1-是
            bid.setClientIp(getClientIp(session));
            bid.setUserAgent(getUserAgent(session));
            
            // 保存出价到数据库
            Long bidId = auctionBidService.placeBid(bid);
            
            // 增加出价次数计数
            Long userBidCount = redisService.incrementUserBidCount(userId, auctionId);
            Long auctionBidCount = redisService.incrementAuctionBidCount(auctionId);
            
            log.info("WebSocket出价成功: 用户ID={}, 拍品ID={}, 出价={}, 出价ID={}, 用户出价次数={}, 拍卖总出价次数={}", 
                userId, itemId, bidAmount, bidId, userBidCount, auctionBidCount);
            
            // 发送成功响应
            Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("bidId", bidId);
            responseData.put("success", true);
            responseData.put("message", "出价成功");
            responseData.put("userBidCount", userBidCount);
            responseData.put("auctionBidCount", auctionBidCount);
            
            // 广播出价消息到拍卖会
            sendBidMessage(auctionId, bid, userBidCount, auctionBidCount);

            // 推送下一次最低出价金额
            pushNextMinimumBid(auctionId, bid.getItemId(), bid.getBidAmountYuan());

            sendMessage(session, createMessage("BID_SUCCESS", "出价成功", responseData));
            
            // 广播出价信息给所有参与拍卖的用户
            Map<String, Object> bidData = new java.util.HashMap<>();
            bidData.put("auctionId", auctionId);
            bidData.put("itemId", itemId);
            bidData.put("userId", userId);
            bidData.put("bidAmount", bidAmount);
            bidData.put("bidId", bidId);
            bidData.put("userBidCount", userBidCount);
            bidData.put("auctionBidCount", auctionBidCount);
            bidData.put("timestamp", System.currentTimeMillis());
            
            broadcastToAuction(auctionId, createMessage("BID_UPDATE", "出价更新", bidData), session.getId());
            
        } catch (Exception e) {
            log.error("处理出价失败: {}", e.getMessage(), e);
            
            // 发送错误响应
            Map<String, Object> errorData = new java.util.HashMap<>();
            errorData.put("success", false);
            errorData.put("message", "出价失败: " + e.getMessage());
            
            sendMessage(session, createMessage("BID_ERROR", "出价失败", errorData));
        }
    }

    /**
     * 处理心跳
     * 
     * @param session WebSocket连接
     */
    private void handlePing(WebSocketSession session) {
        sendMessage(session, createMessage("PONG", "pong", null));
    }

    /**
     * 从WebSocket会话中获取用户ID
     * 
     * @param session WebSocket连接
     * @return 用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            // 从会话属性中获取用户ID
            Object userIdObj = session.getAttributes().get("userId");
            if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            }
            
            // 从URI参数中获取用户ID
            String query = session.getUri().getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("userId=")) {
                        return Long.parseLong(param.substring(7));
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取客户端IP地址
     * 
     * @param session WebSocket连接
     * @return 客户端IP
     */
    private String getClientIp(WebSocketSession session) {
        try {
            // 从请求头中获取真实IP
            String xForwardedFor = session.getHandshakeHeaders().getFirst("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = session.getHandshakeHeaders().getFirst("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            // 从远程地址获取
            return session.getRemoteAddress().getAddress().getHostAddress();
        } catch (Exception e) {
            log.error("获取客户端IP失败: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * 获取User-Agent
     * 
     * @param session WebSocket连接
     * @return User-Agent
     */
    private String getUserAgent(WebSocketSession session) {
        try {
            return session.getHandshakeHeaders().getFirst("User-Agent");
        } catch (Exception e) {
            log.error("获取User-Agent失败: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * 向指定拍卖的所有用户广播消息
     * 
     * @param auctionId 拍卖ID
     * @param message 消息
     * @param excludeSessionId 排除的会话ID
     */
    public void broadcastToAuction(Long auctionId, Map<String, Object> message, String excludeSessionId) {
        Map<String, WebSocketSession> sessions = auctionSessions.get(auctionId);
        if (sessions != null) {
            sessions.values().stream()
                    .filter(session -> !session.getId().equals(excludeSessionId))
                    .forEach(session -> sendMessage(session, message));
        }
    }

    /**
     * 向指定用户发送消息
     * 
     * @param userId 用户ID
     * @param message 消息
     */
    public void sendToUser(Long userId, Map<String, Object> message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    /**
     * 向所有用户广播消息
     * 
     * @param message 消息
     */
    public void broadcastToAll(Map<String, Object> message) {
        userSessions.values().stream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> sendMessage(session, message));
    }

    /**
     * 发送消息到指定连接
     * 
     * @param session WebSocket连接
     * @param message 消息
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                // 确保支持Java 8时间类型序列化
                if (objectMapper.findModules().stream().noneMatch(m -> m instanceof JavaTimeModule)) {
                    objectMapper.registerModule(new JavaTimeModule());
                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                }
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息时发生错误: {}", e.getMessage());
        }
    }

    /**
     * 创建消息
     * 
     * @param type 消息类型
     * @param content 消息内容
     * @param data 消息数据
     * @return 消息Map
     */
    private Map<String, Object> createMessage(String type, String content, Object data) {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", type);
        message.put("content", content);
        message.put("timestamp", System.currentTimeMillis());
        if (data != null) {
            message.put("data", data);
        }
        return message;
    }

    /**
     * 移除会话
     * 
     * @param session WebSocket连接
     */
    private void removeSession(WebSocketSession session) {
        // 从所有拍卖会话中移除
        auctionSessions.values().forEach(sessions -> sessions.remove(session.getId()));
        
        // 从用户会话中移除
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
        
        // 清理空的拍卖会话
        auctionSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * 获取拍卖连接数
     * 
     * @param auctionId 拍卖ID
     * @return 连接数
     */
    public int getAuctionConnectionCount(Long auctionId) {
        Map<String, WebSocketSession> sessions = auctionSessions.get(auctionId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * 获取总连接数
     * 
     * @return 总连接数
     */
    public int getTotalConnectionCount() {
        return userSessions.size();
    }

    /**
     * 推送加价规则给用户
     *
     * @param session WebSocket连接
     * @param auctionId 拍卖ID
     */
    private void pushBidIncrementRules(WebSocketSession session, Long auctionId) {
        try {
            // 获取拍卖会加价规则配置
            BidIncrementConfig config = bidIncrementService.getConfigBySessionId(auctionId);
            if (config != null && config.getRules() != null && !config.getRules().isEmpty()) {
                Map<String, Object> rulesData = new java.util.HashMap<>();
                rulesData.put("auctionId", auctionId);
                rulesData.put("configId", config.getId());
                rulesData.put("configName", config.getConfigName());
                rulesData.put("rules", config.getRules());

                sendMessage(session, createMessage("BID_INCREMENT_RULES", "加价规则", rulesData));
                log.debug("推送加价规则成功: auctionId={}, configId={}", auctionId, config.getId());
            }
        } catch (Exception e) {
            log.error("推送加价规则失败: auctionId={}", auctionId, e);
        }
    }

    /**
     * 推送下一次最低出价金额
     *
     * @param auctionId 拍卖ID
     * @param itemId 拍品ID
     * @param currentPrice 当前价格
     */
    private void pushNextMinimumBid(Long auctionId, Long itemId, java.math.BigDecimal currentPrice) {
        try {
            // 获取拍卖会加价规则配置
            BidIncrementConfig config = bidIncrementService.getConfigBySessionId(auctionId);
            if (config != null) {
                java.math.BigDecimal nextBid = bidIncrementService.getNextMinimumBid(currentPrice, config.getId());

                Map<String, Object> nextBidData = new java.util.HashMap<>();
                nextBidData.put("auctionId", auctionId);
                nextBidData.put("itemId", itemId);
                nextBidData.put("currentPrice", currentPrice);
                nextBidData.put("nextMinimumBid", nextBid);

                broadcastToAuction(auctionId, createMessage("NEXT_MINIMUM_BID", "下一次最低出价", nextBidData), null);
                log.debug("推送下一次最低出价成功: auctionId={}, itemId={}, nextBid={}", auctionId, itemId, nextBid);
            }
        } catch (Exception e) {
            log.error("推送下一次最低出价失败: auctionId={}, itemId={}", auctionId, itemId, e);
        }
    }

    /**
     * 发送出价消息（公开方法，供REST出价后调用）
     */
    public void sendBidMessage(Long auctionId, AuctionBid bid, Long userBidCount, Long auctionBidCount) {
        try {
            Map<String, Object> message = new java.util.HashMap<>();
            message.put("type", "NEW_BID");
            message.put("content", "有新的出价");
            message.put("timestamp", System.currentTimeMillis());
            
            // 获取用户真实信息
            SysUser user = sysUserService.getById(bid.getUserId());
            String displayName = "未知用户";
            if (user != null) {
                // 优先使用昵称，如果没有昵称则使用用户名
                displayName = (user.getNickname() != null && !user.getNickname().trim().isEmpty()) 
                    ? user.getNickname() 
                    : user.getUsername();
            }
            
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("auctionId", bid.getSessionId());
            data.put("itemId", bid.getItemId());
            data.put("bidId", bid.getId());
            data.put("userId", bid.getUserId());
            data.put("username", displayName); // 添加用户真实名称
            data.put("bidAmountYuan", bid.getBidAmountYuan());
            data.put("bidTime", bid.getBidTime());
            data.put("bidSource", bid.getSource());
            data.put("isAutoBid", bid.getIsAuto());
            data.put("userBidCount", userBidCount);
            data.put("auctionBidCount", auctionBidCount);
            
            message.put("data", data);
            
            // 广播到拍卖会
            broadcastToAuction(auctionId, message, null);
        } catch (Exception e) {
            log.error("发送出价消息失败: {}", e.getMessage(), e);
        }
    }
}
