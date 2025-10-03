package com.auction.mapper;

import com.auction.entity.LogisticsCompany;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物流公司配置Mapper接口
 * 
 * @author auction
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface LogisticsCompanyMapper {
    
    /**
     * 插入物流公司配置
     */
    int insert(LogisticsCompany company);
    
    /**
     * 更新物流公司配置
     */
    int update(LogisticsCompany company);
    
    /**
     * 根据ID查询物流公司
     */
    LogisticsCompany selectById(@Param("id") Long id);
    
    /**
     * 查询物流公司列表
     */
    List<LogisticsCompany> selectList(LogisticsCompany company);
    
    /**
     * 根据ID删除物流公司
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 查询启用的物流公司列表
     */
    List<LogisticsCompany> selectEnabledCompanies();
    
    /**
     * 查询所有物流公司
     */
    List<LogisticsCompany> selectAll();
}
