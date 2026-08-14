const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')
const ts = require('typescript')

const storage = new Map()
let requestHandler
let uploadHandler
global.wx = {
  getStorageSync: key => storage.get(key) || '',
  setStorageSync: (key, value) => storage.set(key, value),
  removeStorageSync: key => storage.delete(key),
  getAccountInfoSync: () => ({ miniProgram: { envVersion: 'develop' } }),
  request: options => requestHandler(options),
  uploadFile: options => uploadHandler(options)
}

function loadTypeScriptModule(file, dependencies = {}) {
  const source = ts.transpileModule(fs.readFileSync(path.join(__dirname, file), 'utf8'), {
    compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
  }).outputText
  const module = { exports: {} }
  vm.runInNewContext(source, {
    require: name => dependencies[name] || require(name),
    module,
    exports: module.exports,
    wx,
    Promise,
    Error
  }, { filename: file })
  return module.exports
}

const env = loadTypeScriptModule('../config/env.ts')
const { request, upload } = loadTypeScriptModule('../utils/request.ts', { '../config/env': env })
const auth = loadTypeScriptModule('../utils/auth.ts')

async function run() {
  env.setToken('0123456789abcdef0123456789abcdef')
  requestHandler = options => options.success({ statusCode: 200, data: { code: 200, data: { ok: true } } })
  const result = await request({ url: '/api/mall/health' })
  assert.deepStrictEqual(result, { ok: true })

  let captured
  requestHandler = options => { captured = options; options.success({ statusCode: 200, data: { code: 200, data: [] } }) }
  await request({ url: '/api/mall/orders' })
  assert.strictEqual(captured.header['X-Mall-Authorization'], 'Bearer 0123456789abcdef0123456789abcdef')
  assert.match(captured.header['X-Mall-Device-Id'], /^[a-f0-9]{32}$/)
  assert.strictEqual(captured.url, 'http://localhost:8080/api/mall/orders')

  uploadHandler = options => {
    captured = options
    options.success({ statusCode: 200, data: JSON.stringify({ code: 200, data: { avatar: '/profile/mall/avatar/7/avatar.png' } }) })
  }
  const uploaded = await upload({ url: '/api/mall/member/profile/avatar/upload', filePath: 'wxfile://avatar.png', name: 'file' })
  assert.strictEqual(uploaded.avatar, '/profile/mall/avatar/7/avatar.png')
  assert.strictEqual(captured.url, 'http://localhost:8080/api/mall/member/profile/avatar/upload')
  assert.strictEqual(captured.filePath, 'wxfile://avatar.png')
  assert.strictEqual(captured.name, 'file')
  assert.strictEqual(captured.header['X-Mall-Authorization'], 'Bearer 0123456789abcdef0123456789abcdef')
  assert.match(captured.header['X-Mall-Device-Id'], /^[a-f0-9]{32}$/)
  assert.strictEqual(captured.header['content-type'], undefined)

  uploadHandler = options => options.success({ statusCode: 400, data: JSON.stringify({ code: 400, msg: '头像图片不能超过 5MB' }) })
  await assert.rejects(() => upload({ url: '/api/mall/member/profile/avatar/upload', filePath: 'wxfile://large.png' }), /头像图片不能超过 5MB/)
  uploadHandler = options => options.success({ statusCode: 200, data: '<html>bad gateway</html>' })
  await assert.rejects(() => upload({ url: '/api/mall/member/profile/avatar/upload', filePath: 'wxfile://avatar.png' }), /上传响应格式异常/)

  env.setDevelopApiBaseUrl('https://mall-tunnel.example.com/')
  requestHandler = options => { captured = options; options.success({ statusCode: 200, data: { code: 200, data: { ok: true } } }) }
  await request({ url: '/api/mall/health' })
  assert.strictEqual(captured.url, 'https://mall-tunnel.example.com/api/mall/health')
  assert.throws(() => env.setDevelopApiBaseUrl('http://insecure.example.com'), /HTTPS/)
  env.setDevelopApiBaseUrl('')

  const accountInfo = wx.getAccountInfoSync
  wx.getAccountInfoSync = undefined
  assert.strictEqual(env.getEnvVersion(), '')
  wx.getAccountInfoSync = accountInfo

  const cooldown = auth.startSmsCooldown('13900008131', 60)
  assert.strictEqual(auth.readSmsCooldown().phone, '13900008131')
  assert.ok(auth.getCooldownSeconds(cooldown) >= 59)
  assert.ok(auth.getCooldownSeconds(auth.readSmsCooldown()) >= 59)
  auth.clearSmsCooldown()
  assert.strictEqual(auth.readSmsCooldown(), null)

  requestHandler = options => options.success({ statusCode: 200, data: { code: 401, msg: '请先登录' } })
  await assert.rejects(() => request({ url: '/api/mall/cart' }), /请先登录/)
  assert.strictEqual(env.getToken(), '')

  requestHandler = options => options.fail({ errMsg: 'request:fail timeout' })
  await assert.rejects(() => request({ url: '/api/mall/homepage' }), /timeout/)
  console.log('请求与鉴权基础测试通过：请求、multipart 上传、设备标识、开发隧道、60 秒冷却持久化、401 和网络失败。')
}

run().catch(error => {
  console.error(error)
  process.exit(1)
})
