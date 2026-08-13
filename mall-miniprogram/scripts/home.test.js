const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const source = ts.transpileModule(fs.readFileSync(path.join(__dirname, '../utils/home.ts'), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
const moduleValue = { exports: {} }
vm.runInNewContext(source, { module: moduleValue, exports: moduleValue.exports, require }, { filename: 'utils/home.ts' })
const { normalizeHome, summarizeOrders, localAsset, formatPrice } = moduleValue.exports

const catalog = [
  { spuId: 6, productName: '销量第一', mainImage: '/assets/sneaker.jpg', priceMin: 299, salesCount: 241 },
  { spuId: 4, productName: '运营推荐', mainImage: '/assets/headphones.jpg', priceMin: 699.5, salesCount: 203 },
  { spuId: 2, productName: '销量补位', mainImage: '/assets/lamp.jpg', priceMin: 269, salesCount: 128 }
]

const result = normalizeHome({
  hero: { heroTitle: '后台真实标题', heroPrimaryImage: '/assets/sneaker.jpg' },
  tickerItems: [{ label: 'NOTICE', text: '后台公告' }],
  recommendation: {
    recommendationTitle: '后台推荐',
    recommendationSpuIds: [4, 999],
    recommendationDisplayCount: 3
  }
}, catalog)

assert.strictEqual(result.hero.heroTitle, '后台真实标题')
assert.strictEqual(result.hero.displayImage, '/assets/sneaker.jpg')
assert.strictEqual(result.tickers[0].text, '后台公告')
assert.strictEqual(result.recommendationTitle, '后台推荐')
assert.deepStrictEqual(Array.from(result.products, item => item.spuId), [4, 6, 2])
assert.strictEqual(result.products[0].displayPrice, '699.50')
assert.strictEqual(result.products[0].badge, '本周热销')

const orderSummary = summarizeOrders([
  { status: 'PENDING_PAYMENT' },
  { status: 'PENDING_SHIPMENT' },
  { status: 'SHIPPED' },
  { status: 'COMPLETED' },
  { displayStatus: 'AFTER_SALE', status: 'SHIPPED' },
  { status: 'CANCELLED' }
])
assert.strictEqual(orderSummary.pendingPayment, 1)
assert.strictEqual(orderSummary.pendingShipment, 1)
assert.strictEqual(orderSummary.pendingReceipt, 1)
assert.strictEqual(orderSummary.pendingReview, 1)

assert.strictEqual(localAsset('/profile/untrusted.jpg'), '/assets/backpack.jpg')
assert.strictEqual(formatPrice(299), '299')
assert.strictEqual(formatPrice(299.9), '299.90')

const pageSource = fs.readFileSync(path.join(__dirname, '../pages/home/index.ts'), 'utf8')
const templateSource = fs.readFileSync(path.join(__dirname, '../pages/home/index.wxml'), 'utf8')
assert.match(templateSource, /bindconfirm="openCatalog"/)
assert.match(pageSource, /if \(!this\.data\.loggedIn\) return this\.openLogin\(\)/)
assert.match(pageSource, /wx\.showToast\(\{ title: `商品 \$\{spuId\} 详情将在下一切片开放`/)
console.log('首页数据组装测试通过：运营推荐顺序、销量补位、订单聚合、价格、图片回退和点击边界。')
