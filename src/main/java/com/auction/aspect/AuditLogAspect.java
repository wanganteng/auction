package com.auction.aspect;

import com.auction.entity.AuditLog;
import com.auction.entity.SysUser;
import com.auction.security.CustomUserDetailsService;
import com.auction.service.AuditLogService;
import com.auction.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 审计日志AOP切面
 * 自动记录重要操作日志
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切入点：管理员的审核、发货、删除等敏感操作
     */
    @Pointcut("execution(* com.auction.controller.AdminController.approve*(..))" +
            " || execution(* com.auction.controller.AdminController.reject*(..))" +
            " || execution(* com.auction.controller.AdminController.delete*(..))" +
            " || execution(* com.auction.service.impl.AuctionOrderServiceImpl.rejectShipment(..))" +
            " || execution(* com.auction.service.UserDepositAccountService.approve*(..))" +
            " || execution(* com.auction.service.UserDepositAccountService.rejectTransaction(..))")
    public void auditPointcut() {
    }

    /**
     * 环绕通知：记录审计日志
     */
    @Around("auditPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        HttpServletRequest request = getRequest();
        SysUser currentUser = null;
        try {
            currentUser = SecurityUtils.getCurrentUser();
        } catch (Exception e) {
            log.debug("获取当前用户失败: {}", e.getMessage());
        }
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        
        // 创建审计日志对象
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(currentUser != null ? currentUser.getId() : null);
        auditLog.setUsername(currentUser != null ? currentUser.getUsername() : "匿名");
        
        // 设置操作类型和模块
        setOperationTypeAndModule(auditLog, methodName);
        
        // 设置请求信息
        if (request != null) {
            auditLog.setRequestMethod(request.getMethod());
            auditLog.setRequestUrl(request.getRequestURI());
            auditLog.setIpAddress(getIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        // 设置请求参数
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 过滤掉HttpServletRequest等非业务参数
                Object[] businessArgs = filterBusinessArgs(args);
                auditLog.setRequestParams(objectMapper.writeValueAsString(businessArgs));
            }
        } catch (Exception e) {
            log.warn("序列化请求参数失败: {}", e.getMessage());
        }
        
        // 执行目标方法
        Object result = null;
        try {
            result = joinPoint.proceed();
            
            // 成功
            auditLog.setSuccess(1);
            auditLog.setResponseResult("SUCCESS");
            
            // 提取目标ID（如果是审核操作，第一个参数通常是ID）
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Long) {
                auditLog.setTargetId((Long) args[0]);
            }
            
        } catch (Exception e) {
            // 失败
            auditLog.setSuccess(0);
            auditLog.setErrorMsg(e.getMessage());
            auditLog.setResponseResult("FAILED");
            throw e;
        } finally {
            // 记录执行时长
            long duration = System.currentTimeMillis() - startTime;
            auditLog.setDuration(duration);
            
            // 异步记录日志（不阻塞主流程）
            auditLogService.log(auditLog);
        }
        
        return result;
    }

    /**
     * 根据方法名设置操作类型和模块
     */
    private void setOperationTypeAndModule(AuditLog auditLog, String methodName) {
        if (methodName.startsWith("approve")) {
            auditLog.setOperationType("APPROVE");
            if (methodName.contains("Recharge")) {
                auditLog.setModule("DEPOSIT");
                auditLog.setOperationDesc("审核通过充值申请");
            } else if (methodName.contains("Withdraw")) {
                auditLog.setModule("DEPOSIT");
                auditLog.setOperationDesc("审核通过提现申请");
            } else {
                auditLog.setModule("UNKNOWN");
                auditLog.setOperationDesc("审核通过");
            }
        } else if (methodName.startsWith("reject")) {
            auditLog.setOperationType("REJECT");
            if (methodName.contains("Shipment")) {
                auditLog.setModule("ORDER");
                auditLog.setOperationDesc("拒绝发货并退款");
            } else if (methodName.contains("Transaction") || methodName.contains("Deposit")) {
                auditLog.setModule("DEPOSIT");
                auditLog.setOperationDesc("拒绝保证金申请");
            } else {
                auditLog.setModule("UNKNOWN");
                auditLog.setOperationDesc("审核拒绝");
            }
        } else if (methodName.startsWith("delete")) {
            auditLog.setOperationType("DELETE");
            auditLog.setOperationDesc("删除操作");
            
            // 根据类名判断模块
            if (methodName.contains("Item")) {
                auditLog.setModule("ITEM");
            } else if (methodName.contains("Session")) {
                auditLog.setModule("SESSION");
            } else {
                auditLog.setModule("UNKNOWN");
            }
        } else {
            auditLog.setOperationType("UNKNOWN");
            auditLog.setModule("UNKNOWN");
            auditLog.setOperationDesc(methodName);
        }
    }

    /**
     * 过滤业务参数
     */
    private Object[] filterBusinessArgs(Object[] args) {
        return java.util.Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest)
                        && !(arg instanceof org.springframework.web.multipart.MultipartFile)
                        && !(arg instanceof javax.servlet.http.HttpServletResponse))
                .toArray();
    }


    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取真实IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

