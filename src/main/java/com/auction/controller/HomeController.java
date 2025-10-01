package com.auction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 * 处理根路径访问
 * 
 * @author auction-system
 * @since 1.0.0
 */
@Controller
public class HomeController {
    
    /**
     * 根路径重定向到用户首页
     * 注意：静态资源（如 .html 文件）会由 Spring Boot 自动处理，
     * 不会经过这个控制器
     * 
     * @return 重定向到用户首页
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/auction/user/";
    }
}
