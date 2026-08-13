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
const LOGISTICS_LABELS: Record<string, string> = {
  SHIPPED: '已发货', IN_TRANSIT: '运输中', OUT_FOR_DELIVERY: '派送中', ARRIVED: '已送达',
  DELIVERED: '已签收', EXCEPTION: '运输异常', CORRECTION: '更正说明'
}
const AFTER_SALE_TYPE_LABELS: Record<string, string> = { ONLY_REFUND: '仅退款', RETURN_REFUND: '退货退款' }
const AFTER_SALE_STATUS_LABELS: Record<string, string> = {
  PENDING_REVIEW: '等待审核', APPROVED: '审核通过', RETURN_SHIPPED: '退货已寄出',
  REFUNDING: '退款处理中', SUCCESS: '退款成功', REJECTED: '申请未通过', FAILED: '退款失败'
}
const AFTER_SALE_REASON_LABELS: Record<string, string> = { OTHER: '其他原因', QUALITY: '质量问题', NOT_RECEIVED: '未收到货' }
const ACTIVE_AFTER_SALE_STATUSES = ['PENDING_REVIEW', 'APPROVED', 'RETURN_SHIPPED', 'REFUNDING', 'SUCCESS']

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

function normalizeLogistics(shipment: Record<string, any> | null | undefined) {
  if (!shipment) return null
  const nodes = (shipment.nodes || []).map((item: Record<string, any>) => ({
    ...item,
    statusLabel: LOGISTICS_LABELS[item.nodeStatus] || item.nodeStatus || '物流更新',
    displayTime: formatTime(item.eventTime)
  }))
  const latestNode = nodes[0] || null
  return {
    ...shipment,
    nodes,
    latestNode,
    latestStatusLabel: latestNode?.statusLabel || LOGISTICS_LABELS[shipment.status] || shipment.status || '运输中',
    statusLabel: LOGISTICS_LABELS[shipment.status] || latestNode?.statusLabel || shipment.status || '运输中',
    displayShippedTime: formatTime(shipment.shippedTime),
    displayDeliveredTime: formatTime(shipment.deliveredTime)
  }
}

function normalizeAfterSale(value: Record<string, any>) {
  return {
    ...value,
    typeLabel: AFTER_SALE_TYPE_LABELS[value.type] || value.type || '售后',
    statusLabel: AFTER_SALE_STATUS_LABELS[value.status] || value.status || '处理中',
    reasonLabel: AFTER_SALE_REASON_LABELS[value.reasonCode] || value.reasonCode || '-',
    displayRefundAmount: productUtils.formatPrice(value.refundAmount),
    displayShippingRefundAmount: productUtils.formatPrice(value.shippingRefundAmount),
    displayTime: formatTime(value.createTime),
    displayDeadline: formatTime(value.deadlineTime),
    canSubmitTracking: value.type === 'RETURN_REFUND' && value.status === 'APPROVED',
    items: (value.items || []).map((item: Record<string, any>) => ({
      ...item,
      displayRefundAmount: productUtils.formatPrice(item.refundAmount)
    }))
  }
}

function remainingAfterSaleQuantity(orderItemId: number, orderQuantity: number, afterSales: Array<Record<string, any>>) {
  const occupied = (afterSales || [])
    .filter(item => ACTIVE_AFTER_SALE_STATUSES.includes(item.status))
    .flatMap(item => item.items || [])
    .filter(item => Number(item.orderItemId) === Number(orderItemId))
    .reduce((total, item) => total + Number(item.requestedQuantity || 0), 0)
  return Math.max(0, Number(orderQuantity || 0) - occupied)
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

module.exports = { normalizeOrder, normalizePayments, normalizeLogistics, normalizeAfterSale, remainingAfterSaleQuantity, paymentDeadline, countdown, statusLabel, formatTime, parseTime, createPaymentKey }
