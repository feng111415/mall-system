# 商城系统项目交接总览

> 新会话开始时，先完整阅读本文件和 `CONTEXT.md`，再开始任何修改。
>
> 最后更新：2026-07-30。商城 V0.1、V0.2 已完成；V0.3 按模块独立设计、开发、测试、验收和提交，模块 1“订单详情与支付时限”、模块 2“手工物流与收货”、模块 3“订单项售后”及基础加固已完成。

## 1. 项目状态

这是基于 RuoYi-Vue 的前后端分离单商户 B2C 商城测试项目。后台运营端复用若依能力，商城用户端是独立 Vue 3 应用，商城业务后端以独立模块接入若依工程。

当前已完成：

- **V0.1**：会员、商品、库存、购物车、地址、结算、订单、Mock 支付、履约售后、风控治理、后台运营和数据库迁移。
- **V0.2**：用户端商品浏览、全局框架、商品详情与加购、购物车结算、个人中心地址簿、最终视觉验收和完整回归。
- **V0.3 模块 1**：独立订单详情页、支付尝试记录、订单创建后 30 分钟支付单创建期限、35 分钟支付结果期限、超时关闭及迟到支付自动原路退款。
- **V0.3 模块 2**：若依运营后台手工发货、追加式物流轨迹、会员查看物流与确认收货、签收 7 天自动确认收货。
- **V0.3 模块 3**：订单项级仅退款/退货退款、数量占用与期限校验、服务端退款金额、退货物流、若依审核和退款状态机。
- **V0.3 基础加固**：支付退款幂等、外部 I/O 事务拆分、并发下单上限、短信失败计数持久化，以及商城会员 Token 与若依管理员 Token 隔离。
- V0.3 模块 3 已完成；下一模块为模块 4“个人资料与头像”，等待用户明确确认后再开始。

## 2. 仓库与运行环境

| 项目项 | 当前值 |
| --- | --- |
| 仓库目录 | `C:\Users\Administrator\Desktop\RuoYiWork\RuoYi-Vue-master` |
| Git 分支 | `dev` |
| 远程仓库 | `https://github.com/feng111415/mall-system.git` |
| 最新完成阶段 | V0.3 模块 3：订单项售后 |
| 商城用户端 | `http://localhost:5174` |
| 若依运营端 | `http://localhost:8081` |
| 商城后端 | `http://localhost:8080` |
| Redis | `127.0.0.1:6379` |
| MySQL | `127.0.0.1:3306`，Windows 服务 `MySQL84` |
| 测试数据库 | `mall_migration_test_20260728` |
| Mock 短信验证码 | `123456` |

本轮结束时，`5174`、`6379`、`8080`、`8081` 已在本机运行。正式运营端为 `ruoyi-ui` 的 `8081`；`18080` 和 `18081` 是历史临时后台联调端口。

### 当前工作区规则

- `ai-web/` 是用户的未跟踪目录，必须保留；禁止提交、删除、移动或重构。
- 不修改原业务库 `ry-vue`；所有迁移、联调和验收只使用 `mall_migration_test_20260728`。
- 当前是测试项目，不打包 Docker、不发布、不执行生产部署。
- 后续项目改动完成验证后，必须提交并推送 GitHub `dev`。
- 不执行 `git reset --hard`、`git checkout --`、清理未跟踪目录等破坏性操作。

## 3. 架构与目录

```text
mall-storefront (Vue 3 + Vite + Pinia + Vue Router + Vant)
       |
       | /api/mall/**，商城独立 Bearer Token
       v
mall-business (Spring Boot 商城模块)
       |
       +-- Controller -> Service -> Mapper -> MySQL
       +-- Redis（验证码、缓存、Token）
       +-- 复用 RuoYi 框架、安全、日志、任务等通用能力

ruoyi-ui / ruoyi-admin (若依后台运营端)
```

关键目录：

```text
mall-storefront/                         商城用户端
mall-business/                           商城业务后端
ruoyi-admin/                             后端启动模块
sql/                                     版本化数据库迁移
scripts/mall-storefront-e2e.ps1          商城 API 全链路验收
doc/                                     需求、架构、验收、开发记录
doc/architecture/                        架构图与设计资料
```

核心文档：

