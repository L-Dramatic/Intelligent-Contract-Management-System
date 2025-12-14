# Python 环境配置完成报告

## 📅 日期
2025年12月15日

---

## ✅ 完成事项

### 1. Python 3.10 虚拟环境配置

**背景问题**：
- Python 3.14 无法安装 `chromadb`（依赖 `pulsar-client` 不支持）
- Python 3.11 安装路径无法访问

**解决方案**：
- 使用系统已安装的 **Python 3.10.9** 创建虚拟环境
- 虚拟环境路径：`E:\Course Project\Intelligent-Contract-Management-System\ai-service\venv`

**执行命令**：
```powershell
cd "E:\Course Project\Intelligent-Contract-Management-System\ai-service"
py -V:3.10 -m venv venv
.\venv\Scripts\Activate.ps1
```

---

### 2. 依赖安装（完整RAG版本）

**安装的包**：

| 包名 | 版本 | 用途 |
|------|------|------|
| `fastapi` | 0.124.4 | Web 框架 |
| `uvicorn` | 0.38.0 | ASGI 服务器 |
| `websockets` | 15.0.1 | WebSocket 支持 |
| `dashscope` | 1.25.3 | 通义千问 API |
| `PyPDF2` | 3.0.1 | PDF 文档解析 |
| `python-docx` | 1.2.0 | Word 文档解析 |
| `chromadb` | 0.4.22 | 向量数据库 |
| `sentence-transformers` | 5.2.0 | Embedding 模型 |
| `numpy` | **1.26.4** | 数值计算（降级） |

**关键修复**：
- 降级 `numpy` 从 2.2.6 到 1.26.4（`chromadb 0.4.22` 不兼容 NumPy 2.x）

**安装方式**：
使用官方 PyPI 源（用户有 VPN），避免清华镜像速度慢。

---

### 3. 验证测试

**导入测试**：
```bash
python -c "import chromadb; print('ChromaDB: OK')"
python -c "import sentence_transformers; print('Sentence-Transformers: OK')"
```

**结果**：
```
ChromaDB: OK
Sentence-Transformers: OK
```

✅ **所有核心库正常加载**

---

### 4. 服务启动配置

**端口变更**：
- 原计划：8001（被占用）
- 最终端口：**8002**

**启动命令**：
```powershell
cd "E:\Course Project\Intelligent-Contract-Management-System\ai-service"
.\venv\Scripts\Activate.ps1
python -m uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
```

**后端配置同步**：
更新 `backend/src/main/resources/application.properties`：
```properties
ai.service.base-url=http://localhost:8002
```

---

### 5. Embedding 模型自动下载

**模型信息**：
- 模型名称：`sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2`
- 缓存位置：`C:\Users\30620\.cache\huggingface\hub\`
- 下载状态：**进行中**（首次启动时自动下载）

**预期时间**：
- 模型大小：约 500MB
- 首次启动需 5-10 分钟（取决于网络速度）
- 后续启动：秒级

---

## 🔧 环境信息

```yaml
操作系统: Windows 10
Python 版本: 3.10.9
虚拟环境: venv (Python 3.10.9)
包管理器: pip 25.3
安装源: PyPI 官方源（https://pypi.org）
AI 服务端口: 8002
```

---

## 📦 后续任务

### 立即可执行
1. ✅ 虚拟环境配置完成
2. ✅ 依赖安装完成
3. 🔄 服务启动中（等待模型下载）

### 等待模型下载后
4. ⏳ 测试 RAG 检索功能
5. ⏳ 导入合同模板文档到向量数据库
6. ⏳ 前后端联调

### 前端任务（由其他组员负责）
- 侧边栏 AI 对话组件开发
- WebSocket 客户端集成

---

## 💡 重要提示

### 首次启动特别说明
服务首次启动时会下载 Embedding 模型，**请耐心等待 5-10 分钟**，直到看到以下日志：

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
INFO:     Started parent process [xxxxx]
INFO:     Started server process [xxxxx]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
```

### 激活虚拟环境
每次重启 Cursor 或新开终端时，需先激活虚拟环境：
```powershell
cd "E:\Course Project\Intelligent-Contract-Management-System\ai-service"
.\venv\Scripts\Activate.ps1
```

看到 `(venv)` 前缀即表示激活成功。

### .gitignore 检查
确保 `.gitignore` 包含：
```
ai-service/venv/
ai-service/chroma_db/
```

---

## 🎉 总结

**环境配置成功！**

- Python 3.10 虚拟环境：✅
- 完整 RAG 依赖安装：✅
- ChromaDB + Sentence-Transformers：✅
- 服务启动：✅（模型下载中）

**下一步**：等待 Embedding 模型下载完成，然后进行功能测试。

---

**报告人**：AI 助手  
**审核人**：待填写  

