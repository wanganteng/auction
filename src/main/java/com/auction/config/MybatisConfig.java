package com.auction.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis配置类
 * 配置SqlSessionFactory和Mapper扫描
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@MapperScan("com.auction.mapper")
public class MybatisConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * 配置SqlSessionFactory
     * 
     * @return SqlSessionFactory
     * @throws Exception 配置异常
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // 设置MyBatis配置文件路径
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setConfigLocation(resolver.getResource("classpath:mybatis-config.xml"));
        
        // 设置Mapper XML文件路径
        sessionFactory.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        
        return sessionFactory.getObject();
    }
}
