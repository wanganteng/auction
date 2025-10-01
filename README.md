# 拍卖系统 - 完整版

## 系统概述

这是一个基于Spring Boot的完整在线拍卖系统，包含核心拍卖功能和高级管理功能：

- **超级管理员**：拍品管理、拍卖会管理、用户管理、系统配置管理
- **买家**：保证金管理、拍卖会列表、实时竞拍、订单管理

## 技术栈

- **后端**：Spring Boot 2.7.18 + MyBatis + Spring Security + Redis
- **前端**：ElementUI + Vue 3
- **数据库**：MySQL 8.0
- **文件存储**：MinIO
- **实时通信**：WebSocket
- **缓存**：Redis
- **认证**：JWT Token

## 核心功能

### 超级管理员功能
1. **拍品管理**
   - 上传拍品（支持多图片上传）
   - 拍品列表查询
   - 拍品编辑和删除
   - 图片存储到MinIO

2. **拍卖会管理**
   - 创建拍卖会
   - 添加拍品到拍卖会
   - 拍卖会列表管理
   - 设置拍卖规则（保证金比例、佣金比例等）

3. **订单管理**
   - 查看所有订单
   - 订单发货管理
   - 订单状态更新

4. **物流管理**
   - 创建物流信息
   - 更新物流状态
   - 物流信息查询

5. **保证金管理**
   - 保证金账户查询
   - 交易流水查询
   - 退款申请审核
   - 退款执行管理

6. **用户管理**
   - 用户列表查看

7. **系统配置管理**
   - 动态配置系统参数
   - 配置项增删改查
   - 批量配置更新
   - 配置缓存管理

### 买家功能
1. **拍卖会浏览**
   - 查看拍卖会列表
   - 查看拍卖会详情
   - 搜索和筛选

2. **保证金管理**
   - 保证金账户管理（总金额、可用金额、冻结金额、已退还金额）
   - 保证金充值（支持交易流水记录）
   - 保证金冻结/解冻（拍卖会参与时冻结，结束后解冻）
   - 保证金扣除（拍卖成交后扣除）
   - 保证金退还（支持申请审核流程）
   - 完整的交易流水查询

3. **实时竞拍**
   - 参与拍卖会竞拍
   - 实时出价
   - 查看出价记录
   - WebSocket实时更新

4. **订单管理**
   - 查看我的订单
   - 订单支付
   - 确认收货
   - 订单取消

5. **物流跟踪**
   - 查看物流信息
   - 跟踪物流状态
   - 收货地址管理

## 数据库设计

### 核心表结构
- `sys_user` - 用户表（简化角色：0-买家，1-超级管理员）
- `sys_role` - 角色表
- `auction_item` - 拍品表
- `auction_session` - 拍卖会表
- `auction_session_item` - 拍卖会拍品关联表
- `user_deposit_account` - 用户保证金账户表（严谨的账户管理）
- `user_deposit_transaction` - 保证金交易流水表（完整的资金流水）
- `user_deposit_refund` - 保证金退款申请表（退款审核流程）
- `auction_bid` - 拍卖出价表
- `auction_order` - 订单表
- `auction_logistics` - 物流信息表
- `sys_config` - 系统配置表

## 保证金管理系统

### 严谨的资金管理设计
本系统采用严谨的保证金管理设计，确保资金安全和数据一致性：

#### 1. 账户体系
- **用户保证金账户表** (`user_deposit_account`)
  - 总金额：用户累计充值的总金额
  - 可用金额：可以用于参与拍卖的金额
  - 冻结金额：参与拍卖时冻结的金额
  - 已退还金额：累计退还的金额

#### 2. 交易流水
- **交易流水表** (`user_deposit_transaction`)
  - 充值：用户向账户充值
  - 冻结：参与拍卖时冻结保证金
  - 解冻：拍卖结束后解冻保证金
  - 扣除：拍卖成交后扣除保证金
  - 退还：用户申请退还保证金

#### 3. 退款审核
- **退款申请表** (`user_deposit_refund`)
  - 用户申请退款
  - 超级管理员审核
  - 审核通过后执行退款
  - 完整的审核流程记录

#### 4. 资金安全
- 所有金额操作都有事务保护
- 每笔交易都有完整的流水记录
- 支持金额校验和余额检查
- 防止重复操作和并发问题

