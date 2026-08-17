const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const source = fs.readFileSync(path.resolve(__dirname, '../custom-tab-bar/index.ts'), 'utf8')
const compiled = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
}).outputText

let component
let pages = []
vm.runInNewContext(compiled, {
  Component(definition) { component = definition },
  getCurrentPages() { return pages },
  wx: { switchTab() {} }
})

const instance = {
  data: component.data,
  setData(update) { this.data = { ...this.data, ...update } }
}

assert.doesNotThrow(() => component.methods.updateSelected.call(instance), '空页面栈不应导致 TabBar 报错')
pages = [{}]
assert.doesNotThrow(() => component.methods.updateSelected.call(instance), '无 route 的过渡页面不应导致 TabBar 报错')
pages = [{ route: 'pages/cart/index' }]
component.methods.updateSelected.call(instance)
assert.strictEqual(instance.data.selected, 2, '应按当前页面同步选中项')

console.log('自定义 TabBar 回归测试通过：空页面栈、过渡页面和正常路由。')
