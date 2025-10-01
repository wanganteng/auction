-- 拍卖系统数据库表结构
-- 创建数据库
CREATE DATABASE IF NOT EXISTS auction_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction_db;

-- 用户表
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `user_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '用户类型：0-买家，1-超级管理员',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户角色表
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 商品分类表
CREATE TABLE `auction_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `category_code` varchar(50) NOT NULL COMMENT '分类编码',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标',
  `description` varchar(500) DEFAULT NULL COMMENT '分类描述',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 拍品表
CREATE TABLE `auction_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '拍品ID',
  `item_name` varchar(200) NOT NULL COMMENT '拍品名称',
  `description` text COMMENT '拍品描述',
  `category_id` bigint(20) DEFAULT NULL COMMENT '拍品分类ID',
  `starting_price` decimal(10,2) NOT NULL COMMENT '起拍价',
  `reserve_price` decimal(10,2) DEFAULT NULL COMMENT '保留价（底价）',
  `current_price` decimal(10,2) DEFAULT '0.00' COMMENT '当前最高价',
  `estimated_price` decimal(10,2) DEFAULT NULL COMMENT '拍品估价',
  `item_code` varchar(50) DEFAULT NULL COMMENT '拍品编号',
  `deposit_ratio` decimal(3,2) DEFAULT '0.10' COMMENT '保证金比例',
  `commission_ratio` decimal(3,2) DEFAULT '0.05' COMMENT '佣金比例',
  `is_authentic` tinyint(1) DEFAULT '0' COMMENT '是否保真：0-否，1-是',
  `is_free_shipping` tinyint(1) DEFAULT '0' COMMENT '是否包邮：0-否，1-是',
  `is_returnable` tinyint(1) DEFAULT '0' COMMENT '是否支持退货：0-否，1-是',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '拍品状态：0-下架，1-上架',
  `uploader_id` bigint(20) NOT NULL COMMENT '上传人ID',
  `images` text COMMENT '拍品图片列表（JSON格式）',
  `detail_images` text COMMENT '拍品详情图片列表（JSON格式）',
  `weight` decimal(8,2) DEFAULT NULL COMMENT '拍品重量（克）',
  `dimensions` varchar(100) DEFAULT NULL COMMENT '拍品尺寸（长x宽x高，单位：厘米）',
  `material` varchar(100) DEFAULT NULL COMMENT '拍品材质',
  `era` varchar(50) DEFAULT NULL COMMENT '拍品年代',
  `source` varchar(200) DEFAULT NULL COMMENT '拍品来源',
  `certificate` varchar(500) DEFAULT NULL COMMENT '拍品证书',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_status` (`status`),
  UNIQUE KEY `uk_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拍品表';

-- 拍卖会表
CREATE TABLE `auction_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '拍卖会ID',
  `session_name` varchar(200) NOT NULL COMMENT '拍卖会名称',
  `description` text COMMENT '拍卖会描述',
  `session_type` TINYINT NOT NULL DEFAULT 4 COMMENT '拍卖会类型',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '拍卖会状态：0-草稿，1-待开始，2-进行中，3-已结束，4-已取消',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `creator_id` bigint(20) NOT NULL COMMENT '创建人ID',
  `total_items` int(11) DEFAULT '0' COMMENT '拍品总数',
  `sold_items` int(11) DEFAULT '0' COMMENT '已成交拍品数',
  `view_count` int(11) DEFAULT '0' COMMENT '围观人数',
  `deposit_ratio` decimal(3,2) DEFAULT '0.10' COMMENT '保证金比例',
  `commission_ratio` decimal(3,2) DEFAULT '0.05' COMMENT '佣金比例',
  `is_authentic` tinyint(1) DEFAULT '0' COMMENT '是否保真：0-否，1-是',
  `is_free_shipping` tinyint(1) DEFAULT '0' COMMENT '是否包邮：0-否，1-是',
  `is_returnable` tinyint(1) DEFAULT '0' COMMENT '是否支持退货：0-否，1-是',
  `min_deposit_amount` bigint(20) DEFAULT '10000' COMMENT '最小保证金金额（分）',
  `max_bid_amount` bigint(20) DEFAULT '100000000' COMMENT '最大出价金额（分）',
  `min_increment_amount` bigint(20) DEFAULT '100' COMMENT '最小加价幅度（分）',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '拍卖会封面图片',
  `images` LONGTEXT NULL COMMENT '会场图片列表(JSON)，第一张为封面',
  `rules` text COMMENT '拍卖会规则',
  `is_visible` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否对用户可见：0-隐藏，1-展示',
  `anti_sniping_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用延时拍卖：0-否，1-是',
  `extend_threshold_sec` int(11) NOT NULL DEFAULT '60' COMMENT '临近结束触发延时的阈值（秒）',
  `extend_seconds` int(11) NOT NULL DEFAULT '60' COMMENT '每次顺延的秒数',
  `extend_max_times` int(11) NOT NULL DEFAULT '5' COMMENT '最大顺延次数（0为不限制）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拍卖会表';

-- 拍卖会拍品关联表
CREATE TABLE `auction_session_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `session_id` bigint(20) NOT NULL COMMENT '拍卖会ID',
  `item_id` bigint(20) NOT NULL COMMENT '拍品ID',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_item` (`session_id`,`item_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拍卖会拍品关联表';

