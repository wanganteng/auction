package com.auction.service.impl;

import com.auction.config.MinIOConfig;
import com.auction.service.FileStorageService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件存储服务实现类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinIOConfig minioConfig;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 检查存储桶是否存在
            if (!bucketExists(minioConfig.getBucketName())) {
                createBucket(minioConfig.getBucketName());
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = generateFileName(extension);
            String objectName = folder + "/" + fileName;

            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // 返回文件URL
            String fileUrl = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("文件上传时发生错误: {}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String folder, String contentType) {
        try {
            // 检查存储桶是否存在
            if (!bucketExists(minioConfig.getBucketName())) {
                createBucket(minioConfig.getBucketName());
            }

            // 生成文件名
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String generatedFileName = generateFileName(extension);
            String objectName = folder + "/" + generatedFileName;

            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, -1, 10485760) // 10MB
                    .contentType(contentType)
                    .build()
            );

            // 返回文件URL
            String fileUrl = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("文件上传时发生错误: {}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            if (objectName == null) {
                log.error("无效的文件URL: {}", fileUrl);
                return false;
            }

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build()
            );

            log.info("文件删除成功: {}", fileUrl);
            return true;

        } catch (Exception e) {
            log.error("删除文件时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteFiles(List<String> fileUrls) {
        try {
            List<String> objectNames = new ArrayList<>();
            for (String fileUrl : fileUrls) {
                String objectName = extractObjectName(fileUrl);
                if (objectName != null) {
                    objectNames.add(objectName);
                }
            }

            if (objectNames.isEmpty()) {
                return true;
            }

            minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .objects(objectNames.stream().map(DeleteObject::new).collect(java.util.stream.Collectors.toList()))
                    .build()
            );

            log.info("文件批量删除成功: {}", fileUrls.size());
            return true;

        } catch (Exception e) {
            log.error("批量删除文件时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getDownloadUrl(String fileUrl, int expiry) {
        try {
            String objectName = extractObjectName(fileUrl);
            if (objectName == null) {
                log.error("无效的文件URL: {}", fileUrl);
                return null;
            }

            String downloadUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(expiry)
                    .build()
            );

            return downloadUrl;

        } catch (Exception e) {
            log.error("获取下载URL时发生错误: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            if (objectName == null) {
                return false;
            }

            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build()
            );

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object getFileInfo(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            if (objectName == null) {
                return null;
            }

            StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build()
            );

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("objectName", objectName);
            fileInfo.put("size", stat.size());
            fileInfo.put("contentType", stat.contentType());
            fileInfo.put("lastModified", stat.lastModified());
            fileInfo.put("etag", stat.etag());

            return fileInfo;

        } catch (Exception e) {
            log.error("获取文件信息时发生错误: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean createBucket(String bucketName) {
        try {
            boolean exists = bucketExists(bucketName);
            if (exists) {
                log.info("存储桶已存在: {}", bucketName);
                return true;
            }

            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            );

            log.info("存储桶创建成功: {}", bucketName);
            return true;

        } catch (Exception e) {
            log.error("创建存储桶时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
        } catch (Exception e) {
            log.error("检查存储桶是否存在时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteBucket(String bucketName) {
        try {
            boolean exists = bucketExists(bucketName);
            if (!exists) {
                log.info("存储桶不存在: {}", bucketName);
                return true;
            }

            minioClient.removeBucket(
                RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            );

            log.info("存储桶删除成功: {}", bucketName);
            return true;

        } catch (Exception e) {
            log.error("删除存储桶时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 生成文件名
     * 
     * @param extension 文件扩展名
     * @return 文件名
     */
    private String generateFileName(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }

    /**
     * 从文件URL中提取对象名称
     * 
     * @param fileUrl 文件URL
     * @return 对象名称
     */
    private String extractObjectName(String fileUrl) {
        try {
            String bucketPrefix = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/";
            if (fileUrl.startsWith(bucketPrefix)) {
                return fileUrl.substring(bucketPrefix.length());
            }
            return null;
        } catch (Exception e) {
            log.error("从URL提取对象名称时发生错误: {}", fileUrl);
            return null;
        }
    }
}
