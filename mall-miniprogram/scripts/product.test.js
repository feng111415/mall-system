const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const source = ts.transpileModule(fs.readFileSync(path.join(__dirname, '../utils/product.ts'), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
const moduleValue = { exports: {} }
vm.runInNewContext(source, { module: moduleValue, exports: moduleValue.exports, require }, { filename: 'utils/product.ts' })
const { normalizeProductDetail, normalizeReviews, normalizeCart, detailText } = moduleValue.exports

const appConfig = JSON.parse(fs.readFileSync(path.join(__dirname, '../app.json'), 'utf8'))
const productPage = fs.readFileSync(path.join(__dirname, '../pages/product/index.ts'), 'utf8')
const productMarkup = fs.readFileSync(path.join(__dirname, '../pages/product/index.wxml'), 'utf8')
const cartPage = fs.readFileSync(path.join(__dirname, '../pages/cart/index.ts'), 'utf8')
const cartMarkup = fs.readFileSync(path.join(__dirname, '../pages/cart/index.wxml'), 'utf8')
assert.ok(appConfig.pages.includes('pages/product/index'))
assert.match(cartPage, /mallApi\.getCart\(\)/)
assert.match(cartPage, /updateCartQuantity/)
assert.match(cartPage, /updateCartSelected/)
assert.match(cartPage, /removeCartItem/)
assert.match(cartMarkup, /结算下一切片开放/)
assert.match(productPage, /wx\.navigateBack\(\)/)
assert.match(productPage, /selectedSkuId/)
assert.match(productMarkup, /加入购物车/)
assert.match(productPage, /addToCart/)
assert.doesNotMatch(productPage, /createOrder|checkout/)

const detail = normalizeProductDetail({
  spuId: 1,
  productName: '北欧原木餐椅',
  categoryName: '家居',
  brandName: '日常精选',
  mainImage: '/assets/chair.jpg',
  detailHtml: '<p>实木结构，<strong>圆润边角</strong>。</p><script>alert(1)</script>',
  salesCount: 86,
  skuList: [
    { skuId: 1, skuName: '原木色', price: 399, marketPrice: 459, availableStock: 0, status: '1' },
    { skuId: 2, skuName: '胡桃色', price: 439.5, availableStock: 2, status: '1' }
  ],
  mediaList: [{ mediaUrl: '/profile/untrusted.jpg', mediaType: 'IMAGE' }]
})

assert.strictEqual(detail.categoryBrand, '家居 · 日常精选')
assert.strictEqual(detail.selectedSkuId, 2)
assert.strictEqual(detail.displayPrice, '439.50')
assert.strictEqual(detail.selectedSku.stockLabel, '库存紧张，仅剩 2 件')
assert.strictEqual(detail.skus[0].soldOut, true)
assert.strictEqual(detail.gallery[0], '/assets/chair.jpg')
assert.strictEqual(detail.gallery[1], '/assets/backpack.jpg')
assert.strictEqual(detailText('<p>安全说明</p><img src=x onerror=alert(1)>'), '安全说明')

const reviews = normalizeReviews({
  summary: { reviewCount: 1, averageRating: 4.8, fiveStarCount: 1 },
  reviews: [{ reviewId: 7, rating: 5, content: '很好', createTime: '2026-08-01 10:00:00' }]
})
assert.strictEqual(reviews.summary.averageRating, '4.8')
assert.strictEqual(reviews.reviews[0].reviewerName, '匿名用户')
assert.strictEqual(reviews.reviews[0].stars, '★★★★★')
assert.strictEqual(reviews.reviews[0].displayDate, '2026-08-01')

const cart = normalizeCart({
  items: [
    { skuId: 2, quantity: 2, selectedFlag: '1', productName: '跑鞋', productImage: '/assets/sneaker.jpg', price: 299, availableStock: 16, valid: true, stockShortage: false, lineAmount: 598 },
    { skuId: 3, quantity: 4, selectedFlag: '0', productName: '失效商品', productImage: '/profile/untrusted.jpg', price: 9, availableStock: 0, valid: false, stockShortage: false, lineAmount: 36 }
  ], totalCount: 6, totalPrice: 598, canCheckout: true
})
assert.strictEqual(cart.items[0].selected, true)
assert.strictEqual(cart.items[0].displayLineAmount, '598')
assert.strictEqual(cart.items[1].statusLabel, '商品已失效')
assert.strictEqual(cart.items[1].displayImage, '/assets/backpack.jpg')
assert.strictEqual(cart.canCheckout, true)

console.log('商品详情组装测试通过：SKU 默认选择、实时库存、价格、图片回退、详情清洗和评价摘要。')
