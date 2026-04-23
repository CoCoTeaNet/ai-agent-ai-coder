# LangChain4j Agent v1.1.0 启动脚本 (PowerShell)
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   LangChain4j Agent v1.1.0 启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 尝试查找 IntelliJ IDEA 安装路径
$possiblePaths = @(
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.3\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Program Files\JetBrains\IntelliJ IDEA 2024.1\plugins\maven\lib\maven3\bin\mvn.cmd"
)

$mavenCmd = $null

# 检查常见的 Maven 安装路径
Write-Host "正在查找 Maven..." -ForegroundColor Yellow
foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $mavenCmd = $path
        Write-Host "✓ 找到 Maven: $path" -ForegroundColor Green
        break
    }
}

# 检查系统 PATH 中是否有 mvn
if (-not $mavenCmd) {
    $mvnInPath = Get-Command "mvn.cmd" -ErrorAction SilentlyContinue
    if ($mvnInPath) {
        $mavenCmd = $mvnInPath.Source
        Write-Host "✓ 系统 PATH 中找到 Maven: $mavenCmd" -ForegroundColor Green
    }
}

# 如果找到 Maven，尝试运行项目
if ($mavenCmd) {
    Write-Host ""
    Write-Host "正在启动项目..." -ForegroundColor Yellow
    Write-Host "请稍候，这可能需要几分钟时间..." -ForegroundColor Gray
    Write-Host ""

    try {
        & $mavenCmd spring-boot:run
    } catch {
        Write-Host ""
        Write-Host "✗ Maven 运行出错: $_" -ForegroundColor Red
    }
}
else {
    # 未找到 Maven，显示替代方案
    Write-Host ""
    Write-Host "✗ 未找到 Maven" -ForegroundColor Red
    Write-Host ""
    Write-Host "请使用以下任一方式启动项目:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. 使用 IntelliJ IDEA (推荐):" -ForegroundColor White
    Write-Host "   - 在 IntelliJ 中打开项目文件夹"
    Write-Host "   - 等待 Maven 同步完成"
    Write-Host "   - 运行 AgentWebApplication.java"
    Write-Host ""
    Write-Host "2. 手动安装 Maven 后运行:" -ForegroundColor White
    Write-Host "   - 访问 https://maven.apache.org/download.cgi"
    Write-Host "   - 下载并安装 Maven"
    Write-Host "   - 在项目目录执行: mvn spring-boot:run"
    Write-Host ""
}

Write-Host ""
Write-Host "应用启动后请访问:" -ForegroundColor Cyan
Write-Host "  主界面: http://localhost:8080" -ForegroundColor White
Write-Host "  API文档: http://localhost:8080/api" -ForegroundColor White
Write-Host ""
Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
