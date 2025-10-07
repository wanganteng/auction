package com.auction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ========================================
 * Web配置类（WebConfig）
 * ========================================
 * 功能说明：
 * 1. 配置Spring MVC的静态资源映射
 * 2. 将URL路径映射到classpath下的静态资源文件夹
 * 3. 支持管理员端、用户端、通用三类静态资源
 * 
 * 为什么需要静态资源映射：
 * - Spring Boot默认静态资源路径是/static、/public等
 * - 我们需要自定义路径，如/auction/admin/css/**
 * - 通过映射，可以灵活组织静态资源的目录结构
 * 
 * 资源分类：
 * 1. 管理员端资源：/auction/admin/** -> classpath:/static/admin/**
 * 2. 用户端资源：/auction/user/** -> classpath:/static/user/**
 * 3. 通用资源：/css/**, /js/**, /images/** -> classpath:/static/**
 * 
 * URL访问示例：
 * - 浏览器访问：http://localhost:8080/auction/admin/css/admin.css
 * - 实际映射到：classpath:/static/admin/css/admin.css
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration  // Spring配置类注解
public class WebConfig implements WebMvcConfigurer {

    /**
     * 添加静态资源处理器
     * 
     * 功能说明：
     * 重写WebMvcConfigurer接口的方法，添加自定义静态资源映射
     * 
     * 映射规则：
     * - addResourceHandler：定义URL访问路径（支持通配符**）
     * - addResourceLocations：定义实际文件位置（classpath:开头）
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /* ===== 管理员端静态资源映射 ===== */
        // CSS样式文件：/auction/admin/css/** -> classpath:/static/admin/css/
        registry.addResourceHandler("/auction/admin/css/**")
                .addResourceLocations("classpath:/static/admin/css/");
        
        // JavaScript脚本文件：/auction/admin/js/** -> classpath:/static/admin/js/
        registry.addResourceHandler("/auction/admin/js/**")
                .addResourceLocations("classpath:/static/admin/js/");
        
        // 图片文件：/auction/admin/images/** -> classpath:/static/admin/images/
        registry.addResourceHandler("/auction/admin/images/**")
                .addResourceLocations("classpath:/static/admin/images/");
        
        // 字体文件：/auction/admin/fonts/** -> classpath:/static/admin/fonts/
        registry.addResourceHandler("/auction/admin/fonts/**")
                .addResourceLocations("classpath:/static/admin/fonts/");
        
        /* ===== 用户端静态资源映射 ===== */
        // CSS样式文件：/auction/user/css/** -> classpath:/static/user/css/
        registry.addResourceHandler("/auction/user/css/**")
                .addResourceLocations("classpath:/static/user/css/");
        
        // JavaScript脚本文件：/auction/user/js/** -> classpath:/static/user/js/
        registry.addResourceHandler("/auction/user/js/**")
                .addResourceLocations("classpath:/static/user/js/");
        
        // 图片文件：/auction/user/images/** -> classpath:/static/user/images/
        registry.addResourceHandler("/auction/user/images/**")
                .addResourceLocations("classpath:/static/user/images/");
        
        /* ===== 通用静态资源映射 ===== */
        // 图片文件：/images/** -> classpath:/static/images/
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        
        // CSS样式文件：/css/** -> classpath:/static/css/
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        // JavaScript脚本文件：/js/** -> classpath:/static/js/
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}
