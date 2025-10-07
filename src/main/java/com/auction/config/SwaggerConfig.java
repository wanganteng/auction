package com.auction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ========================================
 * OpenAPI 3配置类（SwaggerConfig）
 * ========================================
 * 功能说明：
 * 1. 配置Swagger/OpenAPI文档生成
 * 2. 提供在线API文档和测试界面
 * 3. 配置JWT认证支持
 * 4. 设置API文档的基本信息（标题、描述、联系方式等）
 * 
 * 什么是Swagger/OpenAPI：
 * - Swagger是一套API文档生成工具
 * - OpenAPI 3是新版本的API规范
 * - 可以自动根据注解生成API文档
 * - 提供可视化的API测试界面
 * 
 * 访问地址：
 * - Swagger UI：http://localhost:8080/swagger-ui.html
 * - API文档：http://localhost:8080/v3/api-docs
 * 
 * 主要用途：
 * 1. 开发阶段：方便前端开发者了解API接口
 * 2. 测试阶段：可以在线测试API接口
 * 3. 文档化：自动生成并维护API文档
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration  // Spring配置类注解
public class SwaggerConfig {

    /**
     * 创建OpenAPI文档Bean
     * 
     * 功能说明：
     * 1. 配置API文档的基本信息
     * 2. 配置JWT安全认证
     * 3. 让Swagger UI支持Bearer Token认证
     * 
     * JWT认证配置：
     * - 类型：HTTP Bearer
     * - 格式：JWT
     * - 使用方式：在Swagger UI的Authorize按钮中输入token
     * 
     * @return OpenAPI配置对象
     */
    @Bean  // 注册为Spring Bean
    public OpenAPI createOpenAPI() {
        return new OpenAPI()
                // 设置API基本信息（标题、描述、版本等）
                .info(apiInfo())
                // 添加全局安全要求：所有接口默认需要JWT认证
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                // 配置安全方案：定义JWT认证的具体配置
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)      // 类型：HTTP认证
                                .scheme("bearer")                    // 方案：Bearer Token
                                .bearerFormat("JWT")                 // 格式：JWT
                                .description("JWT认证，格式：Bearer {token}")));  // 说明文字
    }

    /**
     * API基本信息配置
     * 
     * 功能说明：
     * 配置API文档的标题、描述、版本、联系方式、许可证等信息
     * 这些信息会显示在Swagger UI的顶部
     * 
     * @return Info对象，包含API的基本信息
     */
    private Info apiInfo() {
        return new Info()
                .title("拍卖系统API文档")                      // API文档标题
                .description("拍卖系统后端API接口文档")        // API文档描述
                .version("1.0.0")                             // API版本号
                .contact(new Contact()                         // 联系人信息
                        .name("拍卖系统开发团队")
                        .email("auction@example.com"))
                .license(new License()                         // 许可证信息
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
