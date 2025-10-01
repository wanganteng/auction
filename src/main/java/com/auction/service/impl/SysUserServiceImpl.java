package com.auction.service.impl;

import com.auction.entity.SysUser;
import com.auction.mapper.SysUserMapper;
import com.auction.service.SysUserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SysUser getById(Long id) {
        log.debug("根据ID查询用户: {}", id);
        return sysUserMapper.selectById(id);
    }

    @Override
    public SysUser getByUsername(String username) {
        log.debug("根据用户名查询用户: {}", username);
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public List<SysUser> getAll() {
        log.debug("查询所有用户");
        return sysUserMapper.selectAll();
    }

    @Override
    public PageInfo<SysUser> getByPage(int pageNum, int pageSize) {
        log.debug("分页查询用户: 页码={}, 大小={}", pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<SysUser> users = sysUserMapper.selectAll();
        return new PageInfo<>(users);
    }

    @Override
    public int getTotalCount() {
        log.debug("统计用户总数");
        return sysUserMapper.countAll();
    }

    @Override
    public boolean save(SysUser user) {
        log.debug("保存用户: {}", user.getUsername());
        try {
            // 加密密码
            if (user.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            
            // 设置默认值
            if (user.getStatus() == null) {
                user.setStatus(1); // 默认启用
            }
            if (user.getUserType() == null) {
                user.setUserType(1); // 默认普通用户
            }
            if (user.getCreateTime() == null) {
                user.setCreateTime(LocalDateTime.now());
            }
            if (user.getUpdateTime() == null) {
                user.setUpdateTime(LocalDateTime.now());
            }
            
            int result = sysUserMapper.insert(user);
            if (result > 0) {
                log.info("用户保存成功: {}", user.getUsername());
                return true;
            } else {
                log.error("用户保存失败: {}", user.getUsername());
                return false;
            }
        } catch (Exception e) {
            log.error("保存用户时发生错误: {}", e.getMessage());
            throw new RuntimeException("保存用户失败", e);
        }
    }

    @Override
    public boolean updateById(SysUser user) {
        log.debug("更新用户: {}", user.getId());
        try {
            user.setUpdateTime(LocalDateTime.now());
            int result = sysUserMapper.updateById(user);
            if (result > 0) {
                log.info("用户更新成功: {}", user.getId());
                return true;
            } else {
                log.error("用户更新失败: {}", user.getId());
                return false;
            }
        } catch (Exception e) {
            log.error("更新用户时发生错误: {}", e.getMessage());
            throw new RuntimeException("更新用户失败", e);
        }
    }

    @Override
    public boolean removeById(Long id) {
        log.debug("删除用户: {}", id);
        try {
            int result = sysUserMapper.deleteById(id);
            if (result > 0) {
                log.info("用户删除成功: {}", id);
                return true;
            } else {
                log.error("用户删除失败: {}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("删除用户时发生错误: {}", e.getMessage());
            throw new RuntimeException("删除用户失败", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        log.debug("检查用户名是否存在: {}", username);
        SysUser user = sysUserMapper.selectByUsername(username);
        return user != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("检查邮箱是否存在: {}", email);
        // 这里需要在Mapper中添加根据邮箱查询的方法
        // 暂时返回false
        return false;
    }

    @Override
    public boolean existsByPhone(String phone) {
        log.debug("检查手机号是否存在: {}", phone);
        // 这里需要在Mapper中添加根据手机号查询的方法
        // 暂时返回false
        return false;
    }

    @Override
    public boolean updatePassword(Long userId, String newPassword) {
        log.debug("更新用户密码: {}", userId);
        try {
            SysUser user = new SysUser();
            user.setId(userId);
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());
            
            int result = sysUserMapper.updateById(user);
            if (result > 0) {
                log.info("用户密码更新成功: {}", userId);
                return true;
            } else {
                log.error("用户密码更新失败: {}", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新用户密码时发生错误: {}", e.getMessage());
            throw new RuntimeException("更新用户密码失败", e);
        }
    }

    @Override
    public boolean updateLastLoginTime(Long userId, String loginIp) {
        log.debug("更新用户最后登录时间: {}", userId);
        try {
            SysUser user = new SysUser();
            user.setId(userId);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(loginIp);
            user.setUpdateTime(LocalDateTime.now());
            
            int result = sysUserMapper.updateById(user);
            if (result > 0) {
                log.info("用户最后登录时间更新成功: {}", userId);
                return true;
            } else {
                log.error("用户最后登录时间更新失败: {}", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新用户最后登录时间时发生错误: {}", e.getMessage());
            throw new RuntimeException("更新用户最后登录时间失败", e);
        }
    }

    @Override
    public boolean validatePassword(String username, String password) {
        log.debug("验证用户密码: {}", username);
        try {
            SysUser user = sysUserMapper.selectByUsername(username);
            if (user != null) {
                return passwordEncoder.matches(password, user.getPassword());
            }
            return false;
        } catch (Exception e) {
            log.error("验证用户密码时发生错误: {}", e.getMessage());
            return false;
        }
    }
}