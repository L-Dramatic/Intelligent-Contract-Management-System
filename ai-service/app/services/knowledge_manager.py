#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
知识库管理服务
功能：管理知识库的增删改查操作
"""

import chromadb
from chromadb.utils import embedding_functions
from pathlib import Path
from typing import List, Dict, Optional
import os


class KnowledgeManager:
    """知识库管理类"""
    
    def __init__(self, chroma_db_path: str = None, collection_name: str = "telecom_contracts"):
        """
        初始化知识库管理器
        
        Args:
            chroma_db_path: ChromaDB数据库路径
            collection_name: 集合名称
        """
        if chroma_db_path is None:
            chroma_db_path = str(Path(__file__).parent.parent.parent / 'chroma_db')
        
        self.chroma_db_path = chroma_db_path
        self.collection_name = collection_name
        self.embedding_model_name = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
        
        # 初始化ChromaDB客户端
        self.client = chromadb.PersistentClient(path=chroma_db_path)
        
        # 加载Embedding函数
        self.embedding_function = embedding_functions.SentenceTransformerEmbeddingFunction(
            model_name=self.embedding_model_name
        )
        
        # 获取集合
        self.collection = self.client.get_or_create_collection(
            name=collection_name,
            embedding_function=self.embedding_function
        )
    
    def add_document(self, content: str, metadata: Dict, doc_id: str = None) -> str:
        """
        添加单个文档
        
        Args:
            content: 文档内容
            metadata: 元数据（如source, doc_type, sub_type等）
            doc_id: 文档ID（可选，自动生成）
        
        Returns:
            文档ID
        """
        if doc_id is None:
            doc_id = f"doc_{self.collection.count() + 1}"
        
        self.collection.add(
            documents=[content],
            metadatas=[metadata],
            ids=[doc_id]
        )
        
        return doc_id
    
    def add_documents(self, documents: List[str], metadatas: List[Dict], ids: List[str] = None) -> List[str]:
        """
        批量添加文档
        
        Args:
            documents: 文档内容列表
            metadatas: 元数据列表
            ids: 文档ID列表（可选）
        
        Returns:
            文档ID列表
        """
        if ids is None:
            start_id = self.collection.count() + 1
            ids = [f"doc_{start_id + i}" for i in range(len(documents))]
        
        self.collection.add(
            documents=documents,
            metadatas=metadatas,
            ids=ids
        )
        
        return ids
    
    def delete_document(self, doc_id: str) -> bool:
        """
        删除文档
        
        Args:
            doc_id: 文档ID
        
        Returns:
            是否成功
        """
        try:
            self.collection.delete(ids=[doc_id])
            return True
        except Exception as e:
            print(f"[KnowledgeManager] 删除文档失败: {e}")
            return False
    
    def delete_by_source(self, source_name: str) -> int:
        """
        按来源删除文档
        
        Args:
            source_name: 来源文件名
        
        Returns:
            删除的文档数量
        """
        try:
            # 查找所有匹配的文档
            results = self.collection.get(
                where={"source": source_name}
            )
            
            if results and results['ids']:
                self.collection.delete(ids=results['ids'])
                return len(results['ids'])
            return 0
        except Exception as e:
            print(f"[KnowledgeManager] 按来源删除失败: {e}")
            return 0
    
    def get_stats(self) -> Dict:
        """
        获取知识库统计信息
        
        Returns:
            统计信息字典
        """
        total_count = self.collection.count()
        
        # 获取文档类型分布
        type_distribution = {}
        try:
            all_docs = self.collection.get()
            if all_docs and all_docs['metadatas']:
                for meta in all_docs['metadatas']:
                    doc_type = meta.get('doc_type', 'unknown')
                    sub_type = meta.get('sub_type', 'general')
                    key = f"{doc_type}/{sub_type}"
                    type_distribution[key] = type_distribution.get(key, 0) + 1
        except:
            pass
        
        return {
            "total_documents": total_count,
            "collection_name": self.collection_name,
            "type_distribution": type_distribution,
            "embedding_model": self.embedding_model_name
        }
    
    def list_sources(self) -> List[str]:
        """
        列出所有文档来源
        
        Returns:
            来源文件名列表
        """
        sources = set()
        try:
            all_docs = self.collection.get()
            if all_docs and all_docs['metadatas']:
                for meta in all_docs['metadatas']:
                    source = meta.get('source')
                    if source:
                        sources.add(source)
        except:
            pass
        
        return list(sources)
    
    def clear_all(self) -> bool:
        """
        清空知识库
        
        Returns:
            是否成功
        """
        try:
            # 删除并重建集合
            self.client.delete_collection(self.collection_name)
            self.collection = self.client.get_or_create_collection(
                name=self.collection_name,
                embedding_function=self.embedding_function
            )
            return True
        except Exception as e:
            print(f"[KnowledgeManager] 清空知识库失败: {e}")
            return False


# 测试代码
if __name__ == "__main__":
    print("测试知识库管理服务...")
    
    km = KnowledgeManager()
    stats = km.get_stats()
    
    print(f"\n知识库统计:")
    print(f"  总文档数: {stats['total_documents']}")
    print(f"  集合名称: {stats['collection_name']}")
    print(f"  类型分布: {stats['type_distribution']}")
    
    sources = km.list_sources()
    print(f"\n文档来源 ({len(sources)}个):")
    for s in sources:
        print(f"  - {s}")


