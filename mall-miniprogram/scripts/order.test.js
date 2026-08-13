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
  if (value === './product') return { localProductImage: image => image === '/assets/sneaker.jpg' ? image : '/assets/backpack.jpg', formatPrice: value => Number(value || 0).toFixed(2) }
  return require(value)
} }, { filename: 'utils/order.ts' })
const { normalizeOrder, normalizePayments, paymentDeadline, countdown, statusLabel, createPaymentKey } = moduleValue.exports

const order = normalizeOrder({
  orderId: 8, orderNo: 'M202608130001', status: 'PENDING_PAYMENT', paymentStatus: 'UNPAID', payableAmount: 299,
  productAmount: 319, shippingFee: 0, discountAmount: 20, createTime: '2026-08-13T12:00:00', paymentCreateDeadline: '2026-08-13T12:30:00',
  items: [{ orderItemId: 1, productName: '轻量城市跑鞋', productImage: '/assets/sneaker.jpg', unitPrice: 299, quantity: 1, lineAmount: 299 }],
  operations: [{ operationId: 1, toStatus: 'PENDING_PAYMENT', remark: '会员创建订单', createTime: '2026-08-13T12:00:00' }]
})
assert.strictEqual(order.statusLabel, '待付款')
assert.strictEqual(order.paymentLabel, '未支付')
assert.strictEqual(order.displayAmount, '299.00')
assert.strictEqual(order.itemSummary, '轻量城市跑鞋')
assert.strictEqual(order.operations[0].statusLabel, '待付款')
assert.strictEqual(countdown(paymentDeadline(order), new Date('2026-08-13T12:29:00').getTime()), '01:00')
assert.strictEqual(statusLabel({ status: 'CLOSED', cancelReason: '支付超时自动关闭' }), '支付超时已关闭')
const payments = normalizePayments([{ paymentId: 1, status: 'PAYING', amount: 299, createTime: '2026-08-13T12:01:00' }])
assert.strictEqual(payments[0].statusLabel, '等待支付')
assert.match(createPaymentKey(), /^mp-payment-\d+-[a-f0-9]{16}$/)

const detailSource = fs.readFileSync(path.join(__dirname, '../pages/orders/detail/index.ts'), 'utf8')
const detailMarkup = fs.readFileSync(path.join(__dirname, '../pages/orders/detail/index.wxml'), 'utf8')
const listSource = fs.readFileSync(path.join(__dirname, '../pages/orders/index.ts'), 'utf8')
const listMarkup = fs.readFileSync(path.join(__dirname, '../pages/orders/index.wxml'), 'utf8')
assert.match(detailSource, /this\.data\.orderId, this\.data\.paymentIdempotencyKey/)
assert.match(detailSource, /env\.getEnvVersion\(\) === 'develop'/)
assert.match(detailSource, /!this\.data\.developmentMode \|\| !this\.data\.order\?\.canCreatePayment/)
assert.match(detailSource, /this\.refreshedDeadline !== deadline/)
assert.match(detailSource, /actionError/)
assert.match(detailMarkup, /developmentMode && order\.canCreatePayment/)
assert.match(detailMarkup, /paymentCreateDeadline|剩余/)
assert.match(detailMarkup, /class="action-error"/)
assert.match(listSource, /loggedIn: false/)
assert.doesNotMatch(listSource, /getTabBar/)
assert.match(listMarkup, /data-status="SHIPPED"/)
assert.match(listMarkup, /登录后查看订单/)
console.log('订单与支付测试通过：状态、快照、金额、倒计时、支付记录、开发模拟支付和幂等键。')
