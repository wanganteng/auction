package com.auction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 页面控制器
 * 处理页面跳转请求
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Controller
public class PageController {

    // ==================== 用户页面 ====================

    /**
     * 用户首页 - 直接跳转到拍卖会列表
     */
    @GetMapping("/auction/user/")
    public String userIndex() {
        return "redirect:/auction/user/sessions";
    }
    
    /**
     * 用户首页（根路径）
     */
    @GetMapping("/auction/user")
    public String userIndexRoot() {
        return "redirect:/auction/user/sessions";
    }

    /**
     * 拍卖会列表页
     */
    @GetMapping("/auction/user/sessions")
    public String userSessions() {
        return "user/sessions";
    }

    /**
     * 拍卖会详情页（竞拍页面）
     */
    @GetMapping("/auction/user/sessions/{id}")
    public String userSessionDetail(@PathVariable Long id) {
        return "user/session-detail";
    }

    /**
     * 拍卖会竞拍页面
     */
    @GetMapping("/auction/user/sessions/{id}/bidding")
    public String userBidding(@PathVariable Long id) {
        return "user/bidding";
    }

    /**
     * 用户订单管理页
     */
    @GetMapping("/auction/user/orders")
    public String userOrders() {
        return "user/orders";
    }

    /**
     * 用户订单详情页
     */
    @GetMapping("/auction/user/orders/{id}")
    public String userOrderDetail(@PathVariable Long id) {
        return "user/order-detail";
    }

    /**
     * 用户保证金管理页
     */
    @GetMapping("/auction/user/deposits")
    public String userDeposits() {
        return "user/deposits";
    }

    /**
     * 用户通知中心页
     */
    @GetMapping("/auction/user/notifications")
    public String userNotifications() {
        return "user/notifications";
    }

    /**
     * 用户登录页（重定向到统一登录页）
     */
    @GetMapping("/auction/user/login")
    public String userLogin() {
        return "redirect:/auction/login";
    }

    /**
     * 用户注册页
     */
    @GetMapping("/auction/user/register")
    public String userRegister() {
        return "user/register";
    }

    // ==================== 管理员页面 ====================

    /**
     * 管理员首页
     */
    @GetMapping("/auction/admin/")
    public String adminIndex() {
        return "admin/index";
    }

    /**
     * 管理员首页（带斜杠）
     */
    @GetMapping("/auction/admin")
    public String adminIndexSlash() {
        return "admin/index";
    }

    /**
     * 管理员登录页（重定向到统一登录页）
     */
    @GetMapping("/auction/admin/login")
    public String adminLogin() {
        return "redirect:/auction/login";
    }

    // ==================== 统一登录页面 ====================

    /**
     * 统一登录页
     */
    @GetMapping("/auction/login")
    public String unifiedLogin() {
        return "common/login";
    }

    /**
     * 系统配置管理页
     */
    @GetMapping("/auction/admin/config")
    public String adminConfig() {
        return "admin/config";
    }

    /**
     * 管理员拍品管理页
     */
    @GetMapping("/auction/admin/items")
    public String adminItems() {
        return "admin/index";
    }

    /**
     * 管理员拍品上传页
     */
    @GetMapping("/auction/admin/items/upload")
    public String adminItemUpload() {
        return "admin/item-upload";
    }

    /**
     * 管理员拍卖会管理页
     */
    @GetMapping("/auction/admin/sessions")
    public String adminSessions() {
        return "admin/index";
    }

    /**
     * 管理员拍卖会创建页
     */
    @GetMapping("/auction/admin/sessions/create")
    public String adminSessionCreate() {
        return "admin/session-create";
    }

    /**
     * 管理员订单管理页
     */
    @GetMapping("/auction/admin/orders")
    public String adminOrders() {
        return "admin/orders";
    }

    /**
     * 管理员物流管理页
     */
    @GetMapping("/auction/admin/logistics")
    public String adminLogistics() {
        return "admin/logistics";
    }
}
