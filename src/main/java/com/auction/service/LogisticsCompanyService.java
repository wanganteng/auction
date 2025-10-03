package com.auction.service;

import com.auction.entity.LogisticsCompany;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 物流公司配置服务接口
 * 
 * @author auction
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface LogisticsCompanyService {
    
    /**
     * 创建物流公司配置
     * 
     * @param company 物流公司配置
     * @return 是否成功
     */
    boolean createCompany(LogisticsCompany company);
    
    /**
     * 更新物流公司配置
     * 
     * @param company 物流公司配置
     * @return 是否成功
     */
    boolean updateCompany(LogisticsCompany company);
    
    /**
     * 删除物流公司配置
     * 
     * @param id 物流公司ID
     * @return 是否成功
     */
    boolean deleteCompany(Long id);
    
    /**
     * 根据ID获取物流公司配置
     * 
     * @param id 物流公司ID
     * @return 物流公司配置
     */
    LogisticsCompany getCompanyById(Long id);
    
    /**
     * 获取所有物流公司配置
     * 
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 物流公司配置列表
     */
    PageInfo<LogisticsCompany> getAllCompanies(Integer pageNum, Integer pageSize);
    
    /**
     * 获取启用的物流公司列表
     * 
     * @return 启用的物流公司列表
     */
    List<LogisticsCompany> getEnabledCompanies();
    
    /**
     * 计算物流费用
     * 
     * @param companyId 物流公司ID
     * @param orderAmount 订单金额
     * @return 物流费用
     */
    java.math.BigDecimal calculateShippingFee(Long companyId, java.math.BigDecimal orderAmount);
}
