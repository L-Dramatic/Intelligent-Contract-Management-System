package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.dto.AIChatRequest;
import com.software.contract_system.dto.AIChatResponse;
import com.software.contract_system.dto.AIUndoRequest;
import com.software.contract_system.entity.AIMessage;
import com.software.contract_system.entity.AISession;
import com.software.contract_system.service.AIChatService;
import com.software.contract_system.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI对话控制器
 * 支持Ask模式（问答）和Agent模式（执行修改）
 */
@Slf4j
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI对话", description = "AI辅助起草 - Ask和Agent模式")
public class AIChatController {

    private final AIChatService chatService;
    private final SecurityUtils securityUtils;

    // ==================== 会话管理 ====================

    /**
     * 创建会话
     * 进入起草页面时调用，初始化AI对话上下文
     */
    @PostMapping("/session/create")
    @Operation(summary = "创建会话", description = "创建新的AI对话会话")
    public Result<AISession> createSession(
            @RequestParam(required = false) Long contractId,
            @RequestParam String subTypeCode,
            @RequestParam(defaultValue = "ASK") String mode) {
        Long userId = securityUtils.getCurrentUserId();
        AISession session = chatService.createSession(userId, contractId, subTypeCode, mode);
        return Result.success(session);
    }

    /**
     * 获取会话信息
     */
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "获取会话")
    public Result<AISession> getSession(@PathVariable String sessionId) {
        AISession session = chatService.getSession(sessionId);
        if (session == null) {
            return Result.error("会话不存在或已过期");
        }
        return Result.success(session);
    }

    /**
     * 切换模式
     */
    @PostMapping("/session/{sessionId}/mode")
    @Operation(summary = "切换模式", description = "在Ask和Agent模式之间切换")
    public Result<AISession> switchMode(
            @PathVariable String sessionId,
            @RequestParam String mode) {
        AISession session = chatService.switchMode(sessionId, mode);
        return Result.success(session);
    }

    /**
     * 获取会话历史消息
     */
    @GetMapping("/session/{sessionId}/messages")
    @Operation(summary = "获取对话历史")
    public Result<List<AIMessage>> getSessionMessages(@PathVariable String sessionId) {
        List<AIMessage> messages = chatService.getSessionMessages(sessionId);
        return Result.success(messages);
    }

    /**
     * 更新合同快照
     * 前端内容变化时调用，保持AI上下文同步
     */
    @PostMapping("/session/{sessionId}/snapshot")
    @Operation(summary = "更新合同快照", description = "更新AI上下文中的合同内容")
    public Result<Boolean> updateSnapshot(
            @PathVariable String sessionId,
            @RequestBody String content) {
        chatService.updateContractSnapshot(sessionId, content);
        return Result.success(true);
    }

    // ==================== Ask模式 ====================

    /**
     * Ask模式 - 同步对话
     */
    @PostMapping("/ask")
    @Operation(summary = "Ask模式对话", description = "问答模式，不修改合同内容")
    public Result<AIChatResponse> ask(@RequestBody AIChatRequest request) {
        request.setMode(AISession.MODE_ASK);
        AIChatResponse response = chatService.ask(request);
        return Result.success(response);
    }

    /**
     * Ask模式 - 流式对话 (SSE)
     */
    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Ask模式流式对话", description = "流式输出，打字机效果")
    public Flux<String> askStream(@RequestBody AIChatRequest request) {
        request.setMode(AISession.MODE_ASK);
        return chatService.askStream(request)
                .map(chunk -> "data: " + chunk + "\n\n");
    }

    // ==================== Agent模式 ====================

    /**
     * Agent模式 - 执行命令
     */
    @PostMapping("/agent")
    @Operation(summary = "Agent模式执行", description = "执行修改命令，会修改合同内容")
    public Result<AIChatResponse> executeAgent(@RequestBody AIChatRequest request) {
        request.setMode(AISession.MODE_AGENT);
        AIChatResponse response = chatService.executeAgent(request);
        return Result.success(response);
    }

    /**
     * Agent模式 - 流式执行 (SSE)
     */
    @PostMapping(value = "/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Agent模式流式执行", description = "流式输出执行过程")
    public Flux<String> executeAgentStream(@RequestBody AIChatRequest request) {
        request.setMode(AISession.MODE_AGENT);
        return chatService.executeAgentStream(request)
                .map(chunk -> "data: " + chunk + "\n\n");
    }

    /**
     * 撤销Agent操作
     */
    @PostMapping("/agent/undo")
    @Operation(summary = "撤销操作", description = "撤销Agent的最近一次修改")
    public Result<String> undoAgent(@RequestBody AIUndoRequest request) {
        String restoredContent = chatService.undoAgentAction(request.getUndoToken());
        return Result.success(restoredContent);
    }
}
