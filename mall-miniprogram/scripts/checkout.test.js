const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const source = ts.transpileModule(fs.readFileSync(path.join(__dirname, '../utils/checkout.ts'), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
const moduleValue = { exports: {} }
vm.runInNewContext(source, { module: moduleValue, exports: moduleValue.exports, require: value => {
  if (value === './product') return { localProductImage: image => image === '/assets/chair.jpg' ? image : '/assets/backpack.jpg', formatPrice: value => Number(value || 0).toFixed(2) }
  return require(value)
} }, { filename: 'utils/checkout.ts' })
const { createIdempotencyKey, normalizeAddress, normalizeCheckout } = moduleValue.exports

const key = createIdempotencyKey()
assert.match(key, /^mp-order-\d+-[a-f0-9]{24}$/)
assert.ok(key.length <= 80)
const address = normalizeAddress({ addressId: 4, receiverName: '测试用户', receiverPhone: '13800138000', province: '广东省', city: '深圳市', district: '南山区', detailAddress: '科技园', isDefault: '1' })
assert.strictEqual(address.displayReceiver, '测试用户 13800138000')
assert.strictEqual(address.displayAddress, '广东省 深圳市 南山区 科技园')
assert.strictEqual(address.defaultFlag, true)
const preview = normalizeCheckout({
  addresses: [{ addressId: 4, receiverName: '测试用户', receiverPhone: '13800138000', province: '广东省', city: '深圳市', district: '南山区', detailAddress: '科技园', isDefault: '1' }],
  items: [{ skuId: 11, productName: '轻量城市跑鞋', productImage: '/assets/chair.jpg', unitPrice: 299, quantity: 2, lineAmount: 598 }],
  coupons: [{ memberCouponId: 8, couponName: '新人券', discountAmount: 20, thresholdAmount: 199, status: 'AVAILABLE' }, { memberCouponId: 9, status: 'USED' }],
  productAmount: 598, shippingFee: 0, discountAmount: 20, payableAmount: 578, canSubmit: true, validationMessages: [], selectedMemberCouponId: 8
})
assert.strictEqual(preview.defaultAddressId, 4)
assert.strictEqual(preview.itemCount, 2)
assert.strictEqual(preview.items[0].displayLineAmount, '598.00')
assert.strictEqual(preview.coupons.length, 1)
assert.strictEqual(preview.selectedMemberCouponId, 8)
assert.strictEqual(preview.payableAmount, '578.00')
const pageSource = fs.readFileSync(path.join(__dirname, '../pages/checkout/index.ts'), 'utf8')
assert.match(pageSource, /idempotencyKey: this\.data\.idempotencyKey/)
assert.match(pageSource, /selectedAddressExists \? this\.data\.selectedAddressId : preview\.defaultAddressId/)
assert.match(pageSource, /pages\/addresses\/index\?from=checkout&create=1/)
assert.match(pageSource, /addressBookOpened/)
assert.doesNotMatch(pageSource, /createPayment|mockPayment/)
const markup = fs.readFileSync(path.join(__dirname, '../pages/checkout/index.wxml'), 'utf8')
assert.match(markup, /订单号 \{\{order\.orderNo\}\}/)
assert.match(pageSource, /wx\.redirectTo\(\{ url: `\/pages\/orders\/detail\/index\?orderId=\$\{orderId\}` \}\)/)
assert.match(markup, /查看订单详情/)
assert.doesNotMatch(markup, /下一切片接入/)
console.log('结算组装测试通过：幂等键、地址、商品、优惠券和服务端金额汇总。')
