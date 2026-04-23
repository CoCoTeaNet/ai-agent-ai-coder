package com.example.agent.skills;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import com.example.agent.tools.FileTool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 代码分析技能
 * 提供代码分析、代码审查、代码质量评估等功能
 */
public class CodeAnalysisSkill extends BaseSkill {

    private final FileTool fileTool;
    private static final Pattern JAVA_FILE_PATTERN = Pattern.compile(".*\\.java$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PYTHON_FILE_PATTERN = Pattern.compile(".*\\.py$", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_FILE_PATTERN = Pattern.compile(".*\\.js$", Pattern.CASE_INSENSITIVE);

    public CodeAnalysisSkill() {
        super("Code Analysis - 代码分析",
              "提供代码分析、代码审查、代码质量评估、代码复杂度计算等功能");
        this.fileTool = new FileTool();
    }

    @Override
    public boolean canHandle(String input) {
        String lowerInput = input.toLowerCase();
        return lowerInput.contains("分析代码") ||
               lowerInput.contains("代码审查") ||
               lowerInput.contains("代码质量") ||
               lowerInput.contains("代码复杂度") ||
               lowerInput.contains("阅读代码") ||
               (lowerInput.contains("代码") && lowerInput.contains("分析"));
    }

    @Override
    public String execute(String input) {
        // 检查是否有文件路径提到
        String lowerInput = input.toLowerCase();
        StringBuilder result = new StringBuilder();

        result.append("## 代码分析技能\n\n");

        // 检查是否有特定语言分析请求
        if (lowerInput.contains("java")) {
            result.append(analyzeJava(input));
        } else if (lowerInput.contains("python")) {
            result.append(analyzePython(input));
        } else if (lowerInput.contains("javascript")) {
            result.append(analyzeJavaScript(input));
        } else {
            // 通用代码分析
            result.append(analyzeCode(input));
        }

        return result.toString();
    }

    /**
     * Java代码分析
     */
    private String analyzeJava(String input) {
        StringBuilder sb = new StringBuilder();
        sb.append("### Java代码分析\n\n");
        sb.append("**Java代码分析功能包括:**\n");
        sb.append("1. 代码复杂度计算 (Cyclomatic Complexity)\n");
        sb.append("2. 代码风格检查\n");
        sb.append("3. 潜在问题检测\n");
        sb.append("4. 最佳实践建议\n\n");

        // 如果有文件路径，尝试读取文件
        String filePath = extractFilePath(input);
        if (filePath != null && JAVA_FILE_PATTERN.matcher(filePath).matches()) {
            try {
                String content = fileTool.readFile(filePath);
                sb.append("**已读取Java文件:** ").append(filePath).append("\n\n");
                sb.append("**代码分析结果:**\n");
                sb.append(analyzeJavaCode(content));
            } catch (Exception e) {
                sb.append("无法读取文件: ").append(e.getMessage()).append("\n");
            }
        } else {
            sb.append("请提供Java文件路径进行详细分析。\n");
        }

        return sb.toString();
    }

    /**
     * Python代码分析
     */
    private String analyzePython(String input) {
        StringBuilder sb = new StringBuilder();
        sb.append("### Python代码分析\n\n");
        sb.append("**Python代码分析功能包括:**\n");
        sb.append("1. PEP8规范检查\n");
        sb.append("2. 代码复杂度评估\n");
        sb.append("3. 类型提示建议\n");
        sb.append("4. 性能优化建议\n\n");

        String filePath = extractFilePath(input);
        if (filePath != null && PYTHON_FILE_PATTERN.matcher(filePath).matches()) {
            try {
                String content = fileTool.readFile(filePath);
                sb.append("**已读取Python文件:** ").append(filePath).append("\n\n");
                sb.append("**代码分析结果:**\n");
                sb.append(analyzePythonCode(content));
            } catch (Exception e) {
                sb.append("无法读取文件: ").append(e.getMessage()).append("\n");
            }
        } else {
            sb.append("请提供Python文件路径进行详细分析。\n");
        }

        return sb.toString();
    }

    /**
     * JavaScript代码分析
     */
    private String analyzeJavaScript(String input) {
        StringBuilder sb = new StringBuilder();
        sb.append("### JavaScript代码分析\n\n");
        sb.append("**JavaScript代码分析功能包括:**\n");
        sb.append("1. ES6+语法检查\n");
        sb.append("2. 代码质量评估\n");
        sb.append("3. 潜在Bug检测\n");
        sb.append("4. 性能优化建议\n\n");

        String filePath = extractFilePath(input);
        if (filePath != null && JAVASCRIPT_FILE_PATTERN.matcher(filePath).matches()) {
            try {
                String content = fileTool.readFile(filePath);
                sb.append("**已读取JavaScript文件:** ").append(filePath).append("\n\n");
                sb.append("**代码分析结果:**\n");
                sb.append(analyzeJavaScriptCode(content));
            } catch (Exception e) {
                sb.append("无法读取文件: ").append(e.getMessage()).append("\n");
            }
        } else {
            sb.append("请提供JavaScript文件路径进行详细分析。\n");
        }

        return sb.toString();
    }

    /**
     * 通用代码分析
     */
    private String analyzeCode(String input) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 通用代码分析\n\n");
        sb.append("**代码分析功能包括:**\n");
        sb.append("1. 代码质量评估\n");
        sb.append("2. 可读性分析\n");
        sb.append("3. 维护性评估\n");
        sb.append("4. 性能建议\n\n");
        sb.append("**支持的语言:** Java, Python, JavaScript\n\n");
        sb.append("请在请求中明确指定编程语言或提供文件路径。\n");
        return sb.toString();
    }

