# Spring Boot AI服务集成开发指南

## 📋 任务目标

在 Spring Boot 后端添加AI服务代理层，连接Python AI服务与Vue前端。

---

## 🏗️ 架构设计

```
Vue Frontend (port 5173)
      ↓
Spring Boot Backend (port 8080)
      ↓ (代理)
Python AI Service (port 8000)
```

---

## 📁 文件结构

```
backend/src/main/java/com/xxx/
├── config/
│   └── AIServiceConfig.java          # AI服务配置
├── controller/
│   └── AIController.java              # AI REST接口
├── service/
│   ├── AIService.java                 # AI服务接口
│   └── impl/
│       └── AIServiceImpl.java         # AI服务实现
├── dto/
│   ├── AIGenerateRequest.java         # 生成请求DTO
│   ├── AICheckRequest.java            # 检查请求DTO
│   └── AIResponse.java                # AI响应DTO
└── websocket/
    └── AIWebSocketHandler.java        # WebSocket处理器
```

---

## 🔧 步骤1：添加依赖

**Maven** (`pom.xml`):
```xml
<!-- WebSocket支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- HTTP客户端 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Lombok (可选，简化代码) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

## 🔧 步骤2：配置文件

**application.yml**:
```yaml
ai-service:
  base-url: http://localhost:8000
  websocket-url: ws://localhost:8000/ws/chat
  timeout: 30000  # 30秒超时
  
# WebSocket配置
spring:
  websocket:
    allowed-origins: "*"  # 生产环境改为具体域名
```

---

## 🔧 步骤3：配置类

**AIServiceConfig.java**:
```java
package com.xxx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AIServiceConfig {
    
    @Value("${ai-service.base-url}")
    private String aiServiceBaseUrl;
    
    @Value("${ai-service.timeout}")
    private int timeout;
    
    @Bean
    public WebClient aiWebClient() {
        return WebClient.builder()
                .baseUrl(aiServiceBaseUrl)
                .build();
    }
}
```

**WebSocketConfig.java**:
```java
package com.xxx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/ai")
                .setAllowedOrigins("*")
                .withSockJS();
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```

---

## 🔧 步骤4：DTO定义

**AIGenerateRequest.java**:
```java
package com.xxx.dto;

import lombok.Data;

@Data
public class AIGenerateRequest {
    private String contractType;    // 合同类型
    private String clauseType;      // 条款类型
    private String requirement;     // 具体需求
}
```

**AICheckRequest.java**:
```java
package com.xxx.dto;

import lombok.Data;

@Data
public class AICheckRequest {
    private String contractType;    // 合同类型
    private String clauseContent;   // 待检查条款
}
```

**AIResponse.java**:
```java
package com.xxx.dto;

import lombok.Data;

@Data
public class AIResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private Boolean ragUsed;  // 是否使用RAG
}
```

---

## 🔧 步骤5：Service层

**AIService.java**:
```java
package com.xxx.service;

import com.xxx.dto.*;
import reactor.core.publisher.Mono;

public interface AIService {
    
    /**
     * 生成合同条款
     */
    Mono<AIResponse<String>> generateClause(AIGenerateRequest request);
    
    /**
     * 合规性检查
     */
    Mono<AIResponse<String>> checkCompliance(AICheckRequest request);
    
    /**
     * 获取知识库统计
     */
    Mono<AIResponse<Object>> getKnowledgeStats();
}
```

**AIServiceImpl.java**:
```java
package com.xxx.service.impl;

