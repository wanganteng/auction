package com.auction.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物流公司配置实体类
 * 
 * @author auction
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class LogisticsCompany {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 物流公司名称
     */
    private String companyName;
    
    /**
     * 物流公司代码
     */
    private String companyCode;
    
    /**
     * 基础运费（元）
     */
    private BigDecimal baseFee;
    
    /**
     * 保价费率（百分比，如0.5表示0.5%）
     */
    private BigDecimal insuranceRate;
    
    /**
     * 是否启用（0-禁用，1-启用）
     */
    private Integer enabled;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer deleted;
}