CREATE TABLE `user_deposit_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总保证金金额',
  `available_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '可用保证金金额',
  `frozen_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '冻结保证金金额',
  `refunded_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '已退还保证金金额',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '账户状态：1-正常，2-冻结，3-注销',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户保证金账户表';

-- 保证金交易流水表
DROP TABLE IF EXISTS `user_deposit_transaction`;
CREATE TABLE `user_deposit_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `account_id` bigint(20) NOT NULL COMMENT '保证金账户ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `transaction_no` varchar(50) DEFAULT NULL COMMENT '交易流水号',
  `transaction_type` tinyint(1) NOT NULL COMMENT '交易类型：1-充值，2-提现，3-冻结，4-解冻，5-扣除，6-退还',
  `amount` bigint(20) NOT NULL COMMENT '交易金额(分)',
  `balance_before` bigint(20) NOT NULL COMMENT '交易前余额(分)',
  `balance_after` bigint(20) NOT NULL COMMENT '交易后余额(分)',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联ID（拍卖会ID、订单ID等）',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联类型：auction_session、order等',
  `description` varchar(500) DEFAULT NULL COMMENT '交易描述',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '交易状态：0-待审核，1-成功，2-失败，3-处理中',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_no` (`transaction_no`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_transaction_type` (`transaction_type`),
  KEY `idx_status` (`status`),
  KEY `idx_reviewer` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保证金交易流水表';

-- 保证金退款申请表
CREATE TABLE `user_deposit_refund` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '退款申请ID',
  `account_id` bigint(20) NOT NULL COMMENT '保证金账户ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `refund_no` varchar(50) NOT NULL COMMENT '退款申请单号',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '申请退款金额',
  `available_amount` decimal(10,2) NOT NULL COMMENT '当前可用金额',
  `reason` varchar(500) DEFAULT NULL COMMENT '退款原因',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '退款状态：1-待审核，2-审核通过，3-审核拒绝，4-退款成功，5-退款失败',
  `auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_comment` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_transaction_id` bigint(20) DEFAULT NULL COMMENT '退款交易流水ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保证金退款申请表';

-- 拍卖出价表
CREATE TABLE `auction_bid` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '出价ID',
  `session_id` bigint(20) NOT NULL COMMENT '拍卖会ID',
  `item_id` bigint(20) NOT NULL COMMENT '拍品ID',
  `user_id` bigint(20) NOT NULL COMMENT '出价用户ID',
  `bid_amount` bigint(20) NOT NULL COMMENT '出价金额（分）',
  `bid_amount_yuan` decimal(10,2) NOT NULL COMMENT '出价金额（元）',
  `bid_time` datetime NOT NULL COMMENT '出价时间',
  `source` tinyint(1) NOT NULL DEFAULT '1' COMMENT '出价来源：1-手动出价，2-自动出价',
  `is_auto` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否自动出价：0-否，1-是',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '出价状态：0-有效，1-无效，2-被超越',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_bid_time` (`bid_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拍卖出价表';

-- 订单表
CREATE TABLE `auction_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `session_id` bigint(20) NOT NULL COMMENT '拍卖会ID',
  `item_id` bigint(20) NOT NULL COMMENT '拍品ID',
  `buyer_id` bigint(20) NOT NULL COMMENT '买家ID',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家ID（超级管理员）',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `deposit_amount` decimal(10,2) NOT NULL COMMENT '保证金金额',
  `balance_amount` decimal(10,2) NOT NULL COMMENT '尾款金额',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '订单状态：1-待付款，2-已付款，3-已发货，4-已收货，5-已完成，6-已取消',
  `payment_time` datetime DEFAULT NULL COMMENT '付款时间',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 物流信息表
CREATE TABLE `auction_logistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '物流ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `logistics_company` varchar(100) NOT NULL COMMENT '物流公司',
  `tracking_number` varchar(100) NOT NULL COMMENT '运单号',
  `logistics_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '物流状态：1-已发货，2-运输中，3-已到达，4-已签收',
  `sender_name` varchar(50) DEFAULT NULL COMMENT '发货人姓名',
  `sender_phone` varchar(20) DEFAULT NULL COMMENT '发货人电话',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货人电话',
  `receiver_address` varchar(500) DEFAULT NULL COMMENT '收货地址',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_tracking_number` (`tracking_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流信息表';

-- 拍卖结果表（固化每个场次-拍品的最终结果，便于统计与回溯）
CREATE TABLE `auction_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `session_id` bigint(20) DEFAULT NULL COMMENT '拍卖会ID',
  `item_id` bigint(20) NOT NULL COMMENT '拍品ID',
  `winner_user_id` bigint(20) DEFAULT NULL COMMENT '成交用户ID（流拍为NULL）',
  `final_price` bigint(20) NOT NULL DEFAULT '0' COMMENT '成交价（分）',
  `highest_bid_id` bigint(20) DEFAULT NULL COMMENT '最终最高出价ID',
  `result_status` tinyint(1) NOT NULL COMMENT '结果：0-流拍，1-成交，2-撤拍',
  `order_id` bigint(20) DEFAULT NULL COMMENT '成交订单ID',
  `commission_fee` bigint(20) DEFAULT NULL COMMENT '佣金（分，快照）',
  `deposit_used` bigint(20) DEFAULT NULL COMMENT '使用的保证金（分，快照）',
  `settle_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '结算状态：0-未结算，1-已结算',
  `settle_time` datetime DEFAULT NULL COMMENT '结算时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_item_result` (`session_id`,`item_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_winner_user_id` (`winner_user_id`),
  KEY `idx_result_status` (`result_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拍卖结果表';

-- 系统配置表
CREATE TABLE `sys_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值',
  `config_type` varchar(20) NOT NULL DEFAULT 'STRING' COMMENT '配置类型：STRING-字符串，NUMBER-数字，BOOLEAN-布尔，JSON-JSON对象',
  `description` varchar(255) DEFAULT NULL COMMENT '配置描述',
  `is_system` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否系统配置：0-否，1-是',
  `is_editable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可编辑：0-否，1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_type` (`config_type`),
  KEY `idx_is_system` (`is_system`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';


-- 数据库表结构创建完成
-- 请执行 init_data.sql 来插入初始数据

-- =============== 参拍须知 ===============

-- 须知分类表
CREATE TABLE IF NOT EXISTS `notice_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '启用：0-禁用，1-启用',
  `content_html` MEDIUMTEXT COMMENT '分类富文本内容（文字/图片）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_notice_category_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参拍须知分类表';


-- 用户在线状态表
CREATE TABLE IF NOT EXISTS `user_online_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `last_activity` datetime NOT NULL COMMENT '最后活动时间',
  `ip_address` varchar(45) COMMENT 'IP地址',
  `user_agent` varchar(500) COMMENT '用户代理',
  `session_id` varchar(100) COMMENT '会话ID',
  `is_online` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否在线：0-离线，1-在线',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_online` (`is_online`),
  KEY `idx_last_activity` (`last_activity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户在线状态表';


-- 用户通知表
CREATE TABLE IF NOT EXISTS `user_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `notification_type` tinyint(2) NOT NULL COMMENT '通知类型：1-中标通知，2-订单通知，3-支付通知，4-发货通知，5-系统通知',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联ID（订单ID、拍品ID等）',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联类型（order-订单，item-拍品，session-拍卖会）',
  `link_url` varchar(500) DEFAULT NULL COMMENT '跳转链接',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '读取时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_notification_type` (`notification_type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型：LOGIN,LOGOUT,CREATE,UPDATE,DELETE,APPROVE,REJECT,SHIP,REFUND',
  `module` varchar(50) NOT NULL COMMENT '操作模块：USER,ITEM,SESSION,ORDER,DEPOSIT',
  `operation_desc` varchar(500) NOT NULL COMMENT '操作描述',
  `target_id` bigint(20) DEFAULT NULL COMMENT '操作对象ID',
  `target_type` varchar(50) DEFAULT NULL COMMENT '操作对象类型',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法：GET,POST,PUT,DELETE',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` text DEFAULT NULL COMMENT '请求参数（JSON）',
  `response_result` varchar(500) DEFAULT NULL COMMENT '响应结果',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `success` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否成功：0-失败，1-成功',
  `error_msg` text DEFAULT NULL COMMENT '错误信息',
  `duration` bigint(20) DEFAULT NULL COMMENT '执行时长（毫秒）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_success` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';
