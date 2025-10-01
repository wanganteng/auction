package com.auction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码配置类
 * 将密码编码器配置从SecurityConfig中分离出来，避免循环依赖
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class PasswordConfig {

    /**
     * 密码编码器
     * 
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
