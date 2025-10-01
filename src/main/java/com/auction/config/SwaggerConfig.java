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
 * OpenAPI 3配置类
 * 配置API文档 (替代Swagger 2)
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class SwaggerConfig {

    /**
     * 创建OpenAPI文档
     * 
     * @return OpenAPI
     */
    @Bean
    public OpenAPI createOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT认证")));
    }

    /**
     * API信息
     * 
     * @return Info
     */
    private Info apiInfo() {
        return new Info()
                .title("拍卖系统API文档")
                .description("拍卖系统后端API接口文档")
                .version("1.0.0")
                .contact(new Contact()
                        .name("拍卖系统开发团队")
                        .email("auction@example.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
