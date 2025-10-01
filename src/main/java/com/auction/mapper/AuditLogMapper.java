package com.auction.mapper;

import com.auction.entity.AuditLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Mapper
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入审计日志
     */
    @Insert("INSERT INTO audit_log(user_id, username, operation_type, module, operation_desc, " +
            "target_id, target_type, request_method, request_url, request_params, response_result, " +
            "ip_address, user_agent, success, error_msg, duration, create_time) " +
            "VALUES(#{userId}, #{username}, #{operationType}, #{module}, #{operationDesc}, " +
            "#{targetId}, #{targetType}, #{requestMethod}, #{requestUrl}, #{requestParams}, #{responseResult}, " +
            "#{ipAddress}, #{userAgent}, #{success}, #{errorMsg}, #{duration}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog log);

    /**
     * 查询审计日志列表（分页）
     */
    @Select("<script>" +
            "SELECT * FROM audit_log " +
            "WHERE 1=1 " +
            "<if test='userId != null'>AND user_id = #{userId}</if> " +
            "<if test='operationType != null and operationType != \"\"'>AND operation_type = #{operationType}</if> " +
            "<if test='module != null and module != \"\"'>AND module = #{module}</if> " +
            "<if test='success != null'>AND success = #{success}</if> " +
            "<if test='startTime != null'>AND create_time &gt;= #{startTime}</if> " +
            "<if test='endTime != null'>AND create_time &lt;= #{endTime}</if> " +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<AuditLog> selectList(@Param("userId") Long userId,
                             @Param("operationType") String operationType,
                             @Param("module") String module,
                             @Param("success") Integer success,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime,
                             @Param("offset") int offset,
                             @Param("limit") int limit);

    /**
     * 统计审计日志数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM audit_log " +
            "WHERE 1=1 " +
            "<if test='userId != null'>AND user_id = #{userId}</if> " +
            "<if test='operationType != null and operationType != \"\"'>AND operation_type = #{operationType}</if> " +
            "<if test='module != null and module != \"\"'>AND module = #{module}</if> " +
            "<if test='success != null'>AND success = #{success}</if> " +
            "<if test='startTime != null'>AND create_time &gt;= #{startTime}</if> " +
            "<if test='endTime != null'>AND create_time &lt;= #{endTime}</if>" +
            "</script>")
    int count(@Param("userId") Long userId,
             @Param("operationType") String operationType,
             @Param("module") String module,
             @Param("success") Integer success,
             @Param("startTime") LocalDateTime startTime,
             @Param("endTime") LocalDateTime endTime);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM audit_log WHERE id = #{id}")
    AuditLog selectById(@Param("id") Long id);

    /**
     * 查询最近的操作日志
     */
    @Select("SELECT * FROM audit_log WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<AuditLog> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}

