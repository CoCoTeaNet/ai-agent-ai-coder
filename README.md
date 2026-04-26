# CogniAgent - 企业级智能代理框架

基于 Java 和 LangChain4j 构建的现代化智能代理（AI Agent）框架，提供强大的认知能力、丰富的工具生态和灵活的扩展机制。

## 项目概述

CogniAgent 是一个功能完备的企业级智能代理框架，旨在为开发者提供快速构建 AI 驱动应用的基础设施。框架融合了先进的认知推理能力和丰富的工具生态系统，支持多种大语言模型（LLM），并提供 Web 界面和 RESTful API。

### 核心特性

- 🧠 **高级认知能力**：支持思维链（Chain of Thought）、ReAct 推理模式、自我反思机制
- 🔧 **丰富的工具集**：内置计算器、日期时间、文件操作、网络请求、Excel处理、数据爬取等10+工具
- 🎯 **技能系统**：可扩展的技能架构，支持代码分析、翻译等专业技能
- 🌐 **多模态支持**：支持文本和图片输入，实现多模态交互
- 💾 **智能记忆管理**：自动会话摘要、上下文窗口管理、长期记忆支持
- 🔌 **多模型兼容**：支持 Ollama（本地）、OpenAI、以及所有 OpenAI 兼容接口（如豆包、通义千问等）
- 🖥️ **Web 界面**：现代化的 Web UI，支持实时对话和执行链路可视化
- 📡 **RESTful API**：完整的 API 服务，便于集成到其他系统
- 📊 **执行追踪**：详细的执行链路记录，便于调试和优化
- ⚙️ **高度可配置**：灵活的配置系统，支持运行时调整

## 技术栈

- **Java 17**：现代 Java 编程语言
- **Spring Boot 3.2.0**：企业级应用框架
- **LangChain4j 0.36.2**：领先的 Java AI 开发框架
- **Maven**：项目构建和依赖管理
- **Thymeleaf**：服务端模板引擎
- **Apache POI**：Excel 文件处理
- **Jsoup**：HTML 解析和数据爬取
- **HttpClient 5**：HTTP 客户端

## 快速开始

### 前置要求

1. **JDK 17+**：确保已安装 Java Development Kit 17 或更高版本
2. **Maven 3.6+**：用于项目构建
3. **LLM 服务**（任选其一）：
   - Ollama（本地运行，推荐用于开发测试）
   - OpenAI API 密钥
   - 其他 OpenAI 兼容服务（如豆包、通义千问等）

### 配置环境变量

#### 安全提示

⚠️ **重要安全注意事项**：
- 项目需要 `AI_API_KEY` 环境变量来访问 LLM 服务
- **切勿**将真实的 API 密钥提交到版本控制系统
- 使用 `.env.example` 作为配置模板，实际配置保存在 `.env` 文件中（已在 `.gitignore` 中排除）

#### Windows 系统

```powershell
# PowerShell
$env:AI_API_KEY='your_actual_api_key_here'

# 命令提示符
set AI_API_KEY=your_actual_api_key_here

# 或在 IntelliJ IDEA 的 Run/Debug Configuration 中设置环境变量
```

#### Linux/macOS 系统

```bash
# 临时设置（当前终端会话）
export AI_API_KEY=your_actual_api_key_here

# 永久设置（添加到 shell 配置文件）
echo 'export AI_API_KEY=your_actual_api_key_here' >> ~/.bashrc
source ~/.bashrc

# 运行时设置
AI_API_KEY=your_actual_api_key_here java -jar target/cogniagent.jar
```

#### 使用 .env 文件（推荐）

1. 复制示例配置：`cp .env.example .env`
2. 在 `.env` 文件中填入实际的 API 密钥和配置
3. 确保启动脚本或 IDE 能够读取 `.env` 文件

### 启动方式

#### 方式一：命令行启动（控制台模式）

```bash
# 编译项目
mvn clean compile

# 运行交互式控制台
mvn exec:java -Dexec.mainClass="com.example.agent.AgentApplication"

# 运行单元测试
mvn test
```

#### 方式二：Web 应用启动（推荐）

```bash
# 启动 Web 服务器
mvn spring-boot:run

# 或使用打包后的 JAR
mvn clean package
java -jar target/langchain4j-agent-demo-1.2.0.jar
```

