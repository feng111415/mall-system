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
  if (value === './product') return { localProductImage: value => value, formatPrice: value => Number(value || 0).toFixed(2) }
  return require(value)
} }, { filename: 'utils/checkout.ts' })
const { normalizeAddress, addressPayload } = moduleValue.exports

const address = normalizeAddress({
  addressId: 7, receiverName: '张三', receiverPhone: '13800138000', province: '广东省', city: '深圳市', district: '南山区', detailAddress: '科技园1号', postalCode: '518000', isDefault: '1'
})
assert.strictEqual(address.displayReceiver, '张三 13800138000')
assert.strictEqual(address.displayAddress, '广东省 深圳市 南山区 科技园1号')
assert.strictEqual(address.defaultFlag, true)
assert.deepStrictEqual(JSON.parse(JSON.stringify(addressPayload({ ...address, isDefault: false }))), {
  receiverName: '张三', receiverPhone: '13800138000', province: '广东省', city: '深圳市', district: '南山区', detailAddress: '科技园1号', postalCode: '518000', isDefault: '0'
})

const api = fs.readFileSync(path.join(__dirname, '../services/mallApi.ts'), 'utf8')
const page = fs.readFileSync(path.join(__dirname, '../pages/addresses/index.ts'), 'utf8')
const markup = fs.readFileSync(path.join(__dirname, '../pages/addresses/index.wxml'), 'utf8')
const checkout = fs.readFileSync(path.join(__dirname, '../pages/checkout/index.ts'), 'utf8')
const profile = fs.readFileSync(path.join(__dirname, '../pages/profile/index.wxml'), 'utf8')
const config = JSON.parse(fs.readFileSync(path.join(__dirname, '../app.json'), 'utf8'))
assert.ok(config.pages.includes('pages/addresses/index'))
assert.match(api, /\/api\/mall\/member\/addresses/)
assert.match(api, /method: 'PUT'/)
assert.match(api, /method: 'DELETE'/)
assert.match(page, /getAddresses/)
assert.match(page, /updateAddress/)
assert.match(page, /deleteAddress/)
assert.match(page, /isDefault/)
assert.match(markup, /mode="region"/)
assert.match(markup, /确认删除/)
assert.match(checkout, /pages\/addresses\/index\?from=checkout&create=1/)
assert.match(checkout, /addressBookOpened/)
assert.match(profile, /openAddresses/)
console.log('地址簿测试通过：地址标准化、默认地址 payload、CRUD 接口、表单和路由绑定。')
