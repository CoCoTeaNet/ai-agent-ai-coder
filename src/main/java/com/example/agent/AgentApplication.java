package com.example.agent;

import com.example.agent.config.AgentConfig;

/**
 * Agent 应用程序主入口
 * 演示如何使用 Agent 框架
 */
public class AgentApplication {

    public static void main(String[] args) {
        // 创建 Agent 配置
        AgentConfig config = AgentConfig.builder()
                .provider(AgentConfig.LlmProvider.OPENAI_COMPATIBLE)
                .modelName("doubao-pro-32k")
                .baseUrl("https://ark.cn-beijing.volces.com/api/coding")
                .apiKeyFromEnv()
                .temperature(0.7)
                .maxTokens(2048)
                .systemPrompt("你是一个聪明的助手，使用可用的工具来回答问题和执行任务。")
                .enableTools(true)
                .maxIterations(5)
                .build();

        // 创建 Agent
        BaseAgent agent = new BaseAgent(config);

        // 启动交互式控制台
        agent.startInteractiveConsole();
    }

    /**
     * 简单使用示例
     */
    public static void simpleUsageExample() {
        System.out.println("=== Agent 简单使用示例 ===\n");

        // 使用默认配置创建 Agent
        BaseAgent agent = new BaseAgent();

        // 发送消息
        String response1 = agent.interact("你好，请介绍一下你自己。");
        System.out.println("用户: 你好，请介绍一下你自己。");
        System.out.println("Agent: " + response1 + "\n");

        // 执行任务
        String response2 = agent.executeTask("计算 256 + 45 的结果");
        System.out.println("用户: 计算 256 + 45");
        System.out.println("Agent: " + response2 + "\n");

        // 查看状态
        System.out.println(agent.getStatus());
    }

    /**
     * OpenAI 配置示例
     */
    public static void openAiExample() {
        AgentConfig config = AgentConfig.builder()
                .provider(AgentConfig.LlmProvider.OPENAI)
                .modelName("gpt-3.5-turbo")
                .apiKey("your-api-key-here")
                .temperature(0.7)
                .enableTools(true)
                .build();

        BaseAgent agent = new BaseAgent(config);
        // 使用 agent...
    }

    /**
     * OpenAI 兼容接口配置示例
     */
    public static void openAiCompatibleExample() {
        AgentConfig config = AgentConfig.builder()
                .provider(AgentConfig.LlmProvider.OPENAI_COMPATIBLE)
                .modelName("your-model-name")
                .baseUrl("https://your-custom-endpoint.com/v1")
                .apiKey("your-api-key-here")
                .temperature(0.7)
                .enableTools(true)
                .build();

        BaseAgent agent = new BaseAgent(config);
        // 使用 agent...
    }
}