```text
CONTEXT.md                               领域术语和统一语言
doc/商城系统-需求分析说明书.md             需求基线
doc/商城系统-架构设计说明书.md             架构说明和图
doc/商城系统-订单状态设计.md               订单状态机
doc/商城系统-开发进度.md                   简版进度
doc/development/商城开发记录.md             历史开发记录
doc/development/mall-v0.2-api-contract.md V0.2 商品查询契约
```

## 4. 不可突破的开发规则

### 后端与数据

- 固定分层：`Controller -> Service -> Mapper`；Controller 不写业务规则或 SQL。
- 商城用户端接口统一 `/api/mall/**`，返回统一使用 `AjaxResult`。
- 会员使用商城独立 Bearer Token，绝不混用若依后台管理员 Token。
- 写操作使用 DTO、`@Valid` 和事务；Controller 不得绕过 DTO。
- 数据库变更只能使用版本化 SQL 迁移，不能手工改测试库结构。
- 客户端排序字段必须映射到服务端白名单，禁止原样拼接进 SQL。
- 地址查询、修改、删除、设默认和下单必须按当前会员校验资源归属。
- 下单与支付必须保留幂等控制、库存二次校验和防超卖逻辑。

### 安全与治理

- 短信验证码需要保持手机号校验、过期、重发间隔、日限额和错误次数限制。
- Token 不得出现在 URL、日志或响应中。
- 关键写操作保留操作日志；版本通过 Git 分支、提交和发布脚本管理。
- 后台权限使用 RuoYi RBAC；商城会员权限与管理员权限严格隔离。

### 前端

- 延续 Vue 3、Vite、Vue Router、Pinia、Vant 的既有组织方式，不引入冲突的请求或状态方案。
- 新请求集中在 `src/api/`，全局状态集中在 Store，页面不散落底层请求实现。
- 保持简洁生活方式商城风格，覆盖加载、空状态、错误反馈、交互忙碌态和移动端体验。
- 桌面与移动端都必须验收；移动端五栏底部导航可用，页面不得横向溢出。

## 5. V0.1 已完成能力

- 手机号 + 短信验证码注册登录，开发环境固定验证码为 `123456`。
- 独立会员 Token、会员资料、协议同意和收货地址。
- 分类、品牌、SPU、SKU、库存快照、库存预占和库存流水。
- 购物车、结算预览、订单创建、订单快照、订单幂等和 Mock 支付。
- 订单列表、物流履约、售后退款、基础风控、补偿任务、支付退款对账与告警。
- 若依运营后台、RBAC 菜单权限、操作审计、版本化数据库迁移和全链路验收脚本。

## 6. V0.2 完成清单

| 序号 | 阶段 | 状态 | 结果 |
| --- | --- | --- | --- |
| 1 | 接口与数据基线 | 已完成 | 商品契约、8 个 SPU、17 个 SKU、17 条库存快照 |
| 2 | 商品浏览 | 已完成 | 首页真实商品、分类、搜索、排序、骨架和空状态 |
| 3 | 全局框架 | 已完成 | 桌面导航、移动五栏、全局通知、购物车角标同步 |
| 4 | 商品详情与加购 | 已完成并验收 | SKU、数量、库存、加购反馈、相关推荐和最近浏览 |
| 5 | 购物车与结算体验 | 已完成并验收 | 全选、数量金额、失效处理、地址、备注、结算进度和支付结果 |
| 6 | 个人中心与地址簿 | 已完成并验收 | 地址编辑、删除、默认切换、表单反馈和移动端交互 |
| 7 | 视觉验收、回归、提交 | 已完成 | 构建、53 项后端测试、14 项 API、14 个浏览器视图和 GitHub 同步 |

### 商品与浏览

- 测试库含 8 个已上架 SPU、17 个有效 SKU、17 条库存快照，覆盖家居、数码、穿搭、咖啡和出行。
- `GET /api/mall/catalog/products` 支持 `categoryId`、`keyword`、`sort`；排序只允许 `default`、`sales`、`priceAsc`、`priceDesc`。
- 首页显示全部 8 个真实商品；商品列表提供分类、关键词、销量与价格排序、加载骨架和空状态。

### 商品详情、购物车与结算

- 商品详情默认选中可售 SKU；切换 SKU 后价格与库存同步；售罄 SKU 禁止加购；数量受库存边界限制。
- 详情页安全清理 `detailHtml` 后渲染，提供相关推荐与最近浏览。
- 购物车支持全选、数量调整、删除、刷新、失效清理、库存提示和粘性结算栏。
- 结算页提供地址选择、费用明细、订单备注（最多 200 字）、确认订单、模拟支付和订单入口。
- 订单备注已经由订单详情接口确认落库；重复提交与重复支付受幂等控制。

