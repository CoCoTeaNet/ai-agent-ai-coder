package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 真实搜索工具
 * 使用网络请求访问真实搜索引擎 API
 */
public class RealSearchTool {

    private static final Logger log = LoggerFactory.getLogger(RealSearchTool.class);
    private final NetworkTool networkTool;

    public RealSearchTool() {
        this.networkTool = new NetworkTool();
    }

    /**
     * 使用 DuckDuckGo Instant Answer API 搜索
     * @param query 搜索关键词
     * @return 搜索结果
     */
    @Tool("使用 DuckDuckGo 搜索网络信息")
    public String search(@P("搜索关键词") String query) {
        try {
            // 使用 DuckDuckGo Instant Answer API
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format("https://api.duckduckgo.com/?q=%s&format=json&no_html=1&skip_disambig=1",
                    encodedQuery);

            log.info("搜索: {}", query);

            String response = networkTool.get(url);

            // 解析结果
            return parseDuckDuckGoResponse(response, query);
        } catch (Exception e) {
            log.error("搜索失败: {}", query, e);
            return "搜索失败: " + e.getMessage() + "\n\n尝试使用备用搜索方法...";
        }
    }

    /**
     * 使用备用搜索方法（如维基百科摘要）
     */
    @Tool("搜索维基百科信息")
    public String searchWikipedia(@P("搜索关键词") String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format("https://zh.wikipedia.org/api/rest_v1/page/summary/%s",
                    encodedQuery);

            String response = networkTool.get(url);

            return parseWikipediaResponse(response, query);
        } catch (Exception e) {
            log.error("维基百科搜索失败: {}", query, e);
            return "无法获取维基百科信息: " + e.getMessage();
        }
    }

    /**
     * 解析 DuckDuckGo API 响应
     */
    private String parseDuckDuckGoResponse(String response, String query) {
        StringBuilder result = new StringBuilder();
        List<String> results = new ArrayList<>();

        // 检查是否有定义信息
        if (response.contains("\"Abstract\":\"")) {
            String abstractText = extractBetween(response, "\"Abstract\":\"", "\"");
            if (abstractText != null && !abstractText.isEmpty() && abstractText.length() > 5) {
                results.add("【定义】: " + abstractText);
            }
        }

        // 检查是否有摘要信息
        if (response.contains("\"AbstractText\":\"")) {
            String abstractText = extractBetween(response, "\"AbstractText\":\"", "\"");
            if (abstractText != null && !abstractText.isEmpty() && abstractText.length() > 5) {
                results.add("【摘要】: " + abstractText);
            }
        }

        // 检查是否有 Answer
        if (response.contains("\"Answer\":\"")) {
            String answer = extractBetween(response, "\"Answer\":\"", "\"");
            if (answer != null && !answer.isEmpty() && answer.length() > 5) {
                results.add("【答案】: " + answer);
            }
        }

        // 检查是否有 Definition
        if (response.contains("\"Definition\":\"")) {
            String definition = extractBetween(response, "\"Definition\":\"", "\"");
            if (definition != null && !definition.isEmpty() && definition.length() > 5) {
                results.add("【定义】: " + definition);
            }
        }

        // 如果没有直接结果，返回搜索建议
        if (results.isEmpty()) {
            results.add("未能找到关于 '" + query + "' 的详细信息");
            results.add("建议尝试使用更具体的关键词搜索");
            results.add("或者尝试使用维基百科搜索功能");
        }

        for (int i = 0; i < results.size(); i++) {
            result.append(i + 1).append(". ").append(results.get(i)).append("\n");
        }

        return result.toString().trim();
    }

    /**
     * 解析维基百科响应
     */
    private String parseWikipediaResponse(String response, String query) {
        StringBuilder result = new StringBuilder();

        // 检查是否有标题
        if (response.contains("\"title\":\"")) {
            String title = extractBetween(response, "\"title\":\"", "\"");
            if (title != null && !title.isEmpty()) {
                result.append("【").append(title).append("】\n");
            }
        }

        // 检查是否有摘要
        if (response.contains("\"extract\":\"")) {
            String extract = extractBetween(response, "\"extract\":\"", "\"");
            if (extract != null && !extract.isEmpty()) {
                result.append("【摘要】: ").append(extract).append("\n");
            }
        }

        // 检查是否有页面 URL
        if (response.contains("\"content_urls\":")) {
            if (response.contains("\"desktop\":")) {
                String desktopUrl = extractBetween(response, "\"page\":\"", "\"");
                if (desktopUrl != null && !desktopUrl.isEmpty()) {
                    result.append("【查看详细】: ").append(desktopUrl).append("\n");
                }
            }
        }

        if (result.length() == 0) {
            result.append("未能在维基百科上找到关于 '").append(query).append("' 的信息");
        }

        return result.toString().trim();
    }

    /**
     * 简单的字符串提取方法
     */
    private String extractBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex != -1) {
            startIndex += start.length();
            int endIndex = text.indexOf(end, startIndex);
            if (endIndex != -1) {
                String result = text.substring(startIndex, endIndex);
                // 解码 Unicode 转义序列
                return result.replace("\\u0026", "&").replace("\\n", "\n").replace("\\t", "\t");
            }
        }
        return null;
    }

    /**
     * 实时新闻搜索（使用 NewsAPI）
     */
    @Tool("搜索实时新闻")
    public String searchNews(@P("新闻关键词") String query) {
        // 注意：需要注册 NewsAPI 并获取 API Key
        // 这里只是示例，实际需要配置 API Key
        return "新闻搜索功能需要 NewsAPI Key，当前未配置";
    }

    /**
     * 获取最新科技新闻
     */
    @Tool("获取最新科技新闻")
    public String getTechNews() {
        try {
            // 使用 TechCrunch RSS 或其他新闻源
            String url = "https://techcrunch.com/feed/";

            String response = networkTool.get(url);
            return parseNewsFeed(response, "科技新闻");
        } catch (Exception e) {
            log.error("获取科技新闻失败", e);
            return "无法获取科技新闻: " + e.getMessage();
        }
    }

    /**
     * 解析 RSS 新闻源
     */
    private String parseNewsFeed(String response, String category) {
        StringBuilder result = new StringBuilder();
        result.append("最新 ").append(category).append(":\n\n");

        // 简单的 RSS 解析
        Pattern itemPattern = Pattern.compile("<item>([\\s\\S]*?)</item>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = itemPattern.matcher(response);

        int count = 0;
        while (matcher.find() && count < 5) {
            String item = matcher.group(1);

            // 提取标题
            Pattern titlePattern = Pattern.compile("<title>([\\s\\S]*?)</title>", Pattern.CASE_INSENSITIVE);
            Matcher titleMatcher = titlePattern.matcher(item);
            if (titleMatcher.find()) {
                String title = titleMatcher.group(1).trim();
                // 移除 CDATA
                if (title.startsWith("<![CDATA[")) {
                    title = title.substring(9, title.length() - 3).trim();
                }
                result.append(count + 1).append(". ").append(title).append("\n");
            }

            // 提取链接
            Pattern linkPattern = Pattern.compile("<link>([\\s\\S]*?)</link>", Pattern.CASE_INSENSITIVE);
            Matcher linkMatcher = linkPattern.matcher(item);
            if (linkMatcher.find()) {
                String link = linkMatcher.group(1).trim();
                result.append("   ").append(link).append("\n");
            }

            result.append("\n");
            count++;
        }

        if (count == 0) {
            result.append("未能获取到最新新闻");
        }

        return result.toString().trim();
    }
}
