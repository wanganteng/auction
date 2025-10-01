package com.auction.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件存储服务接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface FileStorageService {

    /**
     * 上传文件
     * 
     * @param file 文件
     * @param folder 文件夹路径
     * @return 文件URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 上传文件流
     * 
     * @param inputStream 文件流
     * @param fileName 文件名
     * @param folder 文件夹路径
     * @param contentType 内容类型
     * @return 文件URL
     */
    String uploadFile(InputStream inputStream, String fileName, String folder, String contentType);

    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     * @return 是否成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 批量删除文件
     * 
     * @param fileUrls 文件URL列表
     * @return 是否成功
     */
    boolean deleteFiles(List<String> fileUrls);

    /**
     * 获取文件下载URL
     * 
     * @param fileUrl 文件URL
     * @param expiry 过期时间（秒）
     * @return 下载URL
     */
    String getDownloadUrl(String fileUrl, int expiry);

    /**
     * 检查文件是否存在
     * 
     * @param fileUrl 文件URL
     * @return 是否存在
     */
    boolean fileExists(String fileUrl);

    /**
     * 获取文件信息
     * 
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    Object getFileInfo(String fileUrl);

    /**
     * 创建存储桶
     * 
     * @param bucketName 存储桶名称
     * @return 是否成功
     */
    boolean createBucket(String bucketName);

    /**
     * 检查存储桶是否存在
     * 
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    boolean bucketExists(String bucketName);

    /**
     * 删除存储桶
     * 
     * @param bucketName 存储桶名称
     * @return 是否成功
     */
    boolean deleteBucket(String bucketName);
}