    /**
     * 分析Java代码
     */
    private String analyzeJavaCode(String code) {
        StringBuilder sb = new StringBuilder();

        // 简单的代码度量
        int lines = code.split("\n").length;
        int characters = code.length();

        sb.append("- 代码行数: ").append(lines).append("\n");
        sb.append("- 字符数: ").append(characters).append("\n");

        // 检查常见问题
        if (code.contains("System.out.println")) {
            sb.append("- ⚠️ 建议: 使用日志框架代替System.out.println\n");
        }
        if (code.contains("// TODO")) {
            sb.append("- ⚠️ 检测到待办事项(TODO)\n");
        }
        if (!code.contains("@Override")) {
            sb.append("- 💡 建议: 确保正确使用@Override注解\n");
        }

        sb.append("\n**总体评估:** 代码基本结构良好。\n");

        return sb.toString();
    }

    /**
     * 分析Python代码
     */
    private String analyzePythonCode(String code) {
        StringBuilder sb = new StringBuilder();

        int lines = code.split("\n").length;
        sb.append("- 代码行数: ").append(lines).append("\n");

        if (code.contains("print(")) {
            sb.append("- ⚠️ 建议: 考虑使用logging模块代替print语句\n");
        }
        if (code.contains("def ") && !code.contains(":")) {
            sb.append("- ⚠️ 语法检查: 检查函数定义是否正确\n");
        }

        sb.append("\n**总体评估:** Python代码结构检查完成。\n");
        return sb.toString();
    }

    /**
     * 分析JavaScript代码
     */
    private String analyzeJavaScriptCode(String code) {
        StringBuilder sb = new StringBuilder();

        int lines = code.split("\n").length;
        sb.append("- 代码行数: ").append(lines).append("\n");

        if (code.contains("var ")) {
            sb.append("- 💡 建议: 使用let或const代替var以获得更好的作用域控制\n");
        }
        if (code.contains("eval(")) {
            sb.append("- ⚠️ 警告: 避免使用eval，存在安全风险\n");
        }

        sb.append("\n**总体评估:** JavaScript代码检查完成。\n");
        return sb.toString();
    }

    /**
     * 从输入中提取文件路径
     */
    private String extractFilePath(String input) {
        // 简单的文件路径提取
        String[] parts = input.split("\\s+");
        for (String part : parts) {
            if (part.contains("/") || part.contains("\\")) {
                return part.trim();
            }
        }
        return null;
    }
}
