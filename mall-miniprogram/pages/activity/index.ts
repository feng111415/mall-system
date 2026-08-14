const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const activityUtils = require('../../utils/activity')

Page({
  data: {
    statusBarHeight: 20,
    loggedIn: false,
    loading: true,
    busy: false,
    busyId: 0,
    error: '',
    feedback: '',
    tab: 'favorites',
    favorites: [] as Array<Record<string, any>>,
    history: [] as Array<Record<string, any>>,
    historyGroups: [] as Array<Record<string, any>>,
    selectedCount: 0,
    allSelected: false,
    clearOpen: false,
    clearError: ''
  },
  onLoad(query: Record<string, string>) {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: info.statusBarHeight || 20, tab: query.tab === 'history' ? 'history' : 'favorites' })
  },
  onShow() { this.loadActivity() },
  async loadActivity() {
    const loggedIn = Boolean(env.getToken())
    if (!loggedIn) return this.setData({ loggedIn: false, loading: false, error: '', favorites: [], history: [], historyGroups: [] })
    this.setData({ loggedIn: true, loading: true, error: '' })
    try {
      const [favorites, history] = await Promise.all([mallApi.getFavorites(), mallApi.getBrowseHistory()])
      const normalizedHistory = (history || []).map(activityUtils.normalizeActivityItem)
      this.setData({
        favorites: (favorites || []).map(activityUtils.normalizeActivityItem),
        history: normalizedHistory,
        historyGroups: activityUtils.groupHistory(normalizedHistory),
        selectedCount: 0,
        allSelected: false,
        loading: false
      })
    } catch (error) {
      this.setData({ loading: false, error: error instanceof Error ? error.message : '收藏与足迹读取失败' })
    }
  },
  changeTab(event: WechatMiniprogram.BaseEvent) {
    this.setData({ tab: String(event.currentTarget.dataset.value || 'favorites'), feedback: '', clearOpen: false })
  },
  openProduct(event: WechatMiniprogram.BaseEvent) {
    const spuId = Number(event.currentTarget.dataset.id || 0)
    const available = event.currentTarget.dataset.available === true || event.currentTarget.dataset.available === 'true'
    if (spuId && available) wx.navigateTo({ url: `/pages/product/index?spuId=${spuId}` })
  },
  async removeFavorite(event: WechatMiniprogram.BaseEvent) {
    const spuId = Number(event.currentTarget.dataset.id || 0)
    if (!spuId || this.data.busyId) return
    this.setData({ busyId: spuId, feedback: '' })
    try {
      await mallApi.removeFavorite(spuId)
      const favorites = this.data.favorites.filter(item => Number(item.spuId) !== spuId)
      this.setData({ favorites, feedback: '已取消收藏' })
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '取消收藏失败' })
    } finally { this.setData({ busyId: 0 }) }
  },
  toggleHistory(event: WechatMiniprogram.BaseEvent) {
    const spuId = Number(event.currentTarget.dataset.id || 0)
    const history = this.data.history.map(item => Number(item.spuId) === spuId ? { ...item, selected: !item.selected } : item)
    this.refreshHistorySelection(history)
  },
  toggleAll() {
    const next = !this.data.allSelected
    this.refreshHistorySelection(this.data.history.map(item => ({ ...item, selected: next })))
  },
  refreshHistorySelection(history: Array<Record<string, any>>) {
    const selectedCount = history.filter(item => item.selected).length
    this.setData({ history, historyGroups: activityUtils.groupHistory(history).map((group: Record<string, any>) => ({ ...group, items: group.items.map((item: Record<string, any>) => ({ ...item, selected: history.find(current => current.spuId === item.spuId)?.selected || false })) })), selectedCount, allSelected: history.length > 0 && selectedCount === history.length })
  },
  async removeHistory(event: WechatMiniprogram.BaseEvent) {
    const spuId = Number(event.currentTarget.dataset.id || 0)
    if (!spuId || this.data.busyId) return
    this.setData({ busyId: spuId, feedback: '' })
    try {
      await mallApi.removeBrowseHistory(spuId)
      this.refreshHistorySelection(this.data.history.filter(item => Number(item.spuId) !== spuId))
      this.setData({ feedback: '浏览记录已删除' })
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '浏览记录删除失败' })
    } finally { this.setData({ busyId: 0 }) }
  },
  async removeSelected() {
    const ids = this.data.history.filter(item => item.selected).map(item => Number(item.spuId))
    if (!ids.length || this.data.busy) return
    this.setData({ busy: true, feedback: '' })
    try {
      for (let index = 0; index < ids.length; index += 100) {
        await mallApi.removeBrowseHistoryBatch(ids.slice(index, index + 100))
      }
      this.refreshHistorySelection(this.data.history.filter(item => !ids.includes(Number(item.spuId))))
      this.setData({ feedback: `已删除 ${ids.length} 条浏览记录` })
    } catch (error) {
      await this.loadActivity()
      this.setData({ feedback: error instanceof Error ? error.message : '批量删除失败，已刷新当前记录' })
    } finally { this.setData({ busy: false }) }
  },
  openClear() { if (this.data.history.length) this.setData({ clearOpen: true, clearError: '', feedback: '' }) },
  closeClear() { if (!this.data.busy) this.setData({ clearOpen: false, clearError: '' }) },
  async clearHistory() {
    if (this.data.busy) return
    this.setData({ busy: true, clearError: '' })
    try {
      await mallApi.clearBrowseHistory()
      this.setData({ history: [], historyGroups: [], selectedCount: 0, allSelected: false, clearOpen: false, feedback: '浏览足迹已清空' })
    } catch (error) {
      this.setData({ clearError: error instanceof Error ? error.message : '清空足迹失败' })
    } finally { this.setData({ busy: false }) }
  },
  goLogin() { wx.switchTab({ url: '/pages/profile/index' }) },
  goBack() { const pages = getCurrentPages(); if (pages.length > 1) wx.navigateBack(); else wx.switchTab({ url: '/pages/profile/index' }) },
  handleRetry() { this.loadActivity() },
  noop() {}
})
