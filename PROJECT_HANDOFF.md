# 商城系统项目交接总览

> 新会话开始时，请先完整阅读本文件，再读取 `CONTEXT.md` 和本文件列出的相关文档。
>
> 最后更新：2026-07-28。商城 V0.2 前五项已完成真实跑通，第五项等待用户验收；**不得开始第六项个人中心与地址簿体验优化**，除非用户明确授权。

## 1. 项目定位

这是基于 RuoYi-Vue 的前后端分离商城测试项目。后台运营端复用若依能力，商城用户端为独立 Vue 3 应用；商城业务后端以独立模块接入若依工程。

当前目标是将 V0.1 的基础交易闭环升级为 V0.2：优先完善用户端商品浏览、商品详情和加购体验，风格保持简洁生活方式商城，但需提升信息层次、反馈和交互。

## 2. 仓库与环境

| 项目项 | 当前值 |
| --- | --- |
| 仓库目录 | `C:\Users\Administrator\Desktop\RuoYiWork\RuoYi-Vue-master` |
| Git 分支 | `dev` |
| 远程仓库 | `https://github.com/feng111415/mall-system.git` |
| 当前已提交基线 | `ddb9547 test: 增加商城前台全链路验收脚本` |
| 商城用户端 | `http://localhost:5174` |
| 商城后端 | `http://localhost:8080` |
| 后台联调后端 | `http://localhost:18080` |
| 若依后台 | `http://localhost:18081` |
| Redis | `127.0.0.1:6379` |
| 测试数据库 | `mall_migration_test_20260728` |
| Mock 短信验证码 | `123456` |

当前通常应有以下端口监听：`5174`、`6379`、`8080`、`18080`、`18081`。

### 重要边界

- 不得修改原业务库 `ry-vue`；迁移与验证使用独立测试库。
- `ai-web/` 是用户的未跟踪目录，必须保留、忽略，禁止提交、删除或重构。
- V0.2 仍有工作区修改和暂存内容，尚未统一提交。不要为了清理工作区执行 reset、checkout 或删除未跟踪文件。
- 当前只是测试项目，用户明确说暂时不打包；不要擅自构建 Docker 镜像或发布。

## 3. 技术架构与目录

```text
mall-storefront (Vue 3 + Vite，商城用户端)
       |
       | /api/mall/**，商城独立 Bearer Token
       v
mall-business (Spring Boot 商城业务模块)
       |
       +-- Controller -> Service -> Mapper -> MySQL
       +-- Redis（验证码、缓存、Token 等基础能力）
       +-- 复用 RuoYi 的框架、安全、任务、日志等通用能力

ruoyi-ui / ruoyi-admin (若依后台运营端)
```

关键目录：

```text
mall-storefront/                         商城用户端
mall-business/                           商城业务后端
sql/                                     版本化数据库迁移
scripts/mall-storefront-e2e.ps1          API 全链路验收脚本
doc/                                     需求、架构、验收和开发记录
doc/development/mall-v0.2-api-contract.md V0.2 商品接口契约
```

完整架构说明和图：`doc/商城系统-架构设计说明书.md`、`doc/architecture/`。

## 4. 开发规则（必须遵守）

### 后端分层与接口

- 固定分层：`Controller -> Service -> Mapper`，不得在 Controller 直接写业务或 SQL。
- 商城用户端接口统一以 `/api/mall/**` 开头，返回统一使用 `AjaxResult`。
- 会员身份使用商城独立的 Bearer Token，不混用后台管理员登录态。
- 写操作使用 DTO、`@Valid` 校验和事务；DTO 不能被 Controller 绕过。
- 数据库变更必须使用可重复验证的版本化 SQL 迁移，不手工改测试库结构。
- 客户端传入的排序字段只能映射到服务端白名单，禁止将字段原样拼接进 SQL。
- 收货地址的查询、修改、删除、设默认和下单必须校验地址归属当前会员。
- 下单和支付必须保留幂等控制、库存二次校验；防止重复点击、刷单和超卖。

### 安全与治理

- 短信验证码登录需考虑手机号格式合规、验证码时效、次数限制和错误次数限制。
- Token 不在 URL、日志或响应中泄露；接口按身份和资源归属鉴权。
- 关键写操作保留操作日志；版本通过 Git 分支、标签、提交与发布脚本管理，可回滚。
- 后台权限采用 RuoYi 的 RBAC；商城会员权限与后台管理员权限隔离。

