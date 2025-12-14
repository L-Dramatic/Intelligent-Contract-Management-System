#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
RAG检索功能测试脚本
"""

import sys
from pathlib import Path

# 添加项目路径
sys.path.insert(0, str(Path(__file__).parent))

from app.services.rag_service import RAGService
from app.services.knowledge_manager import KnowledgeManager


def test_rag():
    """测试RAG检索功能"""
    print("\n" + "=" * 60)
    print("RAG检索功能测试")
    print("=" * 60)
    
    # 初始化服务
    print("\n[1] 初始化RAG服务...")
    try:
        rag = RAGService()
        km = KnowledgeManager()
        print("    初始化成功")
    except Exception as e:
        print(f"    初始化失败: {e}")
        return False
    
    # 获取统计信息
    print("\n[2] 知识库统计:")
    stats = km.get_stats()
    print(f"    总文档数: {stats['total_documents']}")
    print(f"    类型分布: {stats['type_distribution']}")
    
    sources = km.list_sources()
    print(f"    文档来源: {sources}")
    
    if stats['total_documents'] == 0:
        print("\n[提示] 知识库为空，请先运行以下命令导入文档:")
        print("    cd scripts && python process_documents.py")
        return True
    
    # 测试搜索
    print("\n[3] 测试搜索功能...")
    
    test_queries = [
        "基站租赁合同的租赁期限",
        "设备采购的验收标准",
        "网络运维服务的响应时间"
    ]
    
    for query in test_queries:
        print(f"\n    查询: {query}")
        results = rag.search(query, n_results=2)
        
        if results:
            for i, r in enumerate(results, 1):
                print(f"    结果{i}: {r['metadata'].get('source', 'N/A')}")
                print(f"           {r['content'][:80]}...")
        else:
            print("    无结果")
    
    # 测试上下文生成
    print("\n[4] 测试上下文生成...")
    context = rag.get_context_for_generation(
        query="基站租赁合同需要哪些条款",
        n_results=2
    )
    
    if context:
        print(f"    生成的上下文长度: {len(context)} 字符")
        print(f"    预览: {context[:200]}...")
    else:
        print("    无法生成上下文")
    
    print("\n" + "=" * 60)
    print("测试完成!")
    print("=" * 60)
    return True


if __name__ == "__main__":
    test_rag()

