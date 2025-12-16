#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
提示词管理系统
功能：管理和构建用于LLM的各类提示词模板
"""

from typing import Dict, Optional


class PromptManager:
    """提示词管理类"""
    
    def __init__(self):
        """初始化提示词模板"""
        self._init_system_prompts()
        self._init_task_templates()
    
    def _init_system_prompts(self):
        """初始化系统提示词"""
        self.system_prompts = {
            "default": """你是一位专业的电信行业合同法务助手，服务于大型电信运营商。

你的核心职责：
1. 协助起草、审核和优化各类电信合同
2. 从运营商（我方/甲方）角度保护合法权益
3. 提供专业、准确、合规的法律建议
4. 识别合同风险点并提出改进建议

你的专业领域：
- 5G基站租赁合同
- 网络建设工程合同
- 通信设备采购合同
- 网络运维服务合同

回答要求：
- 专业严谨，使用规范法律术语
- 立场明确，始终维护运营商利益
- 条款完整，考虑各种风险场景
- 回答简洁，突出重点""",
            
            "clause_generation": """你是电信行业资深法务专家，专门负责起草合同条款。

你的任务是根据用户需求，生成专业、完善、保护运营商权益的合同条款。

条款撰写原则：
1. 权责明确：清晰界定双方权利义务
2. 风险防控：预设违约责任和救济措施
3. 可执行性：条款具体可量化可执行
4. 合规性：符合相关法律法规要求

输出格式要求：
- 使用"第X条 [条款名称]"格式
- 分款分项编号清晰
- 必要时添加定义和解释条款""",
            
            "compliance_check": """你是电信行业合规审查专家，负责审核合同条款的合规性和风险。

审查重点：
1. 法律合规性：是否符合电信行业法规
2. 权益保护：是否充分保护运营商利益
3. 风险识别：潜在的法律和商业风险
4. 条款完整性：是否存在遗漏或漏洞

输出格式：
1. 【合规评估】总体评分和评价
2. 【风险点】列出发现的问题
3. 【改进建议】具体的修改建议""",
            
            "contract_analysis": """你是电信合同分析专家，负责解读和分析合同内容。

分析维度：
1. 合同结构和主要条款
2. 权利义务分配
3. 风险点和注意事项
4. 与行业标准的对比

请用清晰的结构化方式呈现分析结果。"""
        }
    
    def _init_task_templates(self):
        """初始化任务模板"""
        self.task_templates = {
            "generate_clause": """请根据以下信息生成合同条款：

【合同类型】{contract_type}
【条款类型】{clause_type}
【具体需求】{user_requirement}

{context}

请生成专业、完善的合同条款。""",
            
            "check_compliance": """请审核以下合同条款：

【待审核条款】
{clause_content}

【合同类型】{contract_type}

{context}

请从合规性、风险防控、权益保护等角度进行审核，并给出具体建议。""",
            
            "answer_question": """用户问题：{question}

【相关参考资料】
{context}

请基于参考资料和你的专业知识，为用户提供准确、专业的解答。""",
            
            "optimize_clause": """请优化以下合同条款：

【原条款】
{original_clause}

【优化需求】{optimization_requirement}

{context}

请给出优化后的条款，并说明优化理由。""",
            
            "compare_clauses": """请对比分析以下条款：

【条款A】
{clause_a}

【条款B】
{clause_b}

请从完整性、风险防控、权益保护等角度进行对比分析。"""
        }
    
    def get_system_prompt(self, prompt_type: str = "default") -> str:
        """
        获取系统提示词
        
        Args:
            prompt_type: 提示词类型
        
        Returns:
            系统提示词
        """
        return self.system_prompts.get(prompt_type, self.system_prompts["default"])
    
    def build_prompt(self, template_name: str, context: str = "", **kwargs) -> str:
        """
        构建完整提示词
        
        Args:
            template_name: 模板名称
            context: RAG检索到的上下文（可选）
            **kwargs: 模板参数
        
        Returns:
            构建好的提示词
        """
        template = self.task_templates.get(template_name)
        if not template:
            return kwargs.get('user_requirement', kwargs.get('question', ''))
        
        # 处理上下文
        if context:
            context_section = f"\n【参考资料】\n{context}\n"
        else:
            context_section = ""
        
        # 替换模板变量
        try:
            prompt = template.format(context=context_section, **kwargs)
        except KeyError as e:
            print(f"[PromptManager] 模板参数缺失: {e}")
            prompt = template
        
        return prompt
    
    def build_chat_prompt(self, user_message: str, context: str = "", conversation_history: list = None) -> str:
        """
        构建对话提示词
        
        Args:
            user_message: 用户消息
            context: RAG上下文
            conversation_history: 对话历史
        
        Returns:
            完整的对话提示词
        """
        parts = []
        
        # 添加上下文
        if context:
            parts.append(f"【相关参考资料】\n{context}\n")
        
        # 添加历史对话摘要（最近3轮）
        if conversation_history and len(conversation_history) > 0:
            recent_history = conversation_history[-6:]  # 最近3轮（用户+助手各一条）
            history_text = "\n".join([
                f"{'用户' if i % 2 == 0 else '助手'}: {msg}" 
                for i, msg in enumerate(recent_history)
            ])
            parts.append(f"【对话历史】\n{history_text}\n")
        
        # 添加当前问题
        parts.append(f"【当前问题】\n{user_message}")
        
        return "\n".join(parts)
    
    def get_contract_type_name(self, contract_type: str) -> str:
        """获取合同类型中文名称"""
        type_names = {
            "base_station": "5G基站租赁合同",
            "equipment_procurement": "通信设备采购合同",
            "network_construction": "网络建设工程合同",
            "maintenance": "网络运维服务合同"
        }
        return type_names.get(contract_type, contract_type)


# 测试代码
if __name__ == "__main__":
    pm = PromptManager()
    
    print("=== 系统提示词测试 ===")
    print(pm.get_system_prompt("default")[:200] + "...")
    
    print("\n=== 条款生成模板测试 ===")
    prompt = pm.build_prompt(
        "generate_clause",
        context="参考：租赁期限一般为5-10年...",
        contract_type="5G基站租赁合同",
        clause_type="租赁期限条款",
        user_requirement="需要包含续租条件和提前解约条款"
    )
    print(prompt)





