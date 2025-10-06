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
     * @return 重定向到用户首页
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/auction/user/";
    }
}
