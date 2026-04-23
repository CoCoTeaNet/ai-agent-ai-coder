@echo off
echo.
echo =========================================
echo     LangChain4j Agent v1.1.0 启动脚本
echo =========================================
echo.
echo 请选择启动方式:
echo.
echo 1. 在 IntelliJ IDEA 中运行 (推荐)
echo    - 打开项目文件夹: D:\CodeLife\AI-code-demo
echo    - 等待 Maven 依赖下载完成
echo    - 运行 AgentWebApplication.java
echo.
echo 2. 使用 Maven 命令 (如果已配置)
echo    - 打开终端，执行:
echo    - cd "D:\CodeLife\AI-code-demo"
echo    - mvn clean spring-boot:run
echo.
echo 3. 使用已编译的 .jar 文件
echo    - 确保已编译: mvn clean package
echo    - 运行: java -jar target/langchain4j-agent-demo-1.1.0.jar
echo.
echo 应用启动后访问:
echo    主界面: http://localhost:8080
echo    API文档: http://localhost:8080/api
echo.
echo =========================================
pause
