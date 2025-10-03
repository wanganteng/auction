-- 初始化演示数据：拍品、拍卖会、拍卖会与拍品关联
-- 执行前请确认已导入 schema.sql 并存在必要表结构

-- 1) 插入拍品（拍品金额字段单位为“元”的小数）
INSERT INTO auction_item (
  item_name, description, category_id, starting_price, current_price, reserve_price,
  deposit_ratio, commission_ratio, is_authentic, is_free_shipping, is_returnable,
  status, uploader_id, images, create_time
) VALUES
('清代青花瓷瓶', '清代青花纹饰，保存完好。', 2, 5000.00, 5000.00, 8000.00,
  0.10, 0.05, 1, 1, 1, 1, 1, '["/images/default-item.jpg"]', NOW()),
('明代黄花梨圈椅', '原木包浆，造型典雅。', 5, 15000.00, 15000.00, 20000.00,
  0.10, 0.05, 1, 1, 1, 1, 1, '["/images/default-item.jpg"]', NOW()),
('和田玉挂件', '青白玉质，工艺精细。', 3, 2000.00, 2000.00, 0.00,
  0.10, 0.05, 1, 1, 1, 1, 1, '["/images/default-item.jpg"]', NOW());

-- 2) 创建拍卖会
INSERT INTO auction_session (
  session_name, description, start_time, end_time, creator_id,
  deposit_ratio, commission_ratio,
  is_authentic, is_free_shipping, is_returnable,
  rules, status, cover_image, is_visible,
  anti_sniping_enabled, extend_threshold_sec, extend_seconds, extend_max_times,bid_increment_config_id,
  create_time
) VALUES (
  '春季艺术精品拍卖会',
  '涵盖书画、瓷器、玉器与家具等精品。',
  DATE_ADD(NOW(), INTERVAL 1 DAY),
  DATE_ADD(NOW(), INTERVAL 2 DAY),
  1,
  0.10, 0.05,
  1, 1, 1,
  '竞拍须知：加价幅度固定，拍得后请在48小时内完成支付。',
  1,
  '/images/default-session.jpg',
  1,
  1, 60, 60, 5, 1,
  NOW()
);

-- 3) 关联拍卖会与拍品（将上面插入的拍品加入该拍卖会）
INSERT INTO auction_session_item (session_id, item_id, create_time)
SELECT s.id, i.id, NOW()
FROM auction_session s
JOIN auction_item i ON 1=1
WHERE s.session_name = '春季艺术精品拍卖会'
  AND i.item_name IN ('清代青花瓷瓶', '明代黄花梨圈椅', '和田玉挂件');


