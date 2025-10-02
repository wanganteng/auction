package com.auction.mapper;

import com.auction.entity.BidIncrementConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 加价阶梯配置Mapper接口
 *
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface BidIncrementConfigMapper {

    /**
     * 插入加价阶梯配置
     */
    int insert(BidIncrementConfig config);

    /**
     * 更新加价阶梯配置
     */
    int update(BidIncrementConfig config);

    /**
     * 根据ID查询加价阶梯配置
     */
    BidIncrementConfig selectById(Long id);

    /**
     * 查询启用的加价阶梯配置列表
     */
    List<BidIncrementConfig> selectEnabledList();

    /**
     * 查询所有加价阶梯配置列表
     */
    List<BidIncrementConfig> selectList(BidIncrementConfig config);

    /**
     * 根据ID删除加价阶梯配置
     */
    int deleteById(Long id);

    /**
     * 根据ID更新加价阶梯配置
     */
    int updateById(BidIncrementConfig config);

    /**
     * 根据配置ID查询规则数量
     */
    int countRulesByConfigId(@Param("configId") Long configId);

    /**
     * 根据拍卖会ID查询关联的加价阶梯配置
     */
    BidIncrementConfig selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询所有加价阶梯配置（包括禁用的）
     */
    List<BidIncrementConfig> selectAll();
}
