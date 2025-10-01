package com.auction.controller;

import com.auction.common.Result;
import com.auction.entity.SysUser;
import com.auction.entity.UserAddress;
import com.auction.util.SecurityUtils;
import com.auction.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址管理控制器
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/user/addresses")
@Tag(name = "用户地址管理", description = "用户地址管理相关接口")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    

    /**
     * 获取当前登录用户
     */
    private SysUser getCurrentUser() {
        try {
            return SecurityUtils.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前用户的所有地址
     */
    @GetMapping("/list")
    @Operation(summary = "获取地址列表", description = "获取当前用户的所有收货地址")
    public Result<List<UserAddress>> getAddresses() {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录");
            }

            List<UserAddress> addresses = userAddressService.getAddressesByUserId(currentUser.getId());
            return Result.success(addresses);
        } catch (Exception e) {
            log.error("获取地址列表失败", e);
            return Result.error("获取地址列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    @Operation(summary = "获取默认地址", description = "获取当前用户的默认收货地址")
    public Result<UserAddress> getDefaultAddress() {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录");
            }

            UserAddress address = userAddressService.getDefaultAddress(currentUser.getId());
            return Result.success(address);
        } catch (Exception e) {
            log.error("获取默认地址失败", e);
            return Result.error("获取默认地址失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取地址详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取地址详情", description = "根据ID获取地址详情")
    public Result<UserAddress> getAddressById(@PathVariable Long id) {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录");
            }

            UserAddress address = userAddressService.getAddressById(id);
            if (address == null) {
                return Result.error("地址不存在");
            }

            // 验证权限
            if (!address.getUserId().equals(currentUser.getId())) {
                return Result.error("无权限访问该地址");
            }

            return Result.success(address);
        } catch (Exception e) {
            log.error("获取地址详情失败", e);
            return Result.error("获取地址详情失败：" + e.getMessage());
        }
    }

    /**
     * 新增地址
     */
    @PostMapping("/add")
    @Operation(summary = "新增地址", description = "新增收货地址")
    public Result<String> addAddress(@RequestBody UserAddress address) {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录");
            }

            // 设置用户ID
            address.setUserId(currentUser.getId());

            // 验证必填字段
            if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
                return Result.error("收货人姓名不能为空");
            }
            if (address.getReceiverPhone() == null || address.getReceiverPhone().trim().isEmpty()) {
                return Result.error("收货人电话不能为空");
            }
            if (address.getDetailAddress() == null || address.getDetailAddress().trim().isEmpty()) {
                return Result.error("详细地址不能为空");
            }

            boolean success = userAddressService.addAddress(address);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            log.error("新增地址失败", e);
            return Result.error("新增地址失败：" + e.getMessage());
        }
    }

    /**
     * 更新地址
     */
    @PutMapping("/update")
    @Operation(summary = "更新地址", description = "更新收货地址")
    public Result<String> updateAddress(@RequestBody UserAddress address) {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录");
            }

            if (address.getId() == null) {
                return Result.error("地址ID不能为空");
            }

            // 验证地址是否属于当前用户
            UserAddress existingAddress = userAddressService.getAddressById(address.getId());
            if (existingAddress == null) {
                return Result.error("地址不存在");
            }
            if (!existingAddress.getUserId().equals(currentUser.getId())) {
                return Result.error("无权限修改该地址");
            }

            boolean success = userAddressService.updateAddress(address);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新地址失败", e);
            return Result.error("更新地址失败：" + e.getMessage());
        }
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "删除收货地址")
    public Result<String> deleteAddress(@PathVariable Long id) {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录");
            }

            boolean success = userAddressService.deleteAddress(id, currentUser.getId());
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除地址失败", e);
            return Result.error("删除地址失败：" + e.getMessage());
        }
    }

    /**
     * 设置默认地址
     */
    @PostMapping("/{id}/default")
    @Operation(summary = "设置默认地址", description = "将指定地址设置为默认地址")
    public Result<String> setDefaultAddress(@PathVariable Long id) {
        try {
            SysUser currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录");
            }

            boolean success = userAddressService.setDefaultAddress(id, currentUser.getId());
            if (success) {
                return Result.success("设置成功");
            } else {
                return Result.error("设置失败");
            }
        } catch (Exception e) {
            log.error("设置默认地址失败", e);
            return Result.error("设置默认地址失败：" + e.getMessage());
        }
    }
}

