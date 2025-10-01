package com.auction.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO服务类
 * 处理文件上传、下载、删除等操作
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 上传文件到MinIO
     * 
     * @param file 文件
     * @param path 存储路径
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 检查文件类型
            if (!isAllowedImageType(file)) {
                throw new RuntimeException("不支持的文件类型");
            }

            // 检查文件大小
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                throw new RuntimeException("文件大小超过限制");
            }

            // 生成唯一文件名
            String fileName = generateFileName(file.getOriginalFilename());
            String objectName = path + fileName;

            // 确保存储桶存在
            ensureBucketExists();

            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // 返回文件URL
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传拍品图片
     * 
     * @param file 图片文件
     * @return 图片URL
     */
    public String uploadItemImage(MultipartFile file) {
        return uploadFile(file, "auction/items/");
    }

    /**
     * 上传拍卖会封面图片
     * 
     * @param file 图片文件
     * @return 图片URL
     */
    public String uploadSessionCoverImage(MultipartFile file) {
        return uploadFile(file, "auction/sessions/");
    }

    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件URL
     * 
     * @param objectName 对象名称
     * @return 文件URL
     */
    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(60 * 60 * 24 * 7) // 7天有效期
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                log.info("创建存储桶: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查/创建存储桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储桶操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件类型是否允许
     * 
     * @param file 文件
     * @return 是否允许
     */
    private boolean isAllowedImageType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        String[] allowedTypes = {"jpg", "jpeg", "png", "gif", "webp"};
        for (String allowedType : allowedTypes) {
            if (allowedType.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成唯一文件名
     * 
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        return timestamp + "_" + uuid + extension;
    }
}
