package com.example.agent.services;

import com.example.agent.BaseAgent;
import com.example.agent.config.AgentConfig;
import com.example.agent.services.ExecutionTrace;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final Map<String, String> sessionProviderMap; // 会话与提供商配置ID的关联
    private AgentConfig defaultConfig;
    private String persistentSessionId;

    // 支持的模型列表
    private final List<Map<String, Object>> availableModels;

    public AgentService() {
        this.agentSessions = new ConcurrentHashMap<>();
        this.sessionAttachments = new ConcurrentHashMap<>();
        this.sessionProviderMap = new ConcurrentHashMap<>();

        // 初始化模型列表
        this.availableModels = new ArrayList<>();

        // 初始化默认配置（从提供商配置管理器加载）
        try {
            this.defaultConfig = AgentConfig.builder()
                    .fromDefaultProviderConfig()
                    .build();
        } catch (Exception e) {
            // 如果加载失败，使用默认配置
            this.defaultConfig = AgentConfig.builder()
                    .provider(AgentConfig.LlmProvider.OLLAMA)
                    .modelName("llama2")
                    .baseUrl("http://localhost:11434")
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
        }

        // 创建持久化会话
        this.persistentSessionId = UUID.randomUUID().toString();
        agentSessions.put(persistentSessionId, new BaseAgent(defaultConfig));
        sessionAttachments.put(persistentSessionId, new java.util.ArrayList<>());
    }
    
    /**
     * 获取支持的模型列表
     */
    public List<Map<String, Object>> getAvailableModels() {
        List<Map<String, Object>> models = new ArrayList<>();
        for (Map<String, Object> model : availableModels) {
            Map<String, Object> modelInfo = new HashMap<>();
            modelInfo.put("id", model.get("id"));
            modelInfo.put("name", model.get("name"));
            modelInfo.put("available", model.get("available"));
            models.add(modelInfo);
        }
        return models;
    }
    
    /**
     * 切换当前模型
     */
    public boolean switchModel(String modelId) {
        for (Map<String, Object> model : availableModels) {
            if (model.get("id").equals(modelId)) {
                if (!(Boolean) model.get("available")) {
                    return false; // 模型不可用
                }
                this.defaultConfig = (AgentConfig) model.get("config");
                // 清空现有会话缓存，强制后续使用新的配置重新创建 Agent
                agentSessions.clear();
                // 为默认的持久化会话重新初始化 Agent
                agentSessions.put(persistentSessionId, new BaseAgent(defaultConfig));
                return true;
            }
        }
        return false;
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
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = this.persistentSessionId;
        }
        return agentSessions.computeIfAbsent(sessionId, id -> new BaseAgent(defaultConfig));
    }

    /**
     * 发送消息并获取响应（支持附件）
     */
    public String sendMessage(String sessionId, String message, java.util.List<Map<String, Object>> attachments) {
        BaseAgent agent = getOrCreateAgent(sessionId);
        return agent.interact(message, attachments);
    }

    /**
     * 兼容旧接口
     */
    public String sendMessage(String sessionId, String message) {
        return sendMessage(sessionId, message, null);
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

    /**
     * 切换会话使用的提供商
     */
    public boolean switchSessionProvider(String sessionId, String providerConfigId) {
        try {
            com.example.agent.config.ProviderConfigManager configManager =
                    com.example.agent.config.ProviderConfigManager.getInstance();
            com.example.agent.config.ProviderConfig providerConfig = configManager.getConfig(providerConfigId);

            if (providerConfig == null || !providerConfig.isEnabled()) {
                return false;
            }

            // 创建新的配置
            AgentConfig newConfig = AgentConfig.builder()
                    .fromProviderConfig(providerConfigId)
                    .systemPrompt("你是一个聪明的助手，使用可用的工具来回答问题和执行任务。")
                    .enableTools(true)
                    .maxIterations(5)
                    .enableChainOfThought(true)
                    .enableReactMode(true)
                    .enableSelfReflection(true)
                    .maxConversationSummaryLength(1000)
                    .build();

            // 更新会话
            String targetSessionId = (sessionId == null || sessionId.trim().isEmpty()) ? persistentSessionId : sessionId;
            sessionProviderMap.put(targetSessionId, providerConfigId);
            agentSessions.put(targetSessionId, new BaseAgent(newConfig));

            return true;
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AgentService.class)
                    .error("切换提供商失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取会话当前使用的提供商配置ID
     */
    public String getSessionProviderId(String sessionId) {
        String targetSessionId = (sessionId == null || sessionId.trim().isEmpty()) ? persistentSessionId : sessionId;
        return sessionProviderMap.get(targetSessionId);
    }
}
