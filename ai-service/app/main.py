#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
AI合同助手服务 - 主程序
功能：提供WebSocket实时对话、RAG增强生成
"""

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from typing import Dict
from datetime import datetime
import configparser
import os
from pathlib import Path
import json

# 导入LLM服务
from dashscope import Generation

# 导入提示词服务
from app.services.prompt_manager import PromptManager

# 导入RAG服务（可选，可能不可用）
try:
    from app.services.rag_service import RAGService
    RAG_AVAILABLE = True
except ImportError:
    RAG_AVAILABLE = False
    print("[提示] RAG服务模块不可用（缺少chromadb），将使用纯LLM模式")

# ============================================================
# 配置加载
# ============================================================

# 读取配置文件（从父目录）
config = configparser.ConfigParser()
config_path = os.path.join(os.path.dirname(__file__), '..', 'config.ini')
config.read(config_path, encoding='utf-8')

# 获取API Key
try:
    API_KEY = config.get('LLM', 'tongyi_api_key')
except:
    print("[警告] 无法读取config.ini，使用默认API Key")
    API_KEY = "sk-ab17defa37784b0c8f041677dae4dae0"

# ============================================================
# 应用初始化
# ============================================================

app = FastAPI(
    title="AI合同助手服务",
    description="基于RAG的电信合同智能助手",
    version="0.3.0"
)

# 配置CORS（允许前端跨域访问）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 存储活跃的WebSocket连接
active_connections: Dict[str, WebSocket] = {}

# 存储对话历史（简单版本，生产环境应使用数据库）
conversation_history: Dict[str, list] = {}

# ============================================================
# RAG服务初始化
# ============================================================

print("\n" + "=" * 60)
print("AI合同助手服务启动中...")
print("=" * 60)

# 初始化RAG和Prompt服务
print("\n[1/2] 正在初始化服务...")
chroma_db_path = str(Path(__file__).parent.parent / 'chroma_db')

# 初始化Prompt服务（必需）
prompt_manager = PromptManager()

# 初始化RAG服务（可选）
if RAG_AVAILABLE:
    try:
        rag_service = RAGService(chroma_db_path=chroma_db_path)
        stats = rag_service.get_collection_stats()
        print(f"      RAG服务初始化成功")
        print(f"      知识库文档数: {stats['document_count']}")
        RAG_ENABLED = True
    except Exception as e:
        print(f"      [警告] RAG服务初始化失败: {e}")
        print(f"      将降级为普通对话模式")
        rag_service = None
        RAG_ENABLED = False
else:
    print(f"      [信息] RAG功能未安装（需要chromadb和sentence-transformers）")
    print(f"      当前以纯LLM模式运行")
    rag_service = None
    RAG_ENABLED = False

print(f"\n[2/2] 服务配置完成")
print(f"      RAG增强: {'启用' if RAG_ENABLED else '禁用'}")
print("=" * 60 + "\n")

# ============================================================
# AI调用函数
# ============================================================

def call_ai(user_message: str, user_id: str = None, contract_type: str = None) -> str:
    """
    调用AI生成回复（带RAG增强）
    
    Args:
        user_message: 用户消息
        user_id: 用户ID（用于获取对话历史）
        contract_type: 合同类型（用于精准检索）
    
    Returns:
        AI回复
    """
    try:
        # 获取RAG上下文
        context = ""
        if RAG_ENABLED and rag_service:
            context = rag_service.get_context_for_generation(
                query=user_message,
                contract_type=contract_type,
                n_results=3
            )
            if context:
                print(f"[RAG] 检索到相关文档")
        
        # 获取对话历史
        history = conversation_history.get(user_id, []) if user_id else []
        
        # 构建提示词
        system_prompt = prompt_manager.get_system_prompt("default")
        
        # 安全地获取最近的对话历史
        recent_history = history[-6:] if history else []
        
        user_prompt = prompt_manager.build_chat_prompt(
            user_message=user_message,
            context=context,
            conversation_history=recent_history
        )
        
        # 合并系统提示词和用户提示词
        full_prompt = f"{system_prompt}\n\n{user_prompt}"
        
        # 调用通义千问（使用prompt方式）
        response = Generation.call(
            model='qwen-turbo',
            api_key=API_KEY,
            prompt=full_prompt,
            max_tokens=1500,
            temperature=0.7
        )
        
        if response.status_code == 200:
            ai_reply = response.output.text
            
            # 保存对话历史
            if user_id:
                if user_id not in conversation_history:
                    conversation_history[user_id] = []
                conversation_history[user_id].append(user_message)
                conversation_history[user_id].append(ai_reply)
                
                # 限制历史长度
                if len(conversation_history[user_id]) > 20:
                    conversation_history[user_id] = conversation_history[user_id][-20:]
            
            return ai_reply
        else:
            return f"AI服务暂时不可用，请稍后重试。(错误: {response.message})"
            
    except Exception as e:
        print(f"[AI调用错误] {e}")
        return f"抱歉，处理您的请求时出现错误: {str(e)}"

# ============================================================
# API端点
# ============================================================

@app.get("/")
async def root():
    """服务状态检查"""
    return {
        "service": "AI合同助手",
        "version": "0.3.0",
        "status": "running",
        "rag_enabled": RAG_ENABLED,
        "knowledge_base_docs": rag_service.get_collection_stats()['document_count'] if RAG_ENABLED else 0
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "healthy"}


@app.websocket("/ws/chat/{user_id}")
async def websocket_chat(websocket: WebSocket, user_id: str):
    """
    WebSocket聊天端点
    
    消息格式：
    - 发送: {"message": "用户消息", "contract_type": "base_station"(可选)}
    - 接收: {"type": "message", "content": "AI回复", "timestamp": "..."}
    """
    await websocket.accept()
    active_connections[user_id] = websocket
    print(f"[WebSocket] 用户 {user_id} 已连接")
    
    try:
        # 发送欢迎消息
        welcome_msg = {
            "type": "system",
            "content": "您好！我是AI合同助手，专注于电信行业合同服务。请问有什么可以帮您？",
            "timestamp": datetime.now().isoformat()
        }
        await websocket.send_json(welcome_msg)
        
        # 消息循环
        while True:
            # 接收消息
            data = await websocket.receive_text()
            
            try:
                msg_data = json.loads(data)
                user_message = msg_data.get("message", data)
                contract_type = msg_data.get("contract_type")
            except json.JSONDecodeError:
                user_message = data
                contract_type = None
            
            print(f"[WebSocket] 用户 {user_id}: {user_message[:50]}...")
            
            # 发送"正在思考"状态
            await websocket.send_json({
                "type": "status",
                "content": "thinking",
                "timestamp": datetime.now().isoformat()
            })
            
            # 调用AI
            ai_reply = call_ai(user_message, user_id, contract_type)
            
            # 发送回复
            response = {
                "type": "message",
                "content": ai_reply,
                "timestamp": datetime.now().isoformat(),
                "rag_used": RAG_ENABLED
            }
            await websocket.send_json(response)
            print(f"[WebSocket] AI回复 {user_id}: {ai_reply[:50]}...")
            
    except WebSocketDisconnect:
        print(f"[WebSocket] 用户 {user_id} 断开连接")
    except Exception as e:
        print(f"[WebSocket] 错误: {e}")
    finally:
        if user_id in active_connections:
            del active_connections[user_id]


@app.post("/api/generate")
async def generate_clause(request: dict):
    """
    生成合同条款API
    
    请求体：
    {
        "contract_type": "base_station",
        "clause_type": "租赁期限条款",
        "requirement": "需要包含续租和解约条款"
    }
    """
    contract_type = request.get("contract_type", "")
    clause_type = request.get("clause_type", "")
    requirement = request.get("requirement", "")
    
    # 构建提示词
    context = ""
    if RAG_ENABLED and rag_service:
        context = rag_service.get_context_for_generation(
            query=f"{clause_type} {requirement}",
            contract_type=contract_type,
            n_results=3
        )
    
    prompt = prompt_manager.build_prompt(
        "generate_clause",
        context=context,
        contract_type=prompt_manager.get_contract_type_name(contract_type),
        clause_type=clause_type,
        user_requirement=requirement
    )
    
    # 合并系统提示词
    full_prompt = f"{prompt_manager.get_system_prompt('clause_generation')}\n\n{prompt}"
    
    # 调用AI
    response = Generation.call(
        model='qwen-turbo',
        api_key=API_KEY,
        prompt=full_prompt,
        max_tokens=2000,
        temperature=0.7
    )
    
    if response.status_code == 200:
        return {
            "success": True,
            "clause": response.output.text,
            "rag_used": bool(context)
        }
    else:
        return {
            "success": False,
            "error": response.message
        }


@app.post("/api/check")
async def check_compliance(request: dict):
    """
    合规性检查API
    
    请求体：
    {
        "clause_content": "待检查的条款内容",
        "contract_type": "base_station"
    }
    """
    clause_content = request.get("clause_content", "")
    contract_type = request.get("contract_type", "")
    
    # 获取参考上下文
    context = ""
    if RAG_ENABLED and rag_service:
        context = rag_service.get_context_for_generation(
            query=clause_content[:200],
            contract_type=contract_type,
            n_results=3
        )
    
    prompt = prompt_manager.build_prompt(
        "check_compliance",
        context=context,
        clause_content=clause_content,
        contract_type=prompt_manager.get_contract_type_name(contract_type)
    )
    
    # 合并系统提示词
    full_prompt = f"{prompt_manager.get_system_prompt('compliance_check')}\n\n{prompt}"
    
    # 调用AI
    response = Generation.call(
        model='qwen-turbo',
        api_key=API_KEY,
        prompt=full_prompt,
        max_tokens=1500,
        temperature=0.5
    )
    
    if response.status_code == 200:
        return {
            "success": True,
            "analysis": response.output.text,
            "rag_used": bool(context)
        }
    else:
        return {
            "success": False,
            "error": response.message
        }


@app.get("/api/knowledge/stats")
async def get_knowledge_stats():
    """获取知识库统计信息"""
    stats = {
        "document_count": 0,
        "rag_enabled": RAG_ENABLED,
        "files": {
            "contracts": 0,
            "laws": 0,
            "total": 0
        }
    }
    
    # 获取ChromaDB统计
    if RAG_ENABLED and rag_service:
        chroma_stats = rag_service.get_collection_stats()
        stats["document_count"] = chroma_stats.get("document_count", 0)
        stats["collection_name"] = chroma_stats.get("collection_name", "")
        stats["embedding_model"] = chroma_stats.get("embedding_model", "")
    
    # 统计知识库文件
    kb_path = Path(__file__).parent.parent / 'knowledge_base'
    if kb_path.exists():
        contracts_path = kb_path / 'contracts'
        laws_path = kb_path / 'laws_and_regulations'
        
        contract_files = list(contracts_path.rglob('*.md')) if contracts_path.exists() else []
        law_files = list(laws_path.rglob('*.md')) if laws_path.exists() else []
        
        stats["files"]["contracts"] = len(contract_files)
        stats["files"]["laws"] = len(law_files)
        stats["files"]["total"] = len(contract_files) + len(law_files)
    
    return stats


@app.get("/api/knowledge/files")
async def list_knowledge_files():
    """列出知识库中的所有文件"""
    kb_path = Path(__file__).parent.parent / 'knowledge_base'
    files = []
    
    if not kb_path.exists():
        return {"files": [], "error": "知识库目录不存在"}
    
    # 遍历合同模板
    contracts_path = kb_path / 'contracts'
    if contracts_path.exists():
        for md_file in contracts_path.rglob('*.md'):
            rel_path = md_file.relative_to(kb_path)
            # 确定类型
            if 'template_A' in str(rel_path):
                category = 'A类-工程施工'
            elif 'template_B' in str(rel_path):
                category = 'B类-代维服务'
            elif 'template_C' in str(rel_path):
                category = 'C类-IT服务'
            else:
                category = '合同模板'
            
            files.append({
                "name": md_file.name,
                "path": str(rel_path),
                "type": "CONTRACT",
                "category": category,
                "size": md_file.stat().st_size,
                "modified": datetime.fromtimestamp(md_file.stat().st_mtime).isoformat()
            })
    
    # 遍历法规文件
    laws_path = kb_path / 'laws_and_regulations'
    if laws_path.exists():
        for md_file in laws_path.glob('*.md'):
            rel_path = md_file.relative_to(kb_path)
            files.append({
                "name": md_file.name,
                "path": str(rel_path),
                "type": "LAW",
                "category": "法律法规",
                "size": md_file.stat().st_size,
                "modified": datetime.fromtimestamp(md_file.stat().st_mtime).isoformat()
            })
    
    # 按类型和名称排序
    files.sort(key=lambda x: (x["type"], x["name"]))
    
    return {"files": files, "total": len(files)}


@app.get("/api/knowledge/search")
async def search_knowledge(query: str, limit: int = 5):
    """搜索知识库"""
    if not RAG_ENABLED or not rag_service:
        return {"results": [], "error": "RAG服务未启用"}
    
    if not query or len(query.strip()) < 2:
        return {"results": [], "error": "查询词过短"}
    
    results = rag_service.search(query, n_results=limit)
    
    # 格式化结果
    formatted = []
    for r in results:
        formatted.append({
            "content": r["content"][:500] + "..." if len(r["content"]) > 500 else r["content"],
            "source": r["metadata"].get("source", "未知"),
            "type": r["metadata"].get("doc_type", "未知"),
            "relevance": round(1 - r["distance"], 3) if r["distance"] else 0
        })
    
    return {"results": formatted, "query": query, "count": len(formatted)}


@app.post("/api/knowledge/rebuild")
async def rebuild_knowledge_index():
    """重建知识库索引"""
    import subprocess
    import sys
    
    script_path = Path(__file__).parent.parent / 'scripts' / 'process_documents.py'
    
    if not script_path.exists():
        return {"success": False, "error": "索引脚本不存在"}
    
    try:
        # 异步执行索引脚本（不阻塞）
        result = subprocess.run(
            [sys.executable, str(script_path)],
            capture_output=True,
            text=True,
            timeout=300  # 5分钟超时
        )
        
        if result.returncode == 0:
            # 重新加载RAG服务
            global rag_service, RAG_ENABLED
            if RAG_AVAILABLE:
                try:
                    rag_service = RAGService(chroma_db_path=chroma_db_path)
                    RAG_ENABLED = True
                except:
                    pass
            
            return {
                "success": True,
                "message": "索引重建完成",
                "output": result.stdout[-500:] if result.stdout else ""
            }
        else:
            return {
                "success": False,
                "error": result.stderr[-500:] if result.stderr else "未知错误"
            }
    except subprocess.TimeoutExpired:
        return {"success": False, "error": "索引重建超时（超过5分钟）"}
    except Exception as e:
        return {"success": False, "error": str(e)}


# ============================================================
# 启动入口
# ============================================================

if __name__ == "__main__":
    # 统一使用8765端口，与后端配置保持一致
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8765,
        reload=True
    )

