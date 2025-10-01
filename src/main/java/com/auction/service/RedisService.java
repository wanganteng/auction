package com.auction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis服务类
 * 提供会话存储、计数器等功能
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 会话存储 ====================

    /**
     * 存储用户登录状态
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param userInfo 用户信息
     * @param expireMinutes 过期时间（分钟）
     */
    public void setUserSession(Long userId, String sessionId, Object userInfo, int expireMinutes) {
        try {
            String key = "user:session:" + userId;
            redisTemplate.opsForValue().set(key, userInfo, expireMinutes, TimeUnit.MINUTES);
            
            // 同时存储sessionId到用户ID的映射
            String sessionKey = "session:user:" + sessionId;
            redisTemplate.opsForValue().set(sessionKey, userId, expireMinutes, TimeUnit.MINUTES);
            
            log.debug("用户会话已存储: 用户ID={}, 会话ID={}, 过期时间={}分钟", userId, sessionId, expireMinutes);
        } catch (Exception e) {
            log.error("存储用户会话失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 获取用户登录状态
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    public Object getUserSession(Long userId) {
        try {
            String key = "user:session:" + userId;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取用户会话失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据会话ID获取用户ID
     * 
     * @param sessionId 会话ID
     * @return 用户ID
     */
    public Long getUserIdBySession(String sessionId) {
        try {
            String key = "session:user:" + sessionId;
            Object userId = redisTemplate.opsForValue().get(key);
            return userId != null ? Long.valueOf(userId.toString()) : null;
        } catch (Exception e) {
            log.error("根据会话ID获取用户ID失败: 会话ID={}, 错误: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除用户会话
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    public void removeUserSession(Long userId, String sessionId) {
        try {
            String userKey = "user:session:" + userId;
            String sessionKey = "session:user:" + sessionId;
            
            redisTemplate.delete(userKey);
            redisTemplate.delete(sessionKey);
            
            log.debug("用户会话已删除: 用户ID={}, 会话ID={}", userId, sessionId);
        } catch (Exception e) {
            log.error("删除用户会话失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        try {
            String key = "user:session:" + userId;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("检查用户在线状态失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    // ==================== 计数器功能 ====================

    /**
     * 增加拍卖围观人数
     * 
     * @param auctionId 拍卖ID
     * @return 当前围观人数
     */
    public Long incrementAuctionViewCount(Long auctionId) {
        try {
            String key = "auction:view:" + auctionId;
            Long count = redisTemplate.opsForValue().increment(key);
            
            // 设置过期时间为24小时
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
            
            log.debug("拍卖围观人数增加: 拍卖ID={}, 当前人数={}", auctionId, count);
            return count;
        } catch (Exception e) {
            log.error("增加拍卖围观人数失败: 拍卖ID={}, 错误: {}", auctionId, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 减少拍卖围观人数
     * 
     * @param auctionId 拍卖ID
     * @return 当前围观人数
     */
    public Long decrementAuctionViewCount(Long auctionId) {
        try {
            String key = "auction:view:" + auctionId;
            Long count = redisTemplate.opsForValue().decrement(key);
            
            // 确保不会小于0
            if (count < 0) {
                redisTemplate.opsForValue().set(key, 0);
                count = 0L;
            }
            
            log.debug("拍卖围观人数减少: 拍卖ID={}, 当前人数={}", auctionId, count);
            return count;
        } catch (Exception e) {
            log.error("减少拍卖围观人数失败: 拍卖ID={}, 错误: {}", auctionId, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取拍卖围观人数
     * 
     * @param auctionId 拍卖ID
     * @return 围观人数
     */
    public Long getAuctionViewCount(Long auctionId) {
        try {
            String key = "auction:view:" + auctionId;
            Object count = redisTemplate.opsForValue().get(key);
            return count != null ? Long.valueOf(count.toString()) : 0L;
        } catch (Exception e) {
            log.error("获取拍卖围观人数失败: 拍卖ID={}, 错误: {}", auctionId, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 增加用户出价次数
     * 
     * @param userId 用户ID
     * @param auctionId 拍卖ID
     * @return 当前出价次数
     */
    public Long incrementUserBidCount(Long userId, Long auctionId) {
        try {
            String key = "user:bid:count:" + userId + ":" + auctionId;
            Long count = redisTemplate.opsForValue().increment(key);
            
            // 设置过期时间为24小时
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
            
            log.debug("用户出价次数增加: 用户ID={}, 拍卖ID={}, 当前次数={}", userId, auctionId, count);
            return count;
        } catch (Exception e) {
            log.error("增加用户出价次数失败: 用户ID={}, 拍卖ID={}, 错误: {}", userId, auctionId, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取用户出价次数
     * 
     * @param userId 用户ID
     * @param auctionId 拍卖ID
     * @return 出价次数
     */
    public Long getUserBidCount(Long userId, Long auctionId) {
        try {
            String key = "user:bid:count:" + userId + ":" + auctionId;
            Object count = redisTemplate.opsForValue().get(key);
            return count != null ? Long.valueOf(count.toString()) : 0L;
        } catch (Exception e) {
            log.error("获取用户出价次数失败: 用户ID={}, 拍卖ID={}, 错误: {}", userId, auctionId, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 增加拍卖总出价次数
     * 
     * @param auctionId 拍卖ID
     * @return 当前总出价次数
     */
    public Long incrementAuctionBidCount(Long auctionId) {
        try {
            String key = "auction:bid:count:" + auctionId;
            Long count = redisTemplate.opsForValue().increment(key);
            
            // 设置过期时间为24小时
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
            
            log.debug("拍卖总出价次数增加: 拍卖ID={}, 当前次数={}", auctionId, count);
            return count;
        } catch (Exception e) {
            log.error("增加拍卖总出价次数失败: 拍卖ID={}, 错误: {}", auctionId, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取拍卖总出价次数
     * 
     * @param auctionId 拍卖ID
     * @return 总出价次数
     */
    public Long getAuctionBidCount(Long auctionId) {
        try {
            String key = "auction:bid:count:" + auctionId;
            Object count = redisTemplate.opsForValue().get(key);
            return count != null ? Long.valueOf(count.toString()) : 0L;
        } catch (Exception e) {
            log.error("获取拍卖总出价次数失败: 拍卖ID={}, 错误: {}", auctionId, e.getMessage(), e);
            return 0L;
        }
    }

    // ==================== 通用方法 ====================

    /**
     * 设置键值对
     * 
     * @param key 键
     * @param value 值
     * @param expireMinutes 过期时间（分钟）
     */
    public void set(String key, Object value, int expireMinutes) {
        try {
            redisTemplate.opsForValue().set(key, value, expireMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("设置Redis键值失败: key={}, 错误: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 获取值
     * 
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取Redis值失败: key={}, 错误: {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除键
     * 
     * @param key 键
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除Redis键失败: key={}, 错误: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 检查键是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("检查Redis键是否存在失败: key={}, 错误: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置Hash字段
     * 
     * @param key 键
     * @param field 字段
     * @param value 值
     */
    public void hset(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("设置Hash字段失败: key={}, field={}, 错误: {}", key, field, e.getMessage(), e);
        }
    }

    /**
     * 设置Hash字段（批量）
     * 
     * @param key 键
     * @param hash 哈希表
     */
    public void setHash(String key, java.util.Map<String, Object> hash) {
        try {
            redisTemplate.opsForHash().putAll(key, hash);
        } catch (Exception e) {
            log.error("设置Hash失败: key={}, 错误: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 检查键是否存在（别名方法）
     * 
     * @param key 键
     * @return 是否存在
     */
    public boolean exists(String key) {
        return hasKey(key);
    }

    /**
     * 删除键（别名方法）
     * 
     * @param key 键
     */
    public void del(String key) {
        delete(key);
    }

    /**
     * 设置键的过期时间
     * 
     * @param key 键
     * @param seconds 过期时间（秒）
     */
    public void expire(String key, long seconds) {
        try {
            redisTemplate.expire(key, seconds, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置键过期时间失败: key={}, seconds={}, 错误: {}", key, seconds, e.getMessage(), e);
        }
    }
}
