package com.auction.service;

import com.auction.entity.SysConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface SysConfigService {
    
    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置值，带默认值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 获取数字类型配置值
     */
    Integer getIntConfigValue(String configKey);
    
    /**
     * 获取数字类型配置值，带默认值
     */
    Integer getIntConfigValue(String configKey, Integer defaultValue);
    
    /**
     * 获取长整型配置值
     */
    Long getLongConfigValue(String configKey);
    
    /**
     * 获取长整型配置值，带默认值
     */
    Long getLongConfigValue(String configKey, Long defaultValue);
    
    /**
     * 获取布尔类型配置值
     */
    Boolean getBooleanConfigValue(String configKey);
    
    /**
     * 获取布尔类型配置值，带默认值
     */
    Boolean getBooleanConfigValue(String configKey, Boolean defaultValue);
    
    /**
     * 获取浮点型配置值
     */
    Double getDoubleConfigValue(String configKey);
    
    /**
     * 获取浮点型配置值，带默认值
     */
    Double getDoubleConfigValue(String configKey, Double defaultValue);
    
    /**
     * 设置配置值
     */
    boolean setConfigValue(String configKey, String configValue);
    
    /**
     * 根据ID查询配置
     */
    SysConfig getConfigById(Long id);
    
    /**
     * 根据配置键查询配置
     */
    SysConfig getConfigByKey(String configKey);
    
    /**
     * 查询所有配置
     */
    List<SysConfig> getAllConfigs();
    
    /**
     * 根据配置类型查询配置
     */
    List<SysConfig> getConfigsByType(String configType);
    
    /**
     * 根据是否系统配置查询
     */
    List<SysConfig> getSystemConfigs();
    
    /**
     * 创建配置
     */
    boolean createConfig(SysConfig config);
    
    /**
     * 更新配置
     */
    boolean updateConfig(SysConfig config);
    
    /**
     * 根据ID更新配置
     */
    boolean updateConfigById(SysConfig config);
    
    /**
     * 根据ID删除配置
     */
    boolean deleteConfigById(Long id);
    
    /**
     * 根据配置键删除配置
     */
    boolean deleteConfigByKey(String configKey);
    
    /**
     * 批量更新配置
     */
    boolean batchUpdateConfigs(Map<String, String> configs);
    
    /**
     * 重新加载配置缓存
     */
    void reloadConfigCache();
    
    /**
     * 获取所有配置的Map形式
     */
    Map<String, String> getAllConfigsAsMap();
}
