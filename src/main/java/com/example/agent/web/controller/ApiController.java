package com.example.agent.web.controller;

import com.example.agent.services.AgentService;
import com.example.agent.services.ExecutionTrace;
import com.example.agent.tools.FileTool;
import com.example.agent.tools.NetworkTool;
import com.example.agent.tools.SearchTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * REST API 控制器
 * 提供 Agent 的 REST 接口
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
    private final AgentService agentService;

    @Autowired
    public ApiController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 创建会话
     */
    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> createSession() {
        try {
            String sessionId = agentService.createSession();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * SSE 流式消息发送
     */
    @PostMapping("/chat/stream")
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Gson gson = new GsonBuilder().create();

        executor.execute(() -> {
            try {
                String sessionId = request.getSessionId();
                String message = request.getMessage();

                // 处理附件内容 - 将附件内容追加到消息中
                if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                    StringBuilder messageBuilder = new StringBuilder(message != null ? message : "");

                    for (Map<String, Object> attachment : request.getAttachments()) {
                        String filePath = (String) attachment.get("path");
                        if (filePath != null) {
                            File file = new File(filePath);
                            if (file.exists() && file.isFile()) {
                                try {
                                    String fileContent = readFileContent(file);
                                    String fileName = (String) attachment.get("name");
                                    messageBuilder.append("\n\n=== 附件内容: ").append(fileName).append(" ===\n");
                                    messageBuilder.append(fileContent);
                                } catch (IOException e) {
                                    log.error("读取文件内容失败: " + filePath, e);
                                }
                            }
                        }
                    }

                    message = messageBuilder.toString();
                }

                if (message == null || message.trim().isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("error", "消息不能为空");
                    emitter.send(SseEmitter.event().data(gson.toJson(error)));
                    emitter.complete();
                    return;
                }

                if (sessionId == null || sessionId.trim().isEmpty()) {
                    sessionId = agentService.createSession();
                }

                // 获取执行链路
                java.util.List<ExecutionTrace.TraceStepDTO> trace = agentService.getSessionTrace(sessionId);

                // 分块发送消息
                StringBuilder fullResponse = new StringBuilder();
                String responseMessage = agentService.sendMessage(sessionId, message);

                // 更稳定的流式发送：每 100 个字符发送一次，或者在完整的句子/段落时发送
                int chunkSize = 100; // 每 100 个字符发送一次
                for (int i = 0; i < responseMessage.length(); i += chunkSize) {
                    int endIndex = Math.min(i + chunkSize, responseMessage.length());
                    String chunk = responseMessage.substring(i, endIndex);

                    Map<String, Object> data = new HashMap<>();
                    data.put("success", true);
                    data.put("sessionId", sessionId);
                    data.put("chunk", chunk);
                    data.put("done", endIndex == responseMessage.length());
                    if (endIndex == responseMessage.length()) {
                        data.put("trace", trace);
                    }
                    // 使用 Gson 序列化，确保 JSON 正确转义
                    String jsonData = gson.toJson(data);
                    emitter.send(SseEmitter.event().data(jsonData));
                    Thread.sleep(50); // 模拟延迟
                }

                // 发送剩余内容
                if (fullResponse.length() > 0) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("success", true);
                    data.put("sessionId", sessionId);
                    data.put("chunk", fullResponse.toString());
                    data.put("done", true);
                    data.put("trace", trace);
                    emitter.send(SseEmitter.event().data(gson.toJson(data)));
                }

                emitter.complete();
            } catch (Exception e) {
                log.error("SSE 发送失败", e);
                emitter.completeWithError(e);
            } finally {
                executor.shutdown();
            }
        });

        return emitter;
    }

    /**
     * 发送消息（非流式）
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest request) {
        try {
            String sessionId = request.getSessionId();
            String message = request.getMessage();

            // 处理附件内容 - 将附件内容追加到消息中
            if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                StringBuilder messageBuilder = new StringBuilder(message != null ? message : "");

                for (Map<String, Object> attachment : request.getAttachments()) {
                    String filePath = (String) attachment.get("path");
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists() && file.isFile()) {
                            try {
                                String fileContent = readFileContent(file);
                                String fileName = (String) attachment.get("name");
                                messageBuilder.append("\n\n=== 附件内容: ").append(fileName).append(" ===\n");
                                messageBuilder.append(fileContent);
                            } catch (IOException e) {
                                log.error("读取文件内容失败: " + filePath, e);
                            }
                        }
                    }
                }

                message = messageBuilder.toString();
            }

            if (message == null || message.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "消息不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = agentService.createSession();
            }

            String responseMessage = agentService.sendMessage(sessionId, message);

            // 获取执行链路
            java.util.List<ExecutionTrace.TraceStepDTO> trace = agentService.getSessionTrace(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("response", responseMessage);
            response.put("trace", trace);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 执行任务
     */
    @PostMapping("/task")
    public ResponseEntity<Map<String, Object>> executeTask(@RequestBody TaskRequest request) {
        try {
            String sessionId = request.getSessionId();
            String task = request.getTask();

            if (task == null || task.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "任务不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = agentService.createSession();
            }

            String result = agentService.executeTask(sessionId, task);

            java.util.List<ExecutionTrace.TraceStepDTO> trace = agentService.getSessionTrace(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("result", result);
            response.put("trace", trace);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取会话状态
     */
    @GetMapping("/session/{sessionId}/status")
    public ResponseEntity<Map<String, Object>> getSessionStatus(@PathVariable String sessionId) {
        try {
            String status = agentService.getSessionStatus(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 重置会话
     */
    @PostMapping("/session/{sessionId}/reset")
    public ResponseEntity<Map<String, Object>> resetSession(@PathVariable String sessionId) {
        try {
            agentService.resetSession(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "会话已重置");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String sessionId) {
        try {
            agentService.deleteSession(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "会话已删除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取可用工具列表
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getAvailableTools(@RequestParam(required = false) String sessionId) {
        try {
            List<String> tools = agentService.getAvailableTools(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tools", tools);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取可用技能列表
     */
    @GetMapping("/skills")
    public ResponseEntity<Map<String, Object>> getAvailableSkills(@RequestParam(required = false) String sessionId) {
        try {
            List<String> skills = agentService.getAvailableSkills(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("skills", skills);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取技能统计信息
     */
    @GetMapping("/skills/stats")
    public ResponseEntity<Map<String, Object>> getSkillStats(@RequestParam(required = false) String sessionId) {
        try {
            int enabledCount = agentService.getEnabledSkillCount(sessionId);
            int totalCount = agentService.getTotalSkillCount(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("enabledSkills", enabledCount);
            response.put("totalSkills", totalCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeSessions", agentService.getActiveSessionCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 文件上传功能
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file,
                                                          @RequestParam(required = false) String sessionId) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 保存文件到临时目录
            String fileName = file.getOriginalFilename();
            String fileId = "file_" + System.currentTimeMillis() + "_" + fileName;
            String uploadPath = System.getProperty("java.io.tmpdir") + File.separator + "agent_uploads";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String filePath = uploadPath + File.separator + fileId;
            File dest = new File(filePath);
            file.transferTo(dest);

            // 返回文件信息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("id", fileId);
            fileInfo.put("name", fileName);
            fileInfo.put("size", file.getSize());
            fileInfo.put("type", file.getContentType());
            fileInfo.put("path", filePath);
            fileInfo.put("url", "/api/v1/files/" + fileId); // 下载URL

            // 保存到会话（如果有）
            if (sessionId != null && !sessionId.trim().isEmpty()) {
                agentService.addAttachment(sessionId, fileInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileInfo", fileInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 文件下载功能
     */
    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        try {
            String uploadPath = System.getProperty("java.io.tmpdir") + File.separator + "agent_uploads";
            String fileName = fileId;
            if (fileId.startsWith("file_")) {
                // 提取原始文件名
                fileName = fileId.substring(fileId.indexOf("_") + 1);
                int secondUnderscore = fileName.indexOf("_");
                if (secondUnderscore > 0) {
                    fileName = fileName.substring(secondUnderscore + 1);
                }
            }

            File file = new File(uploadPath + File.separator + fileId);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get(file.getAbsolutePath());
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("文件下载失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 读取文件内容
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            int lineCount = 0;
            int maxLines = 1000; // 限制最大行数，避免超大文件
            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                content.append(line).append("\n");
                lineCount++;
            }
            if (reader.readLine() != null) {
                content.append("\n... (文件内容已截断，只显示前 ").append(maxLines).append(" 行)");
            }
        }
        return content.toString();
    }

    /**
     * 搜索功能
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestBody SearchRequest request) {
        try {
            String query = request.getQuery();

            if (query == null || query.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "搜索词不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            SearchTool searchTool = new SearchTool();
            String result = searchTool.search(query);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 文件操作
     */
    @PostMapping("/file/read")
    public ResponseEntity<Map<String, Object>> readFile(@RequestBody FileReadRequest request) {
        try {
            String filePath = request.getFilePath();

            if (filePath == null || filePath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "文件路径不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            FileTool fileTool = new FileTool();
            String content = fileTool.readFile(filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", content);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 文件写入
     */
    @PostMapping("/file/write")
    public ResponseEntity<Map<String, Object>> writeFile(@RequestBody FileWriteRequest request) {
        try {
            String filePath = request.getFilePath();
            String content = request.getContent();

            if (filePath == null || filePath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "文件路径不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            FileTool fileTool = new FileTool();
            fileTool.writeFile(filePath, content, request.isAppend());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "文件写入成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 网络请求
     */
    @PostMapping("/network/get")
    public ResponseEntity<Map<String, Object>> getRequest(@RequestBody NetworkRequest request) {
        try {
            String url = request.getUrl();

            if (url == null || url.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "URL不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            NetworkTool networkTool = new NetworkTool();
            String result = networkTool.get(url);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 网络请求（POST）
     */
    @PostMapping("/network/post")
    public ResponseEntity<Map<String, Object>> postRequest(@RequestBody NetworkPostRequest request) {
        try {
            String url = request.getUrl();
            String body = request.getBody();

            if (url == null || url.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "URL不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            NetworkTool networkTool = new NetworkTool();
            String result = networkTool.post(url, body, request.getContentType());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 内部静态类
    public static class ChatRequest {
        private String sessionId;
        private String message;
        private java.util.List<Map<String, Object>> attachments;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public java.util.List<Map<String, Object>> getAttachments() {
            return attachments;
        }

        public void setAttachments(java.util.List<Map<String, Object>> attachments) {
            this.attachments = attachments;
        }
    }

    public static class TaskRequest {
        private String sessionId;
        private String task;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task;
        }
    }

    public static class SearchRequest {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    public static class FileReadRequest {
        private String filePath;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }

    public static class FileWriteRequest {
        private String filePath;
        private String content;
        private boolean append;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isAppend() {
            return append;
        }

        public void setAppend(boolean append) {
            this.append = append;
        }
    }

    public static class NetworkRequest {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class NetworkPostRequest {
        private String url;
        private String body;
        private String contentType;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }
}
