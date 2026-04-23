package com.example.agent.skills;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 翻译技能
 * 提供多语言翻译功能
 */
public class TranslationSkill extends BaseSkill {

    private static final Map<String, String> LANGUAGE_CODES;
    private static final Map<String, String> SIMPLE_TRANSLATIONS;

    static {
        LANGUAGE_CODES = new HashMap<>();
        LANGUAGE_CODES.put("英语", "en");
        LANGUAGE_CODES.put("英文", "en");
        LANGUAGE_CODES.put("中文", "zh");
        LANGUAGE_CODES.put("汉语", "zh");
        LANGUAGE_CODES.put("日语", "ja");
        LANGUAGE_CODES.put("日文", "ja");
        LANGUAGE_CODES.put("法语", "fr");
        LANGUAGE_CODES.put("德文", "de");
        LANGUAGE_CODES.put("西班牙语", "es");
        LANGUAGE_CODES.put("韩语", "ko");
        LANGUAGE_CODES.put("韩文", "ko");

        // 简单的预定义翻译
        SIMPLE_TRANSLATIONS = new HashMap<>();
        SIMPLE_TRANSLATIONS.put("你好 英语", "Hello");
        SIMPLE_TRANSLATIONS.put("hello 中文", "你好");
        SIMPLE_TRANSLATIONS.put("谢谢 英语", "Thank you");
        SIMPLE_TRANSLATIONS.put("thank you 中文", "谢谢");
        SIMPLE_TRANSLATIONS.put("goodbye 中文", "再见");
        SIMPLE_TRANSLATIONS.put("再见 英语", "Goodbye");
    }

    public TranslationSkill() {
        super("Translation - 翻译",
              "提供多语言翻译功能，支持中文、英语、日语、法语、德语、西班牙语、韩语等语言的互译");
    }

    @Override
    public boolean canHandle(String input) {
        String lowerInput = input.toLowerCase();
        return lowerInput.contains("翻译") ||
               lowerInput.contains("translate") ||
               lowerInput.contains("英语") ||
               lowerInput.contains("中文") ||
               lowerInput.contains("日文") ||
               lowerInput.contains("日语") ||
               lowerInput.contains("翻成") ||
               lowerInput.contains("翻译成");
    }

    @Override
    public String execute(String input) {
        StringBuilder result = new StringBuilder();
        result.append("## 翻译技能\n\n");

        // 分析翻译请求
        String sourceText = extractSourceText(input);
        String sourceLanguage = detectSourceLanguage(input);
        String targetLanguage = detectTargetLanguage(input);

        if (sourceText.isEmpty()) {
            result.append("请提供要翻译的文本。您可以这样使用：\n");
            result.append("- \"把'你好'翻译成英语\"\n");
            result.append("- \"翻译'Hello'为中文\"\n");
            result.append("- \"Translate 'Good morning' to Chinese\"\n\n");
            result.append("**支持的语言:**\n");
            result.append(String.join(", ", LANGUAGE_CODES.keySet()));
            return result.toString();
        }

        result.append("**原文:** ").append(sourceText).append("\n");
        result.append("**源语言:** ").append(sourceLanguage).append("\n");
        result.append("**目标语言:** ").append(targetLanguage).append("\n\n");

        // 尝试简单翻译
        String translation = translate(sourceText, sourceLanguage, targetLanguage);
        result.append("**翻译结果:**\n").append(translation);

        return result.toString();
    }

