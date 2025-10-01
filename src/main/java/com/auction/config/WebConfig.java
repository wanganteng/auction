package com.auction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置静态资源映射
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 管理员静态资源映射
        registry.addResourceHandler("/auction/admin/css/**")
                .addResourceLocations("classpath:/static/admin/css/");
        
        registry.addResourceHandler("/auction/admin/js/**")
                .addResourceLocations("classpath:/static/admin/js/");
        
        registry.addResourceHandler("/auction/admin/images/**")
                .addResourceLocations("classpath:/static/admin/images/");
        
        registry.addResourceHandler("/auction/admin/fonts/**")
                .addResourceLocations("classpath:/static/admin/fonts/");
        
        // 用户静态资源映射
        registry.addResourceHandler("/auction/user/css/**")
                .addResourceLocations("classpath:/static/user/css/");
        
        registry.addResourceHandler("/auction/user/js/**")
                .addResourceLocations("classpath:/static/user/js/");
        
        registry.addResourceHandler("/auction/user/images/**")
                .addResourceLocations("classpath:/static/user/images/");
        
        // 通用静态资源映射
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}
