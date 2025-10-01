package com.auction.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置类
 * 配置安全策略、认证和授权规则
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;


    /**
     * 认证管理器
     * 
     * @param authConfig 认证配置
     * @return AuthenticationManager
     * @throws Exception 异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 安全过滤器链配置
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
                // 允许预检请求
                .antMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // 公开访问的URL - 最具体的路径放在最前面
                .antMatchers("/auction/login").permitAll()
                .antMatchers("/auction/admin/login").permitAll()
                .antMatchers("/auction/user/login").permitAll()
                .antMatchers("/auction/user/register").permitAll()
                .antMatchers("/auction/admin/static/**").permitAll()
                .antMatchers("/auction/admin/css/**").permitAll()
                .antMatchers("/auction/admin/js/**").permitAll()
                .antMatchers("/auction/admin/images/**").permitAll()
                .antMatchers("/auction/admin/fonts/**").permitAll()
                .antMatchers("/auction/user/css/**").permitAll()
                .antMatchers("/auction/user/js/**").permitAll()
                .antMatchers("/auction/user/images/**").permitAll()
                // 根路径静态资源（统一登录页引用的本地资源）
                .antMatchers("/css/**").permitAll()
                .antMatchers("/js/**").permitAll()
                // API接口 - 登录相关API允许访问
                .antMatchers("/api/auth/login").permitAll()
                .antMatchers("/api/auth/register").permitAll()
                .antMatchers("/api/auth/logout").permitAll()
                .antMatchers("/auction/api/**").permitAll()
                // 其他API需要认证
                .antMatchers("/api/**").authenticated()
                // Swagger文档
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/v3/api-docs/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers("/error").permitAll()
                // 静态资源
                .antMatchers("/images/**").permitAll()
                // 测试页面
                .antMatchers("/test-token.html").permitAll()
                // Chrome DevTools 自动请求
                .antMatchers("/.well-known/**").permitAll()
                // WebSocket连接
                .antMatchers("/ws/**").permitAll()
                .antMatchers("/websocket/**").permitAll()
                // 页面路由改为前端自校验登录态（仅API鉴权）
                .antMatchers("/auction/admin/**").permitAll()
                .antMatchers("/auction/user/**").permitAll()
                // 其他请求需要认证（保护API）
                .anyRequest().authenticated();

        // 添加JWT过滤器 - 但排除登录API
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS配置
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