### 个人中心与地址簿

- 会员 API 已接入地址新增、查询、修改、删除；后端持续按 `addressId + memberId` 校验归属。
- 个人中心提供新增/编辑共用表单、省市区级联、收货人/手机号/地区/详细地址字段校验和错误反馈。
- 地址卡片支持设为默认、编辑、页面内删除确认、忙碌态及全局成功/失败通知。
- 地址编辑、设默认、删除后会刷新列表和地址数量；移动端操作区改为单列布局。

## 7. 最近完整验证

| 验证项 | 命令或方式 | 结果 |
| --- | --- | --- |
| 后端全量测试 | `mvn -pl mall-business -am test` | 53 项通过，0 失败 |
| 用户端生产构建 | `npm.cmd run build`（`mall-storefront`） | 通过 |
| API 全链路 | `powershell -ExecutionPolicy Bypass -File scripts/mall-storefront-e2e.ps1` | 14 项通过 |
| 真实 Edge 交互 | 登录、加购、购物车、结算、支付、地址簿 | 通过 |
| 最终视觉审查 | 7 条核心路由，1440px + 390px | 14 个视图通过，无控制台错误、无图片加载失败、无横向溢出 |

最终视觉审查覆盖：`/`、`/catalog`、`/product/1`、`/cart`、`/checkout`、`/orders`、`/account`。两种视口均满足 `scrollWidth == clientWidth`。

验证只写入独立测试库 `mall_migration_test_20260728`；原库 `ry-vue` 未修改。

## 8. V0.3 模块 1：订单详情与支付时限

- 用户端新增独立路由 `/orders/:orderId`；订单列表点击进入详情，展示商品快照、收货信息、费用、支付记录和操作时间线。
- 订单详情返回 `paymentCreateDeadline`、`paymentResultDeadline`、`canCreatePayment`、`canConfirmPayment`、`canCancel` 和 `payTime`。
- 支付单必须在订单创建后 30 分钟内创建；创建期间订单进入 `PAYING`，渠道创建失败恢复 `UNPAID`。
- 无支付单订单到第 30 分钟关闭；已创建支付单订单等待到第 35 分钟，再关闭支付单与订单并释放库存。
- 关闭后收到支付成功结果时，订单保持 `CLOSED`，支付进入 `REFUNDING -> REFUNDED`；补偿任务与状态变更同事务持久化，再尝试即时原路退款。
- 新增会员接口 `GET /api/mall/orders/{orderId}/payments`，SQL 同时校验订单和支付记录的会员归属，返回前清除幂等键。
- 数据库版本为 `V2.8.0__mall_payment_deadline.sql` 和 `V2.8.1__mall_order_timeout_job.sql`；对应回滚脚本位于 `sql/rollback/`。
- V2.8.1 注册每分钟执行一次的 Quartz 订单超时任务。超时订单保持 `CLOSED` 终态并展示“支付超时已关闭”，释放库存后不会计入每会员最多 3 笔进行中待付款订单。

本模块验收结果：后端 65 项单元测试通过，用户端生产构建通过，API 全链路 17 项通过；Edge 1440px/390px 订单详情无横向溢出、控制台错误或图片失败；V2.8 空库升级、回滚、再升级通过；迟到支付集成验证确认订单关闭、支付退款、库存释放和补偿任务一致。

## 9. 环境启动与停止

### 启动前检查

```powershell
Get-Service MySQL84
Get-NetTCPConnection -State Listen -LocalPort 5174,6379,8080
```

### Redis

```powershell
Set-Location C:\Users\Administrator\Desktop\RuoYiWork\tools\redis
.\redis-server.exe --bind 127.0.0.1 --port 6379 --protected-mode yes
```

### 商城后端

后端必须显式连接测试库，禁止使用配置文件默认的 `ry-vue`：

```powershell
Set-Location C:\Users\Administrator\Desktop\RuoYiWork\RuoYi-Vue-master
$env:RUOYI_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/mall_migration_test_20260728?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai'
$env:RUOYI_DATASOURCE_USERNAME='root'
$env:RUOYI_DATASOURCE_PASSWORD='julietren76'
$env:MALL_SMS_MOCK_CODE='123456'
java -Xms256m -Xmx1024m -jar ruoyi-admin\target\ruoyi-admin.jar --server.port=8080
```

