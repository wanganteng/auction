package com.auction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 配置静态资源访问路径
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置图片资源访问路径 - 使用更高的优先级
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);
        
        // 配置管理后台静态资源
        registry.addResourceHandler("/admin/css/**")
                .addResourceLocations("classpath:/static/admin/css/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/admin/js/**")
                .addResourceLocations("classpath:/static/admin/js/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/admin/images/**")
                .addResourceLocations("classpath:/static/admin/images/")
                .setCachePeriod(3600);
        
        // 配置用户界面静态资源
        registry.addResourceHandler("/user/css/**")
                .addResourceLocations("classpath:/static/user/css/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/user/js/**")
                .addResourceLocations("classpath:/static/user/js/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/user/images/**")
                .addResourceLocations("classpath:/static/user/images/")
                .setCachePeriod(3600);
        
        
        // 确保静态资源优先级
        registry.setOrder(1);
    }
}
