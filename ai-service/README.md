# AI合同助手服务

基于RAG的电信行业智能合同助手服务。

## 目录结构

```
ai-service/
├── app/
│   ├── main.py              # FastAPI主程序
│   ├── services/
│   │   ├── rag_service.py       # RAG检索服务
│   │   ├── knowledge_manager.py # 知识库管理
│   │   └── prompt_manager.py    # 提示词管理
│   └── utils/
│       └── text_processor.py    # 文本处理工具
├── scripts/
│   └── process_documents.py # 文档处理脚本
├── knowledge_base/          # 知识库文档目录
│   ├── contracts/           # 合同模板
│   ├── clauses/             # 条款库
│   └── regulations/         # 法规文件
├── chroma_db/              # 向量数据库
├── config.ini              # 配置文件（含API Key）
├── requirements.txt        # 依赖列表
└── test_*.py/html          # 测试文件
```

## 快速开始

### 1. 安装依赖

```bash
cd ai-service
pip install -r requirements.txt
```

### 2. 配置API Key

编辑 `config.ini`：
```ini
[LLM]
tongyi_api_key=你的API密钥
```

### 3. 导入知识库文档

将合同模板放入 `knowledge_base/contracts/` 目录，然后运行：

```bash
cd scripts
python process_documents.py
```

### 4. 启动服务

**Windows:**
```bash
start_server.bat
```

**或手动:**
```bash
cd app
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### 5. 测试

- 用浏览器打开 `test_chat.html` 进行WebSocket聊天测试
- 访问 `http://localhost:8000` 查看服务状态
- 访问 `http://localhost:8000/docs` 查看API文档

## API接口

### WebSocket聊天
- 端点: `ws://localhost:8000/ws/chat/{user_id}`
- 发送: `{"message": "用户消息", "contract_type": "base_station"}`
- 接收: `{"type": "message", "content": "AI回复", ...}`

### REST API
- `POST /api/generate` - 生成合同条款
- `POST /api/check` - 合规性检查
- `GET /api/knowledge/stats` - 知识库统计

## 合同类型

- `base_station` - 5G基站租赁合同
- `equipment_procurement` - 通信设备采购合同
- `network_construction` - 网络建设工程合同
- `maintenance` - 网络运维服务合同


