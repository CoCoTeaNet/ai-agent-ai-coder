package com.example.agent.services;

import com.example.agent.BaseAgent;
import com.example.agent.config.AgentConfig;
import com.example.agent.services.ExecutionTrace;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 服务管理类
 * 管理多个会话的 Agent 实例
 */
@Service
public class AgentService {

    private final Map<String, BaseAgent> agentSessions;
    private final Map<String, java.util.List<Map<String, Object>>> sessionAttachments;
    private final AgentConfig defaultConfig;
    private String persistentSessionId;

    public AgentService() {
        this.agentSessions = new ConcurrentHashMap<>();
        this.sessionAttachments = new ConcurrentHashMap<>();
        this.defaultConfig = AgentConfig.builder()
                .provider(AgentConfig.LlmProvider.OPENAI_COMPATIBLE)
                .modelName("doubao-seed-code-preview-251028")
                .baseUrl("https://ark.cn-beijing.volces.com/api/coding/v1")
                .apiKeyFromEnv()
                .temperature(0.7)
                .maxTokens(2048)
                .topP(0.9)
                .systemPrompt("你是一个聪明的助手，使用可用的工具来回答问题和执行任务。")
                .enableTools(true)
                .maxIterations(5)
                .enableChainOfThought(true)
                .enableReactMode(true)
                .enableSelfReflection(true)
                .maxConversationSummaryLength(1000)
                .build();
        // 创建持久化会话
        this.persistentSessionId = UUID.randomUUID().toString();
        agentSessions.put(persistentSessionId, new BaseAgent(defaultConfig));
        sessionAttachments.put(persistentSessionId, new java.util.ArrayList<>());
    }

    /**
     * 添加会话附件
     */
    public void addAttachment(String sessionId, Map<String, Object> fileInfo) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = this.persistentSessionId;
        }
        this.sessionAttachments.computeIfAbsent(sessionId, k -> new java.util.ArrayList<>())
                .add(fileInfo);
    }

    /**
     * 获取会话附件
     */
    public java.util.List<Map<String, Object>> getAttachments(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = this.persistentSessionId;
        }
        return this.sessionAttachments.getOrDefault(sessionId, new java.util.ArrayList<>());
    }

    /**
     * 获取或创建持久化会话
     */
    public String createSession() {
        return persistentSessionId;
    }

    /**
     * 获取持久化会话
     */
    public BaseAgent getOrCreateAgent(String sessionId) {
        return agentSessions.computeIfAbsent(persistentSessionId, id -> new BaseAgent(defaultConfig));
    }

    /**
     * 发送消息并获取响应
     */
    public String sendMessage(String sessionId, String message) {
        BaseAgent agent = getOrCreateAgent(sessionId);
        return agent.interact(message);
    }

    /**
     * 执行任务
     */
    public String executeTask(String sessionId, String task) {
        BaseAgent agent = getOrCreateAgent(sessionId);
        return agent.executeTask(task);
    }

    /**
     * 获取会话状态
     */
    public String getSessionStatus(String sessionId) {
        BaseAgent agent = agentSessions.get(persistentSessionId);
        if (agent == null) {
            return "会话不存在";
        }
        return agent.getStatus();
    }

    /**
     * 重置会话（禁用）
     */
    public void resetSession(String sessionId) {
        // 禁用重置，保持对话记忆
    }

    /**
     * 删除会话（禁用）
     */
    public void deleteSession(String sessionId) {
        // 禁用删除，保持对话记忆
    }

    /**
     * 获取可用工具列表
     */
    public java.util.List<String> getAvailableTools(String sessionId) {
        BaseAgent agent = getOrCreateAgent(sessionId);
        return agent.getAvailableTools();
    }

    /**
     * 更新配置
     */
    public void updateConfig(String sessionId, AgentConfig config) {
        // 更新配置时保留会话
        BaseAgent agent = getOrCreateAgent(sessionId);
        // 创建新的 Agent 实例
        agentSessions.put(persistentSessionId, new BaseAgent(config));
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return agentSessions.size();
    }

    /**
     * 获取可用技能列表
     */
    public java.util.List<String> getAvailableSkills(String sessionId) {
        BaseAgent agent = getOrCreateAgent(sessionId);
        return agent.getAvailableSkills();
    }

    /**
     * 获取启用的技能数量
     */
    public int getEnabledSkillCount(String sessionId) {
        BaseAgent agent = getOrCreateAgent(sessionId);
        return agent.getEnabledSkillCount();
    }

    /**
     * 获取所有技能数量
     */
    public int getTotalSkillCount(String sessionId) {
        BaseAgent agent = getOrCreateAgent(sessionId);
        return agent.getTotalSkillCount();
    }

    /**
     * 获取会话的执行链路
     */
    public java.util.List<ExecutionTrace.TraceStepDTO> getSessionTrace(String sessionId) {
        BaseAgent agent = agentSessions.get(persistentSessionId);
        if (agent != null) {
            return agent.getTraceDTOList();
        }
        return new java.util.ArrayList<>();
    }

    /**
     * 启用/禁用执行链路跟踪
     */
    public void setTraceEnabled(String sessionId, boolean enabled) {
        BaseAgent agent = agentSessions.get(persistentSessionId);
        if (agent != null) {
            agent.setTraceEnabled(enabled);
        }
    }
}
