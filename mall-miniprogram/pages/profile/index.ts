const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const auth = require('../../utils/auth')
const memberCenter = require('../../utils/memberCenter')

interface SmsChallenge {
  challengeId: string
  pieceX: number
  positionScale: number
  sceneImage: string
  pieceImage: string
  expiresIn: number
}

Page({
  data: {
    loggedIn: false,
    loading: false,
    error: '',
    feedbackSuccess: false,
    phone: '',
    code: '',
    agreed: false,
    seconds: 0,
    member: null as MallMemberProfile | null,
    avatarLetter: '拾',
    challengeOpen: false,
    challengeLoading: false,
    challengeVerifying: false,
    challengeError: '',
    challenge: null as SmsChallenge | null,
    challengePosition: 120,
    challengePercent: 12,
    statusBarHeight: 20,
    couponCount: 0
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    const cooldown = auth.readSmsCooldown()
    this.setData({
      statusBarHeight: windowInfo.statusBarHeight || 20,
      loggedIn: Boolean(env.getToken()),
      phone: cooldown?.phone || '',
      seconds: auth.getCooldownSeconds(cooldown)
    })
    if (cooldown) this.startCooldownTimer(cooldown.expiresAt)
    if (env.getToken()) this.loadProfile()
  },

  onShow() {
    if (this.getTabBar) this.getTabBar().setData({ selected: 4 })
    if (env.getToken()) this.loadCouponCount()
    const cooldown = auth.readSmsCooldown()
    if (cooldown && cooldown.expiresAt > Date.now()) {
      this.setData({ phone: cooldown.phone, seconds: auth.getCooldownSeconds(cooldown) })
      this.startCooldownTimer(cooldown.expiresAt)
    } else if (this.data.seconds) {
      this.setData({ seconds: 0 })
    }
  },

  onUnload() {
    if (this.cooldownTimer) clearInterval(this.cooldownTimer)
  },

  cooldownTimer: null as ReturnType<typeof setInterval> | null,

  startCooldownTimer(expiresAt: number) {
    if (this.cooldownTimer) clearInterval(this.cooldownTimer)
    const update = () => {
      const seconds = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000))
      this.setData({ seconds })
      if (!seconds) {
        if (this.cooldownTimer) clearInterval(this.cooldownTimer)
        this.cooldownTimer = null
        auth.clearSmsCooldown()
      }
    }
    update()
    if (this.data.seconds) this.cooldownTimer = setInterval(update, 1000)
  },

  handlePhoneInput(event: WechatMiniprogram.Input) {
    this.setData({ phone: String(event.detail.value || '').replace(/\D/g, '').slice(0, 11), error: '', feedbackSuccess: false })
  },

  handleCodeInput(event: WechatMiniprogram.Input) {
    this.setData({ code: String(event.detail.value || '').replace(/\D/g, '').slice(0, 6), error: '', feedbackSuccess: false })
  },

  handleAgreementChange(event: WechatMiniprogram.CheckboxGroupChange) {
    this.setData({ agreed: Boolean(event.detail.value?.length) })
  },

  handleSendCode() {
    this.sendCode()
  },

  async sendCode(challengeTicket = '') {
    if (!/^1[3-9]\d{9}$/.test(this.data.phone)) {
      this.setData({ error: '请输入正确的中国大陆手机号', feedbackSuccess: false })
      return
    }
    if (this.data.seconds || this.data.loading) return
    this.setData({ loading: true, error: '', feedbackSuccess: false })
    try {
      const result = await mallApi.sendSmsCode(this.data.phone, challengeTicket)
      if (result?.challengeRequired) {
        await this.openChallenge()
        return
      }
      const cooldown = auth.startSmsCooldown(this.data.phone, result?.resendAfter || 60)
      this.setData({ seconds: auth.getCooldownSeconds(cooldown), error: '验证码已发送，请查收短信', feedbackSuccess: true })
      this.startCooldownTimer(cooldown.expiresAt)
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '验证码发送失败，请稍后重试', feedbackSuccess: false })
    } finally {
      this.setData({ loading: false })
    }
  },

  async openChallenge() {
    this.setData({ challengeLoading: true, challengeError: '' })
    try {
      const challenge = await mallApi.createSmsChallenge(this.data.phone)
      this.setData({
        challenge,
        challengePosition: challenge.pieceX || 120,
        challengePercent: (challenge.pieceX || 120) / (challenge.positionScale || 10),
        challengeOpen: true
      })
      if (this.getTabBar) this.getTabBar().setData({ hidden: true })
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '安全验证加载失败，请稍后重试', feedbackSuccess: false })
    } finally {
      this.setData({ challengeLoading: false })
    }
  },

  handleChallengeInput(event: WechatMiniprogram.Input) {
    const position = Math.max(0, Math.min(1000, Number(event.detail.value || 0)))
    const scale = this.data.challenge?.positionScale || 10
    this.setData({ challengePosition: position, challengePercent: position / scale, challengeError: '' })
  },

  closeChallenge() {
    if (this.data.challengeVerifying) return
    this.setData({ challengeOpen: false, challenge: null, challengeError: '' })
    if (this.getTabBar) this.getTabBar().setData({ hidden: false })
  },

  async verifyChallenge() {
    if (!this.data.challenge || this.data.challengeVerifying) return
    this.setData({ challengeVerifying: true, challengeError: '' })
    try {
      const result = await mallApi.verifySmsChallenge(this.data.challenge.challengeId, this.data.challengePosition)
      this.setData({ challengeOpen: false, challenge: null })
      if (this.getTabBar) this.getTabBar().setData({ hidden: false })
      await this.sendCode(result.ticket)
    } catch (error) {
      this.setData({ challengeError: error instanceof Error ? error.message : '拼图位置不正确，请重试' })
    } finally {
      this.setData({ challengeVerifying: false })
    }
  },

  async login() {
    if (!/^1[3-9]\d{9}$/.test(this.data.phone)) return this.setData({ error: '请输入正确的中国大陆手机号', feedbackSuccess: false })
    if (!/^\d{6}$/.test(this.data.code)) return this.setData({ error: '请输入 6 位验证码', feedbackSuccess: false })
    if (!this.data.agreed) return this.setData({ error: '请先同意用户协议和隐私政策', feedbackSuccess: false })
    this.setData({ loading: true, error: '', feedbackSuccess: false })
    try {
      const result = await mallApi.loginBySms(this.data.phone, this.data.code)
      env.setToken(result.token)
      this.setData({
        loggedIn: true,
        member: result.member,
        avatarLetter: (result.member.nickname || '拾').slice(0, 1),
        code: '',
        error: result.newMember ? '账号已创建并登录' : '登录成功',
        feedbackSuccess: true
      })
      const app = getApp<IAppOption>()
      app.globalData.member = result.member
      this.loadCouponCount()
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '登录失败，请检查验证码', feedbackSuccess: false })
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadProfile() {
    this.setData({ loading: true, error: '' })
    try {
      const member = await mallApi.getProfile()
      this.setData({ loggedIn: true, member, avatarLetter: (member.nickname || '拾').slice(0, 1) })
      this.loadCouponCount()
    } catch (error) {
      env.clearToken()
      this.setData({ loggedIn: false, member: null, error: error instanceof Error ? error.message : '登录状态已失效，请重新登录', feedbackSuccess: false })
    } finally {
      this.setData({ loading: false })
    }
  },
  async loadCouponCount() {
    try {
      const coupons = await mallApi.getCoupons()
      this.setData({ couponCount: memberCenter.availableCouponCount(coupons || []) })
    } catch (_) { this.setData({ couponCount: 0 }) }
  },

  async logout() {
    this.setData({ loading: true })
    try { await mallApi.logout() } catch (_) { /* token is cleared locally regardless */ }
    env.clearToken()
    const app = getApp<IAppOption>()
    app.globalData.member = null
    this.setData({ loggedIn: false, member: null, avatarLetter: '拾', code: '', couponCount: 0, error: '已退出登录', feedbackSuccess: true })
    this.setData({ loading: false })
  },
  openOrders() { wx.navigateTo({ url: '/pages/orders/index' }) },
  openAfterSales() { wx.navigateTo({ url: '/pages/after-sales/index' }) },
  openCoupons() { wx.navigateTo({ url: '/pages/coupons/index' }) },
  openMessages() { wx.switchTab({ url: '/pages/messages/index' }) }
})
