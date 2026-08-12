const fs = require('fs')
const path = require('path')
const vm = require('vm')

const root = path.resolve(__dirname, '..')
const failures = []

function files(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const target = path.join(directory, entry.name)
    return entry.isDirectory() ? files(target) : [target]
  })
}

for (const file of files(root)) {
  if (file.includes(`${path.sep}node_modules${path.sep}`)) continue
  if (file.endsWith('.json')) {
    try { JSON.parse(fs.readFileSync(file, 'utf8')) } catch (error) { failures.push(`${path.relative(root, file)}: JSON ${error.message}`) }
  }
  if (file.endsWith('.js') && !file.includes(`${path.sep}scripts${path.sep}`)) {
    try { new vm.Script(fs.readFileSync(file, 'utf8'), { filename: file }) } catch (error) { failures.push(`${path.relative(root, file)}: JavaScript ${error.message}`) }
  }
}

const app = JSON.parse(fs.readFileSync(path.join(root, 'app.json'), 'utf8'))
for (const page of app.pages) {
  for (const extension of ['.ts', '.json', '.wxml', '.wxss']) {
    if (!fs.existsSync(path.join(root, `${page}${extension}`))) failures.push(`缺少页面文件：${page}${extension}`)
  }
}

for (const entry of fs.readdirSync(path.join(root, 'pages'), { withFileTypes: true })) {
  if (entry.isDirectory() && !app.pages.includes(`pages/${entry.name}/index`)) failures.push(`页面未注册：pages/${entry.name}/index`)
}

const referencedAssets = files(root)
  .filter(file => !file.includes(`${path.sep}node_modules${path.sep}`) && !file.includes(`${path.sep}scripts${path.sep}`))
  .filter(file => file.endsWith('.wxml') || file.endsWith('.ts'))
  .flatMap(file => [...fs.readFileSync(file, 'utf8').matchAll(/["'](\/assets\/[^"']+)["']/g)].map(match => match[1]))
for (const asset of referencedAssets) {
  if (!fs.existsSync(path.join(root, asset.slice(1)))) failures.push(`素材不存在：${asset}`)
}

if (failures.length) {
  console.error(failures.join('\n'))
  process.exit(1)
}
console.log(`基础工程校验通过：${app.pages.length} 个页面，${referencedAssets.length} 个本地素材引用。`)
