# 使用 IntelliJ IDEA 启动项目

您的电脑上已经安装了 IntelliJ IDEA，这是运行本项目最简单的方式！

## 步骤 1: 在 IntelliJ IDEA 中打开项目

1. 启动 IntelliJ IDEA（您已安装 2024.1, 2025.3 和 2026.1 版本）
2. 点击 "File" -> "Open"
3. 导航到 `D:\CodeLife\AI-code-demo` 文件夹并选择它
4. 点击 "OK"
5. 等待 IDEA 识别项目（右下角会显示 Maven 同步进度）

## 步骤 2: 等待 Maven 依赖下载

- IDEA 会自动识别这是一个 Maven 项目
- 右下角会显示 "Maven Build" 进度条
- 等待所有依赖下载完成（这可能需要几分钟）

## 步骤 3: 运行应用程序

找到并运行主类文件：
1. 在左侧项目视图中，导航到：
   ```
   src/main/java/com/example/agent/web
   ```
2. 找到 `AgentWebApplication.java` 并双击打开
3. 在代码编辑器中，您会看到 `public class AgentWebApplication`
4. 点击类声明左边行号旁边的绿色运行按钮 ▶️
5. 或者：右键点击文件名，选择 "Run 'AgentWebApplication'"

## 步骤 4: 访问应用

启动成功后，您会看到类似这样的控制台输出：
```
==========================================
       Agent Web 应用已启动
==========================================
访问地址: http://localhost:8080
API 文档: http://localhost:8080/api
==========================================
```

然后访问：
- 主界面: http://localhost:8080
- API 文档: http://localhost:8080/api

## v1.1.0 新功能

### 1. 流式响应 📝
- 聊天时会看到实时打字机效果
- 不再需要等待完整响应生成
- 支持取消生成

### 2. 搜索功能 🔍
- 点击右上角的 ⚙️ 按钮进入设置
- 可以配置 Bing Search API 密钥
- 测试搜索功能是否正常工作
- Agent 可以使用搜索工具获取网络信息

### 3. 增强的界面 ✨
- 全新的搜索配置面板
- 更流畅的用户体验
- 响应式设计优化

## 常见问题

**Q: Maven 依赖下载太慢怎么办？**  
A: 在 IDEA 中配置 Maven 使用国内镜像（如阿里云）

**Q: 找不到 AgentWebApplication 怎么办？**  
A: 确保 Maven 同步完成后再查找

**Q: 8080 端口被占用怎么办？**  
A: 在 `src/main/resources/application.properties` 中添加 `server.port=8081` 来更改端口

祝您使用愉快！
