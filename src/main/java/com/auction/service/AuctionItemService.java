package com.auction.service;

import com.auction.entity.AuctionItem;
import com.auction.mapper.AuctionItemMapper;
import com.auction.service.MinioService;
import com.auction.service.SysConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ========================================
 * 拍品服务类（AuctionItemService）
 * ========================================
 * 功能说明：
 * 1. 处理拍品的业务逻辑（CRUD操作）
 * 2. 管理拍品的创建、更新、删除
 * 3. 处理拍品图片的上传和管理
 * 4. 验证拍品数据的完整性和有效性
 * 5. 解析和处理JSON格式的图片列表
 * 
 * 主要功能：
 * - createItem：创建新拍品，自动上传图片并设置默认值
 * - updateItem：更新拍品信息，支持更新图片
 * - deleteItem：删除拍品，同时删除关联的图片文件
 * - getItemById：根据ID查询单个拍品
 * - getItemList：查询拍品列表
 * - updateItemStatus：更新拍品状态（上架/下架）
 * 
 * 技术特点：
 * - 使用@Transactional注解确保数据一致性
 * - 集成MinIO对象存储服务管理图片
 * - 使用Jackson处理JSON格式的图片列表
 * - 完善的异常处理和日志记录
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j     // Lombok注解：自动生成log对象，用于日志记录
@Service   // Spring注解：标记为Service层组件，由Spring容器管理
public class AuctionItemService {

    /* ========================= 依赖注入 ========================= */
    
    @Autowired
    private AuctionItemMapper auctionItemMapper;  // 拍品数据访问对象

    @Autowired
    private MinioService minioService;  // MinIO对象存储服务，用于管理图片

    @Autowired
    private ObjectMapper objectMapper;  // Jackson对象，用于JSON序列化/反序列化

    @Autowired
    private SysConfigService sysConfigService;  // 系统配置服务

    @Autowired
    private CommonImageService commonImageService;  // 通用图片服务

