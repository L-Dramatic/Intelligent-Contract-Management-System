#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
RAG检索服务
功能：从向量数据库中检索相关文档，用于增强AI生成
"""

import chromadb
from chromadb.utils import embedding_functions
from pathlib import Path
from typing import List, Dict, Optional
import chromadb.config


class RAGService:
    """RAG检索服务类"""
    
    def __init__(self, chroma_db_path: str = None, collection_name: str = "telecom_contracts"):
        """
        初始化RAG服务
        
        Args:
            chroma_db_path: ChromaDB数据库路径
            collection_name: 集合名称
        """
        if chroma_db_path is None:
            chroma_db_path = str(Path(__file__).parent.parent.parent / 'chroma_db')
        
        self.chroma_db_path = chroma_db_path
        self.collection_name = collection_name
        self.embedding_model_name = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
        
        # 初始化ChromaDB客户端（兼容0.3.x和0.4.x版本）
        try:
            # 尝试使用新版本API (0.4.x)
            self.client = chromadb.PersistentClient(path=chroma_db_path)
        except (AttributeError, TypeError):
            # 使用旧版本API (0.3.x) - 需要设置持久化路径
            settings = chromadb.config.Settings(
                persist_directory=chroma_db_path,
                chroma_db_impl="duckdb+parquet"
            )
            self.client = chromadb.Client(settings=settings)
        
        # 加载Embedding函数
        self.embedding_function = embedding_functions.SentenceTransformerEmbeddingFunction(
            model_name=self.embedding_model_name
        )
        
        # 获取集合（使用get_or_create_collection确保使用相同的embedding function）
        # 注意：必须使用get_or_create_collection而不是get_collection，
        # 因为chromadb 0.3.23需要embedding function来正确匹配已存在的集合
        self.collection = self.client.get_or_create_collection(
            name=collection_name,
            embedding_function=self.embedding_function
        )
    
    def search(self, query: str, n_results: int = 5, filter_dict: Optional[Dict] = None) -> List[Dict]:
        """
        搜索相关文档
        
        Args:
            query: 查询文本
            n_results: 返回结果数量
            filter_dict: 过滤条件，如 {"doc_type": "contracts"}
        
        Returns:
            相关文档列表，每个文档包含 content, metadata, distance
        """
        if self.collection.count() == 0:
            return []
        
        try:
            # 执行查询
            query_params = {
                "query_texts": [query],
                "n_results": min(n_results, self.collection.count())
            }
            
            if filter_dict:
                query_params["where"] = filter_dict
            
            results = self.collection.query(**query_params)
            
            # 格式化结果
            formatted_results = []
            if results and results['documents'] and results['documents'][0]:
                for i, doc in enumerate(results['documents'][0]):
                    result_item = {
                        "content": doc,
                        "metadata": results['metadatas'][0][i] if results['metadatas'] else {},
                        "distance": results['distances'][0][i] if results['distances'] else 0
                    }
                    formatted_results.append(result_item)
            
            return formatted_results
            
        except Exception as e:
            print(f"[RAG] 搜索出错: {e}")
            return []
    
    def search_by_contract_type(self, query: str, contract_type: str, n_results: int = 5) -> List[Dict]:
        """
        按合同类型搜索
        
        Args:
            query: 查询文本
            contract_type: 合同类型（base_station, equipment_procurement, maintenance, network_construction）
            n_results: 返回结果数量
        
        Returns:
            相关文档列表
        """
        return self.search(query, n_results, filter_dict={"sub_type": contract_type})
    
    def get_context_for_generation(self, query: str, contract_type: str = None, n_results: int = 3) -> str:
        """
        获取用于AI生成的上下文
        
        Args:
            query: 用户查询
            contract_type: 合同类型（可选）
            n_results: 检索结果数量
        
        Returns:
            格式化的上下文字符串
        """
        if contract_type:
            results = self.search_by_contract_type(query, contract_type, n_results)
        else:
            results = self.search(query, n_results)
        
        if not results:
            return ""
        
        context_parts = []
        for i, result in enumerate(results, 1):
            source = result['metadata'].get('source', '未知来源')
            content = result['content']
            context_parts.append(f"【参考文档{i}】来源: {source}\n{content}")
        
        return "\n\n".join(context_parts)
    
    def get_collection_stats(self) -> Dict:
        """获取集合统计信息"""
        return {
            "collection_name": self.collection_name,
            "document_count": self.collection.count(),
            "embedding_model": self.embedding_model_name
        }


# 测试代码
if __name__ == "__main__":
    print("测试RAG服务...")
    
    rag = RAGService()
    stats = rag.get_collection_stats()
    print(f"知识库状态: {stats}")
    
    if stats['document_count'] > 0:
        # 测试搜索
        results = rag.search("基站租赁合同的主要条款", n_results=3)
        print(f"\n搜索结果数量: {len(results)}")
        for i, r in enumerate(results, 1):
            print(f"\n--- 结果 {i} ---")
            print(f"来源: {r['metadata'].get('source', 'N/A')}")
            print(f"内容预览: {r['content'][:100]}...")
    else:
        print("知识库为空，请先运行 process_documents.py 导入文档")