### 前端

- 用户端技术栈：Vue 3、Vite、Vue Router、Pinia（既有项目模式），不要引入与现有架构冲突的状态或请求方案。
- 风格：简洁生活方式商城，增强层次、状态反馈、加载骨架、空状态和移动端体验；不要做营销落地页式的大横幅堆叠。
- 适配桌面和移动端，移动端五栏底部导航可用且不得有横向溢出。
- 新接口、新页面必须按既有 API/Store/组件组织方式接入，不能把请求、业务判断散落在页面中。

## 5. V0.1 已有能力

V0.1 已完成基础商城链路：

- 手机号 + 短信验证码注册登录，固定 Mock 验证码为 `123456`。
- 商品、SKU、库存、购物车、地址、结算、订单、Mock 支付、订单列表和详情。
- 防重复下单/支付的幂等控制，库存扣减及二次校验。
- 后台运营模块、数据库迁移、基础风控与操作治理。
- API 全链路脚本已覆盖登录、浏览、加购、地址、结算、下单、幂等重放和支付。

历史资料按需要读取：

```text
doc/商城系统-需求分析说明书.md
doc/商城系统-架构设计说明书.md
doc/商城系统-订单状态设计.md
doc/商城系统-开发进度.md
doc/商城系统-验收记录.md
doc/商城系统-数据库迁移验证.md
doc/商城系统-前后端联调记录.md
doc/development/商城开发记录.md
```

## 6. V0.2 需求与执行顺序

已与用户确认的 V0.2 目标：

1. 用户端优先。
2. 必要时可补充后端接口，但必须遵循上述第一版架构和规则。
3. 测试库补充 8 个演示商品。
4. 在简洁生活方式风格上增强层次、反馈和互动。
5. 按阶段逐项完成和验证；当前等待第五项验收。

| 序号 | 阶段 | 状态 | 当前结果 |
| --- | --- | --- | --- |
| 1 | 接口与数据基线 | 已完成 | 商品契约、8 个演示 SPU、17 个 SKU、17 条库存快照 |
| 2 | 商品浏览 | 已完成 | 首页真实数据、分类、搜索、排序、骨架与空状态 |
| 3 | 全局框架 | 已完成 | 桌面导航、移动五栏、全局通知、购物车角标同步 |
| 4 | 商品详情与加购 | 已完成并验收 | SKU/数量/库存、加购反馈、相关推荐、最近浏览 |
| 5 | 购物车与结算体验 | 已完成，待用户验收 | 全选、数量与库存反馈、失效清理、结算步骤、地址、备注、支付结果 |
| 6 | 个人中心与地址簿体验 | 未开始 | 地址编辑、删除、默认地址等体验优化 |
| 7 | 视觉验收、回归、提交 | 未开始 | 最后统一审查、回归、Git 提交 |

## 7. V0.2 已完成内容

### 7.1 数据与契约

新增文件：

```text
sql/V2.7.0__mall_v0_2_catalog_seed.sql
doc/development/mall-v0.2-api-contract.md
```

迁移已在独立测试库重复执行验证。当前数据：8 个已上架 SPU、17 个有效 SKU、17 条库存快照。商品覆盖家居、数码、穿搭、咖啡、出行，包含正常库存、低库存与售罄 SKU。

### 7.2 商品浏览接口

扩展接口：

```text
GET /api/mall/catalog/products
参数：categoryId、keyword、sort
sort 白名单：default、sales、priceAsc、priceDesc
```

涉及文件：

```text
mall-business/src/main/java/com/ruoyi/mall/domain/product/dto/MallCatalogProductQuery.java
mall-business/.../MallProductPortalController.java
mall-business/.../IMallProductService.java
mall-business/.../MallProductServiceImpl.java
mall-business/.../MallProductMapper.java
mall-business/src/main/resources/mapper/mall/product/MallProductMapper.xml
mall-business/.../MallProductServiceImplTest.java
```

已验证销量、价格升序和关键字筛选，非法 `sort` 会回退默认排序，未进入动态 SQL。

### 7.3 用户端界面

新增：

```text
mall-storefront/src/components/StoreProductCard.vue
mall-storefront/src/stores/notice.js
```

主要改动：

