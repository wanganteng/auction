package com.auction.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * ========================================
 * Redis配置类（RedisConfig）
 * ========================================
 * 功能说明：
 * 1. 配置Redis的序列化方式，解决存储和读取时的乱码问题
 * 2. 配置RedisTemplate，用于操作Redis数据库
 * 3. 支持Java 8时间类型（LocalDateTime）的序列化
 * 4. 使用JSON格式存储对象，便于阅读和调试
 * 
 * 为什么需要自定义配置：
 * - Spring Boot默认使用JDK序列化，存储的数据是二进制，不可读
 * - 使用JSON序列化后，可以直接在Redis客户端查看数据
 * - 支持Java 8的日期时间类型
 * 
 * 序列化策略：
 * - Key：使用String序列化（简单高效）
 * - Value：使用Jackson JSON序列化（可读性好）
 * - Hash的Key：使用String序列化
 * - Hash的Value：使用Jackson JSON序列化
 * 
 * 应用场景：
 * - 缓存用户会话信息
 * - 缓存热点数据（拍卖会、拍品）
 * - 存储实时统计数据（围观人数、出价次数）
 * - 实现分布式锁
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration  // Spring配置类注解
public class RedisConfig {

    /**
     * Redis模板Bean配置
     * 
     * 功能说明：
     * 1. 创建并配置RedisTemplate对象
     * 2. 设置各种序列化器
     * 3. 注册Java时间模块
     * 4. 注册为Spring Bean供全局使用
     * 
     * 配置详情：
     * - Key序列化：StringRedisSerializer（字符串序列化）
     * - Value序列化：Jackson2JsonRedisSerializer（JSON序列化）
     * - 支持LocalDateTime等Java 8时间类型
     * - 支持多态类型序列化
     * 
     * @param connectionFactory Redis连接工厂（Spring自动注入）
     * @return 配置好的RedisTemplate对象
     */
    @Bean  // 注册为Spring Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 创建RedisTemplate实例
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(connectionFactory);

        /* ===== 配置Value的JSON序列化器 ===== */
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        // 配置ObjectMapper
        ObjectMapper om = new ObjectMapper();
        // 设置所有字段可见性为ANY，包括private字段
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 激活默认类型，支持多态类型序列化（序列化时保存类型信息）
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        // 添加JSR310模块，支持LocalDateTime等Java 8时间类型的序列化
        // 没有这个模块，LocalDateTime会序列化失败
        om.registerModule(new JavaTimeModule());
        // 将配置好的ObjectMapper设置到序列化器
        jackson2JsonRedisSerializer.setObjectMapper(om);

        /* ===== 配置Key的String序列化器 ===== */
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        /* ===== 设置各种序列化器 ===== */
        // key采用String的序列化方式（简单高效）
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson（JSON格式，可读性好）
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        // 初始化RedisTemplate（调用afterPropertiesSet完成初始化）
        template.afterPropertiesSet();

        return template;
    }
}