    /**
     * 翻译核心逻辑
     */
    private String translate(String text, String sourceLang, String targetLang) {
        // 首先检查是否有预定义的简单翻译
        String key = (text.toLowerCase() + " " + targetLang).trim();
        if (SIMPLE_TRANSLATIONS.containsKey(key)) {
            return SIMPLE_TRANSLATIONS.get(key);
        }

        key = (text + " " + targetLang).trim();
        if (SIMPLE_TRANSLATIONS.containsKey(key)) {
            return SIMPLE_TRANSLATIONS.get(key);
        }

        // 模拟翻译（实际项目中应使用真实的翻译API）
        StringBuilder sb = new StringBuilder();
        sb.append("[模拟翻译结果: ").append(text).append(" (").append(sourceLang).append(" → ").append(targetLang).append(")]\n\n");
        sb.append("**说明:** 这是一个模拟的翻译。\n");
        sb.append("在实际项目中，应该集成真实的翻译API服务如:\n");
        sb.append("- Google Translate API\n");
        sb.append("- DeepL API\n");
        sb.append("- 腾讯翻译 API\n");
        sb.append("- 百度翻译 API\n");
        sb.append("- 火山翻译 API\n\n");
        sb.append("您要翻译的内容: ").append(text);

        return sb.toString();
    }

    /**
     * 从输入中提取要翻译的文本
     */
    private String extractSourceText(String input) {
        // 查找引号中的内容
        int firstQuote = input.indexOf("'");
        int secondQuote = input.indexOf("'", firstQuote + 1);
        if (firstQuote != -1 && secondQuote != -1) {
            return input.substring(firstQuote + 1, secondQuote);
        }

        int firstDoubleQuote = input.indexOf("\"");
        int secondDoubleQuote = input.indexOf("\"", firstDoubleQuote + 1);
        if (firstDoubleQuote != -1 && secondDoubleQuote != -1) {
            return input.substring(firstDoubleQuote + 1, secondDoubleQuote);
        }

        // 如果没有引号，尝试提取文本部分
        String lowerInput = input.toLowerCase();
        String[] keywords = {"翻译", "translate", "翻成", "翻译成", "to"};

        for (String keyword : keywords) {
            if (lowerInput.contains(keyword)) {
                int index = lowerInput.indexOf(keyword);
                // 提取关键词后面或前面的文本
                if (index + keyword.length() < input.length()) {
                    String after = input.substring(index + keyword.length()).trim();
                    // 跳过"成"、"到"等词
                    after = after.replaceAll("^(成|到|为|to|in|into)\\s*", "").trim();
                    if (!after.isEmpty()) {
                        // 移除后面的语言描述
                        for (String lang : LANGUAGE_CODES.keySet()) {
                            if (after.contains(lang)) {
                                after = after.substring(0, after.indexOf(lang)).trim();
                            }
                        }
                        if (!after.isEmpty()) {
                            return after;
                        }
                    }
                }
            }
        }

        // 如果没有找到，返回去掉常见词后的结果
        String result = input;
        for (String keyword : keywords) {
            result = result.replaceAll(keyword, "");
        }
        for (String lang : LANGUAGE_CODES.keySet()) {
            result = result.replaceAll(lang, "");
        }

        return result.trim();
    }

    /**
     * 检测源语言
     */
    private String detectSourceLanguage(String input) {
        String lowerInput = input.toLowerCase();

        // 如果是翻译成某种语言，源语言可能是中文
        if (lowerInput.contains("翻译成") || lowerInput.contains("成英语") || lowerInput.contains("to english")) {
            return "中文";
        }

        // 检测输入是否包含非ASCII字符（简单的中文字符检测）
        for (char c : input.toCharArray()) {
            if (c >= 0x4e00 && c <= 0x9fff) {
                return "中文";
            }
        }

        // 默认源语言为英语
        return "英语";
    }

    /**
     * 检测目标语言
     */
    private String detectTargetLanguage(String input) {
        String lowerInput = input.toLowerCase();

        // 检查是否明确指定目标语言
        for (String lang : LANGUAGE_CODES.keySet()) {
            if (lowerInput.contains(lang)) {
                // 检查是否在"翻译成"、"翻成"、"to"之后
                int langIndex = lowerInput.indexOf(lang);
                String before = lowerInput.substring(0, Math.max(0, langIndex)).trim();
                if (before.contains("翻译") || before.contains("成") || before.contains("to")) {
                    return lang;
                }
            }
        }

        // 如果没有指定，默认翻译成中文
        return "中文";
    }
}
