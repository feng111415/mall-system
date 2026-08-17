const path = require('path')
const { chromium } = require(process.env.EDGE_PLAYWRIGHT_CORE || path.resolve(__dirname, '../../tmp/edge-e2e/node_modules/playwright-core'))

const apiBase = process.env.MALL_API_BASE || 'http://127.0.0.1:8080/api/mall'
const webBase = process.env.MALL_WEB_BASE || 'http://127.0.0.1:5174'
const mockCode = process.env.MALL_SMS_MOCK_CODE
if (!mockCode) throw new Error('MALL_SMS_MOCK_CODE must be injected')

const deviceId = crypto.randomUUID().replaceAll('-', '')
const phone = `137${String(Date.now()).slice(-8)}`
let token = ''

async function api(method, pathName, body) {
  const response = await fetch(`${apiBase}${pathName}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'X-Mall-Device-Id': deviceId,
      'X-Forwarded-For': '198.51.100.88',
      'User-Agent': 'Mozilla/5.0 Edge/StorefrontRealE2E',
      ...(token ? { 'X-Mall-Authorization': `Bearer ${token}` } : {})
    },
    body: body === undefined ? undefined : JSON.stringify(body)
  })
  const payload = await response.json()
  if (!response.ok || payload.code !== 200) throw new Error(`${method} ${pathName}: HTTP ${response.status}, ${payload.msg || 'unknown error'}`)
  return payload.data
}

async function prepareMember() {
  const products = await api('GET', '/catalog/products')
  let sellable
  for (const product of products) {
    const detail = await api('GET', `/catalog/products/${product.spuId}`)
    const sku = detail.skuList?.find(item => item.status === '1' && Number(item.availableStock) > 0)
    if (sku) { sellable = { product: detail, sku }; break }
  }
  if (!sellable) throw new Error('没有可售 SKU，无法执行网页真实交易链路')

  await api('POST', '/member/sms-code', { phone })
  const login = await api('POST', '/member/login', {
    phone,
    code: mockCode,
    agreed: true,
    userAgreementVersion: '1.0',
    privacyPolicyVersion: '1.0',
    deviceId
  })
  token = login.token
  await api('POST', '/member/addresses', {
    receiverName: '网页验收用户',
    receiverPhone: phone,
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    detailAddress: '发布候选验收路 17 号',
    postalCode: '100000',
    isDefault: '1'
  })
  return sellable
}

async function main() {
  const { product } = await prepareMember()
  const browser = await chromium.launch({
    executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    headless: true
  })
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } })
  await context.addInitScript(({ memberToken, memberDeviceId }) => {
    sessionStorage.setItem('mall-user-token', memberToken)
    localStorage.setItem('mall-device-id', memberDeviceId)
  }, { memberToken: token, memberDeviceId: deviceId })
  const page = await context.newPage()
  const consoleErrors = []
  const failedResponses = []
  page.on('console', message => { if (message.type() === 'error') consoleErrors.push(message.text()) })
  page.on('response', response => { if (response.status() >= 400) failedResponses.push(`${response.status()} ${response.url()}`) })

  try {
    await page.goto(`${webBase}/product/${product.spuId}`, { waitUntil: 'domcontentloaded' })
    await page.locator('.detail-add-button').waitFor({ state: 'visible' })
    if (!(await page.locator('.detail-add-button').isEnabled())) throw new Error('商品详情默认规格不可售')
    const cartResponse = page.waitForResponse(response => response.request().method() === 'POST' && response.url().includes('/api/mall/cart/items'))
    await page.locator('.detail-add-button').click()
    if ((await (await cartResponse).json()).code !== 200) throw new Error('页面加购接口失败')
    process.stdout.write(`PASS 1. 商品详情选择可售规格并加购：${product.productName}\n`)

    await page.goto(`${webBase}/cart`, { waitUntil: 'domcontentloaded' })
    await page.locator('.cart-item').waitFor({ state: 'visible' })
    await page.getByRole('button', { name: /去结算/ }).click()
    await page.waitForURL('**/checkout')
    await page.locator('.checkout-layout').waitFor({ state: 'visible' })
    process.stdout.write('PASS 2. 购物车加载、选中状态与结算跳转\n')

    await page.locator('.checkout-remark textarea').fill('网页 Edge 真实链路验收')
    const orderResponse = page.waitForResponse(response => response.request().method() === 'POST' && /\/api\/mall\/orders$/.test(response.url()))
    await page.getByRole('button', { name: /提交订单/ }).click()
    const orderPayload = await (await orderResponse).json()
    if (orderPayload.code !== 200 || !orderPayload.data?.orderId) throw new Error(`页面下单失败：${orderPayload.msg || '无订单号'}`)
    const { orderId, orderNo } = orderPayload.data
    await page.getByText(`订单号 ${orderNo}`).waitFor({ state: 'visible' })
    process.stdout.write(`PASS 3. 结算页提交订单：${orderNo}\n`)

    await page.goto(`${webBase}/orders/${orderId}`, { waitUntil: 'domcontentloaded' })
    const cancelButton = page.getByRole('button', { name: '取消订单', exact: true })
    await cancelButton.waitFor({ state: 'visible' })
    const cancelResponse = page.waitForResponse(response => response.request().method() === 'POST' && response.url().endsWith(`/api/mall/orders/${orderId}/cancel`))
    await cancelButton.click()
    if ((await (await cancelResponse).json()).code !== 200) throw new Error('页面取消订单接口失败')
    await page.getByText('已取消', { exact: true }).first().waitFor({ state: 'visible' })
    const finalOrder = await api('GET', `/orders/${orderId}`)
    if (finalOrder.status !== 'CANCELLED' || !String(finalOrder.cancelReason || '').includes('会员主动取消')) {
      throw new Error(`取消后订单状态异常：${finalOrder.status}`)
    }
    process.stdout.write('PASS 4. 订单详情取消订单并核对服务端最终状态\n')

    if (consoleErrors.length) throw new Error(`浏览器控制台错误：${consoleErrors.join(' | ')}`)
    if (failedResponses.length) throw new Error(`浏览器 HTTP 失败：${failedResponses.join(' | ')}`)
    process.stdout.write('STOREFRONT REAL UI E2E PASS\n')
  } finally {
    await context.close()
    await browser.close()
  }
}

main().catch(error => {
  process.stderr.write(`${error.stack || error.message}\n`)
  process.exitCode = 1
})
