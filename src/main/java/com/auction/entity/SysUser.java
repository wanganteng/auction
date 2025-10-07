package com.auction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ========================================
 * 系统用户实体类（SysUser）
 * ========================================
 * 功能说明：
 * 1. 表示拍卖系统中的用户信息
 * 2. 支持两种用户类型：普通买家和超级管理员
 * 3. 包含用户的基本信息（用户名、昵称、联系方式）
 * 4. 包含认证信息（密码、状态）
 * 5. 包含登录信息（最后登录时间、IP）
 * 6. 支持角色和权限系统（RBAC）
 * 
 * 数据库表：sys_user
 * 
 * 用户类型说明：
 * - 普通买家(0)：可以参与竞拍、管理订单、充值保证金
 * - 超级管理员(1)：可以管理拍品、拍卖会、用户、系统配置等
 * 
 * 安全说明：
 * - 密码字段使用@JsonIgnore注解，序列化时自动忽略
 * - 密码在数据库中使用BCrypt加密存储
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data  // Lombok注解：自动生成getter、setter、toString等方法
public class SysUser {

    /* ========================= 基本信息字段 ========================= */

    /**
     * 用户ID（主键）
     * 数据库自动生成的唯一标识
     */
    private Long id;

    /**
     * 用户名（登录账号）
     * 唯一，不可重复
     * 3-20个字符，只能包含字母、数字、下划线
     * 必填字段
     */
    private String username;

    /**
     * 密码（加密存储）
     * 使用BCrypt算法加密
     * 最小6位，建议包含字母、数字、特殊字符
     * @JsonIgnore注解：序列化为JSON时自动忽略此字段，防止密码泄露
     */
    @JsonIgnore
    private String password;

    /**
     * 昵称（显示名称）
     * 用户的显示名称，可以包含中文
     * 如果不设置，默认使用用户名
     * 在出价记录等地方显示
     */
    private String nickname;

    /* ========================= 联系方式字段 ========================= */

    /**
     * 手机号
     * 11位中国大陆手机号
     * 用于接收通知、找回密码等
     * 可选字段
     */
    private String phone;

    /**
     * 邮箱地址
     * 用于接收邮件通知
     * 可选字段
     */
    private String email;

    /**
     * 用户头像URL
     * 存储头像图片的URL地址
     * 如果未上传，使用默认头像
     */
    private String avatar;

    /* ========================= 状态和权限字段 ========================= */

    /**
     * 用户状态
     * 0-禁用：账号被封禁，无法登录和操作
     * 1-启用：正常使用
     * 管理员可以禁用违规用户
     */
    private Integer status;

    /**
     * 用户类型
     * 0-普通买家：只能参与竞拍和管理自己的订单
     * 1-超级管理员：拥有所有权限，可以管理整个系统
     * 决定用户登录后跳转到哪个页面
     */
    private Integer userType;

    /* ========================= 登录信息字段 ========================= */

    /**
     * 最后登录时间
     * 记录用户最后一次成功登录的时间
     * 用于安全审计和活跃度统计
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP地址
     * 记录用户最后一次登录的IP
     * 用于安全审计，检测异常登录
     * 格式：xxx.xxx.xxx.xxx
     */
    private String lastLoginIp;

    /* ========================= 时间戳字段 ========================= */

    /**
     * 创建时间（注册时间）
     * 记录用户账号创建的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 记录用户信息最后一次修改的时间
     * 每次更新用户信息时自动更新
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（软删除）
     * 0-未删除：正常用户
     * 1-已删除：已注销或被删除的用户
     * 软删除可以保留历史数据
     */
    private Integer deleted;

    /* ========================= 非数据库字段（用于RBAC权限系统） ========================= */

    /**
     * 用户角色列表（非数据库字段）
     * 通过sys_user_role关联表查询得到
     * 一个用户可以有多个角色
     * 例如：["管理员", "审核员"]
     */
    private List<SysRole> roles;

    /**
     * 用户权限列表（非数据库字段）
     * 从角色中解析出的所有权限
     * 用于权限验证
     * 例如：["item:create", "item:update", "session:start"]
     */
    private List<String> permissions;
}