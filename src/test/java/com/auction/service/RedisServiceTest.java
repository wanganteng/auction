package com.auction.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis服务测试类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
public class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @BeforeEach
    public void setUp() {
        // 清理测试数据
        redisService.delete("user:session:1");
        redisService.delete("session:user:test-session-123");
        redisService.delete("auction:view:1");
        redisService.delete("user:bid:count:1:1");
        redisService.delete("auction:bid:count:1");
        redisService.delete("test:key");
    }

    @AfterEach
    public void tearDown() {
        // 清理测试数据
        redisService.delete("user:session:1");
        redisService.delete("session:user:test-session-123");
        redisService.delete("auction:view:1");
        redisService.delete("user:bid:count:1:1");
        redisService.delete("auction:bid:count:1");
        redisService.delete("test:key");
    }

    @Test
    public void testUserSession() {
        // 测试用户会话存储
        Long userId = 1L;
        String sessionId = "test-session-123";
        String userInfo = "test-user";
        
        // 存储会话
        redisService.setUserSession(userId, sessionId, userInfo, 30);
        
        // 验证存储
        Object storedUserInfo = redisService.getUserSession(userId);
        assertEquals(userInfo, storedUserInfo);
        
        // 验证会话ID映射
        Long retrievedUserId = redisService.getUserIdBySession(sessionId);
        assertEquals(userId, retrievedUserId);
        
        // 验证在线状态
        assertTrue(redisService.isUserOnline(userId));
        
        // 清理
        redisService.removeUserSession(userId, sessionId);
        assertFalse(redisService.isUserOnline(userId));
    }

    @Test
    public void testAuctionViewCount() {
        // 测试拍卖围观人数计数
        Long auctionId = 1L;
        
        // 初始计数应该为0
        Long initialCount = redisService.getAuctionViewCount(auctionId);
        assertEquals(0L, initialCount);
        
        // 增加围观人数
        Long count1 = redisService.incrementAuctionViewCount(auctionId);
        assertEquals(1L, count1);
        
        Long count2 = redisService.incrementAuctionViewCount(auctionId);
        assertEquals(2L, count2);
        
        // 减少围观人数
        Long count3 = redisService.decrementAuctionViewCount(auctionId);
        assertEquals(1L, count3);
        
        Long count4 = redisService.decrementAuctionViewCount(auctionId);
        assertEquals(0L, count4);
        
        // 确保不会小于0
        Long count5 = redisService.decrementAuctionViewCount(auctionId);
        assertEquals(0L, count5);
    }

    @Test
    public void testBidCount() {
        // 测试出价次数计数
        Long userId = 1L;
        Long auctionId = 1L;
        
        // 初始计数应该为0
        Long initialUserBidCount = redisService.getUserBidCount(userId, auctionId);
        Long initialAuctionBidCount = redisService.getAuctionBidCount(auctionId);
        assertEquals(0L, initialUserBidCount);
        assertEquals(0L, initialAuctionBidCount);
        
        // 增加用户出价次数
        Long userBidCount1 = redisService.incrementUserBidCount(userId, auctionId);
        assertEquals(1L, userBidCount1);
        
        // 增加拍卖总出价次数
        Long auctionBidCount1 = redisService.incrementAuctionBidCount(auctionId);
        assertEquals(1L, auctionBidCount1);
        
        // 再次增加
        Long userBidCount2 = redisService.incrementUserBidCount(userId, auctionId);
        Long auctionBidCount2 = redisService.incrementAuctionBidCount(auctionId);
        assertEquals(2L, userBidCount2);
        assertEquals(2L, auctionBidCount2);
    }

    @Test
    public void testBasicOperations() {
        // 测试基本Redis操作
        String key = "test:key";
        String value = "test-value";
        
        // 设置值
        redisService.set(key, value, 10);
        
        // 获取值
        Object retrievedValue = redisService.get(key);
        assertEquals(value, retrievedValue);
        
        // 检查键是否存在
        assertTrue(redisService.hasKey(key));
        
        // 删除键
        redisService.delete(key);
        assertFalse(redisService.hasKey(key));
    }
}
