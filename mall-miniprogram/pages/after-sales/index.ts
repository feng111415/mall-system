const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const orderUtils = require('../../utils/order')

Page({
  data: {
    statusBarHeight: 20,
    loggedIn: false,
    loading: true,
    error: '',
    afterSales: [] as Array<Record<string, any>>
  },
  onLoad() {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: info.statusBarHeight || 20 })
  },
  onShow() { this.loadAfterSales() },
  async loadAfterSales() {
    const loggedIn = Boolean(env.getToken())
    if (!loggedIn) { this.setData({ loggedIn: false, loading: false, error: '', afterSales: [] }); return }
    this.setData({ loggedIn: true, loading: true, error: '' })
    try {
      const result = await mallApi.getAfterSales()
      this.setData({ afterSales: (result || []).map(orderUtils.normalizeAfterSale), loading: false })
    } catch (error) {
      this.setData({ loading: false, error: error instanceof Error ? error.message : '售后记录读取失败' })
    }
  },
  openDetail(event: WechatMiniprogram.BaseEvent) {
    const afterSaleId = Number(event.currentTarget.dataset.id || 0)
    if (afterSaleId) wx.navigateTo({ url: `/pages/after-sales/detail/index?afterSaleId=${afterSaleId}` })
  },
  goLogin() { wx.switchTab({ url: '/pages/profile/index' }) },
  goOrders() { wx.navigateTo({ url: '/pages/orders/index' }) },
  goBack() { const pages = getCurrentPages(); if (pages.length > 1) wx.navigateBack(); else wx.switchTab({ url: '/pages/profile/index' }) },
  handleRetry() { this.loadAfterSales() }
})
