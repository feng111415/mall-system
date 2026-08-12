# 商城微信小程序

V0.6 使用原生微信小程序实现，与 `mall-storefront/` 相互独立，共享现有 `/api/mall/**` 后端契约。

## 本地运行

1. 安装微信开发者工具。
2. 导入本目录 `mall-miniprogram/`。
3. 没有正式 AppID 时使用测试号；取得 AppID 后替换 `project.config.json` 中的 `appid`。
4. 本地开发默认请求 `http://localhost:8080`，开发者工具中需关闭合法域名校验。

体验版和正式版不会回退到本机地址。发布前必须在 `config/env.js` 为 `trial`、`release` 配置已备案且加入微信后台白名单的 HTTPS API 域名。

## 工程边界

- `utils/request.js` 统一处理 `X-Mall-Authorization`、设备标识、HTTP/业务错误和登录失效。
- `services/mallApi.js` 是页面访问商城接口的统一模块。
- 页面不得计算订单金额、库存扣减、支付时限或退款金额。
- 首页采用已批准的 B 方案；当前非首页 Tab 页是基础工程占位，将由后续纵向业务链路替换。
