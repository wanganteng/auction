package com.auction.service;

import com.auction.entity.AuctionBid;
import com.auction.entity.AuctionItem;
import com.auction.entity.AuctionSession;
import com.auction.entity.UserDepositAccount;
import com.auction.mapper.AuctionBidMapper;
import com.auction.mapper.AuctionItemMapper;
import com.auction.mapper.AuctionSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 拍卖出价服务测试类
 * 重点测试保证金冻结逻辑的正确性
 */
@ExtendWith(MockitoExtension.class)
class AuctionBidServiceTest {

    @Mock
    private AuctionBidMapper auctionBidMapper;

    @Mock
    private AuctionItemMapper auctionItemMapper;

    @Mock
    private AuctionSessionMapper auctionSessionMapper;

    @Mock
    private UserDepositAccountService depositAccountService;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AuctionBidService auctionBidService;

    private AuctionSession testSession;
    private AuctionItem testItem;
    private UserDepositAccount testAccount;

    @BeforeEach
    void setUp() {
        // 设置测试拍卖会
        testSession = new AuctionSession();
        testSession.setId(1L);
        testSession.setDepositRatio(new BigDecimal("0.10")); // 10%保证金比例

        // 设置测试拍品
        testItem = new AuctionItem();
        testItem.setId(1L);
        testItem.setStatus(1); // 上架状态
        testItem.setStartingPrice(new BigDecimal("10")); // 起拍价10元
        testItem.setCurrentPrice(new BigDecimal("10")); // 当前价格10元

        // 设置测试保证金账户
        testAccount = new UserDepositAccount();
        testAccount.setId(1L);
        testAccount.setUserId(1L);
        testAccount.setAvailableAmount(new BigDecimal("1000")); // 可用1000元
        testAccount.setFrozenAmount(BigDecimal.ZERO);
        testAccount.setTotalAmount(new BigDecimal("1000"));
    }

    @Test
    void testFirstBid_ShouldFreezeFullDeposit() {
        // 准备测试数据：用户第一次出价
        AuctionBid bid = createTestBid(1L, 1L, 1L, 1200L); // 出价12元

        // Mock 拍卖会查询
        when(auctionSessionMapper.selectById(1L)).thenReturn(testSession);
        
        // Mock 拍品查询
        when(auctionItemMapper.selectById(1L)).thenReturn(testItem);
        
        // Mock 历史出价查询（第一次出价，无历史记录）
        when(auctionBidMapper.selectList(any())).thenReturn(new ArrayList<>());
        
        // Mock 保证金账户查询
        when(depositAccountService.getAccountByUserId(1L)).thenReturn(testAccount);
        
        // Mock 插入出价记录
        when(auctionBidMapper.insert(any())).thenReturn(1);
        
        // Mock 更新拍品价格
        when(auctionItemMapper.updateById(any())).thenReturn(1);
        
        // Mock 冻结保证金
        when(depositAccountService.freezeAmount(any(), any(), any(), any(), any())).thenReturn(true);

        // 执行测试
        Long bidId = auctionBidService.placeBid(bid);

        // 验证结果
        assertNotNull(bidId);
        
        // 验证冻结了全额保证金：12 * 10% = 1.2元，向上取整为2元
        verify(depositAccountService).freezeAmount(
            eq(1L), 
            eq(new BigDecimal("2")), // 12 * 0.10 = 1.2元，向上取整为2元
            eq(bidId), 
            eq("bid"), 
            eq("出价冻结保证金")
        );
    }

