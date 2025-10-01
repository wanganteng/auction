package com.auction.mapper;

import com.auction.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户地址Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface UserAddressMapper {

    /**
     * 根据ID查询地址
     *
     * @param id 地址ID
     * @return 地址信息
     */
    UserAddress selectById(@Param("id") Long id);

    /**
     * 查询用户的所有地址
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<UserAddress> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    UserAddress selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 插入地址
     *
     * @param address 地址信息
     * @return 影响行数
     */
    int insert(UserAddress address);

    /**
     * 更新地址
     *
     * @param address 地址信息
     * @return 影响行数
     */
    int update(UserAddress address);

    /**
     * 删除地址（逻辑删除）
     *
     * @param id 地址ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 取消用户的所有默认地址
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int cancelDefaultByUserId(@Param("userId") Long userId);

    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @return 影响行数
     */
    int setDefault(@Param("id") Long id);

    /**
     * 统计用户地址数量
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    int countByUserId(@Param("userId") Long userId);
}

