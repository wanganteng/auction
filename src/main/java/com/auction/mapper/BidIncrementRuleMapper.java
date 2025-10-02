package com.auction.mapper;

import com.auction.entity.BidIncrementRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 加价阶梯规则Mapper接口
 *
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface BidIncrementRuleMapper {

    /**
     * 插入加价阶梯规则
     */
    int insert(BidIncrementRule rule);

    /**
     * 批量插入加价阶梯规则
     */
    int insertBatch(@Param("rules") List<BidIncrementRule> rules);

    /**
     * 更新加价阶梯规则
     */
    int update(BidIncrementRule rule);

    /**
     * 根据ID查询加价阶梯规则
     */
    BidIncrementRule selectById(Long id);

    /**
     * 根据配置ID查询规则列表（按排序号升序）
     */
    List<BidIncrementRule> selectListByConfigId(@Param("configId") Long configId);

    /**
     * 根据价格查询适用的加价规则
     */
    BidIncrementRule selectByAmountAndConfigId(@Param("amount") BigDecimal amount, @Param("configId") Long configId);

    /**
     * 根据ID删除加价阶梯规则
     */
    int deleteById(Long id);

    /**
     * 根据配置ID删除所有规则
     */
    int deleteByConfigId(@Param("configId") Long configId);

    /**
     * 根据ID更新加价阶梯规则
     */
    int updateById(BidIncrementRule rule);
}
