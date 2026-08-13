const mallApi = require('../../services/mallApi')
const productUtils = require('../../utils/product')
const env = require('../../config/env')

Page({
  data: {
    statusBarHeight: 20,
    loading: true,
    error: '',
    spuId: 0,
    product: null as Record<string, any> | null,
    reviews: { summary: { reviewCount: 0, averageRating: '0.0', fiveStarCount: 0, fourStarCount: 0 }, reviews: [] as Array<Record<string, any>> },
    selectedImage: '',
    selectedSkuId: 0,
    quantity: 1,
    busy: false,
    feedback: '',
    feedbackSuccess: false
  },

  onLoad(query: Record<string, string>) {
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    const spuId = Number(query.spuId || 0)
    this.setData({ statusBarHeight: windowInfo.statusBarHeight || 20, spuId })
    this.loadProduct()
  },

  async loadProduct() {
    if (!this.data.spuId) {
      this.setData({ loading: false, error: '商品参数无效，请返回重新选择' })
      return
    }
    this.setData({ loading: true, error: '' })
    try {
      const [detailResult, reviewResult] = await Promise.allSettled([
        mallApi.getProduct(this.data.spuId),
        mallApi.getProductReviews(this.data.spuId, { limit: 3 })
      ])
      if (detailResult.status !== 'fulfilled' || !detailResult.value) {
        throw detailResult.status === 'rejected' ? detailResult.reason : new Error('商品已下架或不存在')
      }
      const product = productUtils.normalizeProductDetail(detailResult.value)
      const reviews = reviewResult.status === 'fulfilled'
        ? productUtils.normalizeReviews(reviewResult.value)
        : productUtils.normalizeReviews(null)
      this.setData({
        product,
        reviews,
        selectedSkuId: product.selectedSkuId,
        selectedImage: product.displayImage
      })
      wx.setNavigationBarTitle({ title: product.productName || '商品详情' })
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '商品详情加载失败，请稍后重试' })
    } finally {
      this.setData({ loading: false })
    }
  },

  selectSku(event: WechatMiniprogram.BaseEvent) {
    const skuId = Number(event.currentTarget.dataset.id || 0)
    const sku = this.data.product?.skus?.find((item: Record<string, any>) => Number(item.skuId) === skuId)
    if (!sku || !sku.selectable || sku.soldOut) return
    this.setData({
      selectedSkuId: skuId,
      selectedImage: sku.displayImage,
      'product.selectedSku': sku,
      'product.displayPrice': sku.displayPrice,
      'product.displayMarketPrice': sku.displayMarketPrice
    })
  },

  selectImage(event: WechatMiniprogram.BaseEvent) {
    const image = String(event.currentTarget.dataset.image || '')
    if (image) this.setData({ selectedImage: image })
  },

  changeQuantity(event: WechatMiniprogram.BaseEvent) {
    if (this.data.busy || this.data.product?.selectedSku?.soldOut) return
    const delta = Number(event.currentTarget.dataset.delta || 0)
    const stock = Number(this.data.product?.selectedSku?.availableStock || 0)
    const quantity = Math.max(1, Math.min(stock || 1, this.data.quantity + delta))
    this.setData({ quantity })
  },

  async addToCart() {
    const sku = this.data.product?.selectedSku
    if (!sku || sku.soldOut || this.data.busy) return
    if (!env.getToken()) {
      wx.switchTab({ url: '/pages/profile/index' })
      return
    }
    this.setData({ busy: true, feedback: '', feedbackSuccess: false })
    try {
      await mallApi.addCartItem(Number(sku.skuId), this.data.quantity)
      this.setData({ feedback: `${sku.skuName} × ${this.data.quantity} 已加入购物车`, feedbackSuccess: true })
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '加入购物车失败，请稍后重试', feedbackSuccess: false })
    } finally {
      this.setData({ busy: false })
    }
  },

  goBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) wx.navigateBack()
    else wx.switchTab({ url: '/pages/home/index' })
  },

  openCatalog() {
    wx.switchTab({ url: '/pages/catalog/index' })
  },

  handleRetry() {
    this.loadProduct()
  },

  previewImage() {
    if (!this.data.product?.gallery?.length) return
    wx.previewImage({ current: this.data.selectedImage, urls: this.data.product.gallery })
  }
})
