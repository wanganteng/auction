package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志实体类
 * 记录系统中的重要操作
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class AuditLog {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作类型：LOGIN-登录，LOGOUT-登出，CREATE-创建，UPDATE-更新，DELETE-删除，
     *         APPROVE-审核通过，REJECT-审核拒绝，SHIP-发货，REFUND-退款
     */
    private String operationType;

    /**
     * 操作模块：USER-用户，ITEM-拍品，SESSION-拍卖会，ORDER-订单，DEPOSIT-保证金
     */
    private String module;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 操作对象ID（可选）
     */
    private Long targetId;

    /**
     * 操作对象类型（可选）
     */
    private String targetType;

    /**
     * 请求方法：GET, POST, PUT, DELETE
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 响应结果（简要）
     */
    private String responseResult;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 是否成功：0-失败，1-成功
     */
    private Integer success;

    /**
     * 错误信息（失败时）
     */
    private String errorMsg;

    /**
     * 执行时长（毫秒）
     */
    private Long duration;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

