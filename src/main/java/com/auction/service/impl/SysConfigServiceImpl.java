package com.auction.service.impl;

import com.auction.entity.SysConfig;
import com.auction.mapper.SysConfigMapper;
import com.auction.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置服务实现类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class SysConfigServiceImpl implements SysConfigService {
    
    @Autowired
    private SysConfigMapper sysConfigMapper;
    
    // 配置缓存
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    @Override
    @Cacheable(value = "config", key = "#configKey")
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }
    
    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        if (!StringUtils.hasText(configKey)) {
            return defaultValue;
        }
        
        // 先从缓存获取
        String cachedValue = configCache.get(configKey);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // 从数据库获取
        try {
            SysConfig config = sysConfigMapper.selectByKey(configKey);
            if (config != null) {
                String value = config.getConfigValue();
                configCache.put(configKey, value);
                return value;
            }
        } catch (Exception e) {
            log.error("获取配置失败: configKey={}, error={}", configKey, e.getMessage(), e);
        }
        
        return defaultValue;
    }
    
    @Override
    public Integer getIntConfigValue(String configKey) {
        return getIntConfigValue(configKey, null);
    }
    
    @Override
    public Integer getIntConfigValue(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey);
        if (StringUtils.hasText(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.warn("配置值不是有效的整数: configKey={}, value={}", configKey, value);
            }
        }
        return defaultValue;
    }
    
    @Override
    public Long getLongConfigValue(String configKey) {
        return getLongConfigValue(configKey, null);
    }
    
    @Override
    public Long getLongConfigValue(String configKey, Long defaultValue) {
        String value = getConfigValue(configKey);
        if (StringUtils.hasText(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                log.warn("配置值不是有效的长整数: configKey={}, value={}", configKey, value);
            }
        }
        return defaultValue;
    }
    
    @Override
    public Boolean getBooleanConfigValue(String configKey) {
        return getBooleanConfigValue(configKey, null);
    }
    
    @Override
    public Boolean getBooleanConfigValue(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (StringUtils.hasText(value)) {
            return "true".equalsIgnoreCase(value) || "1".equals(value);
        }
        return defaultValue;
    }
    
    @Override
    public Double getDoubleConfigValue(String configKey) {
        return getDoubleConfigValue(configKey, null);
    }
    
    @Override
    public Double getDoubleConfigValue(String configKey, Double defaultValue) {
        String value = getConfigValue(configKey);
        if (StringUtils.hasText(value)) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                log.warn("配置值不是有效的浮点数: configKey={}, value={}", configKey, value);
            }
        }
        return defaultValue;
    }
    
    @Override
    @CacheEvict(value = "config", key = "#configKey")
    public boolean setConfigValue(String configKey, String configValue) {
        if (!StringUtils.hasText(configKey)) {
            return false;
        }
        
        try {
            // 更新数据库
            int result = sysConfigMapper.updateValueByKey(configKey, configValue);
            if (result > 0) {
                // 更新缓存
                configCache.put(configKey, configValue);
                log.info("配置更新成功: configKey={}, configValue={}", configKey, configValue);
                return true;
            }
        } catch (Exception e) {
            log.error("设置配置失败: configKey={}, configValue={}, error={}", configKey, configValue, e.getMessage(), e);
        }
        
        return false;
    }
    
    @Override
    public SysConfig getConfigById(Long id) {
        if (id == null) {
            return null;
        }
        
        try {
            return sysConfigMapper.selectById(id);
        } catch (Exception e) {
            log.error("根据ID查询配置失败: id={}, error={}", id, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public SysConfig getConfigByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }
        
        try {
            return sysConfigMapper.selectByKey(configKey);
        } catch (Exception e) {
            log.error("根据配置键查询配置失败: configKey={}, error={}", configKey, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<SysConfig> getAllConfigs() {
        try {
            return sysConfigMapper.selectAll();
        } catch (Exception e) {
            log.error("查询所有配置失败: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SysConfig> getConfigsByType(String configType) {
        if (!StringUtils.hasText(configType)) {
            return new ArrayList<>();
        }
        
        try {
            return sysConfigMapper.selectByType(configType);
        } catch (Exception e) {
            log.error("根据类型查询配置失败: configType={}, error={}", configType, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SysConfig> getSystemConfigs() {
        try {
            return sysConfigMapper.selectBySystem(1);
        } catch (Exception e) {
            log.error("查询系统配置失败: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean createConfig(SysConfig config) {
        if (config == null || !StringUtils.hasText(config.getConfigKey())) {
            return false;
        }
        
        try {
            config.setCreateTime(java.time.LocalDateTime.now());
            config.setUpdateTime(java.time.LocalDateTime.now());
            config.setDeleted(0);
            
            int result = sysConfigMapper.insert(config);
            if (result > 0) {
                // 更新缓存
                configCache.put(config.getConfigKey(), config.getConfigValue());
                log.info("配置创建成功: configKey={}", config.getConfigKey());
                return true;
            }
        } catch (Exception e) {
            log.error("创建配置失败: configKey={}, error={}", config.getConfigKey(), e.getMessage(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean updateConfig(SysConfig config) {
        if (config == null || !StringUtils.hasText(config.getConfigKey())) {
            return false;
        }
        
        try {
            config.setUpdateTime(java.time.LocalDateTime.now());
            
            int result = sysConfigMapper.update(config);
            if (result > 0) {
                // 更新缓存
                configCache.put(config.getConfigKey(), config.getConfigValue());
                log.info("配置更新成功: configKey={}", config.getConfigKey());
                return true;
            }
        } catch (Exception e) {
            log.error("更新配置失败: configKey={}, error={}", config.getConfigKey(), e.getMessage(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean updateConfigById(SysConfig config) {
        if (config == null || config.getId() == null) {
            return false;
        }
        
        try {
            config.setUpdateTime(java.time.LocalDateTime.now());
            
            int result = sysConfigMapper.updateById(config);
            if (result > 0) {
                // 更新缓存
                if (StringUtils.hasText(config.getConfigKey())) {
                    configCache.put(config.getConfigKey(), config.getConfigValue());
                }
                log.info("配置更新成功: id={}", config.getId());
                return true;
            }
        } catch (Exception e) {
            log.error("根据ID更新配置失败: id={}, error={}", config.getId(), e.getMessage(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean deleteConfigById(Long id) {
        if (id == null) {
            return false;
        }
        
        try {
            int result = sysConfigMapper.deleteById(id);
            if (result > 0) {
                log.info("配置删除成功: id={}", id);
                return true;
            }
        } catch (Exception e) {
            log.error("根据ID删除配置失败: id={}, error={}", id, e.getMessage(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean deleteConfigByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return false;
        }
        
        try {
            int result = sysConfigMapper.deleteByKey(configKey);
            if (result > 0) {
                // 清除缓存
                configCache.remove(configKey);
                log.info("配置删除成功: configKey={}", configKey);
                return true;
            }
        } catch (Exception e) {
            log.error("根据配置键删除配置失败: configKey={}, error={}", configKey, e.getMessage(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean batchUpdateConfigs(Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            return false;
        }
        
        try {
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                setConfigValue(entry.getKey(), entry.getValue());
            }
            log.info("批量更新配置成功: count={}", configs.size());
            return true;
        } catch (Exception e) {
            log.error("批量更新配置失败: error={}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public void reloadConfigCache() {
        try {
            configCache.clear();
            List<SysConfig> configs = sysConfigMapper.selectAll();
            for (SysConfig config : configs) {
                configCache.put(config.getConfigKey(), config.getConfigValue());
            }
            log.info("配置缓存重新加载成功: count={}", configs.size());
        } catch (Exception e) {
            log.error("重新加载配置缓存失败: error={}", e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, String> getAllConfigsAsMap() {
        try {
            if (configCache.isEmpty()) {
                reloadConfigCache();
            }
            return new HashMap<>(configCache);
        } catch (Exception e) {
            log.error("获取所有配置Map失败: error={}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
}
