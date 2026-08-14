const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const root = path.resolve(__dirname, '..')
function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), 'utf8')
}

function loadTypeScript(relativePath) {
  const source = ts.transpileModule(read(relativePath), {
    compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
  }).outputText
  const module = { exports: {} }
  vm.runInNewContext(source, { module, exports: module.exports, require }, { filename: relativePath })
  return module.exports
}

const profile = loadTypeScript('utils/profile.ts')
assert.strictEqual(profile.normalizeNickname('  阳光  小店  '), '阳光 小店')
assert.strictEqual(profile.validateNickname('拾光'), '')
assert.match(profile.validateNickname('x'), /2 至 20/)
assert.strictEqual(profile.resolveAvatarUrl('/assets/avatars/avatar-mint.svg', 'http://localhost:8080'), '/assets/avatars/avatar-mint.svg')
assert.strictEqual(profile.resolveAvatarUrl('/profile/mall/avatar/7/avatar.png', 'http://localhost:8080'), 'http://localhost:8080/profile/mall/avatar/7/avatar.png')
assert.strictEqual(profile.resolveAvatarUrl('https://cdn.example/avatar.png', 'http://localhost:8080'), 'https://cdn.example/avatar.png')

const api = read('services/mallApi.ts')
assert.match(api, /getAvatarPresets\(\)/)
assert.match(api, /updateNickname\(/)
assert.match(api, /selectPresetAvatar\(/)
assert.match(api, /uploadAvatar\(/)
assert.match(api, /return upload<MallMemberProfile>/)

const request = read('utils/request.ts')
assert.match(request, /function upload</)
assert.match(request, /wx\.uploadFile/)
assert.match(request, /X-Mall-Authorization/)

const page = read('pages/profile-edit/index.ts')
const markup = read('pages/profile-edit/index.wxml')
assert.match(page, /chooseMedia/)
assert.match(page, /sourceType:\s*\['album'\]/)
assert.match(page, /cropImage/)
assert.match(page, /cropScale:\s*'1:1'/)
assert.match(page, /uploadAvatar/)
assert.match(markup, /从相册选择/)
assert.match(markup, /保存昵称/)
const profilePage = read('pages/profile/index.ts')
assert.match(profilePage, /openProfileEditor/)
assert.match(profilePage, /onShow\(\)[\s\S]*globalData\.member[\s\S]*resolveAvatarUrl/)
assert.match(read('pages/profile/index.wxml'), /openProfileEditor/)
const app = JSON.parse(read('app.json'))
assert.ok(app.pages.includes('pages/profile-edit/index'))
console.log('个人资料与头像测试通过：昵称校验、相册选择、1:1 裁剪、multipart 上传和页面注册。')
