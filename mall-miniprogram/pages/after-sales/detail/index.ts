const mallApi = require('../../../services/mallApi')
const orderUtils = require('../../../utils/order')

const LOGISTICS_COMPANIES = [
  { code: 'SF', name: '顺丰速运' }, { code: 'ZTO', name: '中通快递' },
  { code: 'YTO', name: '圆通速递' }, { code: 'STO', name: '申通快递' },
  { code: 'YD', name: '韵达速递' }, { code: 'JD', name: '京东物流' },
  { code: 'EMS', name: '中国邮政 EMS' }, { code: 'JT', name: '极兔速递' },
  { code: 'DEPPON', name: '德邦快递' }
]

Page({
  data: {
    statusBarHeight: 20,
    mode: 'detail',
    orderId: 0,
    orderItemId: 0,
    afterSaleId: 0,
    loading: true,
    busy: false,
    error: '',
    feedback: '',
    order: null as Record<string, any> | null,
    afterSale: null as Record<string, any> | null,
    selectedItem: null as Record<string, any> | null,
    availableQuantity: 0,
    requestedQuantity: 1,
    type: 'ONLY_REFUND',
    reasonCode: 'OTHER',
    reason: '',
    evidenceUrl: '',
    logisticsCompanies: LOGISTICS_COMPANIES,
    companyIndex: -1,
    trackingNo: ''
  },
  onLoad(query: Record<string, string>) {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    const orderId = Number(query.orderId || 0)
    const orderItemId = Number(query.orderItemId || 0)
    const afterSaleId = Number(query.afterSaleId || 0)
    this.setData({ statusBarHeight: info.statusBarHeight || 20, mode: afterSaleId ? 'detail' : 'apply', orderId, orderItemId, afterSaleId })
    if (afterSaleId) this.loadAfterSale()
    else this.loadApplication()
  },
  async loadApplication() {
    if (!this.data.orderId || !this.data.orderItemId) { this.setData({ loading: false, error: '售后申请参数无效' }); return }
    this.setData({ loading: true, error: '' })
    try {
      const [order, afterSales] = await Promise.all([mallApi.getOrder(this.data.orderId), mallApi.getAfterSales()])
      const normalizedOrder = orderUtils.normalizeOrder(order)
      const selectedItem = normalizedOrder.items.find((item: Record<string, any>) => Number(item.orderItemId) === this.data.orderItemId)
      if (!selectedItem) throw new Error('订单项不存在')
      const related = (afterSales || []).filter((item: Record<string, any>) => Number(item.orderId) === this.data.orderId)
      const availableQuantity = orderUtils.remainingAfterSaleQuantity(selectedItem.orderItemId, selectedItem.quantity, related)
      if (!availableQuantity) throw new Error('该商品暂无可申请售后数量')
      this.setData({ order: normalizedOrder, selectedItem, availableQuantity, requestedQuantity: availableQuantity, loading: false })
    } catch (error) {
      this.setData({ loading: false, error: error instanceof Error ? error.message : '售后申请信息读取失败' })
    }
  },
  async loadAfterSale() {
    if (!this.data.afterSaleId) { this.setData({ loading: false, error: '售后单参数无效' }); return }
    this.setData({ loading: true, error: '' })
    try {
      const afterSale = orderUtils.normalizeAfterSale(await mallApi.getAfterSale(this.data.afterSaleId))
      const companyIndex = LOGISTICS_COMPANIES.findIndex(item => item.code === afterSale.returnCompanyCode)
      this.setData({ afterSale, orderId: Number(afterSale.orderId || 0), companyIndex, trackingNo: afterSale.returnTrackingNo || '', loading: false })
    } catch (error) {
      this.setData({ loading: false, error: error instanceof Error ? error.message : '售后详情读取失败' })
    }
  },
  selectType(event: WechatMiniprogram.BaseEvent) { this.setData({ type: String(event.currentTarget.dataset.value || 'ONLY_REFUND'), feedback: '' }) },
  selectReason(event: WechatMiniprogram.BaseEvent) { this.setData({ reasonCode: String(event.currentTarget.dataset.value || 'OTHER'), feedback: '' }) },
  decreaseQuantity() { if (this.data.requestedQuantity > 1) this.setData({ requestedQuantity: this.data.requestedQuantity - 1 }) },
  increaseQuantity() { if (this.data.requestedQuantity < this.data.availableQuantity) this.setData({ requestedQuantity: this.data.requestedQuantity + 1 }) },
  handleReason(event: WechatMiniprogram.Input) { this.setData({ reason: String(event.detail.value || '').slice(0, 255), feedback: '' }) },
  handleEvidence(event: WechatMiniprogram.Input) { this.setData({ evidenceUrl: String(event.detail.value || '').trim().slice(0, 1000), feedback: '' }) },
  handleCompany(event: WechatMiniprogram.PickerChange) { this.setData({ companyIndex: Number(event.detail.value), feedback: '' }) },
  handleTrackingNo(event: WechatMiniprogram.Input) { this.setData({ trackingNo: String(event.detail.value || '').trim().slice(0, 128), feedback: '' }) },
  async submitApplication() {
    if (this.data.busy || !this.data.selectedItem) return
    if (this.data.reasonCode === 'QUALITY' && !this.data.evidenceUrl) { this.setData({ feedback: '质量问题需要填写凭证图片地址' }); return }
    this.setData({ busy: true, feedback: '' })
    try {
      const result = await mallApi.applyAfterSale(this.data.orderId, {
        type: this.data.type,
        reasonCode: this.data.reasonCode,
        reason: this.data.reason.trim(),
        evidenceUrl: this.data.evidenceUrl,
        items: [{ orderItemId: this.data.selectedItem.orderItemId, quantity: this.data.requestedQuantity }]
      })
      wx.redirectTo({ url: `/pages/after-sales/detail/index?afterSaleId=${result.afterSaleId}` })
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '售后申请提交失败' })
    } finally { this.setData({ busy: false }) }
  },
  async submitTracking() {
    if (this.data.busy || !this.data.afterSale?.canSubmitTracking) return
    const company = LOGISTICS_COMPANIES[this.data.companyIndex]
    if (!company || !this.data.trackingNo) { this.setData({ feedback: '请选择物流公司并填写退货运单号' }); return }
    this.setData({ busy: true, feedback: '' })
    try {
      await mallApi.submitReturnTracking(this.data.afterSaleId, company.code, this.data.trackingNo)
      await this.loadAfterSale()
      this.setData({ feedback: '退货物流已提交' })
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '退货物流提交失败' })
    } finally { this.setData({ busy: false }) }
  },
  openOrder() { if (this.data.orderId) wx.redirectTo({ url: `/pages/orders/detail/index?orderId=${this.data.orderId}` }) },
  goBack() { const pages = getCurrentPages(); if (pages.length > 1) wx.navigateBack(); else wx.navigateTo({ url: '/pages/after-sales/index' }) },
  handleRetry() { if (this.data.mode === 'apply') this.loadApplication(); else this.loadAfterSale() }
})
