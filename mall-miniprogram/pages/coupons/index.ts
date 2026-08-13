const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const memberCenter = require('../../utils/memberCenter')

Page({
  data: {
    statusBarHeight: 20,
    loggedIn: false,
    loading: true,
    busyId: 0,
    error: '',
    feedback: '',
    tab: 'mine',
    coupons: [] as Array<Record<string, any>>,
    claimable: [] as Array<Record<string, any>>
  },
  onLoad() {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: info.statusBarHeight || 20 })
  },
  onShow() { this.loadCoupons() },
  async loadCoupons() {
    const loggedIn = Boolean(env.getToken())
    if (!loggedIn) return this.setData({ loggedIn: false, loading: false, error: '', coupons: [], claimable: [] })
    this.setData({ loggedIn: true, loading: true, error: '' })
    try {
      const [coupons, claimable] = await Promise.all([mallApi.getCoupons(), mallApi.getClaimableCoupons()])
      this.setData({
        coupons: (coupons || []).map(memberCenter.normalizeCoupon),
        claimable: (claimable || []).map(memberCenter.normalizeClaimableCoupon),
        loading: false
      })
    } catch (error) {
      this.setData({ loading: false, error: error instanceof Error ? error.message : '优惠券读取失败' })
    }
  },
  changeTab(event: WechatMiniprogram.BaseEvent) {
    this.setData({ tab: String(event.currentTarget.dataset.value || 'mine'), feedback: '' })
  },
  async claim(event: WechatMiniprogram.BaseEvent) {
    const couponId = Number(event.currentTarget.dataset.id || 0)
    if (!couponId || this.data.busyId) return
    this.setData({ busyId: couponId, feedback: '' })
    try {
      await mallApi.claimCoupon(couponId)
      await this.loadCoupons()
      this.setData({ tab: 'mine', feedback: '优惠券已领取，可在结算时选择使用' })
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '优惠券领取失败' })
    } finally { this.setData({ busyId: 0 }) }
  },
  goLogin() { wx.switchTab({ url: '/pages/profile/index' }) },
  goBack() { const pages = getCurrentPages(); if (pages.length > 1) wx.navigateBack(); else wx.switchTab({ url: '/pages/profile/index' }) },
  handleRetry() { this.loadCoupons() }
})
