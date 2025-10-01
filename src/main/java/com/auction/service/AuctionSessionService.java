package com.auction.service;

import com.auction.entity.AuctionItem;
import com.auction.entity.AuctionSession;
import com.auction.mapper.AuctionItemMapper;
import com.auction.mapper.AuctionSessionItemMapper;
import com.auction.mapper.AuctionSessionMapper;
import com.auction.service.MinioService;
import com.auction.service.RedisService;
import com.auction.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 拍卖会服务类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class AuctionSessionService {

    @Autowired
    private AuctionSessionMapper auctionSessionMapper;

    @Autowired
    private AuctionItemMapper auctionItemMapper;

    @Autowired
    private MinioService minioService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private AuctionSessionItemMapper auctionSessionItemMapper;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private CommonImageService commonImageService;

    /**
     * 创建拍卖会
     */
    @Transactional
    public Long createSession(AuctionSession session, List<Long> itemIds, MultipartFile coverImageFile) {
        try {
            // 设置默认值
            setDefaultValues(session);

            // 上传封面图片（必须有至少一张）
            if (coverImageFile != null && !coverImageFile.isEmpty()) {
                String coverImageUrl = minioService.uploadSessionCoverImage(coverImageFile);
                session.setCoverImage(coverImageUrl);
                session.setImages(java.util.Arrays.asList(coverImageUrl).toString());
            } else {
                throw new RuntimeException("请上传至少一张拍卖会图片");
            }

            // 设置拍品数量
            session.setTotalItems(itemIds != null ? itemIds.size() : 0);
            session.setSoldItems(0);
            session.setViewCount(0);

            // 设置创建时间
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());

            // 插入拍卖会
            auctionSessionMapper.insert(session);

            // 前置校验：排除已在未开始/进行中的拍卖会中的拍品
            if (itemIds != null && !itemIds.isEmpty()) {
                List<Long> conflicts = auctionSessionItemMapper.findConflictingItemIds(itemIds);
                if (conflicts != null && !conflicts.isEmpty()) {
                    throw new RuntimeException("部分拍品已在其他未开始/进行中的拍卖会中: " + conflicts);
                }
                // 建立关联
                auctionSessionItemMapper.batchInsert(session.getId(), itemIds);
            }

            log.info("拍卖会创建成功: ID={}, 名称={}", session.getId(), session.getSessionName());
            return session.getId();

        } catch (Exception e) {
            log.error("拍卖会创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("拍卖会创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍卖会
     */
    @Transactional
    public boolean updateSession(AuctionSession session, List<Long> itemIds, MultipartFile coverImageFile) {
        try {
            // 上传新封面图片（如果提供）
            if (coverImageFile != null && !coverImageFile.isEmpty()) {
                String coverImageUrl = minioService.uploadSessionCoverImage(coverImageFile);
                session.setCoverImage(coverImageUrl);
            }

            // 更新拍品数量
            if (itemIds != null) {
                session.setTotalItems(itemIds.size());
            }

            // 设置更新时间
            session.setUpdateTime(LocalDateTime.now());

            // 更新数据库
            int result = auctionSessionMapper.update(session);
            
            if (result <= 0) {
                log.warn("拍卖会更新失败: ID={}", session.getId());
                return false;
            }

            // 重建拍品关联（如果提供了itemIds）
            if (itemIds != null) {
                // 校验冲突
                List<Long> conflicts = auctionSessionItemMapper.findConflictingItemIds(itemIds);
                if (conflicts != null && !conflicts.isEmpty()) {
                    throw new RuntimeException("部分拍品已在其他未开始/进行中的拍卖会中: " + conflicts);
                }
                auctionSessionItemMapper.deleteBySessionId(session.getId());
                if (!itemIds.isEmpty()) {
                    auctionSessionItemMapper.batchInsert(session.getId(), itemIds);
                }
            }

            log.info("拍卖会更新成功: ID={}, 名称={}", session.getId(), session.getSessionName());
            return true;

        } catch (Exception e) {
            log.error("拍卖会更新失败: {}", e.getMessage(), e);
            throw new RuntimeException("拍卖会更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新拍卖会（含多图）
     */
    @Transactional
    public boolean updateSessionWithImages(AuctionSession session, List<MultipartFile> coverImages, String currentImagesJson, List<Long> itemIds) {
        try {
            List<String> finalImages = new ArrayList<>(commonImageService.parseImagesJson(currentImagesJson));
            if (coverImages != null) {
                for (MultipartFile f : coverImages) {
                    if (f != null && !f.isEmpty()) {
                        String url = minioService.uploadSessionCoverImage(f);
                        finalImages.add(url);
                    }
                }
            }
            // 校验最少/最多数量
            if (finalImages.isEmpty()) throw new RuntimeException("请至少上传一张会场图片");
            commonImageService.validateMaxCount(finalImages, commonImageService.getMaxSessionImages(), "拍卖会");
            // 设定封面与图片列表
            session.setCoverImage(finalImages.get(0));
            session.setImages(objectMapper.writeValueAsString(finalImages));
            session.setUpdateTime(LocalDateTime.now());

            int updated = auctionSessionMapper.update(session);
            if (updated <= 0) return false;

            // 重建拍品关联（可选）
            if (itemIds != null) {
                List<Long> conflicts = auctionSessionItemMapper.findConflictingItemIds(itemIds);
                if (conflicts != null && !conflicts.isEmpty()) {
                    throw new RuntimeException("部分拍品已在其他未开始/进行中的拍卖会中: " + conflicts);
                }
                auctionSessionItemMapper.deleteBySessionId(session.getId());
                if (!itemIds.isEmpty()) auctionSessionItemMapper.batchInsert(session.getId(), itemIds);
            }
            return true;
        } catch (Exception e) {
            log.error("更新拍卖会(含多图)失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新失败: " + e.getMessage());
        }
    }

    /**
     * 创建拍卖会（含多图）
     */
    @Transactional
    public Long createSessionWithImages(AuctionSession session, List<MultipartFile> coverImages, String currentImagesJson, List<Long> itemIds) {
        try {
            setDefaultValues(session);

            List<String> finalImages = new ArrayList<>(commonImageService.parseImagesJson(currentImagesJson));
            if (coverImages != null) {
                for (MultipartFile f : coverImages) {
                    if (f != null && !f.isEmpty()) {
                        String url = minioService.uploadSessionCoverImage(f);
                        finalImages.add(url);
                    }
                }
            }
            if (finalImages.isEmpty()) throw new RuntimeException("请至少上传一张会场图片");
            commonImageService.validateMaxCount(finalImages, commonImageService.getMaxSessionImages(), "拍卖会");
            session.setCoverImage(finalImages.get(0));
            session.setImages(objectMapper.writeValueAsString(finalImages));
            session.setTotalItems(itemIds != null ? itemIds.size() : 0);
            session.setSoldItems(0);
            session.setViewCount(0);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());

            auctionSessionMapper.insert(session);

            if (itemIds != null && !itemIds.isEmpty()) {
                List<Long> conflicts = auctionSessionItemMapper.findConflictingItemIds(itemIds);
                if (conflicts != null && !conflicts.isEmpty()) {
                    throw new RuntimeException("部分拍品已在其他未开始/进行中的拍卖会中: " + conflicts);
                }
                auctionSessionItemMapper.batchInsert(session.getId(), itemIds);
            }
            return session.getId();
        } catch (Exception e) {
            log.error("创建拍卖会(含多图)失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询拍卖会
     */
    public AuctionSession getSessionById(Long id) {
        try {
            AuctionSession session = auctionSessionMapper.selectById(id);
            if (session != null) {
                // 动态纠正状态
                recalculateSessionStatus(session);
                // 加载拍品列表
                loadSessionItems(session);
            }
            return session;
        } catch (Exception e) {
            log.error("查询拍卖会失败: ID={}, 错误: {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查询拍卖会列表
     */
    public List<AuctionSession> getSessionList(AuctionSession session) {
        try {
            List<AuctionSession> sessions = auctionSessionMapper.selectList(session);
            // 动态纠正状态并加载拍品列表
            for (AuctionSession s : sessions) {
                recalculateSessionStatus(s);
                loadSessionItems(s);
            }
            return sessions;
        } catch (Exception e) {
            log.error("查询拍卖会列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 删除拍卖会
     */
    @Transactional
    public boolean deleteSession(Long id) {
        try {
            // 获取拍卖会信息
            AuctionSession session = auctionSessionMapper.selectById(id);
            if (session == null) {
                throw new RuntimeException("拍卖会不存在");
            }

            // 删除封面图片
            if (session.getCoverImage() != null) {
                String objectName = extractObjectNameFromUrl(session.getCoverImage());
                if (objectName != null) {
                    minioService.deleteFile(objectName);
                }
            }

            // 恢复拍品状态
            AuctionItem item = new AuctionItem();
            item.setStatus(2); // 审核通过
            item.setUpdateTime(LocalDateTime.now());
            auctionItemMapper.updateBySessionId(id, item);

            // 删除数据库记录
            int result = auctionSessionMapper.deleteById(id);
            
            if (result > 0) {
                log.info("拍卖会删除成功: ID={}", id);
                return true;
            } else {
                log.warn("拍卖会删除失败: ID={}", id);
                return false;
            }

        } catch (Exception e) {
            log.error("拍卖会删除失败: ID={}, 错误: {}", id, e.getMessage(), e);
            throw new RuntimeException("拍卖会删除失败: " + e.getMessage());
        }
    }

    // forceStart 回退移除

    /**
     * 获取可用的拍品列表
     */
    public List<AuctionItem> getAvailableItems() {
        try {
            return auctionItemMapper.selectAvailableForAssignment();
        } catch (Exception e) {
            log.error("查询可用拍品失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 检查拍品是否关联到未开始或进行中的拍卖会
     */
    public boolean hasActiveSessionsForItem(Long itemId) {
        try {
            // 查询拍品关联的拍卖会
            List<AuctionSession> sessions = auctionSessionMapper.selectSessionsByItemId(itemId);
            
            // 检查是否有未开始或进行中的拍卖会
            LocalDateTime now = LocalDateTime.now();
            for (AuctionSession session : sessions) {
                if (session.getStartTime() != null && session.getEndTime() != null) {
                    // 未开始：当前时间 < 开始时间
                    if (now.isBefore(session.getStartTime())) {
                        return true;
                    }
                    // 进行中：开始时间 <= 当前时间 <= 结束时间
                    if (!now.isBefore(session.getStartTime()) && !now.isAfter(session.getEndTime())) {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查拍品关联拍卖会失败: itemId={}, 错误: {}", itemId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(AuctionSession session) {
        // 保证金比例和佣金比例由管理员在创建拍卖会时设置
        // 这里不设置默认值，确保每个拍卖会都有明确的设置
        
        // 已移除最小保证金/最大出价/最小加价的前端录入，这里不再设置默认值

        // 设置默认状态
        if (session.getStatus() == null) {
            session.setStatus(0); // 草稿
        }

        // 设置默认值
        if (session.getIsAuthentic() == null) {
            session.setIsAuthentic(0);
        }
        if (session.getIsFreeShipping() == null) {
            session.setIsFreeShipping(0);
        }
        if (session.getIsReturnable() == null) {
            session.setIsReturnable(0);
        }
    }

    /**
     * 加载拍卖会拍品列表
     */
    private void loadSessionItems(AuctionSession session) {
        try {
            // 这里简化处理，实际应该通过关联表查询
            // 暂时返回空列表
            session.setItems(new ArrayList<>());
        } catch (Exception e) {
            log.warn("加载拍卖会拍品失败: {}", e.getMessage());
        }
    }

    /**
     * 根据当前时间动态纠正拍卖会状态：
     * 1-待开始；2-进行中；3-已结束（4-已取消维持不变）
     */
    private void recalculateSessionStatus(AuctionSession session) {
        try {
            if (session == null) return;
            Integer current = session.getStatus();
            if (current != null && current == 4) {
                return; // 已取消保持不变
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = session.getStartTime();
            LocalDateTime end = session.getEndTime();
            if (start == null || end == null) return;
            int newStatus;
            if (now.isBefore(start)) {
                newStatus = 1; // 待开始
            } else if (!now.isAfter(end)) {
                newStatus = 2; // 进行中
            } else {
                newStatus = 3; // 已结束
            }
            session.setStatus(newStatus);
        } catch (Exception e) {
            log.debug("动态计算拍卖会状态失败: {}", e.getMessage());
        }
    }

    /**
     * 获取拍卖会统计信息
     * 
     * @param sessionId 拍卖会ID
     * @return 统计信息
     */
    public Map<String, Object> getSessionStatistics(Long sessionId) {
        Map<String, Object> statistics = new java.util.HashMap<>();
        
        try {
            // 获取围观人数
            Long viewCount = redisService.getAuctionViewCount(sessionId);
            statistics.put("viewCount", viewCount);
            
            // 获取总出价次数
            Long bidCount = redisService.getAuctionBidCount(sessionId);
            statistics.put("bidCount", bidCount);
            
            // 获取拍卖会基本信息
            AuctionSession session = auctionSessionMapper.selectById(sessionId);
            if (session != null) {
                statistics.put("sessionName", session.getSessionName());
                statistics.put("status", session.getStatus());
                statistics.put("totalItems", session.getTotalItems());
                statistics.put("soldItems", session.getSoldItems());
            }
            
            log.debug("获取拍卖会统计信息: 拍卖会ID={}, 围观人数={}, 出价次数={}", 
                sessionId, viewCount, bidCount);
            
        } catch (Exception e) {
            log.error("获取拍卖会统计信息失败: 拍卖会ID={}, 错误: {}", sessionId, e.getMessage(), e);
        }
        
        return statistics;
    }

    /**
     * 获取用户在某拍卖会的出价统计
     * 
     * @param userId 用户ID
     * @param sessionId 拍卖会ID
     * @return 用户出价统计
     */
    public Map<String, Object> getUserBidStatistics(Long userId, Long sessionId) {
        Map<String, Object> statistics = new java.util.HashMap<>();
        
        try {
            // 获取用户出价次数
            Long userBidCount = redisService.getUserBidCount(userId, sessionId);
            statistics.put("userBidCount", userBidCount);
            
            // 获取拍卖会总出价次数
            Long auctionBidCount = redisService.getAuctionBidCount(sessionId);
            statistics.put("auctionBidCount", auctionBidCount);
            
            // 计算用户出价占比
            if (auctionBidCount > 0) {
                double bidRatio = (double) userBidCount / auctionBidCount;
                statistics.put("bidRatio", Math.round(bidRatio * 100) / 100.0);
            } else {
                statistics.put("bidRatio", 0.0);
            }
            
            log.debug("获取用户出价统计: 用户ID={}, 拍卖会ID={}, 用户出价次数={}, 拍卖总出价次数={}", 
                userId, sessionId, userBidCount, auctionBidCount);
            
        } catch (Exception e) {
            log.error("获取用户出价统计失败: 用户ID={}, 拍卖会ID={}, 错误: {}", 
                userId, sessionId, e.getMessage(), e);
        }
        
        return statistics;
    }

    /**
     * 从URL中提取对象名称
     */
    private String extractObjectNameFromUrl(String url) {
        try {
            // 假设URL格式为: http://localhost:9000/auction-images/auction/sessions/xxx.jpg
            if (url.contains("/auction-images/")) {
                return url.substring(url.indexOf("/auction-images/") + 16);
            }
        } catch (Exception e) {
            log.warn("提取对象名称失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取拍卖会的拍品列表
     */
    public List<AuctionItem> getSessionItems(Long sessionId) {
        try {
            return auctionItemMapper.selectBySessionId(sessionId);
        } catch (Exception e) {
            log.error("获取拍卖会拍品列表失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
