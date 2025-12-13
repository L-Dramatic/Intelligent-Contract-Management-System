package com.software.contract_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/Knife4j 接口文档配置
 * 访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("电信智慧合同系统 API 接口文档") // 文档标题
                        .version("1.0")                     // 文档版本
                        .description("这是后端 API 的在线调试文档，供前端（肖相宇）使用。")); // 描述
    }
}