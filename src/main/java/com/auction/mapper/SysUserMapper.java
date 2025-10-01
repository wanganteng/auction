package com.auction.mapper;

import com.auction.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    SysUser selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 查询所有用户
     * 
     * @return 用户列表
     */
    List<SysUser> selectAll();

    /**
     * 分页查询用户
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户列表
     */
    List<SysUser> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    int countAll();

    /**
     * 插入用户
     * 
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(SysUser user);

    /**
     * 更新用户
     * 
     * @param user 用户信息
     * @return 影响行数
     */
    int updateById(SysUser user);

    /**
     * 根据ID删除用户
     * 
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户名和密码查询用户
     * 
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    SysUser selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}
