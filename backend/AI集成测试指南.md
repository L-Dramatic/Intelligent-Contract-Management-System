# 后端AI集成测试指南

## 🚀 启动步骤

### 1. 确保AI服务运行
```bash
cd ai-service
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 2. 启动Spring Boot
```bash
cd backend
mvn spring-boot:run
```

或者在IDEA中直接运行 `ContractSystemApplication`

## 🧪 测试接口

### 方法1：使用浏览器访问Knife4j

打开：`http://localhost:8080/doc.html`

在"AI服务"分组下可以看到所有接口，点击"调试"即可测试

### 方法2：使用curl命令

```bash
# 1. 健康检查
curl http://localhost:8080/api/ai/health

# 2. 生成条款
curl -X POST http://localhost:8080/api/ai/generate \
  -H "Content-Type: application/json" \
  -d '{
    "contractType": "base_station",
    "clauseType": "租赁期限",
    "requirement": "需要包含续租条件和提前解约条款"
  }'

# 3. 合规检查
curl -X POST http://localhost:8080/api/ai/check \
  -H "Content-Type: application/json" \
  -d '{
    "contractType": "base_station",
    "clauseContent": "租赁期限为5年，到期后如双方无异议可自动续约3年"
  }'

# 4. 获取统计
curl http://localhost:8080/api/ai/stats
```

### 方法3：使用Postman

导入以下URL到Postman测试：
- POST http://localhost:8080/api/ai/generate
- POST http://localhost:8080/api/ai/check
- GET http://localhost:8080/api/ai/stats
- GET http://localhost:8080/api/ai/health

## ✅ 预期结果

### 健康检查
```
AI Service Proxy is running
```

### 生成条款
```json
{
  "success": true,
  "data": "第一条 租赁期限\n1. 本合同租赁期限为5年...",
  "ragUsed": false
}
```

### 合规检查
```json
{
  "success": true,
  "data": "【合规评估】\n整体评分：良好\n【风险点】...",
  "ragUsed": false
}
```

## 🐛 常见问题

### 1. 连接超时
- 检查AI服务是否启动：访问 http://localhost:8000
- 检查配置：application.properties 中的 ai.service.base-url

### 2. 端口冲突
- Spring Boot默认8080
- AI服务默认8000
- 如有冲突请修改配置

### 3. 跨域问题
- 已在AIController配置@CrossOrigin
- 生产环境请改为具体域名

## 📝 给前端同学的接口文档

见：`Docs/后端AI集成开发指南.md` 最后一节

