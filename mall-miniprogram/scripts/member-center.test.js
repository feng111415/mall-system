const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const source = ts.transpileModule(fs.readFileSync(path.join(__dirname, '../utils/memberCenter.ts'), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
const moduleValue = { exports: {} }
vm.runInNewContext(source, { module: moduleValue, exports: moduleValue.exports, require: value => {
  if (value === './product') return { formatPrice: value => Number(value || 0).toFixed(2) }
  return require(value)
} }, { filename: 'utils/memberCenter.ts' })
const center = moduleValue.exports
const api = fs.readFileSync(path.join(__dirname, '../services/mallApi.ts'), 'utf8')
const couponsPage = fs.readFileSync(path.join(__dirname, '../pages/coupons/index.wxml'), 'utf8')
const messagesPage = fs.readFileSync(path.join(__dirname, '../pages/messages/index.wxml'), 'utf8')
const messagesSource = fs.readFileSync(path.join(__dirname, '../pages/messages/index.ts'), 'utf8')
const profilePage = fs.readFileSync(path.join(__dirname, '../pages/profile/index.wxml'), 'utf8')

const available = center.normalizeCoupon({ status: 'AVAILABLE', scopeType: 'ALL', thresholdAmount: 100, discountAmount: 20 })
assert.strictEqual(available.statusLabel, '可使用')
assert.strictEqual(available.scopeLabel, '全场商品')
assert.strictEqual(available.usable, true)
assert.strictEqual(center.normalizeCoupon({ status: 'AVAILABLE', templateStatus: 'PAUSED' }).statusLabel, '已暂停')
assert.strictEqual(center.normalizeCoupon({ status: 'AVAILABLE', validTo: '2020-01-01T00:00:00' }).statusLabel, '已过期')
assert.strictEqual(center.availableCouponCount([{ status: 'AVAILABLE' }, { status: 'USED' }]), 1)
assert.strictEqual(center.normalizeMessage({ readFlag: '0' }).unread, true)
assert.strictEqual(center.normalizeMessage({ readFlag: '1' }).unread, false)
assert.strictEqual(center.resolveMessageAction('/orders/12'), '/pages/orders/detail/index?orderId=12')
assert.strictEqual(center.resolveMessageAction('/after-sales/8'), '/pages/after-sales/detail/index?afterSaleId=8')
assert.strictEqual(center.resolveMessageAction('/account/privacy'), '/pages/profile/index')
assert.strictEqual(center.resolveMessageAction('https://evil.example'), '')
assert.match(api, /\/api\/mall\/coupons/)
assert.match(api, /\/api\/mall\/member\/messages\/summary/)
assert.match(api, /read-all/)
assert.match(couponsPage, /领取/)
assert.match(couponsPage, /服务端校验/)
assert.match(messagesPage, /全部已读/)
assert.match(messagesPage, /查看相关业务/)
assert.match(messagesSource, /markMessageRead/)
assert.match(messagesSource, /resolveMessageAction/)
assert.match(profilePage, /openCoupons/)
assert.match(profilePage, /openMessages/)
console.log('会员中心测试通过：优惠券状态、可用数量、消息已读、业务跳转白名单和页面绑定。')
