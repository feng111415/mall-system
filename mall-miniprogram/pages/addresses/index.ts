const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const checkoutUtils = require('../../utils/checkout')

function emptyForm() {
  return { addressId: 0, receiverName: '', receiverPhone: '', region: [] as string[], regionText: '', detailAddress: '', postalCode: '', isDefault: true }
}

Page({
  data: {
    statusBarHeight: 20,
    loggedIn: false,
    loading: true,
    saving: false,
    busyId: 0,
    error: '',
    feedback: '',
    fromCheckout: false,
    addresses: [] as Array<Record<string, any>>,
    formOpen: false,
    formTitle: '新增收货地址',
    formError: '',
    form: emptyForm(),
    deleteAddress: null as Record<string, any> | null
  },
  onLoad(query: Record<string, string>) {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: info.statusBarHeight || 20, fromCheckout: query.from === 'checkout' })
    if (query.create === '1') this.openCreate()
  },
  onShow() { this.loadAddresses() },
  async loadAddresses() {
    const loggedIn = Boolean(env.getToken())
    if (!loggedIn) return this.setData({ loggedIn: false, loading: false, error: '', addresses: [] })
    this.setData({ loggedIn: true, loading: true, error: '' })
    try {
      const addresses = await mallApi.getAddresses()
      this.setData({ addresses: (addresses || []).map(checkoutUtils.normalizeAddress), loading: false })
    } catch (error) {
      this.setData({ loading: false, error: error instanceof Error ? error.message : '地址簿读取失败' })
    }
  },
  openCreate() {
    this.setData({ formOpen: true, formTitle: '新增收货地址', formError: '', form: emptyForm() })
  },
  openEdit(event: WechatMiniprogram.BaseEvent) {
    const addressId = Number(event.currentTarget.dataset.id || 0)
    const address = this.data.addresses.find(item => Number(item.addressId) === addressId)
    if (!address) return
    this.setData({
      formOpen: true,
      formTitle: '编辑收货地址',
      formError: '',
      form: {
        addressId,
        receiverName: address.receiverName || '',
        receiverPhone: address.receiverPhone || '',
        region: [address.province, address.city, address.district],
        regionText: [address.province, address.city, address.district].filter(Boolean).join(' / '),
        detailAddress: address.detailAddress || '',
        postalCode: address.postalCode || '',
        isDefault: address.isDefault === '1'
      }
    })
  },
  closeForm() { if (!this.data.saving) this.setData({ formOpen: false, formError: '' }) },
  handleInput(event: WechatMiniprogram.Input) {
    const field = String(event.currentTarget.dataset.field || '')
    let value = String(event.detail.value || '')
    if (field === 'receiverPhone') value = value.replace(/\D/g, '').slice(0, 11)
    if (field) this.setData({ [`form.${field}`]: value, formError: '' })
  },
  handleRegion(event: WechatMiniprogram.PickerChange) {
    const region = (event.detail.value || []) as string[]
    this.setData({ 'form.region': region, 'form.regionText': region.join(' / '), formError: '' })
  },
  handleDefault(event: WechatMiniprogram.SwitchChange) { this.setData({ 'form.isDefault': Boolean(event.detail.value) }) },
  validateForm() {
    const form = this.data.form
    if (!form.receiverName.trim()) return '请填写收货人'
    if (!/^1[3-9]\d{9}$/.test(form.receiverPhone)) return '请输入正确的中国大陆手机号'
    if (form.region.length < 3) return '请选择省市区'
    if (!form.detailAddress.trim()) return '请填写详细地址'
    return ''
  },
  async save() {
    if (this.data.saving) return
    const validation = this.validateForm()
    if (validation) return this.setData({ formError: validation })
    const form = this.data.form
    const payload = checkoutUtils.addressPayload({
      ...form,
      province: form.region[0],
      city: form.region[1],
      district: form.region[2]
    })
    this.setData({ saving: true, formError: '' })
    try {
      if (form.addressId) await mallApi.updateAddress(form.addressId, payload)
      else await mallApi.addAddress(payload)
      this.setData({ formOpen: false })
      if (this.data.fromCheckout && !form.addressId) {
        wx.navigateBack()
        return
      }
      await this.loadAddresses()
      this.setData({ feedback: form.addressId ? '收货地址已更新' : '收货地址已新增' })
    } catch (error) {
      this.setData({ formError: error instanceof Error ? error.message : '地址保存失败' })
    } finally { this.setData({ saving: false }) }
  },
  async setDefault(event: WechatMiniprogram.BaseEvent) {
    const addressId = Number(event.currentTarget.dataset.id || 0)
    const address = this.data.addresses.find(item => Number(item.addressId) === addressId)
    if (!address || address.defaultFlag || this.data.busyId) return
    this.setData({ busyId: addressId, feedback: '' })
    try {
      await mallApi.updateAddress(addressId, checkoutUtils.addressPayload({ ...address, isDefault: '1' }))
      await this.loadAddresses()
      this.setData({ feedback: '默认收货地址已更新' })
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '默认地址设置失败' })
    } finally { this.setData({ busyId: 0 }) }
  },
  openDelete(event: WechatMiniprogram.BaseEvent) {
    const addressId = Number(event.currentTarget.dataset.id || 0)
    const address = this.data.addresses.find(item => Number(item.addressId) === addressId)
    if (address) this.setData({ deleteAddress: address, feedback: '' })
  },
  closeDelete() { if (!this.data.busyId) this.setData({ deleteAddress: null }) },
  async confirmDelete() {
    const addressId = Number(this.data.deleteAddress?.addressId || 0)
    if (!addressId || this.data.busyId) return
    this.setData({ busyId: addressId })
    try {
      await mallApi.deleteAddress(addressId)
      this.setData({ deleteAddress: null })
      await this.loadAddresses()
      this.setData({ feedback: '收货地址已删除' })
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '地址删除失败' })
    } finally { this.setData({ busyId: 0 }) }
  },
  goLogin() { wx.switchTab({ url: '/pages/profile/index' }) },
  goBack() { const pages = getCurrentPages(); if (pages.length > 1) wx.navigateBack(); else wx.switchTab({ url: '/pages/profile/index' }) },
  handleRetry() { this.loadAddresses() },
  noop() {}
})
