#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
通义千问API测试脚本
功能：验证API连通性和基本功能
"""

from dashscope import Generation
import configparser
import os

# 读取配置
config = configparser.ConfigParser()
config_path = os.path.join(os.path.dirname(__file__), 'config.ini')
config.read(config_path, encoding='utf-8')

try:
    API_KEY = config.get('LLM', 'tongyi_api_key')
except:
    API_KEY = "sk-ab17defa37784b0c8f041677dae4dae0"


def test_tongyi_api():
    """测试通义千问API"""
    print("\n" + "=" * 60)
    print("通义千问API测试")
    print("=" * 60)
    
    # 测试1: 简单问候
    print("\n[测试1] 基础连通性测试...")
    
    response = Generation.call(
        model='qwen-turbo',
        api_key=API_KEY,
        prompt='你好，请用一句话介绍你自己。'
    )
    
    if response.status_code == 200:
        print("[成功] API连接正常")
        print(f"回复: {response.output.text}")
    else:
        print(f"[失败] API连接失败: {response.message}")
        return False
    
    # 测试2: 专业条款生成
    print("\n" + "-" * 60)
    print("[测试2] 专业条款生成测试...")
    
    prompt_for_clause = """
你是电信行业法务专家。请生成一条5G基站租赁合同中的网络覆盖承诺条款，
要包含：覆盖率指标、信号强度要求、未达标的处理方式。
请站在运营商（甲方）的角度，保护运营商的合法权益。
"""
    
    response_clause = Generation.call(
        model='qwen-turbo',
        api_key=API_KEY,
        prompt=prompt_for_clause,
        max_tokens=500
    )
    
    if response_clause.status_code == 200:
        print("[成功] 专业条款生成成功")
        print(f"生成的条款:\n{response_clause.output.text}")
    else:
        print(f"[失败] 专业条款生成失败: {response_clause.message}")
        return False
    
    # Token使用统计
    print("\n" + "-" * 60)
    print("[统计] Token使用情况:")
    if hasattr(response_clause, 'usage'):
        usage = response_clause.usage
        print(f"  输入tokens: {usage.get('input_tokens', 'N/A')}")
        print(f"  输出tokens: {usage.get('output_tokens', 'N/A')}")
    
    print("\n" + "=" * 60)
    print("测试完成!")
    print("=" * 60)
    return True


if __name__ == "__main__":
    test_tongyi_api()

