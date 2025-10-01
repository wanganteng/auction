package com.auction.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonImageService {

    private final ObjectMapper objectMapper;
    private final MinioService minioService;
    private final SysConfigService sysConfigService;

    public List<String> parseImagesJson(String imagesJson) {
        if (imagesJson == null || imagesJson.trim().isEmpty()) return new ArrayList<>();
        try {
            List<String> list = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            log.warn("解析图片JSON失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public String toImagesJson(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images != null ? images : Collections.emptyList());
        } catch (Exception e) {
            throw new RuntimeException("图片列表序列化失败: " + e.getMessage());
        }
    }

    public List<String> uploadItemImages(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        if (files == null || files.isEmpty()) return urls;
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String url = minioService.uploadItemImage(f);
            urls.add(url);
        }
        return urls;
    }

    public List<String> uploadSessionImages(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        if (files == null || files.isEmpty()) return urls;
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String url = minioService.uploadSessionCoverImage(f);
            urls.add(url);
        }
        return urls;
    }

    public int getMaxItemImages() {
        return sysConfigService.getIntConfigValue("upload.max.item.images", 5);
    }

    public int getMaxSessionImages() {
        return sysConfigService.getIntConfigValue("upload.max.session.images", 1);
    }

    public void validateMinCount(List<String> images, int minCount, String what) {
        if (images == null || images.size() < minCount) {
            throw new RuntimeException(what + "至少需要" + minCount + "张图片");
        }
    }

    public void validateMaxCount(List<String> images, int maxCount, String what) {
        if (images != null && images.size() > maxCount) {
            throw new RuntimeException(what + "最多只能上传" + maxCount + "张图片");
        }
    }
}


