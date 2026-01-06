package com.software.contract_system.service.impl;

import com.software.contract_system.common.BusinessException;
import com.software.contract_system.dto.AIChatRequest;
import com.software.contract_system.dto.AIChatResponse;
import com.software.contract_system.entity.*;
import com.software.contract_system.mapper.*;
import com.software.contract_system.service.AIChatService;
import com.software.contract_system.service.ContractTypeService;
import com.software.contract_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI对话服务实现
 * 核心功能：Ask模式（问答）和Agent模式（执行修改）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private final AISessionMapper sessionMapper;
    private final AIMessageMapper messageMapper;
    private final ContractEditHistoryMapper editHistoryMapper;
    private final ContractMapper contractMapper;
    private final ContractTypeService typeService;
    private final SecurityUtils securityUtils;
    private final WebClient aiWebClient;

    @Override
    @Transactional
    public AISession createSession(Long userId, Long contractId, String subTypeCode, String mode) {
        AISession session = new AISession();
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(userId);
        session.setContractId(contractId);
        session.setSubTypeCode(subTypeCode);
        session.setMode(mode != null ? mode : AISession.MODE_ASK);
        session.setMessageCount(0);
        session.setLastActiveAt(LocalDateTime.now());
        session.setExpiredAt(LocalDateTime.now().plusHours(24)); // 24小时过期
        session.setCreatedAt(LocalDateTime.now());

        // 获取合同类型信息作为上下文
        if (StringUtils.hasText(subTypeCode)) {
            ContractType contractType = typeService.getBySubTypeCode(subTypeCode);
            if (contractType != null) {
                Map<String, Object> contextData = new HashMap<>();
                contextData.put("typeName", contractType.getTypeName());
                contextData.put("subTypeName", contractType.getSubTypeName());
                contextData.put("description", contractType.getDescription());
                session.setContextData(contextData);
            }
        }

        sessionMapper.insert(session);
        log.info("创建AI会话: sessionId={}, userId={}, subTypeCode={}", 
                session.getSessionId(), userId, subTypeCode);
        return session;
    }

    @Override
    public AISession getSession(String sessionId) {
        return sessionMapper.getBySessionId(sessionId);
    }

    @Override
    @Transactional
    public AISession switchMode(String sessionId, String newMode) {
        AISession session = getSession(sessionId);
        if (session == null) {
            throw BusinessException.notFound("会话不存在");
        }
        session.setMode(newMode);
        sessionMapper.updateById(session);
        log.info("切换会话模式: sessionId={}, newMode={}", sessionId, newMode);
        return session;
    }

    @Override
    @Transactional
    public AIChatResponse ask(AIChatRequest request) {
        // 1. 获取或创建会话
        AISession session = getOrCreateSession(request);

        // 2. 保存用户消息
        saveMessage(session.getSessionId(), AIMessage.ROLE_USER, request.getMessage(), AISession.MODE_ASK, null);

        // 3. 构建提示词
        String prompt = buildAskPrompt(session, request);

        // 4. 调用AI服务
        String aiResponse = callAIService(prompt);

        // 5. 保存AI回复
        saveMessage(session.getSessionId(), AIMessage.ROLE_ASSISTANT, aiResponse, AISession.MODE_ASK, null);

        // 6. 更新会话
        sessionMapper.updateLastActive(session.getSessionId());

        // 7. 构建响应
        return AIChatResponse.builder()
                .sessionId(session.getSessionId())
                .mode(AISession.MODE_ASK)
                .content(aiResponse)
                .success(true)
                .suggestions(extractSuggestions(aiResponse))
                .build();
    }

    @Override
    public Flux<String> askStream(AIChatRequest request) {
        // 1. 获取或创建会话
        AISession session = getOrCreateSession(request);

        // 2. 保存用户消息
        saveMessage(session.getSessionId(), AIMessage.ROLE_USER, request.getMessage(), AISession.MODE_ASK, null);

        // 3. 构建提示词
        String prompt = buildAskPrompt(session, request);

        // 4. 调用AI服务流式接口
        return callAIServiceStream(prompt)
                .doOnComplete(() -> {
                    sessionMapper.updateLastActive(session.getSessionId());
                    log.info("Ask流式响应完成: sessionId={}", session.getSessionId());
                });
    }

    @Override
    @Transactional
    public AIChatResponse executeAgent(AIChatRequest request) {
        // 1. 获取或创建会话
        AISession session = getOrCreateSession(request);

        // 2. 确保是Agent模式
        if (!AISession.MODE_AGENT.equals(session.getMode())) {
            session = switchMode(session.getSessionId(), AISession.MODE_AGENT);
        }

        // 3. 保存用户命令
        saveMessage(session.getSessionId(), AIMessage.ROLE_USER, request.getMessage(), AISession.MODE_AGENT, null);

        // 4. 解析并执行命令
        AIChatResponse.AgentAction action = parseAndExecuteCommand(session, request);

        // 5. 构建AI回复
        String aiResponse = buildAgentResponse(action);
        
        // 6. 保存AI回复
        Map<String, Object> actionMap = new HashMap<>();
        actionMap.put("actionType", action.getActionType());
        actionMap.put("fieldPath", action.getFieldPath());
        actionMap.put("locationDesc", action.getLocationDesc());
        saveMessage(session.getSessionId(), AIMessage.ROLE_ASSISTANT, aiResponse, AISession.MODE_AGENT, actionMap);

        // 7. 更新会话
        sessionMapper.updateLastActive(session.getSessionId());

        return AIChatResponse.builder()
                .sessionId(session.getSessionId())
                .mode(AISession.MODE_AGENT)
                .content(aiResponse)
                .success(true)
                .actions(Collections.singletonList(action))
                .build();
    }

    @Override
    public Flux<String> executeAgentStream(AIChatRequest request) {
        // Agent模式的流式执行
        AISession session = getOrCreateSession(request);
        
        // 构建Agent提示词
        String prompt = buildAgentPrompt(session, request);
        
        return callAIServiceStream(prompt)
                .doOnComplete(() -> {
                    sessionMapper.updateLastActive(session.getSessionId());
                });
    }

    @Override
    @Transactional
    public String undoAgentAction(String undoToken) {
        ContractEditHistory history = editHistoryMapper.getByUndoToken(undoToken);
        if (history == null) {
            throw BusinessException.notFound("撤销记录不存在或已撤销");
        }

        String restoredContent = history.getFullContentBefore();
        
        // 如果contractId不为null，更新数据库中的合同表
        // 如果contractId为null（新建合同场景），只返回内容，不更新数据库
        if (history.getContractId() != null) {
        Contract contract = contractMapper.selectById(history.getContractId());
        if (contract == null) {
            throw BusinessException.notFound("合同不存在");
        }
        contract.setContent(restoredContent);
        contractMapper.updateById(contract);
            log.info("撤销Agent操作: contractId={}, undoToken={}", history.getContractId(), undoToken);
        } else {
            // 新建合同场景：contractId为null，只返回内容，不更新数据库
            log.info("撤销Agent操作（新建合同）: sessionId={}, undoToken={}", history.getSessionId(), undoToken);
        }

        // 标记为已撤销
        editHistoryMapper.markAsUndone(history.getId());

        return restoredContent;
    }

    @Override
    public List<AIMessage> getSessionMessages(String sessionId) {
        return messageMapper.getBySessionId(sessionId);
    }

    @Override
    public void updateContractSnapshot(String sessionId, String content) {
        sessionMapper.updateContractSnapshot(sessionId, content);
    }

    // ==================== 私有方法 ====================

    private AISession getOrCreateSession(AIChatRequest request) {
        if (StringUtils.hasText(request.getSessionId())) {
            AISession session = getSession(request.getSessionId());
            if (session != null) {
                return session;
            }
        }
        // 创建新会话
        Long userId = securityUtils.getCurrentUserId();
        return createSession(userId, request.getContractId(), request.getSubTypeCode(), request.getMode());
    }

    private void saveMessage(String sessionId, String role, String content, String mode, Map<String, Object> agentAction) {
        AIMessage message = new AIMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setMode(mode);
        message.setAgentAction(agentAction);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
    }

    private String buildAskPrompt(AISession session, AIChatRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        // 系统角色设定
        prompt.append("你是一个专业的合同起草助手，专门帮助中国移动的员工起草和理解电信行业合同。\n\n");
        
        // 上下文信息
        if (session.getContextData() != null) {
            prompt.append("当前合同类型: ").append(session.getContextData().get("subTypeName")).append("\n");
            prompt.append("类型说明: ").append(session.getContextData().get("description")).append("\n\n");
        }
        
        // 合同内容上下文
        if (StringUtils.hasText(request.getCurrentContent())) {
            prompt.append("当前合同内容:\n```\n").append(request.getCurrentContent()).append("\n```\n\n");
        } else if (StringUtils.hasText(session.getContractSnapshot())) {
            prompt.append("当前合同内容:\n```\n").append(session.getContractSnapshot()).append("\n```\n\n");
        }
        
        // 获取最近的对话历史
        List<AIMessage> recentMessages = messageMapper.getRecentMessages(session.getSessionId(), 6);
        if (!recentMessages.isEmpty()) {
            prompt.append("最近对话历史:\n");
            // 倒序显示（从旧到新）
            for (int i = recentMessages.size() - 1; i >= 0; i--) {
                AIMessage msg = recentMessages.get(i);
                prompt.append(msg.getRole().equals(AIMessage.ROLE_USER) ? "用户: " : "助手: ");
                prompt.append(msg.getContent()).append("\n");
            }
            prompt.append("\n");
        }
        
        // 用户当前问题
        prompt.append("用户问题: ").append(request.getMessage()).append("\n\n");
        prompt.append("请用专业、清晰的语言回答用户的问题。如果涉及法律条款，请确保准确性。");
        
        return prompt.toString();
    }

    private String buildAgentPrompt(AISession session, AIChatRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个合同编辑Agent，需要根据用户命令精确修改合同内容。\n\n");
        prompt.append("当前合同类型: ").append(session.getSubTypeCode()).append("\n\n");
        
        if (StringUtils.hasText(request.getCurrentContent())) {
            prompt.append("当前合同内容:\n```\n").append(request.getCurrentContent()).append("\n```\n\n");
        }
        
        prompt.append("用户命令: ").append(request.getMessage()).append("\n\n");
        prompt.append("请严格按照以下规则执行：\n");
        prompt.append("1. 如果用户要求修改现有内容（如\"改成XXX\"、\"把XX写成YY\"），请只输出修改后的完整合同内容（整段或整份），不要只输出修改的部分。\n");
        prompt.append("2. 如果用户要求添加新内容，请只输出要添加的新内容片段。\n");
        prompt.append("3. 输出内容中请使用真实的换行符（\\n），不要输出字面的\\n字符串。\n");
        prompt.append("4. 保持合同原有格式和结构，只修改用户指定的部分。\n");
        prompt.append("\n现在请执行用户的命令：");
        
        return prompt.toString();
    }

    private AIChatResponse.AgentAction parseAndExecuteCommand(AISession session, AIChatRequest request) {
        String command = request.getMessage().toLowerCase();
        String currentContent = request.getCurrentContent();
        
        // 判断操作类型
        String actionType;
        String newValue = "";
        String oldValue = "";
        String locationDesc = "";
        String fieldPath = "content";
        
        if (command.contains("生成") || command.contains("添加") || command.contains("增加")) {
            actionType = ContractEditHistory.ACTION_INSERT;
            // 调用AI生成内容
            newValue = callAIService(buildAgentPrompt(session, request));
            // 处理换行符转义
            newValue = processNewlines(newValue);
            locationDesc = "合同末尾";
        } else if (command.contains("删除") || command.contains("移除") || command.contains("去掉")) {
            actionType = ContractEditHistory.ACTION_DELETE;
            oldValue = extractOldValue(command, currentContent);
            locationDesc = "删除指定内容";
        } else {
            // 修改操作：让AI返回完整修改后的内容
            actionType = ContractEditHistory.ACTION_REPLACE;
            String aiResponse = callAIService(buildAgentPrompt(session, request));
            // 处理换行符转义
            newValue = processNewlines(aiResponse);
            // 对于修改操作，oldValue是整个当前内容，newValue是AI返回的完整新内容
            oldValue = currentContent;
            locationDesc = "完整替换";
        }
        
        // 保存编辑历史（用于撤销）
        // 允许contractId为null（新建合同场景），支持数据持久化
        String undoToken = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            saveEditHistory(request.getContractId(), session.getSessionId(), actionType, 
                    fieldPath, locationDesc, oldValue, newValue, currentContent, undoToken);
        
        return AIChatResponse.AgentAction.builder()
                .actionType(actionType)
                .fieldPath(fieldPath)
                .locationDesc(locationDesc)
                .oldValue(oldValue)
                .newValue(newValue)
                .undoToken(undoToken)
                .canUndo(true)
                .build();
    }

    private void saveEditHistory(Long contractId, String sessionId, String action, 
            String fieldPath, String locationDesc, String oldValue, String newValue, 
            String fullContentBefore, String undoToken) {
        ContractEditHistory history = new ContractEditHistory();
        history.setContractId(contractId);
        history.setSessionId(sessionId);
        history.setEditType(ContractEditHistory.EDIT_TYPE_AI_AGENT);
        history.setAction(action);
        history.setFieldPath(fieldPath);
        history.setLocationDesc(locationDesc);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setFullContentBefore(fullContentBefore);
        history.setUndoToken(undoToken);
        history.setIsUndone(0);
        history.setOperatorId(securityUtils.getCurrentUserId());
        history.setCreatedAt(LocalDateTime.now());
        editHistoryMapper.insert(history);
    }

    private String buildAgentResponse(AIChatResponse.AgentAction action) {
        StringBuilder response = new StringBuilder();
        response.append("✅ 操作已执行\n\n");
        response.append("**操作类型**: ").append(getActionTypeName(action.getActionType())).append("\n");
        if (StringUtils.hasText(action.getLocationDesc())) {
            response.append("**位置**: ").append(action.getLocationDesc()).append("\n");
        }
        if (StringUtils.hasText(action.getOldValue()) && action.getOldValue().length() < 100) {
            response.append("**原内容**: ").append(action.getOldValue()).append("\n");
        }
        if (StringUtils.hasText(action.getNewValue()) && action.getNewValue().length() < 500) {
            response.append("**新内容**: \n").append(action.getNewValue()).append("\n");
        }
        response.append("\n[撤销此操作]");
        return response.toString();
    }

    private String getActionTypeName(String actionType) {
        switch (actionType) {
            case ContractEditHistory.ACTION_MODIFY: return "修改";
            case ContractEditHistory.ACTION_INSERT: return "插入";
            case ContractEditHistory.ACTION_DELETE: return "删除";
            case ContractEditHistory.ACTION_REPLACE: return "替换";
            default: return actionType;
        }
    }

    private String extractOldValue(String command, String content) {
        // 简化实现：实际应该用AI或NLP来提取
        return "";
    }
    
    /**
     * 处理AI返回内容中的换行符转义问题
     * 将字面的\n字符串转换为真实的换行符
     */
    private String processNewlines(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // 将字面的\n字符串（转义后）转换为真实换行符
        // 但要注意，Java字符串中\\n表示一个字面的\n字符
        // AI可能返回的是字符串"\\n"，需要特殊处理
        return text.replace("\\n", "\n").replace("\\r\\n", "\r\n").replace("\\r", "\r");
    }

    private List<String> extractSuggestions(String aiResponse) {
        // 从AI回复中提取建议（简化实现）
        List<String> suggestions = new ArrayList<>();
        if (aiResponse.contains("建议")) {
            suggestions.add("查看相关法规");
        }
        if (aiResponse.contains("条款")) {
            suggestions.add("生成类似条款");
        }
        return suggestions;
    }

    private String callAIService(String prompt) {
        try {
            // 调用AI服务
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);
            requestBody.put("max_tokens", 2000);
            
            String response = aiWebClient.post()
                    .uri("/api/chat")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            return response != null ? response : getMockAIResponse(prompt);
        } catch (Exception e) {
            log.error("调用AI服务失败，使用模拟响应", e);
            // AI服务不可用时返回模拟响应，便于功能演示
            return getMockAIResponse(prompt);
        }
    }
    
    /**
     * 生成模拟AI响应（用于AI服务不可用时）
     */
    private String getMockAIResponse(String prompt) {
        if (prompt.contains("AGENT") || prompt.contains("修改") || prompt.contains("生成")) {
            return "好的，我已理解您的需求。\n\n" +
                   "**建议操作**：\n" +
                   "1. 根据您的要求，建议在合同相关章节进行修改\n" +
                   "2. 请确保修改内容符合《中华人民共和国民法典》相关规定\n" +
                   "3. 建议与法务部门确认后再行定稿\n\n" +
                   "💡 *提示：AI服务当前处于演示模式，如需完整AI功能，请启动ai-service服务。*";
        } else {
            return "感谢您的提问！\n\n" +
                   "根据合同管理的一般原则，我为您提供以下建议：\n\n" +
                   "**合同起草要点**：\n" +
                   "• 明确双方权利义务\n" +
                   "• 约定清晰的付款条款\n" +
                   "• 设置合理的违约责任\n" +
                   "• 注意保密条款的设置\n\n" +
                   "**相关法规参考**：\n" +
                   "• 《中华人民共和国民法典》合同编\n" +
                   "• 《中华人民共和国招标投标法》\n\n" +
                   "💡 *提示：AI服务当前处于演示模式，如需完整AI功能，请启动ai-service服务。*";
        }
    }

    private Flux<String> callAIServiceStream(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("stream", true);
        
        return aiWebClient.post()
                .uri("/api/chat/stream")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(e -> {
                    log.error("AI流式服务调用失败", e);
                    return Flux.just("AI服务暂时不可用");
                });
    }
}
