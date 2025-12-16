#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
文本处理工具函数
"""
import re


def clean_text(text: str) -> str:
    """
    清理文本，移除多余空白和特殊字符
    
    Args:
        text: 原始文本
    
    Returns:
        清理后的文本
    """
    if not text:
        return ""
    # 替换多个空白字符为单个空格
    text = re.sub(r'\s+', ' ', text)
    text = text.strip()
    return text


def chunk_text(text: str, chunk_size: int = 500, overlap: int = 50) -> list:
    """
    将文本分块
    
    Args:
        text: 原始文本
        chunk_size: 每块大小（字符数）
        overlap: 块之间的重叠字符数
    
    Returns:
        文本块列表
    """
    if not text:
        return []
    
    chunks = []
    start = 0
    text_len = len(text)
    
    while start < text_len:
        end = start + chunk_size
        chunk = text[start:end]
        chunks.append(chunk)
        start += (chunk_size - overlap)
        if start >= text_len:
            break
    
    return chunks


def extract_key_terms(text: str) -> list:
    """
    提取文本中的关键术语（电信行业相关）
    
    Args:
        text: 输入文本
    
    Returns:
        关键术语列表
    """
    # 电信行业常见术语
    telecom_terms = [
        '5G', '4G', 'LTE', '基站', '铁塔', '网络覆盖', 'SLA',
        '运维', '维护', '巡检', '故障', '应急通信',
        '电磁辐射', '信号强度', '覆盖率', '可用性',
        '租赁', '采购', '建设', '施工', '验收',
        '违约', '赔偿', '保密', '知识产权'
    ]
    
    found_terms = []
    for term in telecom_terms:
        if term in text:
            found_terms.append(term)
    
    return found_terms





