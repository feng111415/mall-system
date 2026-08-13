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
    addressFormOpen: false,
    addressSaving: false,
    addressError: '',
    addressForm: { receiverName: '', receiverPhone: '', region: [] as string[], regionText: '', detailAddress: '', isDefault: true }
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({
      statusBarHeight: windowInfo.statusBarHeight || 20,
      idempotencyKey: checkoutUtils.createIdempotencyKey()
    })
    this.loadPreview()
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

  openAddressForm() { this.setData({ addressFormOpen: true, addressError: '', feedback: '' }) },
  closeAddressForm() { if (!this.data.addressSaving) this.setData({ addressFormOpen: false }) },
  handleAddressInput(event: WechatMiniprogram.Input) {
    const field = String(event.currentTarget.dataset.field || '')
    const value = String(event.detail.value || '')
    if (field) this.setData({ [`addressForm.${field}`]: field === 'receiverPhone' ? value.replace(/\D/g, '').slice(0, 11) : value, addressError: '' })
  },
  handleRegionChange(event: WechatMiniprogram.PickerChange) {
    const region = (event.detail.value || []) as string[]
    this.setData({ 'addressForm.region': region, 'addressForm.regionText': region.join(' / '), addressError: '' })
  },
  async saveAddress() {
    const form = this.data.addressForm
    if (!form.receiverName.trim()) return this.setData({ addressError: '请填写收货人' })
    if (!/^1[3-9]\d{9}$/.test(form.receiverPhone)) return this.setData({ addressError: '请输入正确的中国大陆手机号' })
    if (form.region.length < 3) return this.setData({ addressError: '请选择省市区' })
    if (!form.detailAddress.trim()) return this.setData({ addressError: '请填写详细地址' })
    this.setData({ addressSaving: true, addressError: '' })
    try {
      const address = await mallApi.addAddress({
        receiverName: form.receiverName.trim(),
        receiverPhone: form.receiverPhone,
        province: form.region[0],
        city: form.region[1],
        district: form.region[2],
        detailAddress: form.detailAddress.trim(),
        isDefault: form.isDefault ? '1' : '0'
      })
      this.setData({ addressFormOpen: false, selectedAddressId: Number(address.addressId || 0) })
      await this.loadPreview()
      this.setData({ feedback: '收货地址已添加' })
    } catch (error) {
      this.setData({ addressError: error instanceof Error ? error.message : '地址保存失败，请稍后重试' })
    } finally {
      this.setData({ addressSaving: false })
    }
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

  continueShopping() { wx.switchTab({ url: '/pages/catalog/index' }) },

  goBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) wx.navigateBack()
    else wx.switchTab({ url: '/pages/cart/index' })
  },

  goCart() { wx.switchTab({ url: '/pages/cart/index' }) },
  handleRetry() { this.loadPreview() },
  noop() {}
})
