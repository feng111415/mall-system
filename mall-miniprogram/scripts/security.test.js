const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const root = path.resolve(__dirname, '..')
const storage = new Map()
const wx = {
  getStorageSync: key => storage.get(key),
  setStorageSync: (key, value) => storage.set(key, value),
  removeStorageSync: key => storage.delete(key)
}

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), 'utf8')
}

function loadTypeScript(relativePath) {
  const source = ts.transpileModule(read(relativePath), {
    compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
  }).outputText
  const module = { exports: {} }
  vm.runInNewContext(source, { module, exports: module.exports, require, wx, Date }, { filename: relativePath })
  return module.exports
}

const security = loadTypeScript('utils/security.ts')
const sessions = security.normalizeSessions([
  { sessionId: 2, deviceType: 'MOBILE', deviceName: '普通手机', current: false, primaryMobile: false, loginTime: '2026-08-17T08:00:00' },
  { sessionId: 1, deviceType: 'MOBILE', deviceName: '主手机', current: false, primaryMobile: true, loginTime: '2026-08-16T08:00:00' },
  { sessionId: 3, deviceType: 'DESKTOP', deviceName: '当前电脑', current: true, primaryMobile: false, loginTime: '2026-08-15T08:00:00' }
])
assert.deepStrictEqual(Array.from(sessions, value => value.sessionId), [3, 1, 2])
assert.strictEqual(sessions[0].deviceIcon, '/assets/icons/monitor.svg')
assert.strictEqual(security.canReplacePrimary(sessions, 3), false)
const ordinaryMobile = security.normalizeSessions([{ sessionId: 4, deviceType: 'MOBILE', current: true, primaryMobile: false }])
assert.strictEqual(security.canReplacePrimary(ordinaryMobile, 1), true)
assert.strictEqual(security.canReplacePrimary(ordinaryMobile, 0), false)
assert.match(security.formatTime('2026-08-17T09:30:00'), /^08-17 09:30$/)

const cooldown = security.startPrimaryCodeCooldown(60)
const storedExpiresAt = cooldown.expiresAt
assert.ok(security.getPrimaryCodeSeconds(cooldown) >= 59)
assert.strictEqual(security.readPrimaryCodeCooldown().expiresAt, storedExpiresAt)
assert.strictEqual(security.readPrimaryCodeCooldown().expiresAt, storedExpiresAt)
security.clearPrimaryCodeCooldown()
assert.strictEqual(security.readPrimaryCodeCooldown(), null)

const api = read('services/mallApi.ts')
assert.match(api, /getMemberSessions\(\)/)
assert.match(api, /profile\/sessions\/\$\{sessionId\}/)
assert.match(api, /primary-mobile\/code/)
assert.match(api, /replacePrimaryDevice\(/)

const page = read('pages/security/index.ts')
const markup = read('pages/security/index.wxml')
assert.match(page, /revokeMemberSession/)
assert.match(page, /sendPrimaryDeviceCode/)
assert.match(page, /replacePrimaryDevice/)
assert.match(page, /readPrimaryCodeCooldown/)
assert.match(markup, /当前设备/)
assert.match(markup, /主设备/)
assert.match(markup, /确认下线/)
assert.match(markup, /验证码/)
assert.match(read('pages/profile/index.wxml'), /openSecurity/)
const app = JSON.parse(read('app.json'))
assert.ok(app.pages.includes('pages/security/index'))
console.log('会话与设备安全测试通过：会话排序、在线设备聚合、主设备门槛、验证码冷却、下线与接口绑定。')
