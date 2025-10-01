package com.auction.mapper;

import com.auction.entity.UserDepositAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户保证金账户Mapper接口
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface UserDepositAccountMapper {

    /**
     * 插入保证金账户
     */
    int insert(UserDepositAccount account);

    /**
     * 更新保证金账户
     */
    int update(UserDepositAccount account);

    /**
     * 根据ID查询保证金账户
     */
    UserDepositAccount selectById(Long id);

    /**
     * 根据用户ID查询保证金账户
     */
    UserDepositAccount selectByUserId(@Param("userId") Long userId);

    /**
     * 查询保证金账户列表
     */
    List<UserDepositAccount> selectList(UserDepositAccount account);

    /**
     * 根据ID删除保证金账户
     */
    int deleteById(Long id);

    /**
     * 更新账户金额（原子操作）
     */
    int updateAmount(@Param("id") Long id, 
                    @Param("totalAmount") java.math.BigDecimal totalAmount,
                    @Param("availableAmount") java.math.BigDecimal availableAmount,
                    @Param("frozenAmount") java.math.BigDecimal frozenAmount,
                    @Param("refundedAmount") java.math.BigDecimal refundedAmount);
    
    /**
     * 更新账户状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
