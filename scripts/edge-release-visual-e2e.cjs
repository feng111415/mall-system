const fs = require('fs')
const path = require('path')
const playwrightCorePath = process.env.EDGE_PLAYWRIGHT_CORE || '../ruoyi-ui/node_modules/playwright-core'
const { chromium } = require(playwrightCorePath)

const adminToken = process.env.RUOYI_ADMIN_TEST_TOKEN
if (!adminToken) throw new Error('RUOYI_ADMIN_TEST_TOKEN must be injected')

const projectRoot = path.resolve(__dirname, '..')
const outputRoot = path.join(projectRoot, 'mall-storefront', 'test-results', 'release-edge')
fs.mkdirSync(outputRoot, { recursive: true })

const edgePath = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe'
const routes = [
  { name: 'storefront-home-desktop', url: 'http://127.0.0.1:5174/', viewport: { width: 1440, height: 900 } },
  { name: 'storefront-catalog-desktop', url: 'http://127.0.0.1:5174/catalog', viewport: { width: 1440, height: 900 } },
  { name: 'storefront-account-desktop', url: 'http://127.0.0.1:5174/account', viewport: { width: 1440, height: 900 } },
  { name: 'storefront-home-mobile', url: 'http://127.0.0.1:5174/', viewport: { width: 390, height: 844 }, mobile: true },
  { name: 'storefront-catalog-mobile', url: 'http://127.0.0.1:5174/catalog', viewport: { width: 390, height: 844 }, mobile: true },
  { name: 'storefront-account-mobile', url: 'http://127.0.0.1:5174/account', viewport: { width: 390, height: 844 }, mobile: true },
  { name: 'admin-home-desktop', url: 'http://127.0.0.1:8081/mall/overview/home', viewport: { width: 1440, height: 900 }, admin: true },
  { name: 'admin-analytics-desktop', url: 'http://127.0.0.1:8081/mall/overview/analytics', viewport: { width: 1440, height: 900 }, admin: true },
  { name: 'admin-responsibilities-desktop', url: 'http://127.0.0.1:8081/mall/overview/responsibilities', viewport: { width: 1440, height: 900 }, admin: true },
  { name: 'admin-home-mobile', url: 'http://127.0.0.1:8081/mall/overview/home', viewport: { width: 390, height: 844 }, mobile: true, admin: true }
]

async function main() {
  const browser = await chromium.launch({ executablePath: edgePath, headless: true })
  try {
    let count = 0
    for (const route of routes) {
      const context = await browser.newContext({
        viewport: route.viewport,
        isMobile: Boolean(route.mobile),
        hasTouch: Boolean(route.mobile),
        deviceScaleFactor: 1
      })
      if (route.admin) {
        await context.addCookies([{
          name: 'Admin-Token', value: adminToken, domain: '127.0.0.1', path: '/', sameSite: 'Lax'
        }])
      }
      const page = await context.newPage()
      const consoleErrors = []
      const failedResponses = []
      const requestFailures = []
      page.on('console', message => {
        if (message.type() === 'error') consoleErrors.push(message.text())
      })
      page.on('response', response => {
        if (response.status() >= 400) failedResponses.push(`${response.status()} ${response.url()}`)
      })
      page.on('requestfailed', request => requestFailures.push(`${request.url()} ${request.failure()?.errorText || ''}`))

      const response = await page.goto(route.url, { waitUntil: 'domcontentloaded', timeout: 30000 })
      if (!response || response.status() !== 200) throw new Error(`${route.name} navigation failed`)
      await page.waitForTimeout(3500)
      if (route.admin && page.url().includes('/login')) throw new Error(`${route.name} redirected to login`)
      if (route.admin) {
        const cancelButton = page.locator('.el-message-box__btns .el-button').first()
        if (await cancelButton.isVisible().catch(() => false)) {
          await cancelButton.click()
          await page.waitForTimeout(300)
        }
      }

      const layout = await page.evaluate(() => ({
        title: document.title,
        text: (document.body.innerText || '').trim(),
        width: document.documentElement.clientWidth,
        scrollWidth: document.documentElement.scrollWidth,
        bodyHeight: document.body.getBoundingClientRect().height,
        canvases: Array.from(document.querySelectorAll('canvas')).map(canvas => ({ width: canvas.width, height: canvas.height }))
      }))
      if (layout.text.length < 20 || layout.bodyHeight < 100) throw new Error(`${route.name} rendered blank content`)
      if (layout.scrollWidth > layout.width + 1) {
        throw new Error(`${route.name} has horizontal overflow ${layout.scrollWidth}/${layout.width}`)
      }
      if (consoleErrors.length) throw new Error(`${route.name} console errors: ${consoleErrors.join(' | ')}`)
      if (failedResponses.length) throw new Error(`${route.name} failed responses: ${failedResponses.join(' | ')}`)
      if (requestFailures.length) throw new Error(`${route.name} request failures: ${requestFailures.join(' | ')}`)
      if (route.name === 'admin-analytics-desktop' && !layout.text.includes('经营分析')) {
        throw new Error('admin analytics content was not rendered')
      }
      if (route.name === 'admin-responsibilities-desktop' && !layout.text.includes('职责')) {
        throw new Error('admin responsibilities content was not rendered')
      }

      await page.screenshot({ path: path.join(outputRoot, `${route.name}.png`), fullPage: true })
      count += 1
      process.stdout.write(`PASS ${count}. ${route.name} text=${layout.text.length} overflow=0\n`)
      await context.close()
    }
    process.stdout.write('EDGE RELEASE VISUAL E2E PASS\n')
  } finally {
    await browser.close()
  }
}

main().catch(error => {
  process.stderr.write(`${error.stack || error.message}\n`)
  process.exitCode = 1
})