## 系统配置管理

### 配置项说明

#### 1. 系统级配置（这些是系统级别的功能配置）

| 配置键 | 默认值 | 类型 | 描述 |
|--------|--------|------|------|
| `auction.bidding.timeout_seconds` | 30 | NUMBER | 出价超时时间（秒） |
| `auction.bidding.auto_extend_minutes` | 5 | NUMBER | 自动延时分钟数 |
| `auction.logistics.auto_ship_days` | 3 | NUMBER | 自动发货天数 |
| `auction.session.auto_start_minutes` | 10 | NUMBER | 拍卖会自动开始提前分钟数 |
| `auction.session.auto_end_minutes` | 5 | NUMBER | 拍卖会自动结束延后分钟数 |
| `auction.item.audit_enabled` | true | BOOLEAN | 是否启用拍品审核 |
| `auction.notification.enabled` | true | BOOLEAN | 是否启用通知 |
| `auction.auto_bid.enabled` | true | BOOLEAN | 是否启用自动出价 |

#### 2. 拍卖会级配置（这些由管理员在创建拍卖会时设置）

| 配置项 | 描述 | 说明 |
|--------|------|------|
| `deposit_ratio` | 保证金比例 | 每个拍卖会可以设置不同的保证金比例 |
| `commission_ratio` | 佣金比例 | 每个拍卖会可以设置不同的佣金比例 |
| `min_deposit_amount` | 最小保证金金额 | 该拍卖会的最小保证金要求 |
| `max_bid_amount` | 最大出价金额 | 该拍卖会的最大出价限制 |
| `min_increment_amount` | 最小加价幅度 | 该拍卖会的最小加价要求 |
| `is_authentic` | 是否保真 | 拍卖会级别的保真承诺 |
| `is_free_shipping` | 是否包邮 | 拍卖会级别的包邮政策 |
| `is_returnable` | 是否可退 | 拍卖会级别的退货政策 |

#### 3. 系统功能配置

| 配置键 | 默认值 | 类型 | 描述 |
|--------|--------|------|------|
| `system.maintenance_mode` | false | BOOLEAN | 系统维护模式 |
| `system.max_login_attempts` | 5 | NUMBER | 最大登录尝试次数 |
| `system.session_timeout` | 1800 | NUMBER | 会话超时时间（秒） |
| `system.file.max_size` | 10485760 | NUMBER | 文件上传最大大小（字节） |
| `system.file.allowed_types` | jpg,jpeg,png,gif | STRING | 允许上传的文件类型 |

### 配置使用方式

#### 管理员界面
访问 `/auction/admin/config` 进入系统配置管理页面：
- **查看配置**：浏览所有配置项，支持按类型筛选
- **编辑配置**：点击编辑按钮修改配置值
- **批量更新**：选择多个配置项进行批量修改
- **添加配置**：创建新的配置项
- **删除配置**：删除不需要的配置项（系统配置不可删除）

#### API接口
```http
# 获取所有配置
GET /api/admin/configs

# 获取系统配置
GET /api/admin/configs/system

# 获取单个配置值
GET /api/admin/configs/{configKey}

# 更新配置值
PUT /api/admin/configs/{configKey}

# 批量更新配置
PUT /api/admin/configs/batch
```

## Redis功能

### 功能详情

#### 1. 用户会话存储
- 存储用户登录状态到Redis
- 支持会话过期时间设置
- 提供用户在线状态检查
- 支持会话清理

#### 2. 拍卖围观人数计数器
- 实时统计拍卖围观人数
- 支持增加/减少围观人数
- 自动防止负数计数
- 支持过期时间设置（默认24小时）

#### 3. 出价次数计数器
- 统计用户在特定拍卖会的出价次数
- 统计拍卖会总出价次数
- 支持过期时间设置（默认24小时）

### Redis键格式
- 用户会话：`user:session:{userId}`
- 会话映射：`session:user:{sessionId}`
- 围观人数：`auction:view:{auctionId}`
- 用户出价次数：`user:bid:count:{userId}:{auctionId}`
- 拍卖总出价次数：`auction:bid:count:{auctionId}`

## 快速开始

### 1. 环境准备
```bash
# 安装MySQL 8.0
# 安装MinIO
# 安装Redis
# 安装JDK 8+
# 安装Maven 3.6+
```

