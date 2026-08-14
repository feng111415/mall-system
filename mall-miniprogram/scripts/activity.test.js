const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const source = ts.transpileModule(fs.readFileSync(path.join(__dirname, '../utils/activity.ts'), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
const moduleValue = { exports: {} }
vm.runInNewContext(source, { module: moduleValue, exports: moduleValue.exports, require: value => {
  if (value === './product') return { localProductImage: image => image || '/assets/backpack.jpg', formatPrice: value => Number(value || 0).toFixed(2) }
  return require(value)
} }, { filename: 'utils/activity.ts' })
const activity = moduleValue.exports

const favorite = activity.normalizeActivityItem({ spuId: 3, productName: '城市跑鞋', mainImage: '/assets/sneaker.jpg', price: 299, available: true, viewCount: 2 })
assert.strictEqual(favorite.available, true)
assert.strictEqual(favorite.displayPrice, '299.00')
assert.strictEqual(favorite.selected, false)
const invalid = activity.normalizeActivityItem({ spuId: 4, available: false })
assert.strictEqual(invalid.available, false)
const groups = activity.groupHistory([
  { spuId: 1, lastViewTime: '2026-08-14T09:00:00', available: true },
  { spuId: 2, lastViewTime: '2026-08-13T18:00:00', available: true }
], new Date('2026-08-14T12:00:00'))
assert.strictEqual(groups.length, 2)
assert.strictEqual(groups[0].label, '今天')
assert.strictEqual(groups[1].label, '昨天')

const api = fs.readFileSync(path.join(__dirname, '../services/mallApi.ts'), 'utf8')
const page = fs.readFileSync(path.join(__dirname, '../pages/activity/index.ts'), 'utf8')
const markup = fs.readFileSync(path.join(__dirname, '../pages/activity/index.wxml'), 'utf8')
const productPage = fs.readFileSync(path.join(__dirname, '../pages/product/index.ts'), 'utf8')
const productMarkup = fs.readFileSync(path.join(__dirname, '../pages/product/index.wxml'), 'utf8')
const profile = fs.readFileSync(path.join(__dirname, '../pages/profile/index.wxml'), 'utf8')
const config = JSON.parse(fs.readFileSync(path.join(__dirname, '../app.json'), 'utf8'))
assert.ok(config.pages.includes('pages/activity/index'))
assert.match(api, /product-activity\/favorites/)
assert.match(api, /product-activity\/history/)
assert.match(api, /history\/batch-delete/)
assert.match(page, /index \+= 100/)
assert.match(page, /available\) wx\.navigateTo/)
assert.match(markup, /商品已下架或删除/)
assert.match(markup, /确认清空/)
assert.match(productPage, /Promise\.allSettled/)
assert.match(productPage, /favoriteBusy: true/)
assert.match(productPage, /recordBrowseHistory/)
assert.match(productPage, /toggleFavorite/)
assert.match(productMarkup, /已收藏/)
assert.match(profile, /favoriteCount/)
assert.match(profile, /historyCount/)
const profilePage = fs.readFileSync(path.join(__dirname, '../pages/profile/index.ts'), 'utf8')
assert.match(profilePage, /couponCount: 0, favoriteCount: 0, historyCount: 0/)
console.log('收藏足迹测试通过：快照、日期分组、失效商品、批量删除、详情联动和个人中心摘要。')
