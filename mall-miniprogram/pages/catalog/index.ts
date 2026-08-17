const mallApi = require('../../services/mallApi')
const productUtils = require('../../utils/product')

Page({
  data: {
    statusBarHeight: 20,
    loading: true,
    error: '',
    keyword: '',
    selectedCategoryId: 0,
    selectedCategoryName: '全部',
    categoryOpen: false,
    categories: [] as Array<Record<string, any>>,
    products: [] as Array<Record<string, any>>
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: windowInfo.statusBarHeight || 20 })
  },

  onShow() {
    if (this.getTabBar) this.getTabBar().setData({ selected: 1 })
    this.loadCatalog()
  },

  async loadCatalog() {
    this.setData({ loading: true, error: '' })
    try {
      const params: Record<string, string | number> = { sort: 'sales' }
      if (this.data.selectedCategoryId) params.categoryId = this.data.selectedCategoryId
      if (this.data.keyword.trim()) params.keyword = this.data.keyword.trim()
      const [categoryResult, productResult] = await Promise.all([
        mallApi.getCategories(),
        mallApi.getCatalog(params)
      ])
      this.setData({
        categories: [{ categoryId: 0, categoryName: '全部' }, ...(categoryResult || [])],
        products: (productResult || []).map((item: Record<string, any>) => ({
          ...item,
          displayImage: productUtils.localProductImage(item.mainImage),
          displayPrice: productUtils.formatPrice(item.priceMin),
          salesText: `已售 ${Math.max(0, Number(item.salesCount || 0))}`
        }))
      })
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '商品列表加载失败，请稍后重试' })
    } finally {
      this.setData({ loading: false })
    }
  },

  handleKeyword(event: WechatMiniprogram.Input) {
    this.setData({ keyword: String(event.detail.value || '') })
  },

  selectCategory(event: WechatMiniprogram.BaseEvent) {
    this.setData({
      selectedCategoryId: Number(event.currentTarget.dataset.id || 0),
      selectedCategoryName: String(event.currentTarget.dataset.name || '全部'),
      categoryOpen: false
    })
    if (this.getTabBar) this.getTabBar().setData({ hidden: false })
    this.loadCatalog()
  },

  openCategoryPanel() {
    this.setData({ categoryOpen: true })
    if (this.getTabBar) this.getTabBar().setData({ hidden: true })
  },

  closeCategoryPanel() {
    this.setData({ categoryOpen: false })
    if (this.getTabBar) this.getTabBar().setData({ hidden: false })
  },

  openProduct(event: WechatMiniprogram.BaseEvent) {
    const spuId = Number(event.currentTarget.dataset.id || 0)
    if (spuId > 0) wx.navigateTo({ url: `/pages/product/index?spuId=${spuId}` })
  },

  clearSearch() {
    this.setData({ keyword: '' })
    this.loadCatalog()
  },

  handleRetry() {
    this.loadCatalog()
  },

  noop() {}
})
