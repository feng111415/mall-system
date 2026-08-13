const mallApi = require('../../../services/mallApi')
const orderUtils = require('../../../utils/order')
const env = require('../../../config/env')

Page({
  data: {
    statusBarHeight: 20,
    orderId: 0,
    loading: true,
    busy: false,
    error: '',
    actionError: '',
    feedback: '',
    order: null as Record<string, any> | null,
    payments: [] as Array<Record<string, any>>,
    afterSales: [] as Array<Record<string, any>>,
    afterSaleError: '',
    logistics: null as Record<string, any> | null,
    logisticsLoading: false,
    logisticsError: '',
    canConfirmReceipt: false,
    showActions: false,
    countdown: '00:00',
    developmentMode: false,
    cancelOpen: false,
    receiptOpen: false,
    receiptError: '',
    cancelReason: '会员主动取消',
    paymentIdempotencyKey: ''
  },
  timer: null as ReturnType<typeof setInterval> | null,
  refreshedDeadline: 0,

  onLoad(query: Record<string, string>) {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: info.statusBarHeight || 20, orderId: Number(query.orderId || 0), developmentMode: env.getEnvVersion() === 'develop', paymentIdempotencyKey: orderUtils.createPaymentKey() })
    this.loadDetail()
    this.timer = setInterval(() => this.updateCountdown(), 1000)
  },
  onUnload() { if (this.timer) clearInterval(this.timer) },

  async loadDetail(background = false) {
    if (!this.data.orderId) { this.setData({ loading: false, error: '订单参数无效' }); return }
    if (!background) this.setData({ loading: true, error: '', actionError: '' })
    try {
      const [order, payments] = await Promise.all([mallApi.getOrder(this.data.orderId), mallApi.getOrderPayments(this.data.orderId)])
      const normalizedOrder = orderUtils.normalizeOrder(order)
      this.setData({ order: normalizedOrder, payments: orderUtils.normalizePayments(payments), loading: false, error: '' })
      this.updateActionState()
      this.updateCountdown()
      if ((normalizedOrder.status === 'SHIPPED' || normalizedOrder.status === 'COMPLETED') && normalizedOrder.paymentStatus === 'PAID') await this.loadAfterSales()
      else this.setData({ afterSales: [], afterSaleError: '' })
      if (normalizedOrder.status === 'SHIPPED' || normalizedOrder.status === 'COMPLETED') await this.loadLogistics()
      else this.setData({ logistics: null, logisticsLoading: false, logisticsError: '' })
    } catch (error) {
      const message = error instanceof Error ? error.message : '订单详情读取失败'
      this.setData(background ? { actionError: `订单状态刷新失败：${message}` } : { loading: false, error: message })
    }
  },
  async loadAfterSales() {
    try {
      const result = await mallApi.getAfterSales()
      const afterSales = (result || [])
        .filter((item: Record<string, any>) => Number(item.orderId) === this.data.orderId)
        .map(orderUtils.normalizeAfterSale)
      const order = this.data.order
      if (order) {
        const items = order.items.map((item: Record<string, any>) => {
          const availableAfterSaleQuantity = orderUtils.remainingAfterSaleQuantity(item.orderItemId, item.quantity, afterSales)
          const records = afterSales.filter((sale: Record<string, any>) => (sale.items || []).some((detail: Record<string, any>) => Number(detail.orderItemId) === Number(item.orderItemId)))
          return { ...item, availableAfterSaleQuantity, latestAfterSale: records[0] || null }
        })
        this.setData({ order: { ...order, items }, afterSales, afterSaleError: '' })
      }
    } catch (error) {
      this.setData({ afterSales: [], afterSaleError: error instanceof Error ? error.message : '售后记录读取失败' })
    }
  },
  async loadLogistics() {
    this.setData({ logisticsLoading: true, logisticsError: '' })
    try {
      const logistics = orderUtils.normalizeLogistics(await mallApi.getOrderLogistics(this.data.orderId))
      this.setData({ logistics, logisticsLoading: false })
      this.updateActionState()
    } catch (error) {
      this.setData({ logistics: null, logisticsLoading: false, logisticsError: error instanceof Error ? error.message : '物流信息读取失败' })
      this.updateActionState()
    }
  },
  updateActionState() {
    const order = this.data.order
    const canConfirmReceipt = Boolean(order?.status === 'SHIPPED' && this.data.logistics?.status === 'DELIVERED')
    const showActions = Boolean(order?.canCancel
      || (this.data.developmentMode && order?.canCreatePayment)
      || (this.data.developmentMode && order?.canConfirmPayment && this.data.payments[0]?.status === 'PAYING')
      || canConfirmReceipt)
    this.setData({ canConfirmReceipt, showActions })
  },
  updateCountdown() {
    const deadline = orderUtils.paymentDeadline(this.data.order)
    this.setData({ countdown: orderUtils.countdown(deadline) })
    if (deadline > 0 && deadline <= Date.now() && this.refreshedDeadline !== deadline) {
      this.refreshedDeadline = deadline
      this.loadDetail(true)
    }
  },
  async createPayment() {
    if (!this.data.developmentMode || !this.data.order?.canCreatePayment || this.data.busy) return
    this.setData({ busy: true, actionError: '', feedback: '' })
    try {
      await mallApi.createPayment(this.data.orderId, this.data.paymentIdempotencyKey)
      await this.loadDetail()
      this.setData({ feedback: '支付单已创建，请确认模拟支付结果' })
    } catch (error) { this.setData({ actionError: error instanceof Error ? error.message : '支付单创建失败' }) }
    finally { this.setData({ busy: false }) }
  },
  async confirmPayment() {
    const payment = this.data.payments[0]
    if (!this.data.developmentMode || !this.data.order?.canConfirmPayment || payment?.status !== 'PAYING' || this.data.busy) return
    this.setData({ busy: true, actionError: '', feedback: '' })
    try {
      const result = await mallApi.mockPaymentSuccess(payment.paymentNo)
      await this.loadDetail()
      this.setData({ feedback: result.status === 'REFUNDED' ? '支付已超时，款项已原路退回' : '模拟支付成功' })
    } catch (error) { this.setData({ actionError: error instanceof Error ? error.message : '支付结果确认失败' }) }
    finally { this.setData({ busy: false }) }
  },
  openCancel() { if (this.data.order?.canCancel) this.setData({ cancelOpen: true, actionError: '' }) },
  closeCancel() { if (!this.data.busy) this.setData({ cancelOpen: false }) },
  handleCancelReason(event: WechatMiniprogram.Input) { this.setData({ cancelReason: String(event.detail.value || '').slice(0, 200) }) },
  async cancelOrder() {
    if (!this.data.order?.canCancel || this.data.busy) return
    this.setData({ busy: true, actionError: '', feedback: '' })
    try {
      await mallApi.cancelOrder(this.data.orderId, this.data.cancelReason.trim() || '会员主动取消')
      this.setData({ cancelOpen: false })
      await this.loadDetail()
      this.setData({ feedback: '订单已取消' })
    } catch (error) { this.setData({ actionError: error instanceof Error ? error.message : '取消订单失败' }) }
    finally { this.setData({ busy: false }) }
  },
  openReceipt() {
    if (this.data.canConfirmReceipt)
      this.setData({ receiptOpen: true, receiptError: '', actionError: '' })
  },
  closeReceipt() { if (!this.data.busy) this.setData({ receiptOpen: false }) },
  async confirmReceipt() {
    if (!this.data.canConfirmReceipt || this.data.busy) return
    this.setData({ busy: true, receiptError: '', actionError: '', feedback: '' })
    try {
      await mallApi.confirmReceipt(this.data.orderId)
      this.setData({ receiptOpen: false })
      await this.loadDetail()
      this.setData({ feedback: '已确认收货' })
    } catch (error) { this.setData({ receiptError: error instanceof Error ? error.message : '确认收货失败' }) }
    finally { this.setData({ busy: false }) }
  },
  openAfterSale(event: WechatMiniprogram.BaseEvent) {
    const orderItemId = Number(event.currentTarget.dataset.id || 0)
    if (orderItemId) wx.navigateTo({ url: `/pages/after-sales/detail/index?orderId=${this.data.orderId}&orderItemId=${orderItemId}` })
  },
  openAfterSaleDetail(event: WechatMiniprogram.BaseEvent) {
    const afterSaleId = Number(event.currentTarget.dataset.id || 0)
    if (afterSaleId) wx.navigateTo({ url: `/pages/after-sales/detail/index?afterSaleId=${afterSaleId}` })
  },
  goBack() { const pages = getCurrentPages(); if (pages.length > 1) wx.navigateBack(); else wx.navigateTo({ url: '/pages/orders/index' }) },
  handleRetry() { this.loadDetail() },
  noop() {}
})
