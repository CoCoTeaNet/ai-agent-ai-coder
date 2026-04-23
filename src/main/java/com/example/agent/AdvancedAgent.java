package com.example.agent;

import com.example.agent.config.AgentConfig;
import com.example.agent.config.LlmClientFactory;
import com.example.agent.services.MemoryService;
import com.example.agent.skills.SkillManager;
import com.example.agent.tools.CalculatorTool;
import com.example.agent.tools.DateTimeTool;
import com.example.agent.tools.SearchTool;
import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 高级 Agent 实现类
 * 实现 Chain of Thought、ReAct 模式、自我反思、长对话摘要等高级功能
 */
public class AdvancedAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(AdvancedAgent.class);
    private final AgentConfig config;
    private final ChatLanguageModel chatModel;
    private final MemoryService memoryService;
    private final MessageWindowChatMemory chatMemory;
    private final AgentWithTools agentWithTools;
    private final DateTimeTool dateTimeTool;
    private final CalculatorTool calculatorTool;
    private final SearchTool searchTool;
    private final SkillManager skillManager;
    private boolean initialized;
    private String conversationSummary;
    private int totalMessagesProcessed;

    /**
     * Agent 工具接口
     */
    public interface AgentWithTools {
        String chat(String message);
    }

    public AdvancedAgent(AgentConfig config) {
        this.config = config;
        this.chatModel = new LlmClientFactory(config).createChatModel();
        this.memoryService = new MemoryService();
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(40); // 增加到40条消息
        this.dateTimeTool = new DateTimeTool();
        this.calculatorTool = new CalculatorTool();
        this.searchTool = new SearchTool();
        this.skillManager = new SkillManager();
        this.conversationSummary = "";
        this.totalMessagesProcessed = 0;

        // 初始化带工具的 Agent
        if (config.isEnableTools()) {
            this.agentWithTools = AiServices.builder(AgentWithTools.class)
                    .chatLanguageModel(chatModel)
                    .chatMemory(chatMemory)
                    .tools(dateTimeTool, calculatorTool, searchTool)
                    .build();
        } else {
            this.agentWithTools = null;
        }

        // 添加系统提示
        updateSystemPrompt();
        this.initialized = true;
    }

    public AdvancedAgent() {
        this(AgentConfig.builder().build());
    }

    /**
     * 更新系统提示词，根据配置包含各种高级功能
     */
    private void updateSystemPrompt() {
        StringBuilder systemPrompt = new StringBuilder();

        if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
            systemPrompt.append(config.getSystemPrompt()).append("\n\n");
        } else {
            systemPrompt.append("你是一个聪明的助手，使用可用的工具来回答问题和执行任务。\n\n");
        }

        if (config.isEnableChainOfThought()) {
            systemPrompt.append("## Chain of Thought (思维链)\n");
            systemPrompt.append("在回答问题时，请展示你的思考过程：\n");
            systemPrompt.append("1. 首先理解问题\n");
            systemPrompt.append("2. 然后规划解决步骤\n");
            systemPrompt.append("3. 逐步执行和推理\n");
            systemPrompt.append("4. 最后给出结论\n\n");
        }

        if (config.isEnableReactMode()) {
            systemPrompt.append("## ReAct 模式\n");
            systemPrompt.append("当需要使用工具时，请遵循：\n");
            systemPrompt.append("1. 思考：我需要什么工具？\n");
            systemPrompt.append("2. 行动：调用合适的工具\n");
            systemPrompt.append("3. 观察：分析工具结果\n");
            systemPrompt.append("4. 思考：下一步做什么？\n\n");
        }

        if (config.isEnableSelfReflection()) {
            systemPrompt.append("## 自我反思\n");
            systemPrompt.append("在给出最终答案前，请反思：\n");
            systemPrompt.append("1. 我的回答准确吗？\n");
            systemPrompt.append("2. 我是否使用了最佳的工具？\n");
            systemPrompt.append("3. 有没有改进空间？\n\n");
        }

        if (conversationSummary != null && !conversationSummary.isEmpty()) {
            systemPrompt.append("## 对话摘要\n");
            systemPrompt.append(conversationSummary).append("\n\n");
        }

        // 清空并重新添加系统提示
        chatMemory.clear();
        chatMemory.add(new SystemMessage(systemPrompt.toString()));

        // 重新添加历史消息（如果有）
        for (ChatMessage msg : memoryService.getMessages()) {
            chatMemory.add(msg);
        }
    }

    @Override
    public String interact(String message) {
        checkInitialized();
        try {
            memoryService.addUserMessage(message);
            totalMessagesProcessed++;
            log.info("发送消息到 LLM: {}", message);

            String response;

            // 检查是否需要更新对话摘要
            if (memoryService.size() > 30 && config.isEnableChainOfThought()) {
                updateConversationSummary();
            }

            if (config.isEnableTools() && agentWithTools != null) {
                // 使用带工具的 Agent
                response = agentWithTools.chat(message);
            } else {
                // 简单模式：先检测是否需要调用工具
                response = handleWithAdvancedTools(message);
            }

            // 自我反思
            if (config.isEnableSelfReflection()) {
                response = applySelfReflection(message, response);
            }

            log.info("收到 LLM 响应: {}", response);
            memoryService.addAiMessage(response);
            return response;
        } catch (Exception e) {
            log.error("LLM 交互失败", e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return "交互失败: " + e.getMessage() + "\n详细信息: " + sw.toString();
        }
    }

    /**
     * 高级工具调用处理
     */
    private String handleWithAdvancedTools(String message) {
        String lowerMessage = message.toLowerCase();
        String toolResult = null;
        String thinking = "";

        if (config.isEnableChainOfThought()) {
            thinking = "让我思考一下这个问题...\n";
        }

        // 检查是否需要日期时间工具
        if (lowerMessage.contains("今天") || lowerMessage.contains("日期") ||
            lowerMessage.contains("时间") || lowerMessage.contains("几号")) {

            if (config.isEnableReactMode()) {
                thinking += "思考：用户询问日期/时间，我需要调用 DateTime 工具。\n";
                thinking += "行动：正在获取当前时间...\n";
            }

            toolResult = dateTimeTool.getCurrentDateTime();

            if (config.isEnableReactMode()) {
                thinking += "观察：工具返回了 " + toolResult + "\n";
            }
        }
        // 检查是否需要计算器
        else if (lowerMessage.matches(".*[0-9]+.*[+\\-*/].*[0-9]+.*")) {
            String expression = extractExpression(message);

            if (expression != null) {
                if (config.isEnableReactMode()) {
                    thinking += "思考：检测到数学表达式，我需要使用 Calculator 工具。\n";
                    thinking += "行动：正在计算 " + expression + "...\n";
                }

                try {
                    toolResult = calculatorTool.calculate(expression);

                    if (config.isEnableReactMode()) {
                        thinking += "观察：计算结果是 " + toolResult + "\n";
                    }
                } catch (Exception e) {
                    toolResult = "计算错误: " + e.getMessage();
                }
            }
        }

        // 如果有工具结果，让 LLM 整合
        if (toolResult != null) {
            String prompt = message + "\n\n工具执行结果: " + toolResult + "\n请根据这个结果回答用户。";
            return thinking + chatModel.generate(prompt);
        }

        // 否则直接调用 LLM
        return thinking + chatModel.generate(message);
    }

    /**
     * 应用自我反思
     */
    private String applySelfReflection(String originalMessage, String response) {
        String reflectionPrompt =
            "请对以下回答进行自我反思并改进：\n\n" +
            "用户问题: " + originalMessage + "\n\n" +
            "当前回答: " + response + "\n\n" +
            "请从以下角度反思：\n" +
            "1. 准确性 - 回答是否准确？\n" +
            "2. 完整性 - 是否遗漏了重要信息？\n" +
            "3. 清晰度 - 回答是否易于理解？\n" +
            "4. 工具使用 - 是否恰当使用了工具？\n\n" +
            "如果需要改进，请提供改进后的回答。如果已经很好，请原样返回。";

        String reflectionResult = chatModel.generate(reflectionPrompt);

        // 如果反思结果认为有改进，使用改进版本
        if (reflectionResult.length() > response.length() &&
            !reflectionResult.contains("原样返回") &&
            !reflectionResult.contains("已经很好")) {
            return reflectionResult;
        }

        return response;
    }

    /**
     * 更新对话摘要
     */
    private void updateConversationSummary() {
        List<ChatMessage> history = memoryService.getMessages();
        if (history.isEmpty()) return;

        StringBuilder historyText = new StringBuilder();
        for (ChatMessage msg : history) {
            if (msg instanceof UserMessage userMsg) {
                historyText.append("用户: ").append(userMsg.text()).append("\n");
            } else if (msg instanceof AiMessage aiMsg) {
                historyText.append("助手: ").append(aiMsg.text()).append("\n");
            }
        }

        String summaryPrompt =
            "请为以下对话创建一个简洁的摘要（不超过 " +
            config.getMaxConversationSummaryLength() +
            " 字）：\n\n" + historyText.toString();

        conversationSummary = chatModel.generate(summaryPrompt);
        log.info("对话摘要已更新: {}", conversationSummary);

        // 更新系统提示词
        updateSystemPrompt();
    }

    /**
     * 简单的表达式提取
     */
    private String extractExpression(String message) {
        String[] parts = message.split("[,，。. ]");
        for (String part : parts) {
            if (part.matches(".*[0-9+\\-*/].*")) {
                return part.trim();
            }
        }
        return null;
    }

    @Override
    public String executeTask(String task) {
        checkInitialized();
        String taskPrompt = "请执行以下任务: " + task;
        return interact(taskPrompt);
    }

    @Override
    public void reset() {
        memoryService.clear();
        chatMemory.clear();
        conversationSummary = "";
        totalMessagesProcessed = 0;
        updateSystemPrompt();
        initialized = true;
    }

    @Override
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("高级 Agent 状态:\n");
        sb.append("  - 初始化状态: ").append(initialized ? "已初始化" : "未初始化").append("\n");
        sb.append("  - 模型提供商: ").append(config.getProvider()).append("\n");
        sb.append("  - 模型名称: ").append(config.getModelName()).append("\n");
        sb.append("  - 工具启用: ").append(config.isEnableTools() ? "是" : "否").append("\n");
        sb.append("  - 思维链 (Chain of Thought): ").append(config.isEnableChainOfThought() ? "是" : "否").append("\n");
        sb.append("  - ReAct 模式: ").append(config.isEnableReactMode() ? "是" : "否").append("\n");
        sb.append("  - 自我反思: ").append(config.isEnableSelfReflection() ? "是" : "否").append("\n");
        sb.append("  - 记忆服务: ").append(memoryService.getSummary()).append("\n");
        sb.append("  - 处理消息数: ").append(totalMessagesProcessed).append("\n");
        if (conversationSummary != null && !conversationSummary.isEmpty()) {
            sb.append("  - 对话摘要: ").append(conversationSummary.substring(0, Math.min(100, conversationSummary.length()))).append("...\n");
        }
        return sb.toString();
    }

    @Override
    public List<String> getAvailableTools() {
        List<String> tools = new ArrayList<>();
        if (config.isEnableTools()) {
            tools.add("Calculator - 数学计算");
            tools.add("DateTime - 日期时间处理");
            tools.add("Search - 搜索功能");

            // 添加技能
            tools.addAll(skillManager.getSkillDescriptions());
        }
        if (config.isEnableChainOfThought()) {
            tools.add("Chain of Thought - 思维链推理");
        }
        if (config.isEnableReactMode()) {
            tools.add("ReAct - 推理-行动模式");
        }
        if (config.isEnableSelfReflection()) {
            tools.add("Self Reflection - 自我反思");
        }
        return tools;
    }

    /**
     * 获取可用技能列表
     */
    public List<String> getAvailableSkills() {
        return skillManager.getSkillDescriptions();
    }

    /**
     * 获取启用的技能数量
     */
    public int getEnabledSkillCount() {
        return skillManager.getEnabledSkills().size();
    }

    /**
     * 获取所有技能数量
     */
    public int getTotalSkillCount() {
        return skillManager.getSkillCount();
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Agent 未初始化，请先调用 reset() 方法");
        }
    }

    /**
     * 获取聊天历史记录
     */
    public List<ChatMessage> getChatHistory() {
        return memoryService.getMessages();
    }

    /**
     * 获取对话摘要
     */
    public String getConversationSummary() {
        return conversationSummary;
    }

    /**
     * 启动交互式控制台
     */
    public void startInteractiveConsole() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== 高级 Agent 交互控制台 ===");
        System.out.println("输入 '/quit' 或 '/exit' 退出");
        System.out.println("输入 '/status' 查看状态");
        System.out.println("输入 '/tools' 查看可用工具");
        System.out.println("输入 '/reset' 重置对话");
        System.out.println("输入 '/summary' 查看对话摘要");
        System.out.println("============================\n");

        while (true) {
            System.out.print("你> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (input.equals("/quit") || input.equals("/exit")) {
                System.out.println("再见！");
                break;
            }

            if (input.equals("/status")) {
                System.out.println("Agent> \n" + getStatus() + "\n");
                continue;
            }

            if (input.equals("/tools")) {
                System.out.println("Agent> 可用工具:");
                getAvailableTools().forEach(tool -> System.out.println("  - " + tool));
                System.out.println();
                continue;
            }

            if (input.equals("/reset")) {
                reset();
                System.out.println("Agent> 对话已重置\n");
                continue;
            }

            if (input.equals("/summary")) {
                System.out.println("Agent> 对话摘要: " + getConversationSummary() + "\n");
                continue;
            }

            System.out.println("Agent> 思考中...");
            long startTime = System.currentTimeMillis();
            String response = interact(input);
            long endTime = System.currentTimeMillis();
            System.out.println("Agent> " + response);
            System.out.printf("（耗时: %.2f 秒）\n\n", (endTime - startTime) / 1000.0);
        }

        scanner.close();
    }
}
