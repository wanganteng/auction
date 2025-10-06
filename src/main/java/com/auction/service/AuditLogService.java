package com.auction.service;

import com.auction.entity.AuditLog;
import com.auction.mapper.AuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计日志服务
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    /**
     * 记录审计日志
     */
    @Transactional
    public void log(AuditLog auditLog) {
        try {
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            // 审计日志失败不应影响业务，只记录错误
            log.error("记录审计日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 快速记录日志
     */
    public void log(Long userId, String username, String operationType, String module, 
                   String operationDesc, Long targetId, String targetType, 
                   String ipAddress, Integer success) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setOperationType(operationType);
        auditLog.setModule(module);
        auditLog.setOperationDesc(operationDesc);
        auditLog.setTargetId(targetId);
        auditLog.setTargetType(targetType);
        auditLog.setIpAddress(ipAddress);
        auditLog.setSuccess(success);
        
        log(auditLog);
    }

    /**
     * 查询审计日志列表
     */
    public Map<String, Object> queryLogs(Long userId, String operationType, String module,
                                        Integer success, LocalDateTime startTime, LocalDateTime endTime,
                                        Integer pageNum, Integer pageSize) {
        try {
            int offset = (pageNum - 1) * pageSize;
            
            List<AuditLog> logs = auditLogMapper.selectList(
                userId, operationType, module, success, startTime, endTime, offset, pageSize
            );
            
            int total = auditLogMapper.count(
                userId, operationType, module, success, startTime, endTime
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", logs);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", (int) Math.ceil((double) total / pageSize));
            
            return result;
        } catch (Exception e) {
            log.error("查询审计日志失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取用户最近操作日志
     */
    public List<AuditLog> getRecentLogs(Long userId, int limit) {
        try {
            return auditLogMapper.selectRecentByUserId(userId, limit);
        } catch (Exception e) {
            log.error("获取最近日志失败: userId={}, error={}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}

