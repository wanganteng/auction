package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysConfig {
    
    /**
     * 配置ID
     */
    private Long id;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置类型：STRING-字符串，NUMBER-数字，BOOLEAN-布尔，JSON-JSON对象
     */
    private String configType;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 是否系统配置：0-否，1-是
     */
    private Integer isSystem;
    
    /**
     * 是否可编辑：0-否，1-是
     */
    private Integer isEditable;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 删除标志：0-未删除，1-已删除
     */
    private Integer deleted;
    
    /**
     * 配置类型枚举
     */
    public enum ConfigType {
        STRING("STRING", "字符串"),
        NUMBER("NUMBER", "数字"),
        BOOLEAN("BOOLEAN", "布尔"),
        JSON("JSON", "JSON对象");
        
        private final String code;
        private final String desc;
        
        ConfigType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
    }
}