import com.xxx.dto.*;
import com.xxx.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    
    private final WebClient aiWebClient;
    
    @Override
    public Mono<AIResponse<String>> generateClause(AIGenerateRequest request) {
        log.info("调用AI生成条款: {}", request);
        
        return aiWebClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AIResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> log.error("AI生成条款失败", e))
                .onErrorReturn(createErrorResponse("AI服务调用失败"));
    }
    
    @Override
    public Mono<AIResponse<String>> checkCompliance(AICheckRequest request) {
        log.info("调用AI合规检查: {}", request);
        
        return aiWebClient.post()
                .uri("/api/check")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AIResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> log.error("AI合规检查失败", e))
                .onErrorReturn(createErrorResponse("AI服务调用失败"));
    }
    
    @Override
    public Mono<AIResponse<Object>> getKnowledgeStats() {
        return aiWebClient.get()
                .uri("/api/knowledge/stats")
                .retrieve()
                .bodyToMono(AIResponse.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorReturn(createErrorResponse("获取统计信息失败"));
    }
    
    private AIResponse<String> createErrorResponse(String message) {
        AIResponse<String> response = new AIResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
```

---

## 🔧 步骤6：Controller层

**AIController.java**:
```java
package com.xxx.controller;

import com.xxx.dto.*;
import com.xxx.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // 生产环境改为具体域名
public class AIController {
    
    private final AIService aiService;
    
    /**
     * 生成合同条款
     */
    @PostMapping("/generate")
    public Mono<AIResponse<String>> generateClause(@RequestBody AIGenerateRequest request) {
        log.info("收到生成条款请求: {}", request);
        return aiService.generateClause(request);
    }
    
    /**
     * 合规性检查
     */
    @PostMapping("/check")
    public Mono<AIResponse<String>> checkCompliance(@RequestBody AICheckRequest request) {
        log.info("收到合规检查请求: {}", request);
        return aiService.checkCompliance(request);
    }
    
    /**
     * 获取知识库统计
     */
    @GetMapping("/stats")
    public Mono<AIResponse<Object>> getStats() {
        return aiService.getKnowledgeStats();
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("AI Service Proxy is running");
    }
}
```

---

## 🔧 步骤7：WebSocket处理器（可选，更复杂）

**简化方案**：前端直接连接Python AI服务的WebSocket

**完整方案**：通过Spring Boot代理WebSocket

```java
package com.xxx.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class AIWebSocketHandler {
    
    @MessageMapping("/ai/chat")
    @SendTo("/topic/ai/response")
    public String handleChatMessage(String message) {
        log.info("收到聊天消息: {}", message);
        // 转发到Python AI服务
        // 实现代理逻辑
        return "Echo: " + message;
    }
}
```

**建议**：WebSocket部分先让前端直接连Python服务，REST API通过后端代理

---

## 📊 开发步骤总结

1. ✅ 添加依赖（5分钟）
2. ✅ 配置文件（5分钟）
3. ✅ 创建配置类（10分钟）
4. ✅ 定义DTO（15分钟）
5. ✅ 实现Service层（30分钟）
6. ✅ 实现Controller层（15分钟）
7. ✅ 测试接口（30分钟）

**总计**: 约2小时

---

## 🧪 测试方法

### 1. 启动AI服务
```bash
cd ai-service
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 2. 启动Spring Boot
```bash
cd backend
mvn spring-boot:run
```

### 3. 测试接口

**使用Postman/curl**:

```bash
# 生成条款
curl -X POST http://localhost:8080/api/ai/generate \
  -H "Content-Type: application/json" \
  -d '{
    "contractType": "base_station",
    "clauseType": "租赁期限",
    "requirement": "需要包含续租条件"
  }'

# 合规检查
curl -X POST http://localhost:8080/api/ai/check \
  -H "Content-Type: application/json" \
  -d '{
    "contractType": "base_station",
    "clauseContent": "租赁期限为5年..."
  }'

# 获取统计
curl http://localhost:8080/api/ai/stats
```

---

## 📝 前后端接口文档

给前端同学的接口说明：

### 接口1：生成条款
```
POST /api/ai/generate
Content-Type: application/json

Request:
{
  "contractType": "base_station",  // 合同类型
  "clauseType": "租赁期限",         // 条款类型
  "requirement": "需要包含续租条件"  // 具体需求
}

Response:
{
  "success": true,
  "data": "第一条 租赁期限...",
  "ragUsed": false
}
```

### 接口2：合规检查
```
POST /api/ai/check
Content-Type: application/json

Request:
{
  "contractType": "base_station",
  "clauseContent": "待检查的条款内容"
}

Response:
{
  "success": true,
  "data": "【合规评估】\n优秀\n【风险点】...",
  "ragUsed": false
}
```

### WebSocket：实时对话
```
前端直连Python服务：
ws://localhost:8000/ws/chat/{userId}

发送格式：
{
  "message": "用户消息",
  "contract_type": "base_station"  // 可选
}

接收格式：
{
  "type": "message",
  "content": "AI回复",
  "timestamp": "2025-12-15T...",
  "rag_used": false
}
```

---

## ⚠️ 注意事项

1. **跨域配置**
   - 开发环境：允许所有来源
   - 生产环境：改为具体前端域名

2. **超时处理**
   - AI生成可能需要5-10秒
   - 设置合理的超时时间
   - 前端显示加载状态

3. **错误处理**
   - 捕获所有异常
   - 返回友好的错误信息
   - 记录详细日志

4. **安全性**
   - 添加用户认证
   - 限流防滥用
   - 敏感信息脱敏

---

## 🎯 完成标准

- [ ] 所有依赖添加完成
- [ ] 配置文件正确
- [ ] Service层实现完成
- [ ] Controller层实现完成
- [ ] 所有接口测试通过
- [ ] 接口文档提供给前端
- [ ] 日志记录完善

---

**预计完成时间**: 2-3小时  
**难度**: ⭐⭐⭐ 中等  
**优先级**: 🔴 P0 最高

