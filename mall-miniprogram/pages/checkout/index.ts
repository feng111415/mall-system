const mallApi = require('../../services/mallApi')
const checkoutUtils = require('../../utils/checkout')
const env = require('../../config/env')

Page({
  data: {
    statusBarHeight: 20,
    loading: true,
    submitting: false,
    error: '',
    feedback: '',
    preview: null as Record<string, any> | null,
    selectedAddressId: 0,
    selectedCouponId: 0,
    remark: '',
    order: null as Record<string, any> | null,
    idempotencyKey: '',
    addressBookOpened: false
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({
      statusBarHeight: windowInfo.statusBarHeight || 20,
      idempotencyKey: checkoutUtils.createIdempotencyKey()
    })
    this.loadPreview()
  },

  onShow() {
    if (this.data.addressBookOpened) {
      this.setData({ addressBookOpened: false })
      this.loadPreview()
    }
  },

  async loadPreview(memberCouponId?: number) {
    if (!env.getToken()) {
      this.setData({ loading: false, error: '请先登录后再结算' })
      return
    }
    this.setData({ loading: true, error: '', feedback: '' })
    try {
      const couponId = memberCouponId === undefined ? this.data.selectedCouponId : memberCouponId
      const preview = checkoutUtils.normalizeCheckout(await mallApi.getCheckoutPreview(couponId || undefined))
      const selectedAddressExists = preview.addresses.some((item: Record<string, any>) => Number(item.addressId) === this.data.selectedAddressId)
      this.setData({
        preview,
        selectedAddressId: selectedAddressExists ? this.data.selectedAddressId : preview.defaultAddressId,
        selectedCouponId: preview.selectedMemberCouponId,
        loading: false
      })
    } catch (error) {
      this.setData({ loading: false, preview: null, error: error instanceof Error ? error.message : '结算信息读取失败，请返回购物车重试' })
    }
  },

  chooseAddress(event: WechatMiniprogram.BaseEvent) {
    if (this.data.order) return
    this.setData({ selectedAddressId: Number(event.currentTarget.dataset.id || 0), feedback: '' })
  },

  openAddressForm() {
    this.setData({ addressBookOpened: true, feedback: '' })
    wx.navigateTo({ url: '/pages/addresses/index?from=checkout&create=1' })
  },
  async chooseCoupon(event: WechatMiniprogram.BaseEvent) {
    if (this.data.order) return
    const couponId = Number(event.currentTarget.dataset.id || 0)
    this.setData({ selectedCouponId: couponId, feedback: '' })
    await this.loadPreview(couponId)
  },

  handleRemark(event: WechatMiniprogram.Input) {
    if (!this.data.order) this.setData({ remark: String(event.detail.value || '').slice(0, 200) })
  },

  async submitOrder() {
    const preview = this.data.preview
    if (!preview?.canSubmit || !this.data.selectedAddressId || this.data.order || this.data.submitting) return
    this.setData({ submitting: true, error: '', feedback: '' })
    try {
      const order = await mallApi.createOrder({
        idempotencyKey: this.data.idempotencyKey,
        addressId: this.data.selectedAddressId,
        memberCouponId: this.data.selectedCouponId || undefined,
        remark: this.data.remark.trim() || undefined
      })
      this.setData({ order, feedback: '订单创建成功，请在支付时限内完成支付' })
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '订单创建失败，请刷新结算信息后重试' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  openOrderDetail() {
    const orderId = Number(this.data.order?.orderId || 0)
    if (orderId) wx.redirectTo({ url: `/pages/orders/detail/index?orderId=${orderId}` })
  },

  goBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) wx.navigateBack()
    else wx.switchTab({ url: '/pages/cart/index' })
  },

  goCart() { wx.switchTab({ url: '/pages/cart/index' }) },
  handleRetry() { this.loadPreview() }
})