启动后访问：
- **Web 界面**：http://localhost:8080
- **API 文档**：http://localhost:8080/api

#### 方式三：IDE 运行

直接在 IntelliJ IDEA 或其他 IDE 中运行：
- 控制台模式：`com.example.agent.AgentApplication`
- Web 模式：`com.example.agent.web.AgentWebApplication`

## 项目架构

### 目录结构

```
CogniAgent/
├── src/main/java/com/example/agent/
│   ├── Agent.java                    # Agent 核心接口定义
│   ├── BaseAgent.java               # Agent 基础实现（700+ 行核心逻辑）
│   ├── AdvancedAgent.java           # 高级 Agent 扩展
│   ├── AgentApplication.java        # 控制台应用入口
│   ├── config/                      # 配置管理模块
│   │   ├── AgentConfig.java         # Agent 配置类（Builder 模式）
│   │   └── LlmClientFactory.java    # LLM 客户端工厂
│   ├── services/                     # 服务层
│   │   ├── AgentService.java        # Agent 业务服务
│   │   ├── MemoryService.java       # 记忆管理服务
│   │   └── ExecutionTrace.java      # 执行链路追踪
│   ├── skills/                       # 技能系统
│   │   ├── Skill.java               # 技能接口
│   │   ├── BaseSkill.java           # 技能基类
│   │   ├── SkillManager.java        # 技能管理器
│   │   ├── CodeAnalysisSkill.java   # 代码分析技能
│   │   ├── TranslationSkill.java    # 翻译技能
│   │   └── TongJinchengSkill.java   # 自定义技能示例
│   ├── tools/                        # 工具生态系统
│   │   ├── CalculatorTool.java      # 数学计算器
│   │   ├── DateTimeTool.java        # 日期时间工具
│   │   ├── FileTool.java            # 文件操作工具
│   │   ├── NetworkTool.java         # HTTP 请求工具
│   │   ├── RealSearchTool.java      # 网络搜索工具
│   │   ├── ExcelTool.java           # Excel 处理工具
│   │   ├── DataSpiderTool.java      # 数据爬取工具
│   │   └── SearchTool.java          # 搜索工具（基础版）
│   └── web/                          # Web 模块
│       ├── AgentWebApplication.java # Spring Boot 启动类
│       ├── controller/
│       │   ├── ApiController.java   # REST API 控制器
│       │   └── WebController.java   # Web 页面控制器
│       └── resources/
│           ├── static/              # 静态资源（CSS/JS）
│           └── templates/           # Thymeleaf 模板
├── src/test/java/                   # 测试代码
│   └── com/example/agent/
│       └── AgentTest.java          # 单元测试
├── pom.xml                          # Maven 配置
├── .env.example                     # 环境变量模板
└── README.md                        # 项目文档
```

### 核心组件说明

#### 1. Agent 核心层
- **Agent 接口**：定义标准 Agent 行为契约
- **BaseAgent**：实现核心交互逻辑，支持工具调用、技能匹配、执行追踪
- **AdvancedAgent**：提供高级推理能力（思维链、ReAct、自我反思）

#### 2. 配置管理层
- **AgentConfig**：使用 Builder 模式提供灵活配置
- **LlmClientFactory**：工厂模式创建不同 LLM 客户端

#### 3. 工具系统
- 基于 LangChain4j 的 `@Tool` 注解实现
- 支持动态工具注册和调用
- 每个工具都有完整的错误处理和日志记录

#### 4. 技能系统
- 可扩展的技能架构
- 智能技能匹配算法
- 支持技能的启用/禁用管理

#### 5. 记忆服务
- **MemoryService**：管理对话历史
- **MessageWindowChatMemory**：自动窗口管理（最多 40 条消息）
- **对话摘要**：自动生成对话摘要，优化长对话性能

## 配置详解

### AgentConfig 配置项

