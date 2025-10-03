package com.auction.service.impl;

import com.auction.entity.LogisticsCompany;
import com.auction.mapper.LogisticsCompanyMapper;
import com.auction.service.LogisticsCompanyService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流公司配置服务实现类
 * 
 * @author auction
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class LogisticsCompanyServiceImpl implements LogisticsCompanyService {
    
    @Autowired
    private LogisticsCompanyMapper logisticsCompanyMapper;
    
    @Override
    public boolean createCompany(LogisticsCompany company) {
        log.debug("创建物流公司配置: {}", company.getCompanyName());
        
        try {
            // 设置默认值
            if (company.getEnabled() == null) {
                company.setEnabled(1); // 默认启用
            }
            if (company.getSortOrder() == null) {
                company.setSortOrder(0);
            }
            if (company.getDeleted() == null) {
                company.setDeleted(0);
            }
            if (company.getCreateTime() == null) {
                company.setCreateTime(LocalDateTime.now());
            }
            if (company.getUpdateTime() == null) {
                company.setUpdateTime(LocalDateTime.now());
            }
            
            int result = logisticsCompanyMapper.insert(company);
            if (result > 0) {
                log.info("物流公司配置创建成功: {}", company.getCompanyName());
                return true;
            } else {
                log.error("物流公司配置创建失败: {}", company.getCompanyName());
                return false;
            }
        } catch (Exception e) {
            log.error("创建物流公司配置时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean updateCompany(LogisticsCompany company) {
        log.debug("更新物流公司配置: {}", company.getId());
        
        try {
            company.setUpdateTime(LocalDateTime.now());
            int result = logisticsCompanyMapper.update(company);
            if (result > 0) {
                log.info("物流公司配置更新成功: {}", company.getId());
                return true;
            } else {
                log.error("物流公司配置更新失败: {}", company.getId());
                return false;
            }
        } catch (Exception e) {
            log.error("更新物流公司配置时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteCompany(Long id) {
        log.debug("删除物流公司配置: {}", id);
        
        try {
            int result = logisticsCompanyMapper.deleteById(id);
            if (result > 0) {
                log.info("物流公司配置删除成功: {}", id);
                return true;
            } else {
                log.error("物流公司配置删除失败: {}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("删除物流公司配置时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public LogisticsCompany getCompanyById(Long id) {
        log.debug("根据ID获取物流公司配置: {}", id);
        return logisticsCompanyMapper.selectById(id);
    }
    
    @Override
    public PageInfo<LogisticsCompany> getAllCompanies(Integer pageNum, Integer pageSize) {
        log.debug("获取所有物流公司配置: 页码={}, 大小={}", pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<LogisticsCompany> companies = logisticsCompanyMapper.selectAll();
        return new PageInfo<>(companies);
    }
    
    @Override
    public List<LogisticsCompany> getEnabledCompanies() {
        log.debug("获取启用的物流公司列表");
        return logisticsCompanyMapper.selectEnabledCompanies();
    }
    
    @Override
    public BigDecimal calculateShippingFee(Long companyId, BigDecimal orderAmount) {
        log.debug("计算物流费用: 公司ID={}, 订单金额={}", companyId, orderAmount);
        
        try {
            LogisticsCompany company = getCompanyById(companyId);
            if (company == null || company.getEnabled() != 1) {
                log.warn("物流公司不存在或未启用: {}", companyId);
                return BigDecimal.ZERO;
            }
            
            // 基础运费
            BigDecimal baseFee = company.getBaseFee() != null ? company.getBaseFee() : BigDecimal.ZERO;
            
            // 保价费用 = 订单金额 * 保价费率
            BigDecimal insuranceFee = BigDecimal.ZERO;
            if (company.getInsuranceRate() != null && orderAmount != null && orderAmount.compareTo(BigDecimal.ZERO) > 0) {
                insuranceFee = orderAmount.multiply(company.getInsuranceRate()).divide(new BigDecimal("100"));
            }
            
            // 总运费 = 基础运费 + 保价费用
            BigDecimal totalFee = baseFee.add(insuranceFee);
            
            log.info("物流费用计算完成: 基础运费={}, 保价费用={}, 总运费={}", baseFee, insuranceFee, totalFee);
            return totalFee;
            
        } catch (Exception e) {
            log.error("计算物流费用时发生错误: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
}