    @Test
    void testSecondBid_ShouldFreezeDeltaDeposit() {
        // 准备测试数据：用户第二次出价
        AuctionBid bid = createTestBid(1L, 1L, 1L, 1500L); // 出价15元

        // Mock 拍卖会查询
        when(auctionSessionMapper.selectById(1L)).thenReturn(testSession);
        
        // Mock 拍品查询
        when(auctionItemMapper.selectById(1L)).thenReturn(testItem);
        
        // Mock 历史出价查询（用户之前出价12元）
        List<AuctionBid> historicalBids = new ArrayList<>();
        AuctionBid historicalBid = createTestBid(1L, 1L, 1L, 1200L);
        historicalBids.add(historicalBid);
        when(auctionBidMapper.selectList(any())).thenReturn(historicalBids);
        
        // Mock 保证金账户查询
        when(depositAccountService.getAccountByUserId(1L)).thenReturn(testAccount);
        
        // Mock 插入出价记录
        when(auctionBidMapper.insert(any())).thenReturn(1);
        
        // Mock 更新拍品价格
        when(auctionItemMapper.updateById(any())).thenReturn(1);
        
        // Mock 冻结保证金
        when(depositAccountService.freezeAmount(any(), any(), any(), any(), any())).thenReturn(true);

        // 执行测试
        Long bidId = auctionBidService.placeBid(bid);

        // 验证结果
        assertNotNull(bidId);
        
        // 验证只冻结了差额保证金：(15 - 12) * 10% = 0.3元，向上取整为1元
        verify(depositAccountService).freezeAmount(
            eq(1L), 
            eq(new BigDecimal("1")), // (15 - 12) * 0.10 = 0.3元，向上取整为1元
            eq(bidId), 
            eq("bid"), 
            eq("出价冻结保证金")
        );
    }

    @Test
    void testBidWithInsufficientDeposit_ShouldThrowException() {
        // 准备测试数据：用户保证金不足
        AuctionBid bid = createTestBid(1L, 1L, 1L, 2000L); // 出价20元

        // Mock 拍卖会查询
        when(auctionSessionMapper.selectById(1L)).thenReturn(testSession);
        
        // Mock 拍品查询
        when(auctionItemMapper.selectById(1L)).thenReturn(testItem);
        
        // Mock 历史出价查询（无历史记录）
        when(auctionBidMapper.selectList(any())).thenReturn(new ArrayList<>());
        
        // Mock 保证金账户查询（可用余额不足）
        testAccount.setAvailableAmount(new BigDecimal("100")); // 只有100元可用
        when(depositAccountService.getAccountByUserId(1L)).thenReturn(testAccount);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            auctionBidService.placeBid(bid);
        });
        
        assertTrue(exception.getMessage().contains("可用保证金不足"));
    }

    @Test
    void testBidWithSameAmount_ShouldNotFreeze() {
        // 准备测试数据：用户出价相同金额
        AuctionBid bid = createTestBid(1L, 1L, 1L, 1200L); // 出价12元

        // Mock 拍卖会查询
        when(auctionSessionMapper.selectById(1L)).thenReturn(testSession);
        
        // Mock 拍品查询
        when(auctionItemMapper.selectById(1L)).thenReturn(testItem);
        
        // Mock 历史出价查询（用户之前也出价12元）
        List<AuctionBid> historicalBids = new ArrayList<>();
        AuctionBid historicalBid = createTestBid(1L, 1L, 1L, 1200L);
        historicalBids.add(historicalBid);
        when(auctionBidMapper.selectList(any())).thenReturn(historicalBids);
        
        // Mock 保证金账户查询
        when(depositAccountService.getAccountByUserId(1L)).thenReturn(testAccount);
        
        // Mock 插入出价记录
        when(auctionBidMapper.insert(any())).thenReturn(1);
        
        // Mock 更新拍品价格
        when(auctionItemMapper.updateById(any())).thenReturn(1);

        // 执行测试
        Long bidId = auctionBidService.placeBid(bid);

        // 验证结果
        assertNotNull(bidId);
        
        // 验证没有冻结保证金（差额为0）
        verify(depositAccountService, never()).freezeAmount(any(), any(), any(), any(), any());
    }

    private AuctionBid createTestBid(Long id, Long userId, Long itemId, Long bidAmount) {
        AuctionBid bid = new AuctionBid();
        bid.setId(id);
        bid.setUserId(userId);
        bid.setItemId(itemId);
        bid.setSessionId(1L);
        bid.setBidAmountYuan(new BigDecimal(bidAmount).divide(new BigDecimal("100")));
        bid.setBidTime(LocalDateTime.now());
        bid.setStatus(0);
        bid.setSource(1);
        bid.setIsAuto(0);
        bid.setCreateTime(LocalDateTime.now());
        bid.setUpdateTime(LocalDateTime.now());
        bid.setDeleted(0);
        return bid;
    }
}
