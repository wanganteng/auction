package com.auction.service.impl;

import com.auction.entity.UserAddress;
import com.auction.mapper.UserAddressMapper;
import com.auction.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户地址Service实现类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public UserAddress getAddressById(Long id) {
        return userAddressMapper.selectById(id);
    }

    @Override
    public List<UserAddress> getAddressesByUserId(Long userId) {
        return userAddressMapper.selectByUserId(userId);
    }

    @Override
    public UserAddress getDefaultAddress(Long userId) {
        return userAddressMapper.selectDefaultByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAddress(UserAddress address) {
        try {
            // 构建完整地址
            StringBuilder fullAddress = new StringBuilder();
            if (address.getProvince() != null && !address.getProvince().isEmpty()) {
                fullAddress.append(address.getProvince());
            }
            if (address.getCity() != null && !address.getCity().isEmpty()) {
                fullAddress.append(address.getCity());
            }
            if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
                fullAddress.append(address.getDistrict());
            }
            fullAddress.append(address.getDetailAddress());
            address.setFullAddress(fullAddress.toString());

            // 如果设置为默认地址，先取消其他默认地址
            if (address.getIsDefault() != null && address.getIsDefault() == 1) {
                userAddressMapper.cancelDefaultByUserId(address.getUserId());
            }

            // 如果是用户的第一个地址，自动设为默认地址
            int count = userAddressMapper.countByUserId(address.getUserId());
            if (count == 0) {
                address.setIsDefault(1);
            }

            int result = userAddressMapper.insert(address);
            return result > 0;
        } catch (Exception e) {
            log.error("新增地址失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(UserAddress address) {
        try {
            // 构建完整地址
            if (address.getDetailAddress() != null) {
                StringBuilder fullAddress = new StringBuilder();
                if (address.getProvince() != null && !address.getProvince().isEmpty()) {
                    fullAddress.append(address.getProvince());
                }
                if (address.getCity() != null && !address.getCity().isEmpty()) {
                    fullAddress.append(address.getCity());
                }
                if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
                    fullAddress.append(address.getDistrict());
                }
                fullAddress.append(address.getDetailAddress());
                address.setFullAddress(fullAddress.toString());
            }

            // 如果设置为默认地址，先取消其他默认地址
            if (address.getIsDefault() != null && address.getIsDefault() == 1) {
                UserAddress existingAddress = userAddressMapper.selectById(address.getId());
                if (existingAddress != null) {
                    userAddressMapper.cancelDefaultByUserId(existingAddress.getUserId());
                }
            }

            int result = userAddressMapper.update(address);
            return result > 0;
        } catch (Exception e) {
            log.error("更新地址失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long id, Long userId) {
        try {
            // 验证地址是否属于当前用户
            UserAddress address = userAddressMapper.selectById(id);
            if (address == null || !address.getUserId().equals(userId)) {
                log.warn("地址不存在或无权限删除，addressId: {}, userId: {}", id, userId);
                return false;
            }

            int result = userAddressMapper.deleteById(id);
            
            // 如果删除的是默认地址，将最近创建的地址设为默认
            if (result > 0 && address.getIsDefault() == 1) {
                List<UserAddress> addresses = userAddressMapper.selectByUserId(userId);
                if (!addresses.isEmpty()) {
                    userAddressMapper.setDefault(addresses.get(0).getId());
                }
            }

            return result > 0;
        } catch (Exception e) {
            log.error("删除地址失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long id, Long userId) {
        try {
            // 验证地址是否属于当前用户
            UserAddress address = userAddressMapper.selectById(id);
            if (address == null || !address.getUserId().equals(userId)) {
                log.warn("地址不存在或无权限设置，addressId: {}, userId: {}", id, userId);
                return false;
            }

            // 先取消该用户的所有默认地址
            userAddressMapper.cancelDefaultByUserId(userId);

            // 设置当前地址为默认
            int result = userAddressMapper.setDefault(id);
            return result > 0;
        } catch (Exception e) {
            log.error("设置默认地址失败", e);
            throw e;
        }
    }
}

