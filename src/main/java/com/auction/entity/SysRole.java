package com.auction.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class SysRole {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志：0-未删除，1-已删除
     */
    private Integer deleted;
}