package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLEncoder;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 真实的网页数据爬取工具
 */
public class DataSpiderTool {
    private static final Logger log = LoggerFactory.getLogger(DataSpiderTool.class);
    private final Gson gson = new Gson();

    // 【重要提示】：小红书对反爬极其严格。
    // 如果没有配置登录后的真实 Cookie，请求 100% 会被拦截或要求验证码。
    // 请在浏览器中登录小红书，按 F12 打开网络面板，复制任意请求中的 Cookie 填入此处。
    private static final String XHS_COOKIE = "abRequestId=f45f4e8f-8fff-59bb-a2a6-51f5285babf9; ets=1777098753782; webBuild=6.7.4; xsecappid=xhs-pc-web; a1=19dc3570750oa0ephytzlwa9zn5h8n3nc26vwe8xv50000181801; webId=d70792d9b8be1065e928c33135fc823d; acw_tc=0a4aba3017770987564297345e34a4dd3544eb1c8365315a341ad3a352496d; gid=yjfSq2WydjS8yjfSq2W8WJdY28l08dUxv9u1KChE0juI2h28xYIqIE888yYyY8y8DK02Djf8; websectiga=7750c37de43b7be9de8ed9ff8ea0e576519e8cd2157322eb972ecb429a7735d4; sec_poison_id=87f133f4-b908-4dd3-a561-1763afab9c38; web_session=040069b05664f756c171b72dd93b4b12f74ff7; id_token=VjEAAIfFOZNo+I5NYhf900z/so8OQ+zwUs5uZS+b1uU9AED2JUZucWiBCehbFFGuKsv5vzEmEgo5imSpKMRjzuSQmI05RwnZ0OrE/76sHqOPT3l0nMtQ/MbTGJybrBddTXfLrty4; unread={%22ub%22:%2269ea28310000000011020002%22%2C%22ue%22:%2269e0d9c1000000002301191b%22%2C%22uc%22:18}; loadts=1777100446978";

    @Tool("抓取指定平台（如小红书）的真实搜索结果数据，返回JSON格式数组。必须使用此工具获取最新线上数据。")
    public String crawlPlatformData(
            @P("平台名称，例如 'xiaohongshu', '小红书'") String platform,
            @P("搜索关键词，例如 '顺德美食'") String keyword,
            @P("需要抓取的文章数量，例如 10") int count) {
        
        if ("xiaohongshu".equalsIgnoreCase(platform) || "小红书".equals(platform)) {
            return crawlXiaohongshuReal(keyword, count);
        }
        
        return "{\"error\": \"暂不支持该平台的真实抓取: " + platform + "\"}";
    }

    private String crawlXiaohongshuReal(String keyword, int count) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            log.info("开始真实抓取小红书关键词: {}", keyword);
            String url = "https://www.xiaohongshu.com/search_result?keyword=" + URLEncoder.encode(keyword, "UTF-8") + "&source=web_search_result_notes";
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .cookie("Cookie", XHS_COOKIE)
                .timeout(10000)
                .get();

            String html = doc.html();
            
            // 尝试提取前端页面注入的 window.__INITIAL_STATE__ 数据
            Matcher matcher = Pattern.compile("window\\.__INITIAL_STATE__=([^<]+)</script>").matcher(html);
            if (matcher.find()) {
                String jsonStr = matcher.group(1);
                
                // 简单地使用正则从极其复杂的JSON文本中提取需要的信息
                // 注意：这里为了防止反序列化庞大且经常变动的DOM树而采用了正则提取
                Matcher titleMatcher = Pattern.compile("\"display_title\":\"(.*?)\"").matcher(jsonStr);
                Matcher authorMatcher = Pattern.compile("\"user\":\\{\"nickname\":\"(.*?)\",\"avatar\":\".*?\",\"userid\":\"(.*?)\"\\}").matcher(jsonStr);
                Matcher likesMatcher = Pattern.compile("\"liked_count\":\"(.*?)\"").matcher(jsonStr);
                
                int extracted = 0;
                while (titleMatcher.find() && extracted < count) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("文章标题", unescapeJavaString(titleMatcher.group(1)));
                    item.put("内容", "【说明：列表搜索页不返回详细内容，需进入文章详情页，此处仅为摘要。】"); 
                    
                    if (likesMatcher.find()) {
                        item.put("点赞数", likesMatcher.group(1));
                    } else {
                        item.put("点赞数", "未知");
                    }
                    
                    item.put("评论数", "未知"); // 小红书搜索列表通常不返回评论数，详情页才有
                    
                    if (authorMatcher.find()) {
                        item.put("作者", unescapeJavaString(authorMatcher.group(1)));
                        item.put("作者ID", authorMatcher.group(2));
                    } else {
                        item.put("作者", "未知作者");
                        item.put("作者ID", "未知ID");
                    }
                    
                    results.add(item);
                    extracted++;
                }
                
                if (results.isEmpty()) {
                    return "{\"error\": \"获取到了页面，但未能提取到文章数据。可能原因：1.没有配置有效的Cookie被反爬拦截；2.小红书页面结构发生变化。请在 DataSpiderTool 中配置真实的 XHS_COOKIE。\"}";
                }
                
            } else {
                return "{\"error\": \"未能获取到真实数据。已被小红书反爬虫拦截（被重定向到了登录或验证码页）。请务必在 DataSpiderTool 代码中配置你自己在浏览器登录后的真实 Cookie。\"}";
            }
            
            return gson.toJson(results);
        } catch (Exception e) {
            log.error("抓取小红书发生异常", e);
            return "{\"error\": \"真实抓取发生网络或解析异常: " + e.getMessage() + "\"}";
        }
    }
    
    private String unescapeJavaString(String str) {
        if (str == null) return null;
        return str.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\u002F", "/");
    }
}