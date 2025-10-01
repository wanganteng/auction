package com.auction.service;

import com.auction.entity.UserAddress;

import java.util.List;

/**
 * 用户地址Service接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface UserAddressService {

    /**
     * 根据ID查询地址
     *
     * @param id 地址ID
     * @return 地址信息
     */
    UserAddress getAddressById(Long id);

    /**
     * 查询用户的所有地址
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<UserAddress> getAddressesByUserId(Long userId);

    /**
     * 查询用户的默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    UserAddress getDefaultAddress(Long userId);

    /**
     * 新增地址
     *
     * @param address 地址信息
     * @return 是否成功
     */
    boolean addAddress(UserAddress address);

    /**
     * 更新地址
     *
     * @param address 地址信息
     * @return 是否成功
     */
    boolean updateAddress(UserAddress address);

    /**
     * 删除地址
     *
     * @param id 地址ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean deleteAddress(Long id, Long userId);

    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean setDefaultAddress(Long id, Long userId);
}

