const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const orderUtils = require('../../utils/order')

Page({
  data: { statusBarHeight: 20, status: '', loggedIn: false, loading: true, error: '', orders: [] as Array<Record<string, any>> },
  onLoad(query: Record<string, string>) {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: info.statusBarHeight || 20, status: query.status || '' })
  },
  onShow() {
    this.loadOrders()
  },
  async loadOrders() {
    const loggedIn = Boolean(env.getToken())
    if (!loggedIn) { this.setData({ loggedIn: false, loading: false, error: '', orders: [] }); return }
    this.setData({ loggedIn: true, loading: true, error: '' })
    try {
      const params: Record<string, string | number> = { limit: 50 }
      if (this.data.status) params.status = this.data.status
      const orders = await mallApi.getOrders(params)
      this.setData({ orders: (orders || []).map(orderUtils.normalizeOrder), loading: false })
    } catch (error) {
      this.setData({ loading: false, error: error instanceof Error ? error.message : '订单读取失败，请稍后重试' })
    }
  },
  openOrder(event: WechatMiniprogram.BaseEvent) {
    const orderId = Number(event.currentTarget.dataset.id || 0)
    if (orderId) wx.navigateTo({ url: `/pages/orders/detail/index?orderId=${orderId}` })
  },
  selectStatus(event: WechatMiniprogram.BaseEvent) {
    this.setData({ status: String(event.currentTarget.dataset.status || '') })
    this.loadOrders()
  },
  goLogin() { wx.switchTab({ url: '/pages/profile/index' }) },
  goCatalog() { wx.switchTab({ url: '/pages/catalog/index' }) },
  goBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) wx.navigateBack()
    else wx.switchTab({ url: '/pages/home/index' })
  },
  handleRetry() { this.loadOrders() }
})
