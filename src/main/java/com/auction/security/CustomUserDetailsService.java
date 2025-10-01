package com.auction.security;

import com.auction.entity.SysUser;
import com.auction.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("根据用户名加载用户: {}", username);
        
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            log.error("未找到用户: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        if (user.getStatus() == 0) {
            log.error("用户账户已禁用: {}", username);
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }

        return new CustomUserPrincipal(user);
    }

    /**
     * 自定义用户主体类
     * 实现Spring Security的UserDetails接口
     */
    public static class CustomUserPrincipal implements UserDetails {
        private final SysUser user;

        public CustomUserPrincipal(SysUser user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // 根据用户类型添加角色
            if (user.getUserType() == 1) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            
            // 添加用户权限（这里可以根据实际需求扩展）
            if (user.getRoles() != null) {
                user.getRoles().forEach(role -> {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
                });
            }
            
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getStatus() == 1;
        }

        public SysUser getUser() {
            return user;
        }
    }
}