    /**
     * 创建拍品
     * 
     * 功能说明：
     * 1. 上传拍品图片到MinIO
     * 2. 将图片URL列表序列化为JSON存储
     * 3. 设置拍品的默认值
     * 4. 验证拍品必须至少有一张图片
     * 5. 保存拍品到数据库
     * 
     * @param item 拍品对象，包含基本信息
     * @param imageFiles 拍品图片文件列表
     * @return 创建成功的拍品ID
     * @throws RuntimeException 创建失败时抛出异常
     */
    @Transactional  // 事务注解：确保方法内所有数据库操作要么全部成功，要么全部回滚
    public Long createItem(AuctionItem item, List<MultipartFile> imageFiles) {
        try {
            // 设置默认值
            setDefaultValues(item);

            // 上传图片
            if (imageFiles != null && !imageFiles.isEmpty()) {
                List<String> imageUrls = commonImageService.uploadItemImages(imageFiles);
                item.setImages(objectMapper.writeValueAsString(imageUrls));
                // 第一张图片作为详情图片（封面）
                if (!imageUrls.isEmpty()) {
                    item.setDetailImages(objectMapper.writeValueAsString(imageUrls));
                }
            }

            // 验证至少需要一张图片
            validateImages(item);
            // 最多张数限制
            try {
                List<String> imgs = objectMapper.readValue(item.getImages(), new TypeReference<List<String>>() {});
                commonImageService.validateMaxCount(imgs, commonImageService.getMaxItemImages(), "拍品");
            } catch (Exception ignore) {}

            // 简化流程：直接设为可上架/可用状态（2=通过/可用）
            item.setStatus(2);

            // 设置创建时间
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());

            // 插入数据库
            auctionItemMapper.insert(item);

            log.info("拍品创建成功: ID={}, 名称={}", item.getId(), item.getItemName());
            return item.getId();

        } catch (Exception e) {
            log.error("拍品创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("拍品创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍品（不更新图片）
     * 
     * 功能说明：
     * 1. 更新拍品的基本信息（名称、描述、价格等）
     * 2. 不上传新图片，保留原有图片
     * 3. 验证拍品必须至少有一张图片
     * 4. 自动更新修改时间
     * 
     * @param item 包含更新信息的拍品对象
     * @return true-更新成功，false-更新失败
     * @throws RuntimeException 更新失败时抛出异常
     */
    @Transactional
    public boolean updateItem(AuctionItem item) {
        try {
            // 验证至少需要一张图片
            validateImages(item);
            
            // 设置更新时间
            item.setUpdateTime(LocalDateTime.now());

            // 更新数据库
            int result = auctionItemMapper.update(item);
            
            if (result > 0) {
                log.info("拍品更新成功: ID={}, 名称={}", item.getId(), item.getItemName());
                return true;
            } else {
                log.warn("拍品更新失败: ID={}", item.getId());
                return false;
            }
        } catch (Exception e) {
            log.error("拍品更新失败: ID={}, 错误: {}", item.getId(), e.getMessage(), e);
            throw new RuntimeException("拍品更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍品（包含图片）
     * 
     * 功能说明：
     * 1. 更新拍品的基本信息
     * 2. 上传新的拍品图片
     * 3. 替换原有图片URL列表
     * 4. 自动设置第一张图片为详情封面图
     * 
     * @param item 包含更新信息的拍品对象
     * @param imageFiles 新的图片文件列表
     * @return true-更新成功，false-更新失败
     * @throws RuntimeException 更新失败时抛出异常
     */
    @Transactional
    public boolean updateItem(AuctionItem item, List<MultipartFile> imageFiles) {
        try {
            // 上传新图片
            if (imageFiles != null && !imageFiles.isEmpty()) {
                List<String> imageUrls = commonImageService.uploadItemImages(imageFiles);
                item.setImages(objectMapper.writeValueAsString(imageUrls));
                // 第一张图片作为详情图片（封面）
                if (!imageUrls.isEmpty()) {
                    item.setDetailImages(objectMapper.writeValueAsString(imageUrls));
                }
            }

            // 设置更新时间
            item.setUpdateTime(LocalDateTime.now());

            // 更新数据库
            int result = auctionItemMapper.update(item);
            
            if (result > 0) {
                log.info("拍品更新成功: ID={}, 名称={}", item.getId(), item.getItemName());
                return true;
            } else {
                log.warn("拍品更新失败: ID={}", item.getId());
                return false;
            }

        } catch (Exception e) {
            log.error("拍品更新失败: {}", e.getMessage(), e);
            throw new RuntimeException("拍品更新失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询拍品
     * 
     * 功能说明：
     * 1. 从数据库查询拍品详细信息
     * 2. 解析JSON格式的图片列表为List对象
     * 3. 方便前端直接使用
     * 
     * @param id 拍品ID
     * @return 拍品对象，不存在时返回null
     */
    public AuctionItem getItemById(Long id) {
        try {
            AuctionItem item = auctionItemMapper.selectById(id);
            if (item != null) {
                // 解析图片列表
                parseImageLists(item);
            }
            return item;
        } catch (Exception e) {
            log.error("查询拍品失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查询拍品列表
     * 
     * 功能说明：
     * 1. 根据条件查询拍品列表
     * 2. 支持多条件组合查询（名称、分类、状态等）
     * 3. 自动解析每个拍品的图片列表
     * 
     * @param item 查询条件对象，非空字段作为查询条件
     * @return 拍品列表，如果查询失败返回空列表
     */
    public List<AuctionItem> getItemList(AuctionItem item) {
        try {
            List<AuctionItem> items = auctionItemMapper.selectList(item);
            // 解析图片列表
            items.forEach(this::parseImageLists);
            return items;
        } catch (Exception e) {
            log.error("查询拍品列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 删除拍品
     * 
     * 功能说明：
     * 1. 从数据库删除拍品记录
     * 2. 从MinIO删除拍品的所有图片文件
     * 3. 使用事务确保数据一致性
     * 
     * 注意事项：
     * - 删除是物理删除，不可恢复
     * - 如果拍品已在拍卖会中，建议先从拍卖会移除
     * 
     * @param id 要删除的拍品ID
     * @return true-删除成功，false-删除失败
     * @throws RuntimeException 删除失败时抛出异常
     */
    @Transactional
    public boolean deleteItem(Long id) {
        try {
            // 获取拍品信息
            AuctionItem item = auctionItemMapper.selectById(id);
            if (item == null) {
                throw new RuntimeException("拍品不存在");
            }

            // 删除图片文件
            deleteItemImages(item);

            // 删除数据库记录
            int result = auctionItemMapper.deleteById(id);
            
            if (result > 0) {
                log.info("拍品删除成功: ID={}", id);
                return true;
            } else {
                log.warn("拍品删除失败: ID={}", id);
                return false;
            }

        } catch (Exception e) {
            log.error("拍品删除失败: ID={}, 错误: {}", id, e.getMessage(), e);
            throw new RuntimeException("拍品删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍品状态
     */
    @Transactional
    public boolean updateItemStatus(Long id, Integer status) {
        try {
            // 获取拍品信息
            AuctionItem item = auctionItemMapper.selectById(id);
            if (item == null) {
                throw new RuntimeException("拍品不存在");
            }

            // 更新状态
            item.setStatus(status);
            item.setUpdateTime(LocalDateTime.now());

            int result = auctionItemMapper.update(item);
            
            if (result > 0) {
                log.info("拍品状态更新成功: ID={}, 状态={}", id, status);
                return true;
            } else {
                log.warn("拍品状态更新失败: ID={}", id);
                return false;
            }

        } catch (Exception e) {
            log.error("拍品状态更新失败: ID={}, 错误: {}", id, e.getMessage(), e);
            throw new RuntimeException("拍品状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍品（支持图片上传）
     */
    @Transactional
    public boolean updateItemWithImages(Long id, String itemName, Long categoryId, String itemCode, Double startingPrice,
                                       Double estimatedPrice, String dimensions, String material,
                                       String description, Integer status, List<MultipartFile> imageFiles, String currentImages) {
        try {
            // 获取现有拍品信息
            AuctionItem existingItem = auctionItemMapper.selectById(id);
            if (existingItem == null) {
                throw new RuntimeException("拍品不存在");
            }

            // 更新基本信息
            existingItem.setItemName(itemName);
            existingItem.setCategoryId(categoryId);
            if (itemCode != null) existingItem.setItemCode(itemCode);
            existingItem.setStartingPrice(BigDecimal.valueOf(startingPrice));
            if (estimatedPrice != null) existingItem.setEstimatedPrice(BigDecimal.valueOf(estimatedPrice));
            if (dimensions != null) existingItem.setDimensions(dimensions);
            if (material != null) existingItem.setMaterial(material);
            existingItem.setDescription(description);
            existingItem.setStatus(status);
            existingItem.setUpdateTime(LocalDateTime.now());

            // 处理图片 - 使用前端传递的当前图片列表
            if (currentImages != null && !currentImages.trim().isEmpty()) {
                // 直接使用前端传递的当前图片列表
                existingItem.setImages(currentImages);
                // 第一张图片作为详情图片（封面）
                try {
                    List<String> currentImageUrls = objectMapper.readValue(currentImages, new TypeReference<List<String>>() {});
                    if (!currentImageUrls.isEmpty()) {
                        existingItem.setDetailImages(objectMapper.writeValueAsString(currentImageUrls));
                    }
                } catch (Exception e) {
                    log.warn("解析当前图片失败: {}", e.getMessage());
                }
            }
            
            // 如果有新上传的图片，添加到现有图片列表中
            if (imageFiles != null && !imageFiles.isEmpty()) {
                // 获取当前图片列表
                List<String> currentImageUrls = new ArrayList<>();
                if (existingItem.getImages() != null && !existingItem.getImages().isEmpty()) {
                    try {
                        currentImageUrls = objectMapper.readValue(existingItem.getImages(), new TypeReference<List<String>>() {});
                    } catch (Exception e) {
                        log.warn("解析当前图片失败: {}", e.getMessage());
                    }
                }
                
                // 上传新图片
                List<String> newImageUrls = commonImageService.uploadItemImages(imageFiles);
                
                // 合并当前图片和新图片
                currentImageUrls.addAll(newImageUrls);
                
                // 更新图片列表
                existingItem.setImages(objectMapper.writeValueAsString(currentImageUrls));
                // 第一张图片作为详情图片（封面）
                if (!currentImageUrls.isEmpty()) {
                    existingItem.setDetailImages(objectMapper.writeValueAsString(currentImageUrls));
                }
            }

            // 验证至少需要一张图片
            validateImages(existingItem);

            // 更新数据库
            int result = auctionItemMapper.update(existingItem);
            
            if (result > 0) {
                log.info("拍品更新成功: ID={}, 名称={}", id, itemName);
                return true;
            } else {
                log.warn("拍品更新失败: ID={}", id);
                return false;
            }

        } catch (Exception e) {
            log.error("拍品更新失败: ID={}, 错误: {}", id, e.getMessage(), e);
            throw new RuntimeException("拍品更新失败: " + e.getMessage());
        }
    }

    /**
     * 验证至少需要一张图片
     */
    private void validateImages(AuctionItem item) {
        if (item.getImages() == null || item.getImages().trim().isEmpty()) {
            throw new RuntimeException("拍品必须至少包含一张图片");
        }
        
        try {
            List<String> imageList = objectMapper.readValue(item.getImages(), new TypeReference<List<String>>() {});
            if (imageList == null || imageList.isEmpty()) {
                throw new RuntimeException("拍品必须至少包含一张图片");
            }
        } catch (Exception e) {
            throw new RuntimeException("拍品图片数据格式错误");
        }
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(AuctionItem item) {
        // 保证金比例和佣金比例由拍卖会设置，这里不设置默认值
        // 只设置其他默认值

        // 设置默认状态
        if (item.getStatus() == null) {
            item.setStatus(0); // 草稿
        }


        // 设置默认值
        if (item.getIsAuthentic() == null) {
            item.setIsAuthentic(0);
        }
        if (item.getIsFreeShipping() == null) {
            item.setIsFreeShipping(0);
        }
        if (item.getIsReturnable() == null) {
            item.setIsReturnable(0);
        }
        if (item.getCurrentPrice() == null) {
            item.setCurrentPrice(BigDecimal.ZERO);
        }
    }

    /**
     * 上传图片
     */
    private List<String> uploadImages(List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String url = minioService.uploadItemImage(file);
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }

    /**
     * 解析图片列表
     */
    private void parseImageLists(AuctionItem item) {
        try {
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                List<String> imageList = objectMapper.readValue(item.getImages(), new TypeReference<List<String>>() {});
                item.setImageList(imageList);
            }
        } catch (Exception e) {
            log.warn("解析图片列表失败: {}", e.getMessage());
        }
    }

    /**
     * 删除拍品图片
     */
    private void deleteItemImages(AuctionItem item) {
        try {
            if (item.getImageList() != null) {
                for (String imageUrl : item.getImageList()) {
                    // 从URL中提取对象名称
                    String objectName = extractObjectNameFromUrl(imageUrl);
                    if (objectName != null) {
                        minioService.deleteFile(objectName);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("删除拍品图片失败: {}", e.getMessage());
        }
    }

    /**
     * 从URL中提取对象名称
     */
    private String extractObjectNameFromUrl(String url) {
        try {
            // 假设URL格式为: http://localhost:9000/auction-images/auction/items/xxx.jpg
            if (url.contains("/auction-images/")) {
                return url.substring(url.indexOf("/auction-images/") + 16);
            }
        } catch (Exception e) {
            log.warn("提取对象名称失败: {}", e.getMessage());
        }
        return null;
    }
}
