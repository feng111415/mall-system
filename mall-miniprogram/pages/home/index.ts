const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const homeUtils = require('../../utils/home')

Page({
  data: {
    statusBarHeight: 20,
    loading: true,
    error: '',
    loggedIn: false,
    member: null as MallMemberProfile | null,
    avatarLetter: '拾',
    avatarSrc: '',
    greeting: '今天想买什么？',
    hero: null as Record<string, unknown> | null,
    tickers: [] as Array<Record<string, unknown>>,
    recommendationKicker: 'FOR YOU',
    recommendationTitle: '猜你喜欢',
    products: [] as MallHomeProduct[],
    orderSummary: { pendingPayment: 0, pendingShipment: 0, pendingReceipt: 0, pendingReview: 0 },
    activitySummary: { favoriteCount: 0, historyCount: 0 },
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
  },

  onShow() {
    if (this.getTabBar) this.getTabBar().setData({ selected: 0 })
    this.loadHome()
  },

  async loadHome() {
    if (this.data.loading && this.data.products.length) return
    this.setData({ loading: true, error: '', loggedIn: Boolean(env.getToken()) })
    try {
      const [content, catalog] = await Promise.all([
        mallApi.getHome(),
        mallApi.getCatalog({ sort: 'sales' })
      ])
      const home = homeUtils.normalizeHome(content || {}, catalog || [])
      this.setData(home)
      if (env.getToken()) await this.loadMemberSummary()
      else this.resetMemberSummary()
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '首页加载失败，请稍后重试' })
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadMemberSummary() {
    try {
      const [member, orders, activity] = await Promise.all([
        mallApi.getProfile(),
        mallApi.getOrders({ limit: 50 }),
        mallApi.getProductActivitySummary()
      ])
      const app = getApp<IAppOption>()
      app.globalData.member = member
      this.setData({
        loggedIn: true,
        member,
        avatarLetter: (member.nickname || '拾').slice(0, 1),
        avatarSrc: this.localAvatar(member.avatar),
        greeting: `${this.timeGreeting()}，${member.nickname || '拾光会员'}`,
        orderSummary: homeUtils.summarizeOrders(orders || []),
        activitySummary: activity || { favoriteCount: 0, historyCount: 0 }
      })
    } catch (error) {
      if (!env.getToken()) {
        this.resetMemberSummary()
        return
      }
      throw error
    }
  },

  resetMemberSummary() {
    this.setData({
      loggedIn: false,
      member: null,
      avatarSrc: '',
      avatarLetter: '拾',
      greeting: `${this.timeGreeting()}，今天想买什么？`,
      orderSummary: { pendingPayment: 0, pendingShipment: 0, pendingReceipt: 0, pendingReview: 0 },
      activitySummary: { favoriteCount: 0, historyCount: 0 }
    })
  },

  localAvatar(avatar?: string) {
    if (!avatar || !avatar.startsWith('/assets/avatars/')) return ''
    const filename = avatar.split('/').pop() || ''
    return /^avatar-(berry|coral|graphite|mint|sky|sunny)\.svg$/.test(filename) ? `/assets/avatars/${filename}` : ''
  },

  timeGreeting() {
    const hour = new Date().getHours()
    if (hour < 6) return '夜深了'
    if (hour < 12) return '早上好'
    if (hour < 18) return '下午好'
    return '晚上好'
  },

  handleRetry() {
    this.loadHome()
  },

  openLogin() {
    wx.switchTab({ url: '/pages/profile/index' })
  },

  openOrders(event: WechatMiniprogram.BaseEvent) {
    if (!this.data.loggedIn) return this.openLogin()
    const status = event.currentTarget.dataset.status || ''
    wx.navigateTo({ url: `/pages/orders/index${status ? `?status=${status}` : ''}` })
  },

  openService(event: WechatMiniprogram.BaseEvent) {
    if (!this.data.loggedIn) return this.openLogin()
    wx.showToast({ title: `${event.currentTarget.dataset.title}将在对应模块开放`, icon: 'none' })
  },

  openCatalog() {
    wx.switchTab({ url: '/pages/catalog/index' })
  },

  openProduct(event: WechatMiniprogram.BaseEvent) {
    const spuId = Number(event.currentTarget.dataset.id || 0)
    wx.showToast({ title: `商品 ${spuId} 详情将在下一切片开放`, icon: 'none' })
  }
})
