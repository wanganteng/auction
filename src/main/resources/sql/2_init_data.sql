-- 数据库初始化数据脚本
-- 用于初始化基础数据和测试数据
-- 注意：此脚本需要在 1_schema.sql 之后执行

-- 1. 插入基础角色数据
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限'),
('买家', 'BUYER', '买家，只能参与拍卖');

-- 2. 插入基础用户数据
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `user_type`, `status`, `create_time`, `update_time`, `deleted`) VALUES
('admin', '$2a$12$p60tqDGV0s8maxJ8seuvKuRm2hunWoHg6vWtyD83i6y9TDopkzP0a', '超级管理员', 1, 1, NOW(), NOW(), 0),
('buyer1', '$2a$12$YTUCrguZBpldHMxILvpSLuUAqQ7LBEGAlKjg0IKz9uM5bNUp26GFS', '买家1', 0, 1, NOW(), NOW(), 0),
('buyer2', '$2a$12$YTUCrguZBpldHMxILvpSLuUAqQ7LBEGAlKjg0IKz9uM5bNUp26GFS', '买家2', 0, 1, NOW(), NOW(), 0),
('buyer3', '$2a$12$YTUCrguZBpldHMxILvpSLuUAqQ7LBEGAlKjg0IKz9uM5bNUp26GFS', '买家3', 0, 1, NOW(), NOW(), 0);

-- 3. 插入用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES 
(1, 1), -- admin -> 超级管理员
(2, 2), -- buyer1 -> 买家
(3, 2), -- buyer2 -> 买家
(4, 2); -- buyer3 -> 买家

-- 4. 插入基础商品分类数据
INSERT INTO `auction_category` (`parent_id`, `category_name`, `category_code`, `sort_order`, `icon`, `description`, `status`, `create_time`, `update_time`, `deleted`) VALUES
(0, '艺术品', 'ART', 1, 'el-icon-picture', '绘画、雕塑、书法等艺术品', 1, NOW(), NOW(), 0),
(0, '珠宝首饰', 'JEWELRY', 2, 'el-icon-star-on', '钻石、黄金、玉石等珠宝首饰', 1, NOW(), NOW(), 0),
(0, '古董收藏', 'ANTIQUE', 3, 'el-icon-trophy', '古董、文物、收藏品', 1, NOW(), NOW(), 0),
(0, '奢侈品', 'LUXURY', 4, 'el-icon-goods', '名牌包包、手表、服装等奢侈品', 1, NOW(), NOW(), 0),
(0, '其他', 'OTHER', 5, 'el-icon-more', '其他拍卖商品', 1, NOW(), NOW(), 0);

-- 5. 插入子分类数据
INSERT INTO `auction_category` (`parent_id`, `category_name`, `category_code`, `sort_order`, `icon`, `description`, `status`, `create_time`, `update_time`, `deleted`) VALUES
(1, '中国画', 'CHINESE_PAINTING', 1, 'el-icon-picture-outline', '中国传统绘画作品', 1, NOW(), NOW(), 0),
(1, '油画', 'OIL_PAINTING', 2, 'el-icon-picture-outline', '西方油画作品', 1, NOW(), NOW(), 0),
(1, '书法', 'CALLIGRAPHY', 3, 'el-icon-edit', '书法作品', 1, NOW(), NOW(), 0),
(2, '钻石', 'DIAMOND', 1, 'el-icon-star-on', '钻石首饰', 1, NOW(), NOW(), 0),
(2, '翡翠', 'JADE', 2, 'el-icon-star-on', '翡翠首饰', 1, NOW(), NOW(), 0),
(2, '黄金', 'GOLD', 3, 'el-icon-star-on', '黄金首饰', 1, NOW(), NOW(), 0),
(3, '瓷器', 'PORCELAIN', 1, 'el-icon-trophy', '古代瓷器', 1, NOW(), NOW(), 0),
(3, '玉器', 'JADE_WARE', 2, 'el-icon-trophy', '古代玉器', 1, NOW(), NOW(), 0),
(3, '铜器', 'BRONZE', 3, 'el-icon-trophy', '古代铜器', 1, NOW(), NOW(), 0),
(4, '手表', 'WATCH', 1, 'el-icon-time', '名牌手表', 1, NOW(), NOW(), 0),
(4, '包包', 'BAG', 2, 'el-icon-goods', '名牌包包', 1, NOW(), NOW(), 0),
(4, '服装', 'CLOTHING', 3, 'el-icon-goods', '名牌服装', 1, NOW(), NOW(), 0);

