const mallApi = require('../../services/mallApi')
const productUtils = require('../../utils/product')
const env = require('../../config/env')

Page({
  data: {
    statusBarHeight: 20,
    loading: true,
    busySkuId: 0,
    error: '',
    feedback: '',
    loggedIn: false,
    items: [] as Array<Record<string, any>>,
    totalCount: 0,
    totalPrice: '0',
    selectedCount: 0,
    canCheckout: false
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: windowInfo.statusBarHeight || 20 })
  },

  onShow() {
    if (this.getTabBar) this.getTabBar().setData({ selected: 2 })
    this.loadCart()
  },

  async loadCart() {
    const loggedIn = Boolean(env.getToken())
    this.setData({ loggedIn, loading: loggedIn, error: '', feedback: '' })
    if (!loggedIn) {
      this.setData({ loading: false, items: [], totalCount: 0, totalPrice: '0', selectedCount: 0, canCheckout: false })
      return
    }
    try {
      const cart = productUtils.normalizeCart(await mallApi.getCart())
      this.applyCart(cart)
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '购物车读取失败，请稍后重试' })
    } finally {
      this.setData({ loading: false })
    }
  },

  applyCart(cart: Record<string, any>) {
    this.setData({
      items: cart.items,
      totalCount: cart.totalCount,
      totalPrice: cart.totalPrice,
      selectedCount: cart.items.filter((item: Record<string, any>) => item.selected).length,
      canCheckout: cart.canCheckout
    })
  },

  async updateItem(skuId: number, action: () => Promise<unknown>, successMessage: string) {
    if (this.data.busySkuId || !skuId) return
    this.setData({ busySkuId: skuId, feedback: '', error: '' })
    try {
      await action()
      await this.loadCart()
      this.setData({ feedback: successMessage })
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '购物车更新失败，请刷新重试' })
    } finally {
      this.setData({ busySkuId: 0 })
    }
  },

  changeQuantity(event: WechatMiniprogram.BaseEvent) {
    const skuId = Number(event.currentTarget.dataset.id || 0)
    const delta = Number(event.currentTarget.dataset.delta || 0)
    const item = this.data.items.find((value: Record<string, any>) => Number(value.skuId) === skuId)
    if (!item || !item.valid || item.stockShortage) return
    const quantity = Math.max(1, Math.min(Number(item.availableStock || 1), Number(item.quantity || 1) + delta))
    if (quantity === item.quantity) return
    this.updateItem(skuId, () => mallApi.updateCartQuantity(skuId, quantity), '商品数量已更新')
  },

  toggleSelected(event: WechatMiniprogram.BaseEvent) {
    const skuId = Number(event.currentTarget.dataset.id || 0)
    const selected = String(event.currentTarget.dataset.selected) !== 'true'
    const item = this.data.items.find((value: Record<string, any>) => Number(value.skuId) === skuId)
    if (!item || !item.valid || item.stockShortage) return
    this.updateItem(skuId, () => mallApi.updateCartSelected(skuId, selected), selected ? '已选中商品' : '已取消选择')
  },

  removeItem(event: WechatMiniprogram.BaseEvent) {
    const skuId = Number(event.currentTarget.dataset.id || 0)
    this.updateItem(skuId, () => mallApi.removeCartItem(skuId), '商品已从购物车移除')
  },

  goLogin() { wx.switchTab({ url: '/pages/profile/index' }) },
  goCatalog() { wx.switchTab({ url: '/pages/catalog/index' }) },
  openCheckout() {
    if (this.data.canCheckout && !this.data.busySkuId) wx.navigateTo({ url: '/pages/checkout/index' })
  },
  handleRetry() { this.loadCart() },
  noop() {}
})
