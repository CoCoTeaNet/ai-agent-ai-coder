# LangChain4j Agent Demo

基于 Java 和 LangChain4j 框架的智能 Agent 基础架构。

## 项目概述

这是一个使用 LangChain4j 框架构建的智能 Agent 项目，提供以下特性：

- **支持多种 LLM 模型**：Ollama（本地运行，如 Llama 2）、OpenAI、OpenAI 兼容接口
- **内置工具**：计算器、日期时间、搜索工具
- **会话管理**：完整的会话记忆和上下文保持
- **配置灵活**：支持详细的配置选项
- **交互式控制台**：简单易用的命令行界面

## 技术栈

- **Java 17**：编程语言
- **LangChain4j 0.36.2**：AI 开发框架
- **Maven**：构建工具
- **Ollama**：本地 LLM 模型运行时

## 快速开始

### 前提条件

1. 安装 Java 17 或更高版本
2. 安装 Ollama（用于运行本地 LLM 模型）
3. 拉取所需的模型：`ollama pull llama2`

### 配置环境变量

**重要提示**：
- 项目需要 `AI_API_KEY` 环境变量才能使用 OpenAI 兼容接口
- 绝对不要将真实 API 密钥提交到 GitHub
- 使用 `.env.example` 作为配置模板，实际配置放在 `.env` 中（`.env` 文件已在 gitignore 中）

**Windows 系统设置环境变量：**

```bash
# 命令提示符
set AI_API_KEY=your_actual_api_key_here

# PowerShell
$env:AI_API_KEY='your_actual_api_key_here'

# 或者在 IntelliJ IDEA 的 Run/Debug Configuration 中设置
```

**Linux/macOS 系统设置环境变量：**

```bash
# 临时设置（当前终端会话）
export AI_API_KEY=your_actual_api_key_here

# 永久设置（添加到 ~/.bashrc 或 ~/.zshrc）
echo 'export AI_API_KEY=your_actual_api_key_here' >> ~/.bashrc
source ~/.bashrc

# 运行时设置
AI_API_KEY=your_actual_api_key_here java -jar app.jar
```

**使用 .env 文件（可选）：**
1. 复制 `.env.example` 为 `.env`
2. 在 `.env` 中填入实际的 API 密钥
3. 确保 IDE 或启动脚本能读取这个文件

### 运行项目

```bash
# 编译项目
mvn clean compile

# 运行主程序
mvn exec:java -Dexec.mainClass="com.example.agent.AgentApplication"

# 运行测试
mvn test
```

### 使用 IDE 运行

直接运行 `src/main/java/com/example/agent/AgentApplication.java` 类。

## 项目结构

```
├── src/main/java/com/example/agent/
│   ├── Agent.java                    # Agent 接口定义
│   ├── BaseAgent.java               # Agent 基础实现类
│   ├── AgentApplication.java        # 应用程序主入口
│   ├── config/                      # 配置管理
│   │   ├── AgentConfig.java         # Agent 配置类
│   │   └── LlmClientFactory.java    # LLM 客户端工厂类
│   ├── services/                     # 服务类
│   │   └── MemoryService.java       # 记忆服务类
│   └── tools/                        # 工具类
│       ├── CalculatorTool.java      # 计算器工具
│       ├── DateTimeTool.java        # 日期时间工具
│       └── SearchTool.java          # 搜索工具
└── src/test/java/                   # 测试类
    └── com/example/agent/
        └── AgentTest.java          # Agent 单元测试
```

## 配置说明

### AgentConfig 配置选项

```java
AgentConfig config = AgentConfig.builder()
    .provider(AgentConfig.LlmProvider.OLLAMA)   // LLM 提供商
    .modelName("llama2")                       // 模型名称
    .baseUrl("http://localhost:11434")         // API 基础 URL
    .temperature(0.7)                          // 温度参数
    .maxTokens(2048)                           // 最大 Token 数
    .topP(0.9)                                 // 核采样参数
    .systemPrompt("你的系统提示")              // 系统提示
    .enableTools(true)                         // 是否启用工具
    .maxIterations(5)                          // 最大迭代次数
    .build();
```

### 支持的 LLM 提供商

- **OLLAMA**：本地运行的 LLM 模型（推荐，不需要 API 密钥）
- **OPENAI**：OpenAI 官方 API
- **OPENAI_COMPATIBLE**：OpenAI 兼容接口（如 OpenLLaMA 等）

## 使用示例

### 基本使用

```java
// 创建 Agent 实例
BaseAgent agent = new BaseAgent();

// 发送消息
String response = agent.interact("你好，我需要帮助");

// 执行任务
String taskResult = agent.executeTask("计算 123 + 456");

// 获取状态
String status = agent.getStatus();

// 重置状态
agent.reset();
```

### 工具使用示例

```java
// 计算数学表达式
agent.interact("计算 256 * 45 + 30 / 2");

// 查询日期时间
agent.interact("今天是星期几？");

// 搜索信息
agent.interact("搜索 LangChain4j 的官方文档");
```

## 工具扩展

### 添加新工具

1. 创建新的工具类，继承自 `dev.langchain4j.agent.tool.Tool` 注解
2. 在 `BaseAgent` 中注册工具
3. 在 `AgentConfig` 中启用工具

### 示例

```java
package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class MyCustomTool {
    @Tool("我的自定义工具功能说明")
    public String myToolMethod(@P("参数描述") String parameter) {
        // 工具实现
        return "结果";
    }
}
```

## 运行测试

```bash
mvn test
```

测试会覆盖以下方面：

- Agent 创建和初始化
- 基本交互
- 工具功能
- 配置验证
- 会话管理

## 故障排除

### 常见问题

1. **Ollama 连接失败**：
   - 确保 Ollama 服务正在运行：`ollama serve`
   - 检查地址是否正确：`http://localhost:11434`

2. **模型未找到**：
   - 使用 `ollama pull <model-name>` 下载模型

3. **工具未加载**：
   - 检查 `AgentConfig` 中的 `enableTools` 配置
   - 检查工具类是否被正确注册

## 参考资源

- [LangChain4j 官方文档](https://langchain4j.dev/)
- [Ollama 文档](https://ollama.com/docs)
- [Java 编程](https://docs.oracle.com/javase/tutorial/)

## 许可证

MIT
