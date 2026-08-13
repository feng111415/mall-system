const productUtils = require('./product')

const STATUS_LABELS: Record<string, string> = {
  PENDING_PAYMENT: '待付款', PENDING_SHIPMENT: '待发货', SHIPPED: '已发货',
  AFTER_SALE: '售后中', COMPLETED: '已完成', CANCELLED: '已取消', CLOSED: '已关闭'
}
const PAYMENT_LABELS: Record<string, string> = {
  UNPAID: '未支付', PAYING: '等待支付结果', PAID: '已支付', FAILED: '支付未完成',
  REFUNDING: '异常支付退款中', REFUNDED: '异常支付已退款'
}
const PAYMENT_ATTEMPT_LABELS: Record<string, string> = {
  CREATING: '正在创建', PAYING: '等待支付', SUCCESS: '支付成功', FAILED: '创建失败',
  CLOSED: '已超时关闭', REFUNDING: '退款处理中', REFUNDED: '已原路退款'
}

function parseTime(value?: string) {
  if (!value) return 0
  const time = new Date(String(value).replace(/-/g, '/').replace('T', ' ')).getTime()
  return Number.isFinite(time) ? time : 0
}

function formatTime(value?: string) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function statusLabel(order: Record<string, any>) {
  if (order.displayStatus === 'AFTER_SALE') return Number(order.activeAfterSaleCount || 0) > 1 ? `${order.activeAfterSaleCount} 项售后处理中` : '售后处理中'
  if (order.status === 'CLOSED' && String(order.cancelReason || '').includes('超时')) return '支付超时已关闭'
  return STATUS_LABELS[order.displayStatus || order.status] || order.displayStatus || order.status || '处理中'
}

function normalizeOrder(order: Record<string, any>) {
  const items = order.items || []
  return {
    ...order,
    statusLabel: statusLabel(order),
    paymentLabel: PAYMENT_LABELS[order.paymentStatus] || order.paymentStatus || '-',
    displayTime: formatTime(order.createTime),
    displayAmount: productUtils.formatPrice(order.payableAmount),
    displayProductAmount: productUtils.formatPrice(order.productAmount),
    displayShippingFee: productUtils.formatPrice(order.shippingFee),
    displayDiscountAmount: productUtils.formatPrice(order.discountAmount),
    itemSummary: items.length ? `${items[0].productName || '商品'}${items.length > 1 ? ` 等 ${items.length} 件` : ''}` : '查看订单明细',
    items: items.map((item: Record<string, any>) => ({ ...item, displayImage: productUtils.localProductImage(item.productImage), displayUnitPrice: productUtils.formatPrice(item.unitPrice), displayLineAmount: productUtils.formatPrice(item.lineAmount) })),
    operations: (order.operations || []).slice().reverse().map((item: Record<string, any>) => ({ ...item, displayTime: formatTime(item.createTime), statusLabel: STATUS_LABELS[item.toStatus] || item.toStatus || '状态更新' }))
  }
}

function normalizePayments(payments: Array<Record<string, any>> | null | undefined) {
  return (payments || []).map(item => ({ ...item, statusLabel: PAYMENT_ATTEMPT_LABELS[item.status] || item.status || '处理中', displayAmount: productUtils.formatPrice(item.amount), displayTime: formatTime(item.createTime) }))
}

function paymentDeadline(order: Record<string, any> | null) {
  if (!order) return 0
  return parseTime(order.paymentStatus === 'PAYING' ? order.paymentResultDeadline : order.paymentCreateDeadline)
}

function countdown(deadline: number, now = Date.now()) {
  const seconds = deadline ? Math.max(0, Math.ceil((deadline - now) / 1000)) : 0
  return `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`
}

function createPaymentKey() {
  let random = ''
  while (random.length < 16) random += Math.floor(Math.random() * 0x100000000).toString(16).padStart(8, '0')
  return `mp-payment-${Date.now()}-${random.slice(0, 16)}`
}

module.exports = { normalizeOrder, normalizePayments, paymentDeadline, countdown, statusLabel, formatTime, parseTime, createPaymentKey }
