package com.auction.mapper;

import com.auction.entity.AuctionSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ========================================
 * 拍卖会Mapper接口（AuctionSessionMapper）
 * ========================================
 * 功能说明：
 * 1. 定义拍卖会相关的数据库操作接口
 * 2. 使用MyBatis框架进行ORM映射
 * 3. 提供基础的CRUD操作
 * 4. 提供关联查询（拍品、加价配置等）
 * 
 * MyBatis说明：
 * - @Mapper注解：标记为MyBatis的Mapper接口
 * - 对应XML：resources/mapper/AuctionSessionMapper.xml
 * - XML中定义具体的SQL语句
 * 
 * 数据库表：auction_session
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper  // MyBatis注解：Spring自动扫描并生成实现类
public interface AuctionSessionMapper {

    /**
     * 插入拍卖会
     * 
     * 功能：向数据库插入一条新的拍卖会记录
     * SQL：INSERT INTO auction_session ...
     * 
     * @param session 拍卖会对象，包含所有字段信息
     * @return 影响的行数，成功插入返回1
     */
    int insert(AuctionSession session);

    /**
     * 更新拍卖会
     * 
     * 功能：根据ID更新拍卖会信息（动态SQL，只更新非空字段）
     * SQL：UPDATE auction_session SET ... WHERE id = #{id}
     * 
     * @param session 拍卖会对象，id必填，其他字段选填
     * @return 影响的行数，成功更新返回1
     */
    int update(AuctionSession session);

    /**
     * 根据ID查询拍卖会
     * 
     * 功能：查询单个拍卖会的基本信息
     * SQL：SELECT * FROM auction_session WHERE id = #{id}
     * 
     * @param id 拍卖会ID
     * @return 拍卖会对象，不存在返回null
     */
    AuctionSession selectById(Long id);

    /**
     * 根据ID查询拍卖会（包含加价阶梯配置）
     * 
     * 功能：查询拍卖会并关联查询加价阶梯配置信息
     * SQL：使用LEFT JOIN关联bid_increment_config表
     * 
     * 应用场景：
     * 前端显示拍卖会详情时，需要知道加价规则
     * 
     * @param id 拍卖会ID
     * @return 拍卖会对象（包含bidIncrementConfig字段）
     */
    AuctionSession selectByIdWithBidIncrement(Long id);

    /**
     * 查询拍卖会列表（多条件查询）
     * 
     * 功能：根据条件查询拍卖会列表（动态SQL）
     * 支持的查询条件：名称、状态、类型、时间范围等
     * SQL：SELECT * FROM auction_session WHERE ...（条件动态拼接）
     * 
     * @param session 查询条件对象，非空字段作为查询条件
     * @return 拍卖会列表，无结果返回空列表
     */
    List<AuctionSession> selectList(AuctionSession session);

    /**
     * 根据ID删除拍卖会
     * 
     * 功能：从数据库中删除拍卖会记录
     * SQL：DELETE FROM auction_session WHERE id = #{id}
     * 
     * 注意：物理删除，不可恢复
     * 
     * @param id 要删除的拍卖会ID
     * @return 影响的行数，成功删除返回1
     */
    int deleteById(Long id);

    /**
     * 查询所有拍卖会
     * 
     * 功能：查询数据库中所有未删除的拍卖会
     * SQL：SELECT * FROM auction_session WHERE deleted = 0
     * 
     * @return 所有拍卖会列表
     */
    List<AuctionSession> selectAll();

    /**
     * 根据ID更新拍卖会
     * 
     * 功能：更新拍卖会信息（只更新非空字段）
     * SQL：UPDATE auction_session SET ... WHERE id = #{id}
     * 
     * @param session 拍卖会对象，id必填
     * @return 影响的行数
     */
    int updateById(AuctionSession session);

    /**
     * 根据拍品ID查询关联的拍卖会
     * 
     * 功能：查询某个拍品所在的所有拍卖会
     * SQL：通过auction_session_item关联表查询
     * 
     * 应用场景：
     * - 检查拍品是否已在拍卖会中
     * - 查看拍品的拍卖历史
     * 
     * @param itemId 拍品ID
     * @return 包含该拍品的拍卖会列表
     */
    List<AuctionSession> selectSessionsByItemId(Long itemId);

    /**
     * 根据加价阶梯配置ID查询使用该配置的拍卖会列表
     * 
     * 功能：查询哪些拍卖会使用了指定的加价阶梯配置
     * SQL：SELECT * FROM auction_session WHERE bid_increment_config_id = #{configId}
     * 
     * 应用场景：
     * - 删除或修改加价配置前，检查是否有拍卖会正在使用
     * - 防止删除正在使用的配置导致拍卖会出错
     * 
     * @param configId 加价阶梯配置ID
     * @return 使用该配置的拍卖会列表
     */
    List<AuctionSession> selectSessionsByBidIncrementConfigId(@Param("configId") Long configId);
}