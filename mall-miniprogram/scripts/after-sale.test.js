const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const source = ts.transpileModule(fs.readFileSync(path.join(__dirname, '../utils/order.ts'), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
const moduleValue = { exports: {} }
vm.runInNewContext(source, { module: moduleValue, exports: moduleValue.exports, require: value => {
  if (value === './product') return { localProductImage: value => value, formatPrice: value => Number(value || 0).toFixed(2) }
  return require(value)
} }, { filename: 'utils/order.ts' })
const { normalizeAfterSale, remainingAfterSaleQuantity } = moduleValue.exports

const afterSale = normalizeAfterSale({
  afterSaleId: 8,
  type: 'RETURN_REFUND',
  status: 'APPROVED',
  reasonCode: 'QUALITY',
  refundAmount: 299,
  shippingRefundAmount: 12,
  createTime: '2026-08-13T13:00:00',
  items: [{ afterSaleItemId: 1, orderItemId: 9, productName: '轻量城市跑鞋', requestedQuantity: 1, refundAmount: 299 }]
})
assert.strictEqual(afterSale.typeLabel, '退货退款')
assert.strictEqual(afterSale.statusLabel, '审核通过')
assert.strictEqual(afterSale.reasonLabel, '质量问题')
assert.strictEqual(afterSale.displayRefundAmount, '299.00')
assert.strictEqual(afterSale.displayShippingRefundAmount, '12.00')
assert.strictEqual(afterSale.items[0].displayRefundAmount, '299.00')
assert.strictEqual(afterSale.canSubmitTracking, true)
assert.strictEqual(normalizeAfterSale({ type: 'ONLY_REFUND', status: 'APPROVED' }).canSubmitTracking, false)
assert.strictEqual(normalizeAfterSale({ type: 'RETURN_REFUND', status: 'RETURN_SHIPPED' }).canSubmitTracking, false)

const records = [
  { status: 'PENDING_REVIEW', items: [{ orderItemId: 9, requestedQuantity: 1 }] },
  { status: 'REJECTED', items: [{ orderItemId: 9, requestedQuantity: 2 }] },
  { status: 'SUCCESS', items: [{ orderItemId: 9, requestedQuantity: 1 }] }
]
assert.strictEqual(remainingAfterSaleQuantity(9, 4, records), 2)
assert.strictEqual(remainingAfterSaleQuantity(10, 2, records), 2)

const apiSource = fs.readFileSync(path.join(__dirname, '../services/mallApi.ts'), 'utf8')
const detailSource = fs.readFileSync(path.join(__dirname, '../pages/after-sales/detail/index.ts'), 'utf8')
const detailMarkup = fs.readFileSync(path.join(__dirname, '../pages/after-sales/detail/index.wxml'), 'utf8')
const listSource = fs.readFileSync(path.join(__dirname, '../pages/after-sales/index.ts'), 'utf8')
const orderDetailSource = fs.readFileSync(path.join(__dirname, '../pages/orders/detail/index.ts'), 'utf8')
const orderDetailMarkup = fs.readFileSync(path.join(__dirname, '../pages/orders/detail/index.wxml'), 'utf8')
const profileMarkup = fs.readFileSync(path.join(__dirname, '../pages/profile/index.wxml'), 'utf8')

assert.match(apiSource, /\/api\/mall\/after-sales/)
assert.match(apiSource, /\/api\/mall\/orders\/\$\{orderId\}\/after-sales/)
assert.match(apiSource, /\/return-tracking/)
assert.match(detailSource, /reasonCode === 'QUALITY' && !this\.data\.evidenceUrl/)
assert.match(detailSource, /items: \[\{ orderItemId: this\.data\.selectedItem\.orderItemId, quantity: this\.data\.requestedQuantity \}\]/)
assert.doesNotMatch(detailSource, /refundAmount\s*:/)
assert.match(detailSource, /LOGISTICS_COMPANIES\[this\.data\.companyIndex\]/)
assert.match(detailMarkup, /退款金额由服务端/)
assert.match(detailMarkup, /afterSale\.canSubmitTracking/)
assert.match(listSource, /mallApi\.getAfterSales/)
assert.match(orderDetailSource, /remainingAfterSaleQuantity/)
assert.match(orderDetailMarkup, /申请售后/)
assert.match(orderDetailMarkup, /latestAfterSale\.statusLabel/)
assert.match(profileMarkup, /bindtap="openAfterSales"/)

console.log('订单项售后测试通过：状态、数量占用、质量凭证、服务端金额、申请与退货物流契约。')