-- 6. 插入系统配置数据
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`, `is_system`, `is_editable`) VALUES
-- 系统功能配置
('auction.bidding.auto_extend_minutes', '5', 'NUMBER', '自动延时分钟数', 1, 1),
('auction.logistics.auto_ship_days', '3', 'NUMBER', '自动发货天数', 1, 1),
('auction.session.auto_start_minutes', '10', 'NUMBER', '拍卖会自动开始提前分钟数', 1, 1),
('auction.session.auto_end_minutes', '5', 'NUMBER', '拍卖会自动结束延后分钟数', 1, 1),
('auction.bidding.timeout_seconds', '30', 'NUMBER', '出价超时时间（秒）', 1, 1),
('auction.notification.enabled', 'true', 'BOOLEAN', '是否启用通知', 1, 1),
('auction.auto_bid.enabled', 'true', 'BOOLEAN', '是否启用自动出价', 1, 1),
('auction.item.audit_enabled', 'true', 'BOOLEAN', '是否启用拍品审核', 1, 1),

-- 系统安全配置
('system.maintenance_mode', 'false', 'BOOLEAN', '系统维护模式', 1, 1),
('system.max_login_attempts', '5', 'NUMBER', '最大登录尝试次数', 1, 1),
('system.session_timeout', '1800', 'NUMBER', '会话超时时间（秒）', 1, 1),
('system.file.max_size', '10485760', 'NUMBER', '文件上传最大大小（字节）', 1, 1),
('system.file.allowed_types', 'jpg,jpeg,png,gif', 'STRING', '允许上传的文件类型', 1, 1);

-- 7. 参拍须知：预置分类
INSERT INTO `notice_category` (`name`, `sort_order`, `enabled`, `content_html`,`create_time`, `update_time`) VALUES
('拍卖流程', 1, 1,'<p>注册/登录 → 支付保证金 → 进入会场出价 → 成交后支付尾款 → 发货收货。</p>', NOW(), NOW()),
('拍卖规则', 2, 1,'<ul><li>出价需不低于当前价且满足最小加价幅度。</li><li>出价成功不可撤销，请谨慎操作。</li></ul>', NOW(), NOW()),
('保证金说明', 3, 1,'<p>用于确保竞拍行为的严肃性；未成交按规则原路退还。成交后用于抵扣部分货款或作为违约保障。</p>', NOW(), NOW()),
('拍卖须知', 4, 1,'<p>请仔细阅读拍品描述与图片，必要时可联系平台咨询。竞拍成功视为认可拍品现状。</p>', NOW(), NOW()),
('净价说明', 5, 1,'<p>净价不含佣金、运费、保险及其他可能产生的费用，最终以订单结算为准。</p>', NOW(), NOW()),
('物流及其他费用说明', 6, 1,'<p>如会场未标注包邮，运费与保险费由买家承担。支持到付或平台代收，具体以订单为准。</p>', NOW(), NOW()),
('收退货说明', 7, 1,'<p>签收前务必当面查验。非质量问题一般不支持退货；若存在争议，请在签收后24小时内联系客服。</p>', NOW(), NOW()),
('法律法规', 8, 1, '<p>请遵守相关法律法规与平台规则，严禁违法违规拍品交易。平台保留对违规账号处理的权利。</p>', NOW(), NOW());



-- 插入默认的加价阶梯配置
INSERT INTO `bid_increment_config` (`id`,`config_name`, `description`, `status`, `creator_id`) VALUES
(1,'默认加价阶梯', '系统默认的加价阶梯配置', 1, 1);


-- 插入默认的加价规则
INSERT INTO `bid_increment_rule` (`config_id`, `min_amount`, `max_amount`, `increment_amount`, `sort_order`) VALUES
(1, 0.00, 100.00, 1.00, 1),
(1, 100.00, 500.00, 5.00, 2),
(1, 500.00, 1000.00, 10.00, 3),
(1, 1000.00, 5000.00, 50.00, 4),
(1, 5000.00, 10000.00, 100.00, 5),
(1, 10000.00, NULL, 200.00, 6);