```java
AgentConfig config = AgentConfig.builder()
    // LLM 提供商选择
    .provider(AgentConfig.LlmProvider.OPENAI_COMPATIBLE)  // OLLAMA / OPENAI / OPENAI_COMPATIBLE
    
    // 模型配置
    .modelName("doubao-pro-32k")                         // 模型名称
    .baseUrl("https://ark.cn-beijing.volces.com/api/coding")  // API 地址
    .apiKeyFromEnv()                                      // 从环境变量读取 API Key
    
    // 生成参数
    .temperature(0.7)                                     // 温度参数 (0.0-1.0)
    .maxTokens(2048)                                      // 最大 Token 数
    .topP(0.9)                                            // 核采样参数
    
    // 系统提示
    .systemPrompt("你是一个聪明的助手，使用可用的工具来回答问题和执行任务。")
    
    // 高级功能开关
    .enableTools(true)                                    // 启用工具调用
    .enableChainOfThought(true)                           // 启用思维链推理
    .enableReactMode(true)                                // 启用 ReAct 模式
    .enableSelfReflection(true)                           // 启用自我反思
    
    // 会话管理
    .maxIterations(5)                                     // 最大迭代次数
    .maxConversationSummaryLength(200)                    // 对话摘要最大长度
    
    .build();
```

### 支持的 LLM 提供商

| 提供商 | 说明 | 适用场景 |
|--------|------|----------|
| **OLLAMA** | 本地运行的开源模型 | 开发测试、隐私敏感场景 |
| **OPENAI** | OpenAI 官方 API | 生产环境、高质量需求 |
| **OPENAI_COMPATIBLE** | 兼容 OpenAI 接口的服务 | 豆包、通义千问、自定义部署 |

### 高级功能说明

#### 思维链（Chain of Thought）
让 Agent 展示完整的思考过程：
1. 理解问题
2. 规划解决步骤
3. 逐步执行和推理
4. 给出结论

#### ReAct 模式（Reasoning + Acting）
结合推理和行动的循环模式：
1. **思考**：我需要什么工具？
2. **行动**：调用合适的工具
3. **观察**：分析工具结果
4. **思考**：下一步做什么？

#### 自我反思（Self Reflection）
在给出最终答案前进行自我评估：
- 回答准确性检查
- 工具使用合理性评估
- 改进空间识别

## 使用示例

### 基础交互

```java
// 创建 Agent 实例
BaseAgent agent = new BaseAgent();

// 简单对话
String response = agent.interact("你好，请介绍一下你自己");
System.out.println(response);

// 执行任务
String result = agent.executeTask("计算 256 × 45 + 30 ÷ 2");
System.out.println("计算结果: " + result);

// 查看状态
System.out.println(agent.getStatus());

// 重置对话
agent.reset();
```

### 多模态交互（图片 + 文本）

```java
import java.util.*;

// 准备附件
List<Map<String, Object>> attachments = new ArrayList<>();
Map<String, Object> imageAttachment = new HashMap<>();
imageAttachment.put("path", "/path/to/image.png");
imageAttachment.put("type", "image/png");
imageAttachment.put("name", "示例图片");
attachments.add(imageAttachment);

// 发送带图片的消息
String response = agent.interact("这张图片里有什么？", attachments);
```

### 工具使用示例

```java
// 数学计算
agent.interact("计算 256 * 45 + 30 / 2");

// 日期时间查询
agent.interact("今天是星期几？现在几点？");

// 文件操作
agent.interact("读取文件 /path/to/file.txt 的内容");

// 网络请求
agent.interact("访问 https://api.example.com/data 获取数据");

// 网络搜索
agent.interact("搜索 LangChain4j 的最新文档");

// Excel 处理
agent.interact("创建 Excel 文件并写入数据");

// 数据爬取
agent.interact("从网页提取指定信息");
```

### Web API 使用

```bash
# 发送对话请求
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请介绍一下 CogniAgent",
    "sessionId": "user-123"
  }'

# 获取执行链路
curl http://localhost:8080/api/trace/user-123

# 查看可用工具
curl http://localhost:8080/api/tools
```

## 工具系统详解

### 内置工具列表

| 工具名称 | 功能描述 | 使用场景 |
|---------|---------|----------|
| **CalculatorTool** | 数学表达式计算 | 算术运算、复杂公式计算 |
| **DateTimeTool** | 日期时间处理 | 获取当前时间、日期格式化、日期计算 |
| **FileTool** | 文件操作 | 读写文件、目录管理、文件搜索 |
| **NetworkTool** | HTTP 请求 | API 调用、网页下载、数据传输 |
| **RealSearchTool** | 网络搜索 | DuckDuckGo 搜索、维基百科查询 |
| **ExcelTool** | Excel 处理 | 创建/读取 Excel 文件、数据导出 |
| **DataSpiderTool** | 数据爬取 | 网页数据提取、结构化数据采集 |

