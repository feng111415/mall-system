const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const memberCenter = require('../../utils/memberCenter')

Page({
  data: {
    statusBarHeight: 20,
    loggedIn: false,
    loading: true,
    busy: false,
    error: '',
    feedback: '',
    summaries: [] as Array<Record<string, any>>,
    totalMessageCount: 0,
    messages: [] as Array<Record<string, any>>,
    category: '',
    categoryLabel: '全部消息',
    detail: null as Record<string, any> | null,
    detailAction: ''
  },
  onLoad() {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: info.statusBarHeight || 20 })
  },
  onShow() {
    if (this.getTabBar) this.getTabBar().setData({ selected: 3 })
    this.loadMessages()
  },
  async loadMessages() {
    const loggedIn = Boolean(env.getToken())
    if (!loggedIn) return this.setData({ loggedIn: false, loading: false, error: '', summaries: [], messages: [] })
    this.setData({ loggedIn: true, loading: true, error: '' })
    try {
      const [summaries, messages] = await Promise.all([
        mallApi.getMessageSummary(),
        mallApi.getMessages(this.data.category ? { category: this.data.category } : undefined)
      ])
      const normalizedSummaries = (summaries || []).map(memberCenter.normalizeMessageSummary)
      this.setData({
        summaries: normalizedSummaries,
        totalMessageCount: normalizedSummaries.reduce((total: number, item: Record<string, any>) => total + item.totalCount, 0),
        messages: (messages || []).map(memberCenter.normalizeMessage),
        loading: false
      })
    } catch (error) {
      this.setData({ loading: false, error: error instanceof Error ? error.message : '消息读取失败' })
    }
  },
  chooseCategory(event: WechatMiniprogram.BaseEvent) {
    const category = String(event.currentTarget.dataset.category || '')
    const summary = this.data.summaries.find(item => item.category === category)
    this.setData({ category, categoryLabel: summary?.label || '全部消息', feedback: '' })
    this.loadMessages()
  },
  async openMessage(event: WechatMiniprogram.BaseEvent) {
    const messageId = Number(event.currentTarget.dataset.id || 0)
    if (!messageId || this.data.busy) return
    this.setData({ busy: true, feedback: '' })
    try {
      const value = memberCenter.normalizeMessage(await mallApi.getMessage(messageId))
      if (value.unread) await mallApi.markMessageRead(messageId)
      this.setData({ detail: { ...value, unread: false }, detailAction: memberCenter.resolveMessageAction(value.actionPath) })
      await this.refreshAfterRead()
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '消息详情读取失败' })
    } finally { this.setData({ busy: false }) }
  },
  async refreshAfterRead() {
    const summaries = (await mallApi.getMessageSummary()).map(memberCenter.normalizeMessageSummary)
    this.setData({
      summaries,
      totalMessageCount: summaries.reduce((total: number, item: Record<string, any>) => total + item.totalCount, 0),
      messages: this.data.messages.map(item => this.data.detail?.messageId === item.messageId ? { ...item, unread: false, readFlag: '1' } : item)
    })
  },
  async markAllRead() {
    if (this.data.busy || !this.data.messages.some(item => item.unread)) return
    this.setData({ busy: true, feedback: '' })
    try {
      const count = await mallApi.markAllMessagesRead(this.data.category || undefined)
      await this.loadMessages()
      this.setData({ feedback: `已将 ${Number(count || 0)} 条消息标记为已读` })
    } catch (error) {
      this.setData({ feedback: error instanceof Error ? error.message : '全部已读操作失败' })
    } finally { this.setData({ busy: false }) }
  },
  closeDetail() { if (!this.data.busy) this.setData({ detail: null, detailAction: '' }) },
  openAction() {
    const url = this.data.detailAction
    if (!url) return
    this.setData({ detail: null, detailAction: '' })
    if (url === '/pages/profile/index') wx.switchTab({ url })
    else wx.navigateTo({ url })
  },
  goLogin() { wx.switchTab({ url: '/pages/profile/index' }) },
  handleRetry() { this.loadMessages() },
  noop() {}
})