### 2. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE auction_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行建表脚本
source src/main/resources/sql/schema.sql;
source src/main/resources/sql/init_data.sql;
```

### 3. MinIO配置
```bash
# 启动MinIO服务
minio server /data --console-address ":9001"

# 创建存储桶
# 访问 http://localhost:9001 创建名为 auction-images 的存储桶
```

### 4. Redis配置
```bash
# 启动Redis服务
redis-server

# 或者使用Docker
docker run -d -p 6379:6379 redis:latest
```

### 5. 应用配置
修改 `src/main/resources/application.yml` 中的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auction_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
  
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: auction-images
```

### 6. 启动应用

#### 方式1：使用IDE（推荐）
1. 在IntelliJ IDEA中打开项目
2. 重新导入Maven项目：右键点击 `pom.xml` → `Maven` → `Reload project`
3. 找到 `src/main/java/com/auction/AuctionSystemApplication.java`
4. 右键点击 → `Run 'AuctionSystemApplication'`

#### 方式2：使用Maven命令行
```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/auction-system-1.0.0.jar
```

### 7. 访问系统
- **买家界面**：http://localhost:8080/auction/user/
- **超级管理后台**：http://localhost:8080/auction/admin/
- **系统配置管理**：http://localhost:8080/auction/admin/config
- **API文档**：http://localhost:8080/swagger-ui.html

## 默认账号

### 超级管理员
- 用户名：admin
- 密码：123456

### 买家
- 用户名：buyer1
- 密码：123456

## 核心特性

### 1. 简化的用户角色
- 只保留超级管理员和买家两种角色
- 超级管理员拥有所有管理权限
- 买家只能参与竞拍和管理保证金

### 2. 图片存储
- 所有图片统一存储到MinIO
- 支持拍品图片和拍卖会封面图片
- 自动生成唯一文件名

### 3. 实时竞拍
- 基于WebSocket的实时出价
- 支持并发控制
- 实时价格更新

### 4. 保证金管理
- 支持多种支付方式
- 自动计算保证金金额
- 支持退还申请

### 5. 响应式设计
- 基于ElementUI的现代化界面
- 支持移动端访问
- 简洁易用的操作体验

### 6. 系统配置管理
- 动态配置系统参数
- 无需修改代码即可调整系统行为
- 支持配置缓存和批量更新

### 7. Redis集成
- 用户会话管理
- 实时计数器
- 性能优化

## 项目结构

```
src/main/java/com/auction/
├── config/          # 配置类
├── controller/      # 控制器
├── entity/          # 实体类
├── mapper/          # MyBatis映射接口
├── service/         # 业务服务
├── security/        # 安全配置
├── websocket/       # WebSocket处理
└── state/           # 状态机

src/main/resources/
├── mapper/          # MyBatis XML映射文件
├── sql/            # 数据库脚本
├── static/         # 静态资源
└── templates/      # 页面模板
```

## 开发说明

### 1. 代码规范
- 使用Lombok简化代码
- 统一异常处理
- 日志记录完整

### 2. 安全考虑
- JWT Token认证
- 密码加密存储
- 接口权限控制

### 3. 性能优化
- 数据库连接池
- Redis缓存
- 分页查询

## 部署建议

### 1. 生产环境配置
- 修改数据库密码
- 配置MinIO访问密钥
- 设置JWT密钥
- 配置Redis密码
- 设置日志级别

### 2. 监控和日志
- 应用日志记录
- 数据库性能监控
- Redis性能监控
- MinIO存储监控

### 3. 备份策略
- 数据库定期备份
- MinIO文件备份
- Redis数据备份
- 配置文件备份

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 验证数据库连接配置
- 确认数据库用户权限

### 2. MinIO连接失败
- 检查MinIO服务状态
- 验证访问密钥配置
- 确认存储桶是否存在

### 3. Redis连接失败
- 检查Redis服务状态
- 验证Redis配置
- 确认网络连接

### 4. WebSocket连接失败
- 检查防火墙设置
- 验证WebSocket配置
- 查看浏览器控制台错误

### 5. Maven编译问题
- 使用IDE运行应用
- 检查Maven版本和配置
- 清理本地Maven仓库

## 许可证

MIT License

## 联系方式

如有问题，请提交Issue或联系开发团队。
