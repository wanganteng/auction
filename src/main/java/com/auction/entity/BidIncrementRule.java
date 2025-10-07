package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ========================================
 * 加价阶梯规则实体类（BidIncrementRule）
 * ========================================
 * 功能说明：
 * 1. 定义加价阶梯配置中的单条规则
 * 2. 规定某个价格区间的最小加价幅度
 * 3. 多条规则组成完整的加价阶梯配置
 * 4. 按sortOrder排序，价格从低到高
 * 
 * 数据库表：bid_increment_rule
 * 
 * 规则说明：
 * - 每条规则定义一个价格区间和对应的加价幅度
 * - 当前价格落在哪个区间，就使用该区间的加价幅度
 * - 价格区间左闭右开：[minAmount, maxAmount)
 * 
 * 规则示例：
 * 规则1：minAmount=0, maxAmount=1000, incrementAmount=10
 *        含义：0-1000元区间，每次至少加10元
 * 
 * 规则2：minAmount=1000, maxAmount=5000, incrementAmount=50
 *        含义：1000-5000元区间，每次至少加50元
 * 
 * 规则3：minAmount=5000, maxAmount=null, incrementAmount=100
 *        含义：5000元以上，每次至少加100元
 * 
 * 验证逻辑：
 * - 当前价格1500元，落在规则2区间
 * - 下次出价至少1550元（1500 + 50）
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter等方法
public class BidIncrementRule {

    /* ========================= 基本信息字段 ========================= */

    /**
     * 规则ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /**
     * 加价阶梯配置ID
     * 关联bid_increment_config表
     * 表示该规则属于哪个配置
     * 一个配置可以有多条规则
     */
    private Long configId;

    /* ========================= 价格区间字段 ========================= */

    /**
     * 价格下限（包含，元）
     * 该规则适用的价格区间的最小值
     * 例如：1000 表示1000元及以上
     * 使用BigDecimal确保精度
     */
    private BigDecimal minAmount;

    /**
     * 价格上限（不包含，元）
     * 该规则适用的价格区间的最大值
     * 例如：5000 表示小于5000元
     * null表示无上限
     * 
     * 注意：价格区间是左闭右开 [minAmount, maxAmount)
     */
    private BigDecimal maxAmount;

    /**
     * 加价金额（元）
     * 该价格区间的最小加价幅度
     * 例如：50 表示每次至少加50元
     * 
     * 用户出价时的验证：
     * 新出价 >= 当前价格 + incrementAmount
     */
    private BigDecimal incrementAmount;

    /**
     * 排序号
     * 用于控制规则的显示顺序
     * 数字越小越靠前
     * 通常按价格从低到高排序
     * 例如：1, 2, 3...
     */
    private Integer sortOrder;

    /* ========================= 时间戳字段 ========================= */

    /**
     * 创建时间
     * 规则创建的时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 规则最后修改的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志（软删除）
     * 0-未删除：正常规则
     * 1-已删除：已删除的规则
     */
    private Integer deleted;

    /* ========================= 非数据库字段 ========================= */

    /**
     * 加价阶梯配置信息（非数据库字段）
     * 关联查询得到的配置对象
     * 包含配置名称、描述等信息
     */
    private BidIncrementConfig config;
}
