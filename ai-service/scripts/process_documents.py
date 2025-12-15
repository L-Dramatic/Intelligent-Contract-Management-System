#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
知识库文档处理脚本
功能：将合同模板、条款、法规文档向量化存入Chroma
"""

import os
import sys
from pathlib import Path

# 添加父目录到路径
sys.path.insert(0, str(Path(__file__).parent.parent))

import chromadb
from chromadb.utils import embedding_functions
import re

# 配置
CHROMA_DB_PATH = str(Path(__file__).parent.parent / 'chroma_db')
COLLECTION_NAME = "telecom_contracts"
EMBEDDING_MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
CHUNK_SIZE = 500
CHUNK_OVERLAP = 50


def get_embedding_function():
    """获取Embedding函数"""
    return embedding_functions.SentenceTransformerEmbeddingFunction(
        model_name=EMBEDDING_MODEL_NAME
    )


def extract_text_from_pdf(file_path):
    """从PDF提取文本"""
    try:
        import PyPDF2
        text = ""
        with open(file_path, 'rb') as file:
            reader = PyPDF2.PdfReader(file)
            for page in reader.pages:
                page_text = page.extract_text()
                if page_text:
                    text += page_text
        return text
    except Exception as e:
        print(f"[错误] 无法从PDF文件 {file_path} 提取文本: {e}")
        return ""


def extract_text_from_docx(file_path):
    """从Word提取文本"""
    try:
        from docx import Document
        doc = Document(file_path)
        text = ""
        for paragraph in doc.paragraphs:
            text += paragraph.text + "\n"
        return text
    except Exception as e:
        print(f"[错误] 无法从DOCX文件 {file_path} 提取文本: {e}")
        return ""


def extract_text_from_txt(file_path):
    """从TXT提取文本"""
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            return file.read()
    except UnicodeDecodeError:
        try:
            with open(file_path, 'r', encoding='gbk') as file:
                return file.read()
        except Exception as e:
            print(f"[错误] 无法从TXT文件 {file_path} 提取文本: {e}")
            return ""
    except Exception as e:
        print(f"[错误] 无法从TXT文件 {file_path} 提取文本: {e}")
        return ""


def clean_text(text):
    """清理文本"""
    if not text:
        return ""
    text = re.sub(r'\s+', ' ', text)
    text = text.strip()
    return text


def chunk_text(text, chunk_size=CHUNK_SIZE, overlap=CHUNK_OVERLAP):
    """将文本分块"""
    if not text:
        return []
    
    chunks = []
    start = 0
    text_len = len(text)
    
    while start < text_len:
        end = start + chunk_size
        chunk = text[start:end]
        if chunk.strip():
            chunks.append(chunk)
        start += (chunk_size - overlap)
        if start >= text_len:
            break
    
    return chunks


def process_documents():
    """处理所有文档并存入Chroma"""
    print("\n" + "=" * 60)
    print("知识库文档处理脚本")
    print("=" * 60)
    
    print(f"\n[1] 初始化ChromaDB客户端，路径: {CHROMA_DB_PATH}")
    client = chromadb.PersistentClient(path=CHROMA_DB_PATH)
    
    print(f"[2] 加载Embedding模型: {EMBEDDING_MODEL_NAME}")
    try:
        embedding_function = get_embedding_function()
        print("    Embedding模型加载成功")
    except Exception as e:
        print(f"    [错误] Embedding模型加载失败: {e}")
        print("    请确保网络连接正常，或手动下载模型。")
        sys.exit(1)
    
    print(f"[3] 获取或创建集合: {COLLECTION_NAME}")
    collection = client.get_or_create_collection(
        name=COLLECTION_NAME,
        embedding_function=embedding_function
    )
    
    knowledge_base_dir = Path(__file__).parent.parent / 'knowledge_base'
    print(f"[4] 扫描知识库目录: {knowledge_base_dir}")
    
    documents_to_add = []
    metadatas_to_add = []
    ids_to_add = []
    
    doc_id_counter = collection.count()
    processed_files = 0
    
    for root, _, files in os.walk(knowledge_base_dir):
        for file_name in files:
            file_path = Path(root) / file_name
            file_extension = file_path.suffix.lower()
            
            # 跳过非文档文件
            if file_extension not in ['.pdf', '.docx', '.txt', '.doc']:
                continue
            
            print(f"    处理文件: {file_path.name}")
            
            text = ""
            if file_extension == '.pdf':
                text = extract_text_from_pdf(file_path)
            elif file_extension in ['.docx', '.doc']:
                text = extract_text_from_docx(file_path)
            elif file_extension == '.txt':
                text = extract_text_from_txt(file_path)
            
            if not text:
                print(f"    [警告] 文件 {file_path.name} 提取文本为空，跳过")
                continue
            
            cleaned_text = clean_text(text)
            chunks = chunk_text(cleaned_text)
            
            if not chunks:
                continue
            
            # 提取文档类型作为metadata
            try:
                relative_path = file_path.relative_to(knowledge_base_dir)
                doc_type = relative_path.parts[0] if len(relative_path.parts) > 0 else "unknown"
                sub_type = relative_path.parts[1] if len(relative_path.parts) > 1 else "general"
            except:
                doc_type = "unknown"
                sub_type = "general"
            
            for i, chunk in enumerate(chunks):
                doc_id_counter += 1
                documents_to_add.append(chunk)
                metadatas_to_add.append({
                    "source": str(file_path.name),
                    "doc_type": doc_type,
                    "sub_type": sub_type,
                    "chunk_id": i,
                    "file_name": file_name
                })
                ids_to_add.append(f"doc_{doc_id_counter}")
            
            processed_files += 1
            print(f"    -> 生成 {len(chunks)} 个文档块")
    
    if documents_to_add:
        print(f"\n[5] 添加 {len(documents_to_add)} 个文档块到Chroma...")
        collection.add(
            documents=documents_to_add,
            metadatas=metadatas_to_add,
            ids=ids_to_add
        )
        print(f"    添加成功！当前集合总数: {collection.count()}")
    else:
        print("\n[5] 没有新文档块需要添加")
    
    print("\n" + "=" * 60)
    print(f"处理完成！共处理 {processed_files} 个文件")
    print("=" * 60)


if __name__ == "__main__":
    process_documents()


