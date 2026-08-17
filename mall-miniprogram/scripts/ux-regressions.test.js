const assert = require('assert')
const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')
const read = relativePath => fs.readFileSync(path.join(root, relativePath), 'utf8')

const profileMarkup = read('pages/profile/index.wxml')
const profileStyles = read('pages/profile/index.wxss')
assert.match(profileMarkup, /class="login-card login-card-compact"/)
assert.match(profileStyles, /\.login-card-compact\s*\{[^}]*max-width:\s*340px/s)
assert.match(profileStyles, /\.login-card-compact\s+\.field\s*\{[^}]*min-height:\s*58px/s)

const productSource = read('pages/product/index.ts')
const productMarkup = read('pages/product/index.wxml')
const productStyles = read('pages/product/index.wxss')
assert.match(productSource, /feedbackTimer/)
assert.match(productSource, /setTransientFeedback\(/)
assert.match(productSource, /setTimeout\([^]*feedback:\s*''[^]*2500/s)
assert.match(productMarkup, /class="purchase-quantity"/)
assert.match(productMarkup, /class="bottom-bar"><button bindtap="openCatalog">继续逛逛<\/button><button class="primary"/)
assert.doesNotMatch(productMarkup, /class="buy-group"/)
assert.match(productMarkup, /class="sticky-header" style="padding-top: \{\{statusBarHeight\}\}px;"/)
assert.match(productMarkup, /class="feedback[^>]*style="top: \{\{statusBarHeight \+ 60\}\}px;"/)
assert.match(productStyles, /\.sticky-header\s*\{[^}]*position:\s*sticky[^}]*backdrop-filter:\s*blur\(14px\)/s)
assert.match(productStyles, /\.sku-section\s*\{[^}]*border-bottom:\s*0/s)
assert.match(productStyles, /\.quantity button\s*\{[^}]*display:\s*flex[^}]*align-items:\s*center[^}]*justify-content:\s*center/s)
assert.match(productStyles, /\.quantity text\s*\{[^}]*display:\s*flex[^}]*align-items:\s*center[^}]*justify-content:\s*center/s)
assert.match(productStyles, /\.feedback\s*\{[^}]*position:\s*fixed[^}]*pointer-events:\s*none/s)
assert.doesNotMatch(productStyles, /\.feedback\s*\{[^}]*bottom:/s)

const couponStyles = read('pages/coupons/index.wxss')
assert.match(couponStyles, /\.tabs button\s*\{[^}]*display:\s*flex[^}]*height:\s*34px[^}]*align-items:\s*center[^}]*justify-content:\s*center/s)

const catalogMarkup = read('pages/catalog/index.wxml')
const catalogStyles = read('pages/catalog/index.wxss')
assert.doesNotMatch(catalogMarkup, /category-scroll/)
assert.match(catalogMarkup, /class="category-grid"/)
assert.match(catalogMarkup, /class="category \{\{selectedCategoryId === item\.categoryId \? 'active' : ''\}\}"[^>]*bindtap="selectCategory"/)
assert.match(catalogStyles, /\.category-grid\s*\{[^}]*display:\s*grid[^}]*grid-template-columns:\s*repeat\(3,minmax\(0,1fr\)\)/s)

const cartStyles = read('pages/cart/index.wxss')
assert.match(cartStyles, /\.check\s*\{[^}]*display:\s*flex[^}]*width:\s*26px\s*!important[^}]*min-width:\s*26px\s*!important[^}]*align-items:\s*center[^}]*justify-content:\s*center[^}]*margin:\s*0\s*!important/s)
assert.match(cartStyles, /\.check::after\s*\{[^}]*display:\s*none/s)
assert.match(cartStyles, /\.quantity button\s*\{[^}]*display:\s*flex[^}]*align-items:\s*center[^}]*justify-content:\s*center/s)

const checkoutStyles = read('pages/checkout/index.wxss')
assert.match(checkoutStyles, /\.remark-section textarea\s*\{[^}]*box-sizing:\s*border-box[^}]*max-width:\s*100%/s)
assert.match(checkoutStyles, /\.summary \.primary\s*\{[^}]*display:\s*flex[^}]*align-items:\s*center[^}]*justify-content:\s*center[^}]*margin-left:\s*0\s*!important[^}]*margin-right:\s*0\s*!important/s)

const orderDetailStyles = read('pages/orders/detail/index.wxss')
assert.match(orderDetailStyles, /\.actions button\s*\{[^}]*display:\s*flex[^}]*align-items:\s*center[^}]*justify-content:\s*center/s)
assert.match(orderDetailStyles, /\.cancel-sheet textarea\s*\{[^}]*box-sizing:\s*border-box[^}]*max-width:\s*100%/s)

const homeMarkup = read('pages/home/index.wxml')
const homeSource = read('pages/home/index.ts')
for (const handler of ['openCoupons', 'openAddresses', 'openActivity', 'openAfterSales']) {
  assert.match(homeMarkup, new RegExp(`bindtap="${handler}"`))
  assert.match(homeSource, new RegExp(`${handler}\\(\\)`))
}

const orderMarkup = read('pages/orders/index.wxml')
const orderStyles = read('pages/orders/index.wxss')
assert.match(orderMarkup, /class="back" bindtap="goBack"/)
assert.match(orderSource(), /goBack\(\)/)
assert.match(orderSource(), /if \(this\.data\.status\) params\.status = this\.data\.status/)
assert.doesNotMatch(orderSource(), /status: this\.data\.status \|\| undefined/)
assert.match(orderStyles, /\.filters\s*\{[^}]*grid-template-columns:\s*repeat\(3,minmax\(0,1fr\)\)/s)
assert.match(orderStyles, /\.filter\s*\{[^}]*min-width:\s*0/s)

function orderSource() {
  return read('pages/orders/index.ts')
}

console.log('小程序体验回归测试通过：登录区、收藏反馈、首页入口、订单导航和商品底栏。')
