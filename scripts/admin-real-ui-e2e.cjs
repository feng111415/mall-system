const { execFileSync } = require('child_process')
const path = require('path')
const { chromium } = require(process.env.EDGE_PLAYWRIGHT_CORE || path.resolve(__dirname, '../../tmp/edge-e2e/node_modules/playwright-core'))

const base = process.env.RUOYI_WEB_BASE || 'http://127.0.0.1:8081'
const apiBase = process.env.RUOYI_API_BASE || 'http://127.0.0.1:8080'
const username = process.env.RUOYI_ADMIN_TEST_USER || 'ops_lead_test'
const password = process.env.RUOYI_ADMIN_TEST_PASSWORD
const redisPort = process.env.RUOYI_REDIS_PORT || '6379'
if (!password) throw new Error('RUOYI_ADMIN_TEST_PASSWORD must be injected')

async function login() {
  const captchaResponse = await fetch(`${apiBase}/captchaImage`)
  const captcha = await captchaResponse.json()
  const rawCode = execFileSync('D:\\360Downloads\\redis-cli.exe', ['-p', redisPort, '--raw', 'GET', `captcha_codes:${captcha.uuid}`], { encoding: 'utf8' }).trim()
  let code
  try { code = JSON.parse(rawCode) } catch { code = rawCode.replace(/^"|"$/g, '') }
  const response = await fetch(`${apiBase}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password, code, uuid: captcha.uuid })
  })
  const result = await response.json()
  if (!response.ok || result.code !== 200 || !result.token) throw new Error(`后台登录失败：${result.msg || response.status}`)
  return result.token
}

const routes = [
  ['/mall/overview/home', '运营工作台'],
  ['/mall/overview/analytics', '经营分析中心'],
  ['/mall/overview/responsibilities', '岗位职责'],
  ['/mall/product-inventory/product-inventory-center', '商品库存中心'],
  ['/mall/product-inventory/product', '商品管理'],
  ['/mall/product-inventory/inventory', '库存管理'],
  ['/mall/order-fulfillment/order-center', '订单运营中心'],
  ['/mall/order-fulfillment/logistics', '物流履约'],
  ['/mall/after-sale-funds/after-sale', '售后'],
  ['/mall/after-sale-funds/reconciliation', '对账'],
  ['/mall/after-sale-funds/center', '售后资金处理中心'],
  ['/mall/member-operations/member', '会员'],
  ['/mall/content-marketing/homepage', '首页内容运营'],
  ['/mall/content-marketing/coupons', '优惠券'],
  ['/mall/content-marketing/reviews', '评价']
]
const routeFilter = process.env.RUOYI_ADMIN_TEST_ROUTE
const selectedRoutes = routeFilter ? routes.filter(([route]) => route === routeFilter) : routes

const detailActions = {
  '/mall/product-inventory/product-inventory-center': '详情',
  '/mall/order-fulfillment/order-center': '详情',
  '/mall/order-fulfillment/logistics': '轨迹',
  '/mall/after-sale-funds/after-sale': '详情',
  '/mall/after-sale-funds/center': '详情',
  '/mall/member-operations/member': '账号与隐私'
}

async function main() {
  const token = await login()
  const browser = await chromium.launch({ executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe', headless: true })
  try {
    for (let index = 0; index < selectedRoutes.length; index += 1) {
      const [route, expected] = selectedRoutes[index]
      const context = await browser.newContext({ viewport: { width: 1440, height: 900 } })
      await context.addCookies([{ name: 'Admin-Token', value: token, domain: '127.0.0.1', path: '/', sameSite: 'Lax' }])
      const page = await context.newPage()
      const errors = []
      const failures = []
      page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
      page.on('response', response => { if (response.status() >= 400) failures.push(`${response.status()} ${response.url()}`) })
      const response = await page.goto(`${base}${route}`, { waitUntil: 'domcontentloaded', timeout: 30000 })
      await page.waitForTimeout(1800)
      const securityPrompt = page.locator('.el-message-box__wrapper:visible')
      if (await securityPrompt.count()) {
        await securityPrompt.locator('.el-message-box__btns button').first().click()
        await securityPrompt.waitFor({ state: 'hidden' })
      }
      const bodyText = (await page.locator('body').innerText()).trim()
      if (!response || response.status() !== 200 || page.url().includes('/login')) throw new Error(`${route} 未成功进入后台`)
      if (bodyText.length < 30 || !bodyText.includes(expected)) throw new Error(`${route} 内容不完整，缺少“${expected}”`)
      if (/页面不存在|页面加载失败|找不到页面/.test(bodyText)) throw new Error(`${route} 页面显示错误状态`)

      const search = page.locator('button:visible').filter({ hasText: /搜索|查询/ }).first()
      if (await search.count()) await search.click()
      const actionLabel = detailActions[route]
      const detail = actionLabel ? page.locator('button:visible').filter({ hasText: actionLabel }).first() : null
      if (detail && await detail.count()) {
        await detail.click()
        await page.waitForTimeout(250)
        const dialogs = page.locator('.el-dialog:visible, .el-drawer:visible')
        if (!(await dialogs.count())) throw new Error(`${route} 点击详情后没有打开抽屉或弹窗`)
      }
      if (failures.length) throw new Error(`${route} 请求失败：${failures.join(' | ')}`)
      if (errors.length) throw new Error(`${route} 控制台错误：${errors.join(' | ')}`)
      process.stdout.write(`PASS ${index + 1}. ${route}：加载、权限、查询/详情交互\n`)
      await context.close()
    }
    process.stdout.write('ADMIN REAL UI E2E PASS\n')
  } finally {
    await browser.close()
  }
}

main().catch(error => {
  process.stderr.write(`${error.stack || error.message}\n`)
  process.exitCode = 1
})
