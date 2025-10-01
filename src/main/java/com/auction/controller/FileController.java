package com.auction.controller;

import com.auction.common.Result;
import com.auction.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 * 处理文件上传、下载、删除等操作
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@Tag(name = "文件管理", description = "文件上传下载相关接口")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 上传单个文件
     * 
     * @param file 文件
     * @param folder 文件夹路径
     * @return 上传结果
     */
    @PostMapping("/upload")
    @Operation(summary = "上传单个文件", description = "上传单个文件到MinIO存储")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "common") String folder) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件大小（限制10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (!isAllowedFileType(contentType)) {
                return Result.error("不支持的文件类型");
            }

            String fileUrl = fileStorageService.uploadFile(file, folder);
            
            Map<String, Object> data = new HashMap<>();
            data.put("fileUrl", fileUrl);
            data.put("fileName", file.getOriginalFilename());
            data.put("fileSize", file.getSize());
            data.put("contentType", contentType);
            data.put("folder", folder);

            return Result.success("文件上传成功", data);

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     * 
     * @param files 文件列表
     * @param folder 文件夹路径
     * @return 上传结果
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件到MinIO存储")
    public Result<Map<String, Object>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(defaultValue = "common") String folder) {
        try {
            if (files == null || files.length == 0) {
                return Result.error("文件不能为空");
            }

            if (files.length > 10) {
                return Result.error("一次最多上传10个文件");
            }

            List<Map<String, Object>> uploadedFiles = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                try {
                    if (file.isEmpty()) {
                        failedFiles.add(file.getOriginalFilename() + " (文件为空)");
                        continue;
                    }

                    // 检查文件大小
                    if (file.getSize() > 10 * 1024 * 1024) {
                        failedFiles.add(file.getOriginalFilename() + " (文件过大)");
                        continue;
                    }

                    // 检查文件类型
                    String contentType = file.getContentType();
                    if (!isAllowedFileType(contentType)) {
                        failedFiles.add(file.getOriginalFilename() + " (不支持的文件类型)");
                        continue;
                    }

                    String fileUrl = fileStorageService.uploadFile(file, folder);
                    
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("fileUrl", fileUrl);
                    fileInfo.put("fileName", file.getOriginalFilename());
                    fileInfo.put("fileSize", file.getSize());
                    fileInfo.put("contentType", contentType);
                    
                    uploadedFiles.add(fileInfo);

                } catch (Exception e) {
                    log.error("上传文件 {} 时发生错误: {}", file.getOriginalFilename(), e.getMessage());
                    failedFiles.add(file.getOriginalFilename() + " (上传失败)");
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("uploadedFiles", uploadedFiles);
            data.put("failedFiles", failedFiles);
            data.put("totalCount", files.length);
            data.put("successCount", uploadedFiles.size());
            data.put("failedCount", failedFiles.size());

            return Result.success("批量上传完成", data);

        } catch (Exception e) {
            log.error("批量文件上传失败: {}", e.getMessage());
            return Result.error("批量上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "从MinIO存储中删除指定文件")
    public Result<String> deleteFile(@RequestParam String fileUrl) {
        try {
            if (fileStorageService.deleteFile(fileUrl)) {
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件删除失败");
            }

        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     * 
     * @param fileUrls 文件URL列表
     * @return 删除结果
     */
    @DeleteMapping("/delete/batch")
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    public Result<String> deleteFiles(@RequestBody List<String> fileUrls) {
        try {
            if (fileUrls == null || fileUrls.isEmpty()) {
                return Result.error("文件URL列表不能为空");
            }

            if (fileStorageService.deleteFiles(fileUrls)) {
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件删除失败");
            }

        } catch (Exception e) {
            log.error("批量文件删除失败: {}", e.getMessage());
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件下载URL
     * 
     * @param fileUrl 文件URL
     * @param expiry 过期时间（秒，默认1小时）
     * @return 下载URL
     */
    @GetMapping("/download-url")
    @Operation(summary = "获取文件下载URL", description = "获取文件的临时下载链接")
    public Result<Map<String, Object>> getDownloadUrl(
            @RequestParam String fileUrl,
            @RequestParam(defaultValue = "3600") int expiry) {
        try {
            String downloadUrl = fileStorageService.getDownloadUrl(fileUrl, expiry);
            if (downloadUrl != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("downloadUrl", downloadUrl);
                data.put("expiry", expiry);
                return Result.success("获取成功", data);
            } else {
                return Result.error("获取下载URL失败");
            }

        } catch (Exception e) {
            log.error("获取下载URL失败: {}", e.getMessage());
            return Result.error("获取下载URL失败：" + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     * 
     * @param fileUrl 文件URL
     * @return 检查结果
     */
    @GetMapping("/exists")
    @Operation(summary = "检查文件是否存在", description = "检查指定文件是否存在于MinIO存储中")
    public Result<Map<String, Object>> fileExists(@RequestParam String fileUrl) {
        try {
            boolean exists = fileStorageService.fileExists(fileUrl);
            Map<String, Object> data = new HashMap<>();
            data.put("exists", exists);
            data.put("fileUrl", fileUrl);
            return Result.success("检查完成", data);

        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", e.getMessage());
            return Result.error("检查文件失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     * 
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取文件信息", description = "获取文件的详细信息")
    public Result<Object> getFileInfo(@RequestParam String fileUrl) {
        try {
            Object fileInfo = fileStorageService.getFileInfo(fileUrl);
            if (fileInfo != null) {
                return Result.success("获取成功", fileInfo);
            } else {
                return Result.error("获取文件信息失败");
            }

        } catch (Exception e) {
            log.error("获取文件信息失败: {}", e.getMessage());
            return Result.error("获取文件信息失败：" + e.getMessage());
        }
    }

    /**
     * 检查文件类型是否允许
     * 
     * @param contentType 内容类型
     * @return 是否允许
     */
    private boolean isAllowedFileType(String contentType) {
        if (contentType == null) {
            return false;
        }

        // 允许的文件类型
        String[] allowedTypes = {
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain", "text/csv"
        };

        for (String allowedType : allowedTypes) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }

        return false;
    }
}
