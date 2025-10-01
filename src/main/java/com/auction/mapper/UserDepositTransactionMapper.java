package com.auction.mapper;

import com.auction.entity.UserDepositTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 保证金交易流水Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface UserDepositTransactionMapper {

    /**
     * 插入交易流水
     */
    int insert(UserDepositTransaction transaction);

    /**
     * 更新交易流水
     */
    int update(UserDepositTransaction transaction);

    /**
     * 根据ID查询交易流水
     */
    UserDepositTransaction selectById(Long id);

    /**
     * 根据交易流水号查询
     */
    UserDepositTransaction selectByTransactionNo(@Param("transactionNo") String transactionNo);

    /**
     * 查询交易流水列表
     */
    List<UserDepositTransaction> selectList(UserDepositTransaction transaction);

    /**
     * 根据账户ID查询交易流水
     */
    List<UserDepositTransaction> selectByAccountId(@Param("accountId") Long accountId);

    /**
     * 根据用户ID查询交易流水
     */
    List<UserDepositTransaction> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据关联ID和类型查询交易流水
     */
    List<UserDepositTransaction> selectByRelatedId(@Param("relatedId") Long relatedId, 
                                                  @Param("relatedType") String relatedType);

    /**
     * 根据ID删除交易流水
     */
    int deleteById(Long id);

    /**
     * 查询待审核的交易列表
     * @param transactionType 交易类型，null表示查询所有类型
     */
    List<UserDepositTransaction> selectPendingTransactions(@Param("transactionType") Integer transactionType);
}
