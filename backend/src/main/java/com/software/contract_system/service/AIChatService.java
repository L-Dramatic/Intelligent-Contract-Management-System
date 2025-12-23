package com.software.contract_system.service;

import com.software.contract_system.dto.AIChatRequest;
import com.software.contract_system.dto.AIChatResponse;
import com.software.contract_system.entity.AIMessage;
import com.software.contract_system.entity.AISession;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI对话服务接口
 * 支持Ask模式（问答）和Agent模式（执行）
 */
public interface AIChatService {

    /**
     * 创建新会话
     * @param userId 用户ID
     * @param contractId 合同ID（可选）
     * @param subTypeCode 合同子类型
     * @param mode 初始模式 (ASK/AGENT)
     * @return 会话信息
     */
    AISession createSession(Long userId, Long contractId, String subTypeCode, String mode);

    /**
     * 获取会话
     */
    AISession getSession(String sessionId);

    /**
     * 切换模式
     */
    AISession switchMode(String sessionId, String newMode);

    /**
     * Ask模式 - 同步对话
     * @param request 对话请求
     * @return 对话响应
     */
    AIChatResponse ask(AIChatRequest request);

    /**
     * Ask模式 - 流式对话 (SSE)
     * @param request 对话请求
     * @return 流式响应
     */
    Flux<String> askStream(AIChatRequest request);

    /**
     * Agent模式 - 执行命令
     * @param request 命令请求
     * @return 执行结果（包含修改详情）
     */
    AIChatResponse executeAgent(AIChatRequest request);

    /**
     * Agent模式 - 流式执行
     */
    Flux<String> executeAgentStream(AIChatRequest request);

    /**
     * 撤销Agent操作
     * @param undoToken 撤销令牌
     * @return 撤销后的合同内容
     */
    String undoAgentAction(String undoToken);

    /**
     * 获取会话历史消息
     */
    List<AIMessage> getSessionMessages(String sessionId);

    /**
     * 更新合同快照（用于Agent上下文）
     */
    void updateContractSnapshot(String sessionId, String content);
}
