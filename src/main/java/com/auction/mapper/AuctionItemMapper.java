package com.auction.mapper;

import com.auction.entity.AuctionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ========================================
 * 拍品Mapper接口（AuctionItemMapper）
 * ========================================
 * 功能说明：
 * 1. 定义拍品相关的数据库操作接口
 * 2. 使用MyBatis框架进行ORM映射
 * 3. 提供基础的CRUD操作（增删改查）
 * 4. 提供多种查询方法（按ID、状态、用户、拍卖会等）
 * 
 * MyBatis说明：
 * - @Mapper注解：标记为MyBatis的Mapper接口
 * - 对应的XML配置文件：resources/mapper/AuctionItemMapper.xml
 * - XML文件中定义SQL语句的具体实现
 * - 方法名对应XML中的id属性
 * 
 * 数据库表：auction_item
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper  // MyBatis注解：标记为Mapper接口，Spring自动扫描并生成实现类
public interface AuctionItemMapper {

    /**
     * 插入拍品
     * 
     * 功能：向数据库插入一条新的拍品记录
     * SQL：INSERT INTO auction_item ...
     * 
     * @param item 拍品对象，包含所有字段信息
     * @return 影响的行数，成功插入返回1
     */
    int insert(AuctionItem item);

    /**
     * 更新拍品
     * 
     * 功能：根据ID更新拍品信息（动态SQL，只更新非空字段）
     * SQL：UPDATE auction_item SET ... WHERE id = #{id}
     * 
     * @param item 拍品对象，id必填，其他字段选填
     * @return 影响的行数，成功更新返回1
     */
    int update(AuctionItem item);

    /**
     * 根据ID查询拍品
     * 
     * 功能：查询单个拍品的完整信息
     * SQL：SELECT * FROM auction_item WHERE id = #{id}
     * 
     * @param id 拍品ID
     * @return 拍品对象，不存在返回null
     */
    AuctionItem selectById(Long id);

    /**
     * 查询拍品列表（多条件查询）
     * 
     * 功能：根据条件查询拍品列表（动态SQL）
     * 支持的查询条件：拍品名称、状态、分类、上传人等
     * SQL：SELECT * FROM auction_item WHERE ...（条件动态拼接）
     * 
     * @param item 查询条件对象，非空字段作为查询条件
     * @return 拍品列表，无结果返回空列表
     */
    List<AuctionItem> selectList(AuctionItem item);

    /**
     * 根据ID删除拍品
     * 
     * 功能：从数据库中删除拍品记录
     * SQL：DELETE FROM auction_item WHERE id = #{id}
     * 
     * 注意：这是物理删除，不可恢复
     * 建议：生产环境使用软删除（更新deleted字段）
     * 
     * @param id 要删除的拍品ID
     * @return 影响的行数，成功删除返回1
     */
    int deleteById(Long id);

    /**
     * 根据拍卖会ID批量更新拍品状态
     * 
     * 功能：更新某个拍卖会下所有拍品的状态
     * 应用场景：拍卖会开始时，将所有拍品状态改为"拍卖中"
     * SQL：UPDATE auction_item SET ... WHERE session_id = #{sessionId}
     * 
     * @param sessionId 拍卖会ID
     * @param item 要更新的拍品对象（只使用status等字段）
     * @return 影响的行数
     */
    int updateBySessionId(@Param("sessionId") Long sessionId, @Param("item") AuctionItem item);

    /**
     * 根据状态查询拍品
     * 
     * 功能：查询指定状态的所有拍品
     * SQL：SELECT * FROM auction_item WHERE status = #{status}
     * 
     * @param status 拍品状态（0-下架，1-上架）
     * @return 拍品列表
     */
    List<AuctionItem> selectByStatus(@Param("status") Integer status);

    /**
     * 查询所有拍品
     * 
     * 功能：查询数据库中所有未删除的拍品
     * SQL：SELECT * FROM auction_item WHERE deleted = 0
     * 
     * @return 所有拍品列表
     */
    List<AuctionItem> selectAll();

    /**
     * 根据用户ID查询拍品
     * 
     * 功能：查询指定用户上传的所有拍品
     * SQL：SELECT * FROM auction_item WHERE uploader_id = #{userId}
     * 
     * @param userId 上传人ID（管理员）
     * @return 该用户上传的拍品列表
     */
    List<AuctionItem> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据ID更新拍品
     * 
     * 功能：更新拍品信息（只更新非空字段）
     * SQL：UPDATE auction_item SET ... WHERE id = #{id}
     * 
     * @param item 拍品对象，id必填
     * @return 影响的行数
     */
    int updateById(AuctionItem item);

    /**
     * 根据拍卖会ID查询拍品
     * 
     * 功能：查询某个拍卖会包含的所有拍品
     * 应用场景：拍卖会详情页显示拍品列表
     * SQL：通过auction_session_item关联表查询
     * 
     * @param sessionId 拍卖会ID
     * @return 该拍卖会的拍品列表
     */
    List<AuctionItem> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询可分配到拍卖会的拍品
     * 
     * 功能：查询可以加入新拍卖会的拍品
     * 条件：
     * 1. 拍品状态为"上架"（status=1）
     * 2. 拍品未被任何"未开始"或"进行中"的拍卖会占用
     * 
     * 应用场景：
     * 管理员创建拍卖会时，在拍品选择器中只显示可用的拍品
     * 避免将同一拍品加入多个同时进行的拍卖会
     * 
     * SQL：复杂查询，使用子查询排除已占用的拍品
     * 
     * @return 可用拍品列表
     */
    List<AuctionItem> selectAvailableForAssignment();
}