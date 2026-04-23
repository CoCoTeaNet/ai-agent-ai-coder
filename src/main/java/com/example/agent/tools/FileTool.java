package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件操作工具
 * 提供文件读写、目录操作等功能
 */
public class FileTool {

    private static final Logger log = LoggerFactory.getLogger(FileTool.class);
    private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容
     */
    @Tool("读取文件内容")
    public String readFile(@P("文件路径") String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return "错误：文件不存在";
            }
            if (!Files.isRegularFile(path)) {
                return "错误：不是有效的文件";
            }
            if (!Files.isReadable(path)) {
                return "错误：文件不可读";
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取文件失败: {}", filePath, e);
            return "错误：" + e.getMessage();
        }
    }

    /**
     * 写入文件内容
     * @param filePath 文件路径
     * @param content 内容
     * @param append 是否追加
     * @return 操作结果
     */
    @Tool("写入文件内容")
    public String writeFile(@P("文件路径") String filePath,
                           @P("内容") String content,
                           @P("是否追加（可选，默认为false）") boolean append) {
        try {
            Path path = Paths.get(filePath);
            // 确保目录存在
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, content, StandardOpenOption.CREATE,
                    append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING);
            return "文件写入成功";
        } catch (Exception e) {
            log.error("写入文件失败: {}", filePath, e);
            return "错误：" + e.getMessage();
        }
    }

    /**
     * 列出目录内容
     * @param directoryPath 目录路径
     * @return 目录内容列表
     */
    @Tool("列出目录内容")
    public String listDirectory(@P("目录路径") String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                return "错误：目录不存在";
            }
            if (!Files.isDirectory(path)) {
                return "错误：不是有效的目录";
            }
            if (!Files.isReadable(path)) {
                return "错误：目录不可读";
            }

            List<String> files = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    String type = Files.isDirectory(entry) ? "[目录]" : "[文件]";
                    String size = Files.isRegularFile(entry) ? Files.size(entry) + " bytes" : "";
                    files.add(type + " " + entry.getFileName() + (size.isEmpty() ? "" : " (" + size + ")"));
                }
            }
            return String.join("\n", files);
        } catch (Exception e) {
            log.error("列出目录失败: {}", directoryPath, e);
            return "错误：" + e.getMessage();
        }
    }

    /**
     * 创建目录
     * @param directoryPath 目录路径
     * @return 操作结果
     */
    @Tool("创建目录")
    public String createDirectory(@P("目录路径") String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            Files.createDirectories(path);
            return "目录创建成功";
        } catch (Exception e) {
            log.error("创建目录失败: {}", directoryPath, e);
            return "错误：" + e.getMessage();
        }
    }

    /**
     * 删除文件或目录
     * @param path 路径
     * @return 操作结果
     */
    @Tool("删除文件或目录")
    public String deleteFile(@P("文件或目录路径") String path) {
        try {
            Path target = Paths.get(path);
            if (!Files.exists(target)) {
                return "错误：路径不存在";
            }
            if (Files.isDirectory(target)) {
                Files.walk(target)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (Exception e) {
                                log.error("删除失败: {}", p, e);
                            }
                        });
            } else {
                Files.delete(target);
            }
            return "删除成功";
        } catch (Exception e) {
            log.error("删除失败: {}", path, e);
            return "错误：" + e.getMessage();
        }
    }

    /**
     * 获取文件信息
     * @param filePath 文件路径
     * @return 文件信息
     */
    @Tool("获取文件信息")
    public String getFileInfo(@P("文件路径") String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return "错误：文件不存在";
            }

            StringBuilder info = new StringBuilder();
            info.append("路径：").append(path.toAbsolutePath()).append("\n");
            info.append("类型：").append(Files.isDirectory(path) ? "目录" : "文件").append("\n");
            if (Files.isRegularFile(path)) {
                info.append("大小：").append(Files.size(path)).append(" bytes\n");
            }
            info.append("存在：是\n");
            info.append("可读：").append(Files.isReadable(path)).append("\n");
            info.append("可写：").append(Files.isWritable(path)).append("\n");
            info.append("最后修改时间：").append(Files.getLastModifiedTime(path)).append("\n");
            return info.toString();
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", filePath, e);
            return "错误：" + e.getMessage();
        }
    }

    /**
     * 复制文件或目录
     * @param sourcePath 源路径
     * @param targetPath 目标路径
     * @return 操作结果
     */
    @Tool("复制文件或目录")
    public String copyFile(@P("源路径") String sourcePath, @P("目标路径") String targetPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            if (!Files.exists(source)) {
                return "错误：源路径不存在";
            }

            if (Files.isDirectory(source)) {
                for (Path sourcePath1 : Files.walk(source).toList()) {
                    try {
                        Path targetPath1 = target.resolve(source.relativize(sourcePath1));
                        if (Files.isDirectory(sourcePath1)) {
                            Files.createDirectories(targetPath1);
                        } else {
                            Files.copy(sourcePath1, targetPath1,
                                    StandardCopyOption.REPLACE_EXISTING,
                                    StandardCopyOption.COPY_ATTRIBUTES);
                        }
                    } catch (Exception e) {
                        log.error("复制失败: {}", sourcePath1, e);
                    }
                }
            } else {
                if (Files.isDirectory(target)) {
                    target = target.resolve(source.getFileName());
                }
                if (target.getParent() != null) {
                    Files.createDirectories(target.getParent());
                }
                Files.copy(source, target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
            }
            return "复制成功";
        } catch (Exception e) {
            log.error("复制失败: {} -> {}", sourcePath, targetPath, e);
            return "错误：" + e.getMessage();
        }
    }

    /**
     * 移动文件或目录
     * @param sourcePath 源路径
     * @param targetPath 目标路径
     * @return 操作结果
     */
    @Tool("移动文件或目录")
    public String moveFile(@P("源路径") String sourcePath, @P("目标路径") String targetPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            if (!Files.exists(source)) {
                return "错误：源路径不存在";
            }

            if (Files.isDirectory(target)) {
                target = target.resolve(source.getFileName());
            }
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return "移动成功";
        } catch (Exception e) {
            log.error("移动失败: {} -> {}", sourcePath, targetPath, e);
            return "错误：" + e.getMessage();
        }
    }
}