### 开发自定义工具

#### 步骤 1：创建工具类

```java
package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class MyCustomTool {
    
    @Tool("工具功能描述，会被 LLM 理解")
    public String myToolMethod(
        @P("参数1的描述") String param1,
        @P("参数2的描述") int param2
    ) {
        // 实现工具逻辑
        String result = "处理结果";
        return result;
    }
}
```

#### 步骤 2：在 BaseAgent 中注册工具

```java
// 在 BaseAgent 构造函数中添加
MyCustomTool myTool = new MyCustomTool();

this.agentWithTools = AiServices.builder(AgentWithTools.class)
    .chatLanguageModel(chatModel)
    .chatMemory(chatMemory)
    .tools(dateTimeTool, calculatorTool, realSearchTool, 
           fileTool, networkTool, excelTool, dataSpiderTool,
           myTool)  // 添加新工具
    .build();
```

#### 步骤 3：在配置中启用工具

```java
AgentConfig config = AgentConfig.builder()
    .enableTools(true)  // 确保启用工具
    .build();
```

## 技能系统

### 什么是技能？

技能是比工具更高级的功能单元，可以组合多个工具和复杂的业务逻辑，实现特定的专业功能。

### 内置技能

| 技能名称 | 功能描述 |
|---------|---------|
| **CodeAnalysisSkill** | 代码分析、质量检查、重构建议 |
| **TranslationSkill** | 多语言翻译、术语管理 |
| **TongJinchengSkill** | 自定义业务技能示例 |

### 开发自定义技能

```java
package com.example.agent.skills;

public class MyCustomSkill extends BaseSkill {
    
    public MyCustomSkill() {
        super(
            "my_skill",                              // 技能 ID
            "我的自定义技能",                         // 技能名称
            "技能功能描述，用于匹配用户意图",          // 技能描述
            true                                      // 是否启用
        );
    }
    
    @Override
    public String execute(String input) {
        // 实现技能逻辑
        // 可以调用工具、访问外部服务等
        
        String result = "技能执行结果";
        return result;
    }
    
    @Override
    public boolean canHandle(String input) {
        // 判断是否能处理该输入
        return input.toLowerCase().contains("关键词");
    }
}
```

### 注册技能

```java
// 在 SkillManager 中注册
skillManager.registerSkill(new MyCustomSkill());
```

## 测试与质量保证

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=AgentTest

# 生成测试报告
mvn surefire-report:report
```

### 测试覆盖范围

- ✅ Agent 初始化和配置验证
- ✅ 基础交互和对话流程
- ✅ 工具调用和功能验证
- ✅ 会话管理和记忆服务
- ✅ 配置参数边界测试
- ✅ 异常处理和错误恢复

### 代码质量

- 完整的 JavaDoc 文档注释
- 统一的代码风格和命名规范
- 详细的日志记录和错误追踪
- 模块化设计和低耦合架构

## 故障排除

### 常见问题及解决方案

#### 1. LLM 连接失败

**问题**：无法连接到 LLM 服务

**解决方案**：
```bash
# Ollama 用户
ollama serve                              # 确保服务运行
ollama list                               # 检查模型是否已下载
ollama pull llama2                        # 下载所需模型

# 检查网络连接
curl http://localhost:11434/api/tags      # 测试 Ollama 连接

# OpenAI 用户
# 验证 API Key 是否正确
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer $AI_API_KEY"
```

#### 2. 工具未加载

**问题**：Agent 无法调用工具

**解决方案**：
- 检查 `AgentConfig.enableTools` 是否为 `true`
- 确认工具类已正确注册到 `AiServices`
- 查看日志中的工具加载信息

#### 3. 内存溢出

**问题**：长时间对话导致内存不足

**解决方案**：
```java
// 调整聊天记忆窗口大小
MessageWindowChatMemory.withMaxMessages(20);  // 减少消息数量

// 启用对话摘要
config.enableChainOfThought(true);  // 自动生成摘要
```

#### 4. API 响应缓慢

**问题**：Agent 响应时间过长

**解决方案**：
- 减少 `maxTokens` 配置
- 降低 `temperature` 值（更快的确定性输出）
- 启用对话摘要以减少上下文长度
- 考虑使用更快的模型或本地部署

#### 5. 环境变量未生效

**问题**：`AI_API_KEY` 设置后仍然报错

**解决方案**：
```bash
# Windows PowerShell
$env:AI_API_KEY='your_key'
mvn spring-boot:run