```text
mall-storefront/src/App.vue
mall-storefront/src/stores/cart.js
mall-storefront/src/styles/phase-two.css
mall-storefront/src/views/HomeView.vue
mall-storefront/src/views/CatalogView.vue
mall-storefront/src/views/ProductDetailView.vue
mall-storefront/src/views/CartView.vue
mall-storefront/src/views/AccountView.vue
```

当前页面能力：

- 首页展示真实热销商品、分类场景和加载骨架。
- 商品列表支持关键词、分类、综合/销量/价格排序和空状态。
- 全局搜索可清除，顶部购物车角标会同步；有统一成功/失败通知。
- 商品详情默认选择可售 SKU；SKU 切换时价格和库存同步；售罄 SKU 禁止加购；数量限制为 `1..库存`；重复点击有防护。
- 商品详情会安全清理 `detailHtml` 后再渲染，展示相关推荐，并将最近浏览商品 ID 写入 localStorage。
- 购物车的数量修改、选择和删除已有全局反馈；登录/退出与 Token 失效会同步清空或刷新角标。

### 7.4 购物车与结算体验

- 购物车支持全选/取消全选、按商品更新数量、移除商品、清理失效商品和手动刷新。
- 商品行明确展示可售、低库存、库存不足和失效状态，以及单价、数量和小计。
- 结算栏展示已选件数、合计金额和不能结算的具体原因；移动端结算栏不被通知或底部导航遮挡。
- 结算页增加确认订单、模拟支付、支付完成三步状态；地址默认标记、商品金额拆分和订单汇总更清晰。
- 订单备注随下单请求提交，限制 200 字；支付完成后可直接进入订单中心。
- 没有新增后端或数据库能力，继续复用既有购物车、结算、订单幂等和 Mock 支付接口。

## 8. 已完成验证

后端单元测试：

```powershell
mvn -pl mall-business -am test
```

最近完整结果：`53 tests passed`。

前端构建：

```powershell
Set-Location mall-storefront
npm.cmd run build
```

结果：通过。

API 全链路验收：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/mall-storefront-e2e.ps1
```

最近多次通过，覆盖：路由可访问、后端健康检查、匿名购物车拦截、短信登录、商品/SKU 浏览、重复加购后的数量正确为 2、无地址结算拦截、地址新增与查询、结算预览、订单创建、订单幂等重放、Mock 支付、订单详情与列表。

已使用本机 Edge 无头截图检查首页、商品列表、商品详情的桌面/移动布局和移动端五栏导航。截图临时位于 `C:\Windows\Temp\mall-*.png`。

第五项浏览器验收已真实跑通：两种商品加购、数量更新、全选切换、结算预览、默认地址、订单备注、订单创建、Mock 支付和订单备注落库均通过；桌面与 390px 移动布局无横向溢出或关键元素重叠。

## 9. 当前必须执行的工作：第五项用户验收

第五项已完成开发、自动化回归和真实 Edge 浏览器验收，当前等待用户查看页面并确认结果；未获得明确授权前不得开始第六项。

第五项已确认：

1. 两种 SKU 可同时选择，数量、单价、小计和总金额联动。
2. 全选和取消全选会同步更新结算按钮状态。
3. 移动端购物车无横向溢出，通知不遮挡结算栏。
4. 结算页正确展示地址、商品、费用、备注和三步进度。
5. 订单备注已通过订单详情接口确认落库。
6. 模拟支付完成后进度进入第三步，并可进入订单中心。

## 10. 当前 Git 工作区处理原则

以下 V0.2 文件已暂存：

```text
doc/development/mall-v0.2-api-contract.md
sql/V2.7.0__mall_v0_2_catalog_seed.sql
```

其余 V0.2 代码修改仍未暂存，V0.2 尚未提交。后续只能在完成第 1 至第 5 项用户验收、进行代码审查和完整回归后，再由用户确认是否统一提交至 `dev`。

不得处理的未跟踪目录：`ai-web/`。

## 11. 新会话建议开场指令

新会话可直接发送：

```text
请先读取 C:\Users\Administrator\Desktop\RuoYiWork\RuoYi-Vue-master\PROJECT_HANDOFF.md 和 CONTEXT.md，继续商城 V0.2。第五项购物车与结算体验已完成并等待用户验收，不要开始第六项；保留 ai-web/，不要修改 ry-vue，也不要清理或提交现有工作区。
```