健康检查：

```powershell
Invoke-RestMethod http://127.0.0.1:8080/api/mall/health
```

### 商城用户端

```powershell
Set-Location C:\Users\Administrator\Desktop\RuoYiWork\RuoYi-Vue-master\mall-storefront
npm.cmd run dev -- --host 0.0.0.0
```

访问：`http://localhost:5174/`。

停止临时进程时，只停止对应 Java、Node 或 Redis 进程；不要停止 MySQL84 服务、删除测试库或清理 `ai-web/`。

## 10. 新会话入口

新会话可直接发送：

```text
请先读取 C:\Users\Administrator\Desktop\RuoYiWork\RuoYi-Vue-master\PROJECT_HANDOFF.md 和 CONTEXT.md。商城 V0.1、V0.2、V0.3 模块 1 及基础加固已完成并通过回归，当前没有进行中的需求。请先根据我接下来的明确需求评估范围；保留 ai-web/ 和 prototypes/mall-v0.3-visual-prototype/，不要修改 ry-vue，不要清理工作区，也不要擅自发布或打包 Docker。
```

## 11. 2026-07-30 完成记录：V0.3 模块 2 手工物流与收货

V0.3 模块 2 已完成并通过回归。正式运营端统一使用 `ruoyi-ui` 若依后台，独立 `mall-admin` 不再作为正式运营端。

- 完成一单一物流单、后台手工发货、固定节点追加：已发货、运输中、派送中、已签收、运输异常、更正说明。
- 追加节点服务端校验时间顺序；已签收后禁止继续追加；关键后台写操作写入订单操作审计日志。
- 会员可查询本人物流并主动确认收货；签收 7 天后 Quartz 自动确认收货。
- 若依运营端新增物流履约菜单和接口：`GET /mall/logistics/orders`、`POST /mall/logistics/orders/{orderId}/ship`、`GET /mall/logistics/shipments/{shipmentId}`、`POST /mall/logistics/shipments/{shipmentId}/nodes`。
- 用户端订单详情展示物流时间线和确认收货入口。
- 数据库迁移为 `sql/V2.9.0__mall_manual_logistics.sql`，回滚脚本为 `sql/rollback/V2.9.0__mall_manual_logistics_rollback.sql`。

验证结果：`mvn -pl mall-business -am test` 75 项通过；`mvn -pl ruoyi-admin -am package -DskipTests` 通过；商城用户端构建通过；若依开发端 `http://localhost:8081` 编译成功；后端 `http://localhost:8080/api/mall/health` 返回 `code=200/status=UP`；V2.9.0 在 `mall_migration_test_20260728` 完成回滚、升级、重复升级验证，Quartz 任务保持 1 条、物流权限保持 4 条。

补充修复：V2.9.0 菜单名称曾因使用错误字符集执行迁移显示为 `????`。迁移脚本已增加基于 UTF-8 字节值的幂等修复；测试库 3960-3963 已恢复为“物流履约、物流查询、订单发货、追加轨迹”。若依后台刷新页面即可看到正确名称。

物流公司约束补充：手工发货已改为固定物流公司下拉选择；后端同步使用白名单校验并按编码反查公司名称，忽略客户端自带的公司名称，防止自由填写或前后端名称不一致。当前支持顺丰速运、中通快递、圆通速递、申通快递、韵达速递、京东物流、中国邮政 EMS、极兔速递、德邦快递；`MOCK` 仅保留测试用途，不在正式下拉框展示。

## V0.4 待办：运营端菜单信息架构调整

用户已确认：V0.4 一并评估将“物流履约”和“库存管理”归入“会员管理”业务分组。当前 V0.3 保持现有菜单层级，不提前改动。

- V0.4 需要同时调整若依菜单父级、菜单排序、前端路由展示和 RBAC 权限继承关系。
- 需要设计版本化 SQL 迁移与回滚，兼容已有菜单 ID、权限标识和历史授权数据。
- 需要验证管理员已有角色权限不丢失，并做桌面端菜单、物流履约、库存管理真实链路回归。

## 12. 2026-07-30 完成记录：V0.3 基础加固

在继续 V0.3 UI、物流和个人中心之前，已完成架构审查发现的高风险问题加固。代码、自动化测试、真实链路验证和收尾复查均已完成，本次随交接文档一并提交到 `dev`。

