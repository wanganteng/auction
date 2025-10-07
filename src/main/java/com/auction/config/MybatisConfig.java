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
 * ========================================
 * MyBatis配置类（MybatisConfig）
 * ========================================
 * 功能说明：
 * 1. 配置MyBatis框架的核心组件
 * 2. 配置SqlSessionFactory（MyBatis的核心工厂类）
 * 3. 配置Mapper接口的扫描路径
 * 4. 配置Mapper XML文件的位置
 * 
 * MyBatis说明：
 * - MyBatis是一个优秀的持久层框架
 * - 支持自定义SQL、存储过程和高级映射
 * - 避免了几乎所有的JDBC代码和手动设置参数
 * - 使用XML或注解来配置和映射原生类型、接口和Java对象
 * 
 * 配置内容：
 * 1. SqlSessionFactory：MyBatis的核心工厂，用于创建SqlSession
 * 2. Mapper扫描：自动扫描com.auction.mapper包下的所有接口
 * 3. XML映射：加载classpath:mapper/*.xml中的SQL映射文件
 * 4. 全局配置：加载mybatis-config.xml中的全局设置
 * 
 * 目录结构：
 * - Mapper接口：src/main/java/com/auction/mapper/*.java
 * - Mapper XML：src/main/resources/mapper/*.xml
 * - 全局配置：src/main/resources/mybatis-config.xml
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration  // Spring配置类注解
@MapperScan("com.auction.mapper")  // 扫描Mapper接口的包路径
public class MybatisConfig {

    /* ========================= 依赖注入 ========================= */

    @Autowired
    private DataSource dataSource;  // 数据源，Spring Boot自动配置

    /**
     * 配置SqlSessionFactory Bean
     * 
     * 功能说明：
     * 1. 创建MyBatis的核心工厂对象
     * 2. 设置数据源（数据库连接池）
     * 3. 加载MyBatis全局配置文件
     * 4. 加载所有Mapper XML映射文件
     * 
     * SqlSessionFactory作用：
     * - 创建SqlSession对象（数据库会话）
     * - 管理Mapper接口的实现
     * - 执行SQL语句
     * 
     * 配置文件说明：
     * - mybatis-config.xml：MyBatis全局配置（驼峰命名、延迟加载等）
     * - mapper/*.xml：各个Mapper的SQL语句定义
     * 
     * @return SqlSessionFactory对象
     * @throws Exception 配置失败时抛出异常
     */
    @Bean  // 注册为Spring Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        // 创建SqlSessionFactory构建器
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        // 设置数据源
        sessionFactory.setDataSource(dataSource);
        
        // 创建资源解析器，用于加载classpath下的文件
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        
        // 设置MyBatis全局配置文件路径
        // 配置文件包含：驼峰命名映射、延迟加载、缓存等设置
        sessionFactory.setConfigLocation(resolver.getResource("classpath:mybatis-config.xml"));
        
        // 设置Mapper XML文件路径（支持通配符*）
        // 会加载classpath:mapper/目录下的所有.xml文件
        sessionFactory.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        
        // 构建并返回SqlSessionFactory对象
        return sessionFactory.getObject();
    }
}
