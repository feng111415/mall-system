# 商城 V0.2 接口契约

本文只定义用户端 V0.2 新增或扩展的接口边界。接口仍位于 `/api/mall`，返回统一使用 `AjaxResult`，会员身份仍通过商城独立 Bearer Token 解析，不复用若依后台登录态。

## 商品浏览

`GET /api/mall/catalog/products`

| 参数 | 必填 | 规则 | 说明 |
| --- | --- | --- | --- |
| `categoryId` | 否 | 正整数 | 一级分类 ID。 |
| `keyword` | 否 | 去除首尾空白后最多 50 字符 | 匹配商品名称、品牌名和分类名。旧参数 `productName` 继续兼容。 |
| `sort` | 否 | `default`、`sales`、`priceAsc`、`priceDesc` | 服务端白名单排序，缺省为 `default`。 |

该接口保持匿名可访问，只返回已上架、未删除商品。排序字段在服务层转换为固定 SQL 分支，严禁将客户端字段直接拼接到 SQL。列表只返回商品展示所需信息；SKU 规格和实时可售库存仍通过详情接口获取。

`GET /api/mall/catalog/products/{spuId}`

保持匿名可访问，仅允许读取已上架商品。返回商品详情、图片列表和 SKU 可售数量，实际加购、结算和下单仍会在服务端再次校验库存。

## 地址簿

现有接口满足 V0.2 地址管理，不新增并行接口：

| 方法 | 路径 | 规则 |
| --- | --- | --- |
| `GET` | `/api/mall/member/addresses` | 返回当前会员的未删除地址，默认地址排在最前。 |
| `POST` | `/api/mall/member/addresses` | 新增地址；`isDefault=1` 时清除该会员其他默认地址。 |
| `PUT` | `/api/mall/member/addresses/{addressId}` | 仅能修改当前会员自己的地址。 |
| `DELETE` | `/api/mall/member/addresses/{addressId}` | 仅能逻辑删除当前会员自己的地址。 |

所有地址写操作都需商城 Bearer Token、`@Valid` 参数校验和会员归属校验。地址没有单独的“设默认”接口，前端通过更新接口提交完整地址和 `isDefault=1`，避免出现两套默认地址写入路径。

## 订单备注

订单创建接口已有 `remark` 字段。V0.2 前端会在 `POST /api/mall/orders` 的原有请求体中提交该字段，不引入单独的订单备注接口，也不改变订单幂等键规则。
