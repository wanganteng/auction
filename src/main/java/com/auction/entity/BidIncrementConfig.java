package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ========================================
 * 加价阶梯配置实体类（BidIncrementConfig）
 * ========================================
 * 功能说明：
 * 1. 定义拍卖会的加价阶梯规则配置
 * 2. 一个配置包含多条加价规则
 * 3. 不同价格区间有不同的最小加价幅度
 * 4. 用于规范竞拍行为，避免恶意加价
 * 
 * 数据库表：bid_increment_config
 * 
 * 什么是加价阶梯：
 * - 根据当前价格，规定每次最少要加价多少
 * - 价格越高，最小加价幅度越大
 * - 例如：0-1000元每次加10元，1000-5000元每次加50元
 * 
 * 应用场景：
 * - 创建拍卖会时选择一个加价阶梯配置
 * - 用户出价时自动验证是否符合规则
 * - 防止用户每次只加1元恶意竞价
 * 
 * 配置示例：
 * 配置名称："标准加价阶梯"
 * 规则1：0-1000元，每次加10元
 * 规则2：1000-5000元，每次加50元
 * 规则3：5000元以上，每次加100元
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter等方法
public class BidIncrementConfig {

    /* ========================= 基本信息字段 ========================= */

    /**
     * 配置ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /**
     * 配置名称
     * 例如："标准加价阶梯"、"精品加价阶梯"
     * 用于在界面上选择和识别
     */
    private String configName;

    /**
     * 配置描述
     * 详细说明该配置的适用场景和规则特点
     * 例如："适用于一般拍品，价格越高加价幅度越大"
     */
    private String description;

    /* ========================= 状态字段 ========================= */

    /**
     * 是否启用
     * 0-禁用：不可在新拍卖会中使用
     * 1-启用：可以在创建拍卖会时选择
     * 已在使用的配置不建议禁用
     */
    private Integer status;

    /* ========================= 关联信息字段 ========================= */

    /**
     * 创建人ID
     * 关联sys_user表
     * 记录是哪位管理员创建的配置
     */
    private Long creatorId;

    /* ========================= 时间戳字段 ========================= */

    /**
     * 创建时间
     * 配置创建的时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 配置最后修改的时间
     * 每次修改规则都会更新
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志（软删除）
     * 0-未删除：正常配置
     * 1-已删除：已删除的配置
     */
    private Integer deleted;

    /* ========================= 非数据库字段（用于关联查询） ========================= */

    /**
     * 加价规则列表（非数据库字段）
     * 关联bid_increment_rule表查询得到
     * 一个配置包含多条规则，按价格区间排序
     * 
     * 规则对象包含：
     * - minAmount：价格区间下限
     * - maxAmount：价格区间上限
     * - incrementAmount：最小加价幅度
     */
    private List<BidIncrementRule> rules;

    /**
     * 创建人信息（非数据库字段）
     * 关联查询得到的创建人详细信息
     * 用于显示创建者的姓名
     */
    private SysUser creator;
}
