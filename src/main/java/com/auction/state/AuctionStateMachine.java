package com.auction.state;

import com.auction.entity.AuctionItem;
import com.auction.entity.AuctionSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 拍卖状态机
 * 管理拍卖商品和拍卖会的状态转换
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class AuctionStateMachine {

    /**
     * 商品状态转换规则
     */
    private static final Map<String, Map<Integer, Integer>> ITEM_STATE_TRANSITIONS = new HashMap<>();

    /**
     * 拍卖会状态转换规则
     */
    private static final Map<String, Map<Integer, Integer>> SESSION_STATE_TRANSITIONS = new HashMap<>();

    static {
        // 初始化商品状态转换规则
        initItemStateTransitions();
        
        // 初始化拍卖会状态转换规则
        initSessionStateTransitions();
    }

    /**
     * 初始化商品状态转换规则
     */
    private static void initItemStateTransitions() {
        // APPROVE操作：待审核(1) -> 审核通过(2)
        Map<Integer, Integer> approveTransitions = new HashMap<>();
        approveTransitions.put(1, 2); // 1-待审核 -> 2-审核通过
        ITEM_STATE_TRANSITIONS.put("APPROVE", approveTransitions);

        // REJECT操作：待审核(1) -> 审核拒绝(3)
        Map<Integer, Integer> rejectTransitions = new HashMap<>();
        rejectTransitions.put(1, 3); // 1-待审核 -> 3-审核拒绝
        ITEM_STATE_TRANSITIONS.put("REJECT", rejectTransitions);

        // START_AUCTION操作：审核通过(2) -> 拍卖中(4)
        Map<Integer, Integer> startAuctionTransitions = new HashMap<>();
        startAuctionTransitions.put(2, 4); // 2-审核通过 -> 4-拍卖中
        ITEM_STATE_TRANSITIONS.put("START_AUCTION", startAuctionTransitions);

        // SOLD操作：拍卖中(4) -> 已成交(5)
        Map<Integer, Integer> soldTransitions = new HashMap<>();
        soldTransitions.put(4, 5); // 4-拍卖中 -> 5-已成交
        ITEM_STATE_TRANSITIONS.put("SOLD", soldTransitions);

        // UNSOLD操作：拍卖中(4) -> 流拍(6)
        Map<Integer, Integer> unsoldTransitions = new HashMap<>();
        unsoldTransitions.put(4, 6); // 4-拍卖中 -> 6-流拍
        ITEM_STATE_TRANSITIONS.put("UNSOLD", unsoldTransitions);

        // OFFLINE操作：审核通过(2) -> 已下架(7) 或 已成交(5) -> 已下架(7) 或 流拍(6) -> 已下架(7)
        Map<Integer, Integer> offlineTransitions = new HashMap<>();
        offlineTransitions.put(2, 7); // 2-审核通过 -> 7-已下架
        offlineTransitions.put(5, 7); // 5-已成交 -> 7-已下架
        offlineTransitions.put(6, 7); // 6-流拍 -> 7-已下架
        ITEM_STATE_TRANSITIONS.put("OFFLINE", offlineTransitions);
    }

    /**
     * 初始化拍卖会状态转换规则
     */
    private static void initSessionStateTransitions() {
        // START操作：待开始(1) -> 进行中(2)
        Map<Integer, Integer> startTransitions = new HashMap<>();
        startTransitions.put(1, 2); // 1-待开始 -> 2-进行中
        SESSION_STATE_TRANSITIONS.put("START", startTransitions);

        // CANCEL操作：待开始(1) -> 已取消(4) 或 进行中(2) -> 已取消(4)
        Map<Integer, Integer> cancelTransitions = new HashMap<>();
        cancelTransitions.put(1, 4); // 1-待开始 -> 4-已取消
        cancelTransitions.put(2, 4); // 2-进行中 -> 4-已取消
        SESSION_STATE_TRANSITIONS.put("CANCEL", cancelTransitions);

        // FINISH操作：进行中(2) -> 已结束(3)
        Map<Integer, Integer> finishTransitions = new HashMap<>();
        finishTransitions.put(2, 3); // 2-进行中 -> 3-已结束
        SESSION_STATE_TRANSITIONS.put("FINISH", finishTransitions);
    }

    /**
     * 检查商品状态转换是否有效
     * 
     * @param currentStatus 当前状态
     * @param action 操作
     * @param targetStatus 目标状态
     * @return 是否有效
     */
    public boolean canTransitionItem(Integer currentStatus, String action, Integer targetStatus) {
        log.debug("检查商品状态转换: {} -> {} (操作: {})", currentStatus, targetStatus, action);
        
        Map<Integer, Integer> transitions = ITEM_STATE_TRANSITIONS.get(action);
        if (transitions == null) {
            log.warn("未找到操作对应的状态转换: {}", action);
            return false;
        }
        
        Integer allowedTargetStatus = transitions.get(currentStatus);
        boolean valid = allowedTargetStatus != null && allowedTargetStatus.equals(targetStatus);
        
        if (!valid) {
            log.warn("无效的商品状态转换: {} -> {} (操作: {})", currentStatus, targetStatus, action);
        }
        
        return valid;
    }

    /**
     * 检查拍卖会状态转换是否有效
     * 
     * @param currentStatus 当前状态
     * @param action 操作
     * @param targetStatus 目标状态
     * @return 是否有效
     */
    public boolean canTransitionSession(Integer currentStatus, String action, Integer targetStatus) {
        log.debug("检查拍卖会状态转换: {} -> {} (操作: {})", currentStatus, targetStatus, action);
        
        Map<Integer, Integer> transitions = SESSION_STATE_TRANSITIONS.get(action);
        if (transitions == null) {
            log.warn("未找到操作对应的状态转换: {}", action);
            return false;
        }
        
        Integer allowedTargetStatus = transitions.get(currentStatus);
        boolean valid = allowedTargetStatus != null && allowedTargetStatus.equals(targetStatus);
        
        if (!valid) {
            log.warn("无效的拍卖会状态转换: {} -> {} (操作: {})", currentStatus, targetStatus, action);
        }
        
        return valid;
    }

    /**
     * 执行商品状态转换
     * 
     * @param item 商品
     * @param action 操作
     * @param targetStatus 目标状态
     * @return 是否成功
     */
    public boolean transitionItem(AuctionItem item, String action, Integer targetStatus) {
        if (item == null) {
            log.error("商品为空");
            return false;
        }
        
        if (!canTransitionItem(item.getStatus(), action, targetStatus)) {
            log.error("无效的商品状态转换: {} -> {} (操作: {})", 
                    item.getStatus(), targetStatus, action);
            return false;
        }
        
        Integer oldStatus = item.getStatus();
        item.setStatus(targetStatus);
        item.setUpdateTime(LocalDateTime.now());
        
        log.info("商品状态转换成功: {} -> {} (操作: {})", 
                oldStatus, targetStatus, action);
        
        return true;
    }

    /**
     * 执行拍卖会状态转换
     * 
     * @param session 拍卖会
     * @param action 操作
     * @param targetStatus 目标状态
     * @return 是否成功
     */
    public boolean transitionSession(AuctionSession session, String action, Integer targetStatus) {
        if (session == null) {
            log.error("拍卖会为空");
            return false;
        }
        
        if (!canTransitionSession(session.getStatus(), action, targetStatus)) {
            log.error("无效的拍卖会状态转换: {} -> {} (操作: {})", 
                    session.getStatus(), targetStatus, action);
            return false;
        }
        
        Integer oldStatus = session.getStatus();
        session.setStatus(targetStatus);
        session.setUpdateTime(LocalDateTime.now());
        
        log.info("拍卖会状态转换成功: {} -> {} (操作: {})",
                oldStatus, targetStatus, action);
        
        return true;
    }

    /**
     * 获取商品状态描述
     * 
     * @param status 状态码
     * @return 状态描述
     */
    public String getItemStatusDesc(Integer status) {
        if (status == null) return "未知";
        
        switch (status) {
            case 1: return "待审核";
            case 2: return "审核通过";
            case 3: return "审核拒绝";
            case 4: return "拍卖中";
            case 5: return "已成交";
            case 6: return "流拍";
            case 7: return "已下架";
            default: return "未知";
        }
    }

    /**
     * 获取拍卖会状态描述
     * 
     * @param status 状态码
     * @return 状态描述
     */
    public String getSessionStatusDesc(Integer status) {
        if (status == null) return "未知";
        
        switch (status) {
            case 1: return "待开始";
            case 2: return "进行中";
            case 3: return "已结束";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 检查商品是否可以开始拍卖
     * 
     * @param item 商品
     * @return 是否可以开始拍卖
     */
    public boolean canStartAuction(AuctionItem item) {
        return item != null && 
               item.getStatus().equals(2) && // 2-审核通过
               item.getStartingPrice() != null && item.getStartingPrice().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查商品是否可以结束拍卖
     * 
     * @param item 商品
     * @return 是否可以结束拍卖
     */
    public boolean canEndAuction(AuctionItem item) {
        return item != null && 
               item.getStatus().equals(4); // 4-拍卖中
    }

    /**
     * 检查拍卖会是否可以开始
     * 
     * @param session 拍卖会
     * @return 是否可以开始
     */
    public boolean canStartSession(AuctionSession session) {
        return session != null && 
               session.getStatus().equals(1) && // 1-待开始
               session.getStartTime() != null && 
               session.getStartTime().isBefore(LocalDateTime.now());
    }

    /**
     * 检查拍卖会是否可以结束
     * 
     * @param session 拍卖会
     * @return 是否可以结束
     */
    public boolean canEndSession(AuctionSession session) {
        return session != null && 
               session.getStatus().equals(2) && // 2-进行中
               session.getEndTime() != null && 
               session.getEndTime().isBefore(LocalDateTime.now());
    }
}