# Linux/macOS
export AI_API_KEY='your_key'
mvn spring-boot:run

# 或在 IDE 中配置环境变量
# IntelliJ IDEA: Run -> Edit Configurations -> Environment Variables
```

## 最佳实践

### 性能优化

1. **合理配置上下文窗口**
   - 短对话：20-30 条消息
   - 长对话：启用自动摘要功能
   - 避免超过模型的 token 限制

2. **工具调用优化**
   - 仅在必要时启用工具
   - 为常用工具编写清晰的描述
   - 避免工具间的循环调用

3. **缓存策略**
   - 对频繁查询的数据进行缓存
   - 复用 Agent 实例而非每次创建
   - 使用连接池管理 HTTP 请求

### 安全建议

1. **API 密钥管理**
   - 使用环境变量或密钥管理服务
   - 定期轮换 API 密钥
   - 不要在代码中硬编码密钥

2. **输入验证**
   - 对用户输入进行 sanitization
   - 限制文件上传大小和类型
   - 验证 URL 和网络请求目标

3. **权限控制**
   - 为不同用户配置不同的工具权限
   - 限制敏感工具的访问
   - 实施速率限制防止滥用

### 生产部署

1. **日志和监控**
   - 启用详细的执行链路追踪
   - 监控 API 响应时间和错误率
   - 设置告警阈值

2. **高可用配置**
   - 使用负载均衡器分发请求
   - 配置健康检查端点
   - 实施熔断和降级策略

3. **资源管理**
   - 设置 JVM 堆内存限制
   - 配置线程池大小
   - 监控系统资源使用情况

## 路线图与发展规划

### v1.2.0（当前版本）✅

- ✅ 基础 Agent 框架
- ✅ 7+ 内置工具
- ✅ 3 个专业技能
- ✅ Web 界面和 RESTful API
- ✅ 多模态支持（文本+图片）
- ✅ 执行链路追踪
- ✅ 高级推理模式（CoT、ReAct、自我反思）

### v1.3.0（计划中）

- 🔄 数据库持久化（会话历史、配置）
- 🔄 更多预置技能（数据分析、报告生成）
- 🔄 WebSocket 实时通信
- 🔄 插件市场架构
- 🔄 性能分析和优化工具

### v2.0.0（长远规划）

- 🔮 分布式 Agent 集群
- 🔮 多 Agent 协作系统
- 🔮 可视化工作流编辑器
- 🔮 企业级权限管理
- 🔮 Kubernetes 原生支持
- 🔮 微服务架构拆分

---

## 贡献指南

欢迎贡献代码、报告问题或提出建议！

### 贡献步骤

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送到分支：`git push origin feature/amazing-feature`
5. 提交 Pull Request

### 开发规范

- 遵循 Java 代码规范
- 添加完整的单元测试
- 更新相关文档
- 保持代码简洁和可读性

## 参考资源

### 官方文档

- [LangChain4j 官方文档](https://langchain4j.dev/) - Java AI 开发框架
- [Spring Boot 文档](https://spring.io/projects/spring-boot) - 企业级应用框架
- [Ollama 文档](https://ollama.com/docs) - 本地 LLM 运行时
- [OpenAI API 文档](https://platform.openai.com/docs) - OpenAI 接口参考

### 相关技术

- [Java 17 教程](https://docs.oracle.com/en/java/javase/17/) - Java 编程语言
- [Maven 指南](https://maven.apache.org/guides/) - 项目构建工具
- [Apache POI](https://poi.apache.org/) - Office 文档处理
- [Jsoup](https://jsoup.org/) - HTML 解析库

### 社区资源

- [LangChain4j GitHub](https://github.com/langchain4j/langchain4j)
- [Spring Boot GitHub](https://github.com/spring-projects/spring-boot)
- [Awesome LLM Apps](https://github.com/dair-ai/awesome-llm-apps)

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 联系方式

- 📧 Email: your-email@example.com
- 🌐 Website: https://cogniagent.dev
- 💬 Issues: [GitHub Issues](https://github.com/your-repo/cogniagent/issues)

---

<div align="center">

**Made with ❤️ by CogniAgent Team**

如果这个项目对你有帮助，请考虑给它一个 ⭐ Star！

</div>
