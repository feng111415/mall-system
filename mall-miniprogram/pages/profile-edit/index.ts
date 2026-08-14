const mallApi = require('../../services/mallApi')
const env = require('../../config/env')
const profileUtils = require('../../utils/profile')

interface AvatarPresetView extends MallAvatarPreset {
  avatarSrc: string
}

Page({
  data: {
    statusBarHeight: 20,
    loading: true,
    error: '',
    feedback: '',
    feedbackSuccess: false,
    member: null as MallMemberProfile | null,
    avatarSrc: '',
    avatarLetter: '拾',
    nickname: '',
    nicknameChangesRemaining: 0,
    nicknameChangeWindowDays: 30,
    presets: [] as AvatarPresetView[],
    selectedPresetCode: '',
    savingNickname: false,
    savingAvatar: false
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: windowInfo.statusBarHeight || 20 })
    if (!env.getToken()) {
      wx.switchTab({ url: '/pages/profile/index' })
      return
    }
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true, error: '', feedback: '' })
    try {
      const [member, presets] = await Promise.all([
        mallApi.getProfile(),
        mallApi.getAvatarPresets()
      ])
      const baseUrl = env.getApiBaseUrl()
      const presetViews = (presets || []).map((preset: MallAvatarPreset) => ({
        ...preset,
        avatarSrc: profileUtils.resolveAvatarUrl(preset.url, baseUrl)
      }))
      this.setData({
        presets: presetViews,
        selectedPresetCode: presetViews.find((preset: MallAvatarPreset) => preset.url === member.avatar)?.code || ''
      })
      this.applyMember(member)
    } catch (error) {
      this.setData({ error: error instanceof Error ? error.message : '个人资料加载失败，请稍后重试' })
    } finally {
      this.setData({ loading: false })
    }
  },

  applyMember(member: MallMemberProfile) {
    const app = getApp<IAppOption>()
    app.globalData.member = member
    this.setData({
      member,
      avatarSrc: profileUtils.resolveAvatarUrl(member.avatar, env.getApiBaseUrl()),
      avatarLetter: (member.nickname || '拾').slice(0, 1),
      nickname: member.nickname || '',
      nicknameChangesRemaining: Math.max(0, Number(member.nicknameChangesRemaining || 0)),
      nicknameChangeWindowDays: Math.max(1, Number(member.nicknameChangeWindowDays || 30))
    })
  },

  handleNicknameInput(event: WechatMiniprogram.Input) {
    this.setData({ nickname: String(event.detail.value || ''), feedback: '' })
  },

  async saveNickname() {
    if (this.data.savingNickname) return
    const nickname = profileUtils.normalizeNickname(this.data.nickname)
    const validationError = profileUtils.validateNickname(nickname)
    if (validationError) {
      this.setFeedback(validationError, false)
      return
    }
    if (nickname === this.data.member?.nickname) {
      this.setFeedback('昵称没有变化', true)
      return
    }
    this.setData({ savingNickname: true, feedback: '' })
    try {
      const member = await mallApi.updateNickname(nickname)
      this.applyMember(member)
      this.setFeedback('昵称已更新', true)
    } catch (error) {
      this.setFeedback(error instanceof Error ? error.message : '昵称保存失败，请稍后重试', false)
    } finally {
      this.setData({ savingNickname: false })
    }
  },

  selectPreset(event: WechatMiniprogram.BaseEvent) {
    if (this.data.savingAvatar) return
    this.setData({ selectedPresetCode: String(event.currentTarget.dataset.code || ''), feedback: '' })
  },

  async savePresetAvatar() {
    const presetCode = this.data.selectedPresetCode
    if (!presetCode || this.data.savingAvatar) {
      if (!presetCode) this.setFeedback('请选择一个预设头像', false)
      return
    }
    this.setData({ savingAvatar: true, feedback: '' })
    try {
      const member = await mallApi.selectPresetAvatar(presetCode)
      this.applyMember(member)
      this.setFeedback('头像已更新', true)
    } catch (error) {
      this.setFeedback(error instanceof Error ? error.message : '头像保存失败，请稍后重试', false)
    } finally {
      this.setData({ savingAvatar: false })
    }
  },

  chooseAvatarFromAlbum() {
    if (this.data.savingAvatar) return
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album'],
      sizeType: ['compressed'],
      success: result => {
        const file = result.tempFiles && result.tempFiles[0]
        if (!file) return
        if (Number(file.size || 0) > 5 * 1024 * 1024) {
          this.setFeedback('头像图片不能超过 5MB', false)
          return
        }
        this.cropAvatar(file.tempFilePath)
      },
      fail: error => {
        if (!String(error.errMsg || '').includes('cancel')) this.setFeedback('无法读取相册图片，请稍后重试', false)
      }
    })
  },

  cropAvatar(filePath: string) {
    if (typeof wx.cropImage !== 'function') {
      this.setFeedback('当前微信版本不支持头像裁剪，请升级后重试', false)
      return
    }
    wx.cropImage({
      src: filePath,
      cropScale: '1:1',
      success: result => this.uploadCroppedAvatar(result.tempFilePath),
      fail: error => {
        if (!String(error.errMsg || '').includes('cancel')) this.setFeedback('头像裁剪失败，请重新选择', false)
      }
    })
  },

  async uploadCroppedAvatar(filePath: string) {
    if (this.data.savingAvatar) return
    this.setData({ savingAvatar: true, feedback: '' })
    try {
      const member = await mallApi.uploadAvatar(filePath)
      this.setData({ selectedPresetCode: '' })
      this.applyMember(member)
      this.setFeedback('头像已更新', true)
    } catch (error) {
      this.setFeedback(error instanceof Error ? error.message : '头像上传失败，请稍后重试', false)
    } finally {
      this.setData({ savingAvatar: false })
    }
  },

  setFeedback(message: string, success: boolean) {
    this.setData({ feedback: message, feedbackSuccess: success })
  },

  handleRetry() { this.loadData() },
  goBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) wx.navigateBack()
    else wx.switchTab({ url: '/pages/profile/index' })
  }
})
