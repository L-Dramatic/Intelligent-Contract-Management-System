# AI服务开发指南

## 📍 当前进度

### ✅ 已完成

| 阶段 | 任务 | 状态 |
|------|------|------|
| 基础架构 | 创建ai-service目录结构 | ✅ 完成 |
| 基础架构 | 配置requirements.txt依赖 | ✅ 完成 |
| 基础架构 | 配置API Key (config.ini) | ✅ 完成 |
| 核心服务 | RAG检索服务 (rag_service.py) | ✅ 完成 |
| 核心服务 | 知识库管理 (knowledge_manager.py) | ✅ 完成 |
| 核心服务 | 提示词系统 (prompt_manager.py) | ✅ 完成 |
| 核心服务 | 主服务集成RAG (main.py) | ✅ 完成 |
| 测试文件 | API测试、RAG测试、聊天测试页面 | ✅ 完成 |
| 文档 | SRS v5.0 | ✅ 完成 |
| 版本控制 | 代码已push到GitHub | ✅ 完成 |

### 🔄 待完成

| 阶段 | 任务 | 优先级 |
|------|------|--------|
| 环境配置 | 安装Python依赖 | P0 |
| 知识库 | 收集合同模板文档 | P0 |
| 知识库 | 导入文档到向量数据库 | P0 |
| 测试验证 | 启动服务并测试 | P1 |
| 前端集成 | 实现侧边栏AI对话组件 | P2 |
| 后端集成 | 添加AI服务代理接口 | P2 |

---

## 🚀 下一步操作指南

### 第1步：安装依赖

```bash
cd ai-service
pip install -r requirements.txt
```

> ⚠️ 首次安装 sentence-transformers 会下载约500MB的模型文件

### 第2步：收集合同模板

将找到的合同模板放入对应目录：

```
ai-service/knowledge_base/
├── contracts/
│   ├── base_station/      # 5G基站租赁合同
│   ├── equipment_procurement/  # 设备采购合同
│   ├── network_construction/   # 网络建设合同
│   └── maintenance/       # 运维服务合同
├── clauses/               # 标准条款
└── regulations/           # 法规文件
```

**支持格式**: PDF、Word(.docx)、TXT

### 第3步：导入知识库

```bash
cd ai-service/scripts
python process_documents.py
```

### 第4步：启动服务测试

```bash
cd ai-service
start_server.bat
```

或手动：
```bash
cd ai-service/app
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### 第5步：验证功能

1. 打开浏览器访问 `http://localhost:8000` 查看服务状态
2. 打开 `ai-service/test_chat.html` 测试WebSocket聊天
3. 访问 `http://localhost:8000/docs` 查看API文档

---

## 📁 项目结构

```
ai-service/
├── app/
│   ├── main.py              # FastAPI主程序（WebSocket + REST API）
│   └── services/
│       ├── rag_service.py       # RAG检索服务
│       ├── knowledge_manager.py # 知识库管理
│       └── prompt_manager.py    # 提示词管理
├── scripts/
│   └── process_documents.py # 文档向量化脚本
├── knowledge_base/          # 放置合同模板的目录
├── chroma_db/              # 向量数据库（自动生成）
├── config.ini              # API Key配置
├── requirements.txt        # Python依赖
├── start_server.bat        # Windows启动脚本
└── test_*.py/html          # 测试文件
```

---

## 🔗 API接口

### WebSocket 实时对话
- 端点: `ws://localhost:8000/ws/chat/{user_id}`
- 用于前端侧边栏AI对话

### REST API
| 接口 | 方法 | 功能 |
|------|------|------|
| `/api/generate` | POST | 生成合同条款 |
| `/api/check` | POST | 合规性检查 |
| `/api/knowledge/stats` | GET | 知识库统计 |

---

## 🎯 质量标准

1. **RAG检索质量**: 相关文档召回率 > 80%
2. **生成质量**: 条款专业性、完整性、运营商立场
3. **响应速度**: AI生成 < 10秒
4. **稳定性**: 服务可用性 > 99%

