# AI 服务快速启动指南

## 🚀 启动步骤

### 1. 激活 Python 3.10 虚拟环境

```powershell
cd "E:\Course Project\Intelligent-Contract-Management-System\ai-service"
.\venv\Scripts\Activate.ps1
```

**确认**：看到命令行前缀变为 `(venv)`

---

### 2. 启动 AI 服务

```powershell
python -m uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
```

**首次启动**：需等待 5-10 分钟下载 Embedding 模型  
**后续启动**：秒级启动

**成功标志**：
```
============================================================
AI合同助手服务启动中...
============================================================
[1/2] 正在初始化服务...
      ✅ RAG服务初始化成功
[2/2] 服务配置完成
      RAG增强: 启用
============================================================
INFO:     Uvicorn running on http://0.0.0.0:8002 (Press CTRL+C to quit)
INFO:     Application startup complete.
```

---

### 3. 验证服务

在浏览器访问：
- **服务状态**：http://localhost:8002
- **API 文档**：http://localhost:8002/docs
- **WebSocket 测试**：打开 `test_chat.html`

**健康检查**：
```powershell
curl http://localhost:8002/
```

预期输出：
```json
{
  "service": "AI合同助手",
  "version": "0.3.0",
  "status": "running",
  "rag_enabled": true,
  "knowledge_base_docs": 0
}
```

---

## 🔌 端口信息

- **AI 服务**：8002
- **Spring Boot 后端**：8080（如需启动）

---

## 🛠️ 后端集成启动（可选）

### 启动 Spring Boot

```powershell
cd "E:\Course Project\Intelligent-Contract-Management-System\backend"
mvn -q -DskipTests spring-boot:run
```

**配置已更新**：
```properties
ai.service.base-url=http://localhost:8002
```

---

## 📦 知识库导入（可选）

如果有合同模板文档（PDF/Word），可导入知识库：

```powershell
cd "E:\Course Project\Intelligent-Contract-Management-System\ai-service"
.\venv\Scripts\Activate.ps1

# 将文档放入 knowledge_base/ 目录，然后运行：
python scripts/process_documents.py
```

---

## ⚠️ 常见问题

### Q1: 端口被占用
```
ERROR: [WinError 10013] ...
```

**解决**：杀掉占用进程或换端口
```powershell
# 查找占用 8002 端口的进程
netstat -ano | findstr :8002

# 杀掉进程（替换 <PID>）
taskkill /PID <PID> /F
```

### Q2: 虚拟环境未激活
```
ModuleNotFoundError: No module named 'fastapi'
```

**解决**：执行步骤 1 激活虚拟环境

### Q3: 模型下载慢
**现象**：首次启动卡住不动

**解决**：
- 确保 VPN 开启
- 耐心等待 5-10 分钟
- 查看终端日志确认下载进度

---

## 📝 日常使用

### 启动顺序
1. AI 服务（必须）
2. Spring Boot 后端（如需前后端联调）
3. 前端开发服务器（Vue）

### 停止服务
- **AI 服务**：终端按 `Ctrl+C`
- **Spring Boot**：终端按 `Ctrl+C`

### 查看日志
- AI 服务：直接在启动终端查看
- Spring Boot：查看 `backend/logs/` 目录

---

## ✅ 验证清单

启动后，确认以下项目：

- [ ] 虚拟环境已激活（命令行有 `(venv)` 前缀）
- [ ] AI 服务启动成功（http://localhost:8002 返回JSON）
- [ ] API 文档可访问（http://localhost:8002/docs）
- [ ] RAG 已启用（`rag_enabled: true`）
- [ ] 无致命错误日志

---

## 🔗 相关文档

- [AI服务开发完成总结报告](./AI服务开发完成总结报告.md)
- [Python环境配置完成报告](./Python环境配置完成报告.md)
- [API测试脚本](../../ai-service/test_api.py)
- [WebSocket测试页](../../ai-service/test_chat.html)

---

**最后更新**：2025年12月15日  
**适用版本**：v0.3.0  


