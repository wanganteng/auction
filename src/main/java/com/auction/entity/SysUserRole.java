package com.auction.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class SysUserRole {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}