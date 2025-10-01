package com.auction.mapper;

import com.auction.entity.UserDepositRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 保证金退款申请Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface UserDepositRefundMapper {

    /**
     * 插入退款申请
     */
    int insert(UserDepositRefund refund);

    /**
     * 更新退款申请
     */
    int update(UserDepositRefund refund);

    /**
     * 根据ID查询退款申请
     */
    UserDepositRefund selectById(Long id);

    /**
     * 根据退款申请单号查询
     */
    UserDepositRefund selectByRefundNo(@Param("refundNo") String refundNo);

    /**
     * 查询退款申请列表
     */
    List<UserDepositRefund> selectList(UserDepositRefund refund);

    /**
     * 根据账户ID查询退款申请
     */
    List<UserDepositRefund> selectByAccountId(@Param("accountId") Long accountId);

    /**
     * 根据用户ID查询退款申请
     */
    List<UserDepositRefund> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据状态查询退款申请
     */
    List<UserDepositRefund> selectByStatus(@Param("status") Integer status);

    /**
     * 根据ID删除退款申请
     */
    int deleteById(Long id);
}
