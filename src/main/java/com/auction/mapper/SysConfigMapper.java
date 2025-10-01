package com.auction.mapper;

import com.auction.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统配置Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface SysConfigMapper {
    
    /**
     * 根据ID查询配置
     */
    SysConfig selectById(@Param("id") Long id);
    
    /**
     * 根据配置键查询配置
     */
    SysConfig selectByKey(@Param("configKey") String configKey);
    
    /**
     * 查询所有配置
     */
    List<SysConfig> selectAll();
    
    /**
     * 根据配置类型查询配置
     */
    List<SysConfig> selectByType(@Param("configType") String configType);
    
    /**
     * 根据是否系统配置查询
     */
    List<SysConfig> selectBySystem(@Param("isSystem") Integer isSystem);
    
    /**
     * 插入配置
     */
    int insert(SysConfig config);
    
    /**
     * 更新配置
     */
    int update(SysConfig config);
    
    /**
     * 根据ID更新配置
     */
    int updateById(SysConfig config);
    
    /**
     * 根据配置键更新配置值
     */
    int updateValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
    
    /**
     * 根据ID删除配置
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据配置键删除配置
     */
    int deleteByKey(@Param("configKey") String configKey);
}
