package com.example.agent.services;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 记忆服务
 * 管理会话历史和上下文记忆
 */
public class MemoryService {

    private final List<ChatMessage> messages;
    private final String sessionId;
    private final int maxHistorySize;

    public MemoryService() {
        this(UUID.randomUUID().toString(), 20);
    }

    public MemoryService(String sessionId, int maxHistorySize) {
        this.sessionId = sessionId;
        this.maxHistorySize = maxHistorySize;
        this.messages = new ArrayList<>();
    }

    /**
     * 添加用户消息
     */
    public void addUserMessage(String content) {
        messages.add(UserMessage.from(content));
        trimHistory();
    }

    /**
     * 添加 AI 消息
     */
    public void addAiMessage(String content) {
        messages.add(AiMessage.from(content));
        trimHistory();
    }

    /**
     * 获取会话历史
     */
    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * 获取会话 ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 清除会话历史
     */
    public void clear() {
        messages.clear();
    }

    /**
     * 获取会话历史大小
     */
    public int size() {
        return messages.size();
    }

    /**
     * 修剪历史记录，确保不超过最大大小
     */
    private void trimHistory() {
        if (messages.size() > maxHistorySize) {
            int removeCount = messages.size() - maxHistorySize;
            messages.subList(0, removeCount).clear();
        }
    }

    /**
     * 获取摘要信息
     */
    public String getSummary() {
        return String.format("会话 ID: %s, 历史记录数量: %d, 最大容量: %d",
                sessionId, messages.size(), maxHistorySize);
    }

    /**
     * 创建新的记忆服务实例
     */
    public static MemoryService createNew() {
        return new MemoryService();
    }

    /**
     * 创建带有指定会话 ID 的记忆服务实例
     */
    public static MemoryService createWithSessionId(String sessionId) {
        return new MemoryService(sessionId, 20);
    }
}
