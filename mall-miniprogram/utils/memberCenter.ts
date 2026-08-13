const productUtils = require('./product')

const COUPON_STATUS_LABELS: Record<string, string> = {
  AVAILABLE: '可使用',
  LOCKED: '订单占用中',
  USED: '已使用',
  EXPIRED: '已过期',
  PAUSED: '已暂停'
}

const SCOPE_LABELS: Record<string, string> = {
  ALL: '全场商品',
  CATEGORY: '指定分类',
  PRODUCT: '指定商品'
}

function formatTime(value: unknown) {
  if (!value) return '-'
  const date = new Date(String(value).replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = (number: number) => String(number).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function normalizeCoupon(value: Record<string, any>) {
  const deadline = value.validTo ? new Date(String(value.validTo).replace(' ', 'T')).getTime() : 0
  const status = value.status === 'AVAILABLE' && value.templateStatus && value.templateStatus !== 'PUBLISHED'
    ? 'PAUSED'
    : value.status === 'AVAILABLE' && deadline > 0 && deadline < Date.now()
      ? 'EXPIRED'
      : value.status || 'AVAILABLE'
  return {
    ...value,
    status,
    statusLabel: COUPON_STATUS_LABELS[status] || status,
    scopeLabel: SCOPE_LABELS[value.scopeType] || '指定范围',
    displayThreshold: productUtils.formatPrice(value.thresholdAmount),
    displayDiscount: productUtils.formatPrice(value.discountAmount),
    displayValidFrom: formatTime(value.validFrom),
    displayValidTo: formatTime(value.validTo),
    usable: status === 'AVAILABLE'
  }
}

function normalizeClaimableCoupon(value: Record<string, any>) {
  return {
    ...normalizeCoupon({ ...value, status: 'AVAILABLE' }),
    remainingQuantity: Math.max(0, Number(value.totalQuantity || 0) - Number(value.claimedQuantity || 0))
  }
}

function normalizeMessageSummary(value: Record<string, any>) {
  return {
    ...value,
    unreadCount: Number(value.unreadCount || 0),
    totalCount: Number(value.totalCount || 0),
    displayTime: formatTime(value.latestTime)
  }
}

function normalizeMessage(value: Record<string, any>) {
  return {
    ...value,
    unread: String(value.readFlag || '0') !== '1',
    displayTime: formatTime(value.createTime)
  }
}

function resolveMessageAction(path: unknown) {
  const value = String(path || '').trim()
  let match = value.match(/^\/orders\/(\d+)(?:\/logistics)?$/)
  if (match) return `/pages/orders/detail/index?orderId=${match[1]}`
  if (value === '/orders') return '/pages/orders/index'
  match = value.match(/^\/after-sales\/(\d+)$/)
  if (match) return `/pages/after-sales/detail/index?afterSaleId=${match[1]}`
  if (value === '/after-sales') return '/pages/after-sales/index'
  if (value === '/account' || value === '/account/privacy') return '/pages/profile/index'
  return ''
}

function availableCouponCount(values: Array<Record<string, any>>) {
  return (values || []).map(normalizeCoupon).filter(item => item.usable).length
}

module.exports = {
  normalizeCoupon,
  normalizeClaimableCoupon,
  normalizeMessageSummary,
  normalizeMessage,
  resolveMessageAction,
  availableCouponCount,
  formatTime
}
