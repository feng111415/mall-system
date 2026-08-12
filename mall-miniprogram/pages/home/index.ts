const mallApi = require('../../services/mallApi')

Page({
  data: {
    statusBarHeight: 20,
    loading: true,
    error: '',
    home: null,
    orderSummary: { pendingPayment: 0, pendingShipment: 0, pendingReceipt: 1, pendingReview: 0 },
    services: [
      { title: '优惠券', subtitle: '结算自动匹配', icon: '/assets/icons/ticket-percent.svg', color: 'yellow' },
      { title: '地址簿', subtitle: '管理收货地址', icon: '/assets/icons/map-pin.svg', color: 'cyan' },
      { title: '收藏足迹', subtitle: '继续看心动商品', icon: '/assets/icons/heart.svg', color: 'coral' },
      { title: '售后服务', subtitle: '查看申请进度', icon: '/assets/icons/headset.svg', color: 'lilac' }
    ]
  },
  onLoad() {
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: windowInfo.statusBarHeight || 20 })
    this.loadHome()
  },
  onShow() {
    if (this.getTabBar) this.getTabBar().setData({ selected: 0 })
  },
  async loadHome() {
    this.setData({ loading: true, error: '' })
    try {
      const home = await mallApi.getHome()
      this.setData({ home })
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '首页加载失败，请稍后重试' })
    } finally {
      this.setData({ loading: false })
    }
  },
  handleRetry() {
    this.loadHome()
  },
  openOrders(event: WechatMiniprogram.BaseEvent) {
    const status = event.currentTarget.dataset.status || ''
    wx.navigateTo({ url: `/pages/orders/index${status ? `?status=${status}` : ''}` })
  },
  openService(event: WechatMiniprogram.BaseEvent) {
    wx.showToast({ title: `${event.currentTarget.dataset.title}将在对应模块开放`, icon: 'none' })
  }
})
