package com.auction.service;

import com.auction.entity.SysUser;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 用户服务接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface SysUserService {

    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    SysUser getById(Long id);

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 查询所有用户
     * 
     * @return 用户列表
     */
    List<SysUser> getAll();

    /**
     * 分页查询用户
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<SysUser> getByPage(int pageNum, int pageSize);

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    int getTotalCount();

    /**
     * 保存用户
     * 
     * @param user 用户信息
     * @return 是否成功
     */
    boolean save(SysUser user);

    /**
     * 更新用户
     * 
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateById(SysUser user);

    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 是否成功
     */
    boolean removeById(Long id);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 更新密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean updatePassword(Long userId, String newPassword);

    /**
     * 更新最后登录时间
     * 
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 是否成功
     */
    boolean updateLastLoginTime(Long userId, String loginIp);

    /**
     * 验证密码
     * 
     * @param username 用户名
     * @param password 密码
     * @return 是否匹配
     */
    boolean validatePassword(String username, String password);
}