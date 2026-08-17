const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const security = require('../../utils/security')

type SessionView = ReturnType<typeof security.normalizeSession>

Page({
  data: {
    statusBarHeight: 20,
    loading: true,
    error: '',
    feedback: '',
    feedbackSuccess: false,
    sessions: [] as SessionView[],
    currentSession: null as SessionView | null,
    mobileCount: 0,
    desktopCount: 0,
    primaryChangesRemaining: 0,
    primaryChangeWindowDays: 30,
    canReplacePrimary: false,
    revokeTarget: null as SessionView | null,
    revokeBusy: false,
    primaryOpen: false,
    primaryCode: '',
    primaryError: '',
    primaryCodeSending: false,
    primaryReplacing: false,
    primarySeconds: 0
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    const cooldown = security.readPrimaryCodeCooldown()
    this.setData({
      statusBarHeight: windowInfo.statusBarHeight || 20,
      primarySeconds: security.getPrimaryCodeSeconds(cooldown)
    })
    if (cooldown) this.startPrimaryTimer(cooldown.expiresAt)
    if (!env.getToken()) {
      wx.switchTab({ url: '/pages/profile/index' })
      return
    }
    this.loadSessions()
  },

  onUnload() {
    if (this.primaryTimer) clearInterval(this.primaryTimer)
  },

  primaryTimer: null as ReturnType<typeof setInterval> | null,

  async onPullDownRefresh() {
    await this.loadSessions(false)
    wx.stopPullDownRefresh()
  },

  async loadSessions(showLoading = true) {
    if (showLoading) this.setData({ loading: true })
    this.setData({ error: '', feedback: '' })
    try {
      this.applyOverview(await mallApi.getMemberSessions())
    } catch (error) {
      if (!env.getToken()) {
        wx.switchTab({ url: '/pages/profile/index' })
        return
      }
      this.setData({ error: error instanceof Error ? error.message : '登录设备加载失败，请稍后重试' })
    } finally {
      if (showLoading) this.setData({ loading: false })
    }
  },

  applyOverview(overview?: MallMemberSessionOverview) {
    const sessions = security.normalizeSessions(overview?.sessions || [])
    const remaining = Math.max(0, Number(overview?.primaryChangesRemaining || 0))
    this.setData({
      sessions,
      currentSession: sessions.find((session: SessionView) => session.current) || null,
      mobileCount: sessions.filter((session: SessionView) => session.deviceType === 'MOBILE').length,
      desktopCount: sessions.filter((session: SessionView) => session.deviceType === 'DESKTOP').length,
      primaryChangesRemaining: remaining,
      primaryChangeWindowDays: Math.max(1, Number(overview?.primaryChangeWindowDays || 30)),
      canReplacePrimary: security.canReplacePrimary(sessions, remaining)
    })
  },

  requestRevoke(event: WechatMiniprogram.BaseEvent) {
    const sessionId = Number(event.currentTarget.dataset.id || 0)
    const session = this.data.sessions.find((value: SessionView) => value.sessionId === sessionId)
    if (!session || session.current) return
    this.setData({ revokeTarget: session, feedback: '' })
  },

  closeRevoke() {
    if (!this.data.revokeBusy) this.setData({ revokeTarget: null })
  },

  async confirmRevoke() {
    const target = this.data.revokeTarget
    if (!target || this.data.revokeBusy) return
    this.setData({ revokeBusy: true, feedback: '' })
    try {
      this.applyOverview(await mallApi.revokeMemberSession(target.sessionId))
      this.setData({ revokeTarget: null })
      this.setFeedback('设备已下线', true)
    } catch (error) {
      if (!env.getToken()) {
        wx.switchTab({ url: '/pages/profile/index' })
        return
      }
      this.setFeedback(error instanceof Error ? error.message : '设备下线失败，请稍后重试', false)
    } finally {
      this.setData({ revokeBusy: false })
    }
  },

  openPrimary() {
    if (!this.data.canReplacePrimary) return
    this.setData({ primaryOpen: true, primaryCode: '', primaryError: '', feedback: '' })
  },

  closePrimary() {
    if (this.data.primaryCodeSending || this.data.primaryReplacing) return
    this.setData({ primaryOpen: false, primaryCode: '', primaryError: '' })
  },

  handlePrimaryCodeInput(event: WechatMiniprogram.Input) {
    this.setData({
      primaryCode: String(event.detail.value || '').replace(/\D/g, '').slice(0, 6),
      primaryError: ''
    })
  },

  async sendPrimaryCode() {
    if (this.data.primarySeconds || this.data.primaryCodeSending) return
    this.setData({ primaryCodeSending: true, primaryError: '' })
    try {
      const result = await mallApi.sendPrimaryDeviceCode()
      const cooldown = security.startPrimaryCodeCooldown(result?.resendAfter || 60)
      this.setData({ primarySeconds: security.getPrimaryCodeSeconds(cooldown) })
      this.startPrimaryTimer(cooldown.expiresAt)
    } catch (error) {
      if (!env.getToken()) {
        wx.switchTab({ url: '/pages/profile/index' })
        return
      }
      this.setData({ primaryError: error instanceof Error ? error.message : '验证码发送失败，请稍后重试' })
    } finally {
      this.setData({ primaryCodeSending: false })
    }
  },

  async confirmPrimary() {
    if (this.data.primaryReplacing) return
    if (!/^\d{6}$/.test(this.data.primaryCode)) {
      this.setData({ primaryError: '请输入 6 位验证码' })
      return
    }
    this.setData({ primaryReplacing: true, primaryError: '' })
    try {
      this.applyOverview(await mallApi.replacePrimaryDevice(this.data.primaryCode))
      security.clearPrimaryCodeCooldown()
      if (this.primaryTimer) clearInterval(this.primaryTimer)
      this.primaryTimer = null
      this.setData({ primaryOpen: false, primaryCode: '', primarySeconds: 0 })
      this.setFeedback('当前设备已设为主设备', true)
    } catch (error) {
      if (!env.getToken()) {
        wx.switchTab({ url: '/pages/profile/index' })
        return
      }
      this.setData({ primaryError: error instanceof Error ? error.message : '主设备更换失败，请稍后重试' })
    } finally {
      this.setData({ primaryReplacing: false })
    }
  },

  startPrimaryTimer(expiresAt: number) {
    if (this.primaryTimer) clearInterval(this.primaryTimer)
    const update = () => {
      const seconds = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000))
      this.setData({ primarySeconds: seconds })
      if (!seconds) {
        if (this.primaryTimer) clearInterval(this.primaryTimer)
        this.primaryTimer = null
        security.clearPrimaryCodeCooldown()
      }
    }
    update()
    if (this.data.primarySeconds) this.primaryTimer = setInterval(update, 1000)
  },

  setFeedback(message: string, success: boolean) {
    this.setData({ feedback: message, feedbackSuccess: success })
  },

  noop() {},
  handleRetry() { this.loadSessions() },
  goBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) wx.navigateBack()
    else wx.switchTab({ url: '/pages/profile/index' })
  }
})
