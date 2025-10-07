package com.auction.mapper;

import com.auction.entity.AuctionBid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ========================================
 * 拍卖出价Mapper接口（AuctionBidMapper）
 * ========================================
 * 功能说明：
 * 1. 定义出价记录相关的数据库操作接口
 * 2. 使用MyBatis框架进行ORM映射
 * 3. 提供基础的CRUD操作
 * 4. 提供统计和查询方法（最高出价、出价数量等）
 * 
 * MyBatis说明：
 * - @Mapper注解：标记为MyBatis的Mapper接口
 * - 对应XML：resources/mapper/AuctionBidMapper.xml
 * - XML中定义SQL语句实现
 * 
 * 数据库表：auction_bid
 * 
 * 业务说明：
 * - 每次用户出价都会创建一条记录
 * - 记录包含用户、拍品、出价金额、时间等信息
 * - 用于显示出价历史和统计分析
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper  // MyBatis注解
public interface AuctionBidMapper {

    /**
     * 插入出价记录
     * 
     * 功能：向数据库插入一条新的出价记录
     * SQL：INSERT INTO auction_bid ...
     * 
     * 应用场景：用户每次出价都会调用此方法
     * 
     * @param bid 出价对象，包含用户ID、拍品ID、出价金额等
     * @return 影响的行数，成功插入返回1
     */
    int insert(AuctionBid bid);

    /**
     * 更新出价记录
     * 
     * 功能：更新出价记录的状态等信息
     * SQL：UPDATE auction_bid SET ... WHERE id = #{id}
     * 
     * 应用场景：
     * - 撤销出价（将status改为无效）
     * - 更新出价备注
     * 
     * @param bid 出价对象，id必填
     * @return 影响的行数
     */
    int update(AuctionBid bid);

    /**
     * 根据ID查询出价记录
     * 
     * 功能：查询单条出价记录的详细信息
     * SQL：SELECT * FROM auction_bid WHERE id = #{id}
     * 
     * @param id 出价记录ID
     * @return 出价对象，不存在返回null
     */
    AuctionBid selectById(Long id);

    /**
     * 查询出价记录列表（多条件查询）
     * 
     * 功能：根据条件查询出价记录列表
     * 支持的查询条件：用户ID、拍品ID、拍卖会ID、状态等
     * SQL：SELECT * FROM auction_bid WHERE ...（条件动态拼接）
     * 
     * 排序：默认按出价时间倒序（最新的在前）
     * 
     * @param bid 查询条件对象，非空字段作为查询条件
     * @return 出价记录列表
     */
    List<AuctionBid> selectList(AuctionBid bid);

    /**
     * 查询拍品最高出价
     * 
     * 功能：查询指定拍品的当前最高出价记录
     * SQL：SELECT * FROM auction_bid WHERE item_id = #{itemId} AND status = 0 
     *      ORDER BY bid_amount_yuan DESC LIMIT 1
     * 
     * 应用场景：
     * - 判断谁是当前的最高出价者
     * - 计算下一次出价的最低金额
     * - 拍卖结束时确定中标者
     * 
     * @param itemId 拍品ID
     * @return 最高出价记录，如果没有出价返回null
     */
    AuctionBid selectHighestBid(@Param("itemId") Long itemId);

    /**
     * 根据拍卖会ID查询出价记录
     * 
     * 功能：查询某个拍卖会的所有出价记录
     * SQL：SELECT * FROM auction_bid WHERE session_id = #{auctionId}
     * 
     * 应用场景：
     * - 查看拍卖会的所有出价活动
     * - 统计拍卖会的热度
     * 
     * @param auctionId 拍卖会ID
     * @return 出价记录列表
     */
    List<AuctionBid> selectByAuctionId(@Param("auctionId") Long auctionId);

    /**
     * 根据拍卖会ID统计出价数量
     * 
     * 功能：统计某个拍卖会的总出价次数
     * SQL：SELECT COUNT(*) FROM auction_bid WHERE session_id = #{auctionId}
     * 
     * 应用场景：
     * - 显示拍卖会的出价次数统计
     * - 评估拍卖会的活跃度
     * 
     * @param auctionId 拍卖会ID
     * @return 出价次数
     */
    int countByAuctionId(@Param("auctionId") Long auctionId);
}