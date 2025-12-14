package com.software.contract_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * AI服务配置类
 */
@Configuration
public class AIServiceConfig {
    
    @Value("${ai.service.base-url}")
    private String aiServiceBaseUrl;
    
    @Value("${ai.service.timeout}")
    private int timeout;
    
    @Bean
    public WebClient aiWebClient() {
        return WebClient.builder()
                .baseUrl(aiServiceBaseUrl)
                .build();
    }
}


