package com.example.agent;

import com.example.agent.config.AgentConfig;
import com.example.agent.tools.CalculatorTool;
import com.example.agent.tools.DateTimeTool;
import com.example.agent.tools.SearchTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Agent 单元测试类
 * 测试 Agent 的核心功能
 */
public class AgentTest {

    @Test
    public void testAgentCreation() {
        AgentConfig config = AgentConfig.builder()
                .modelName("llama2")
                .temperature(0.7)
                .build();

        BaseAgent agent = new BaseAgent(config);

        assertNotNull(agent);
        System.out.println("Agent 创建成功: " + agent.getStatus());
    }

    @Test
    public void testBasicInteraction() {
        BaseAgent agent = new BaseAgent();
        assertNotNull(agent);

        // 获取状态信息
        String status = agent.getStatus();
        System.out.println("Agent 状态: " + status);
        assertFalse(status.isEmpty());
    }

    @Test
    public void testAvailableTools() {
        BaseAgent agent = new BaseAgent();

        // 默认应该启用工具
        assertTrue(agent.getAvailableTools().size() > 0, "应该启用默认工具");
        System.out.println("可用工具: " + agent.getAvailableTools());
    }

    @Test
    public void testToolsIndividually() {
        // 测试计算器工具
        CalculatorTool calculatorTool = new CalculatorTool();
        String result = calculatorTool.calculate("2 + 2 * 3");
        assertEquals("8.0", result);
        System.out.println("计算器工具测试成功: 2 + 2 * 3 = " + result);

        // 测试日期时间工具
        DateTimeTool dateTimeTool = new DateTimeTool();
        String date = dateTimeTool.getCurrentDate();
        System.out.println("日期时间工具测试成功: 当前日期 " + date);
        assertNotNull(date);

        // 测试搜索工具
        SearchTool searchTool = new SearchTool();
        String searchResult = searchTool.search("langchain4j java");
        System.out.println("搜索工具测试成功: " + searchResult.substring(0, 50) + "...");
        assertFalse(searchResult.isEmpty());
    }

    @Test
    public void testMemoryService() {
        BaseAgent agent = new BaseAgent();

        // 检查状态信息是否包含记忆服务
        String status = agent.getStatus();
        assertTrue(status.contains("记忆服务") || status.contains("Memory"), "状态信息应包含记忆服务信息");

        System.out.println("记忆服务测试成功");
    }

    @Test
    public void testResetFunctionality() {
        BaseAgent agent = new BaseAgent();

        // 重置操作
        agent.reset();
        System.out.println("重置功能测试成功");

        // 再次获取状态
        String status = agent.getStatus();
        assertNotNull(status);
    }

    @Test
    public void testDisableTools() {
        AgentConfig config = AgentConfig.builder()
                .enableTools(false)
                .build();

        BaseAgent agent = new BaseAgent(config);

        assertTrue(agent.getAvailableTools().isEmpty(), "禁用工具后应返回空列表");
        System.out.println("禁用工具测试成功");
    }

    @Test
    public void testCustomSystemPrompt() {
        String customPrompt = "你是一个专业的程序员助手，精通 Java 语言。";
        AgentConfig config = AgentConfig.builder()
                .systemPrompt(customPrompt)
                .build();

        BaseAgent agent = new BaseAgent(config);

        // 无法直接获取系统提示，但可以验证创建成功
        assertNotNull(agent);
        System.out.println("系统提示配置测试成功");
    }

    @Test
    public void testConfigProperties() {
        AgentConfig config = AgentConfig.builder()
                .modelName("codellama:7b")
                .temperature(0.3)
                .maxTokens(4096)
                .baseUrl("http://localhost:11434")
                .enableTools(false)
                .maxIterations(10)
                .systemPrompt("自定义系统提示")
                .build();

        assertEquals("codellama:7b", config.getModelName());
        assertEquals(0.3, config.getTemperature(), 0.001);
        assertEquals(4096, config.getMaxTokens());
        assertFalse(config.isEnableTools());
        assertEquals(10, config.getMaxIterations());
        assertFalse(config.getSystemPrompt().isEmpty());

        System.out.println("配置属性测试成功");
    }

    @Test
    public void testDifferentProviders() {
        // 测试 Ollama
        AgentConfig ollamaConfig = AgentConfig.builder()
                .provider(AgentConfig.LlmProvider.OLLAMA)
                .modelName("llama2")
                .build();
        assertNotNull(new BaseAgent(ollamaConfig));

        // 测试 OpenAI
        AgentConfig openAiConfig = AgentConfig.builder()
                .provider(AgentConfig.LlmProvider.OPENAI)
                .modelName("gpt-3.5-turbo")
                .apiKey("test-key")
                .build();
        assertNotNull(new BaseAgent(openAiConfig));

        // 测试 OpenAI 兼容接口
        AgentConfig compatibleConfig = AgentConfig.builder()
                .provider(AgentConfig.LlmProvider.OPENAI_COMPATIBLE)
                .modelName("chatglm-6b")
                .baseUrl("http://localhost:8000")
                .apiKey("test-key")
                .build();
        assertNotNull(new BaseAgent(compatibleConfig));

        System.out.println("不同 LLM 提供商配置测试成功");
    }

    @Test
    public void testCalculatorTool() {
        CalculatorTool calculator = new CalculatorTool();
        String result = calculator.calculate("256 * 45 + 30 / 2");
        assertEquals("11535.0", result);
        System.out.println("计算器工具复杂运算测试成功");
    }
}