- 支付与退款 Port 增加稳定的支付单号/退款单号幂等键；Mock Adapter 对相同键返回稳定的通道流水号。
- 售后退款和异常支付退款拆分为“事务内准备 -> 通道调用 -> 事务内落库”，外部 I/O 不再占用订单、支付或退款行锁；通道结果不完整时保留 `REFUNDING` 以便使用同一幂等键安全重试。
- 创建订单前锁定会员行后再统计进行中未支付订单，保证“最多 3 笔”在并发下单时为原子约束。
- 短信验证码错误次数迁移到 `REQUIRES_NEW` 事务，避免登录异常回滚失败计数。
- 商城前后端改用 `X-Mall-Authorization`；若依后台仍使用 `Authorization`，两种 Token 不再被同一 JWT 过滤器混淆。
- 新增状态持久化 module：`MallRefundApprovalStateService`、`MallLatePaymentRefundStateService`、`MallSmsVerificationAttemptService`。

已完成验证：

- `mvn -pl mall-business -am test`：67 项通过。
- `npm.cmd run build`（`mall-storefront`）：通过。
- `mvn -pl ruoyi-admin -am package -DskipTests`：通过。
- `/api/mall/health` 健康检查通过；真实登录、下单、支付与退款链路已使用 `X-Mall-Authorization` 验证通过。
- 收尾差异检查通过；`ai-web/`、`prototypes/mall-v0.3-visual-prototype/` 及用户私人会话资料均未纳入提交。

## 13. 2026-07-30 完成记录：V0.3 模块 3 订单项售后

V0.3 模块 3 已完成并通过回归。售后由原整单退款扩展为订单项级流程，正式运营端继续使用 `ruoyi-ui` 若依后台。

- 会员可针对已发货或已完成的已支付订单申请仅退款、退货退款；支持多订单项和每项数量。
- 服务端按订单项快照计算商品退款金额；仅整单全数量申请时退运费，前端金额不参与计算。
- 对超数量、活跃售后重复占用和超过支付后 7 天期限执行硬拦截；质量问题必须提供凭证，但全数量质量退货不标记为恶意。
- 退货物流只接受顺丰、中通、圆通、申通、韵达、京东物流、EMS、极兔和德邦固定编码；商城端使用下拉选择。
- 若依运营端支持列表、详情、审核通过、驳回和退款；退货退款必须先审核、由会员提交物流，再由运营确认收到退货并退款。
- 退款保持“事务内准备 -> 通道调用 -> 事务内落库”，以售后单号作为稳定退款幂等键；通道结果不完整时保留 `REFUNDING` 供安全重试。
- 新增迁移 `sql/V2.10.0__mall_item_after_sale.sql` 和回滚脚本，创建 `mall_item_after_sale`、`mall_item_after_sale_item` 及 3 条若依菜单权限。
- 修复订单加锁查询遗漏 `pay_time` 的问题，真实数据库中的售后期限校验现在按支付时间生效。

验证结果：

- `mvn -pl mall-business -am test`：85 项通过，0 失败。
- 商城用户端与若依运营端生产构建均通过；若依保留项目既有的 3 项警告。
- 既有会员登录、商品、购物车、地址、结算、下单、支付链路 17 项通过。
- 真实订单项售后链路通过：申请退货退款 -> 审核通过 -> 提交顺丰退货物流 -> 确认收到并退款 -> `SUCCESS`；另验证重复数量拒绝和运营驳回。
- V2.10 在 `mall_migration_test_20260728` 完成回滚、升级、重复升级，最终 2 张表、3 条菜单、权限无重复；原库 `ry-vue` 未修改。
- Edge 1440px/390px 检查商城订单详情和若依售后页，均无页面横向溢出、控制台错误或请求失败；若依移动端表格可横向查看完整字段。

下一步：等待用户确认后进入 V0.3 模块 4“个人资料与头像”。

## 14. 2026-07-30 会话暂停点

- V0.3 模块 3 已提交并推送：`38e0112 feat: 完成V0.3订单项售后模块`。
- 当前 `dev` 与 `origin/dev` 一致，工作区只保留用户未跟踪的 `ai-web/`、视觉原型和会话说明。
- 明日继续时先确认服务与测试库状态，再从 V0.3 模块 4“个人资料与头像”开始设计和开发。
- 模块 4 必须独立完成迁移、后端测试、前端构建、真实链路、Edge 桌面/移动检查和 Git 提交后，才能进入模块 5。
