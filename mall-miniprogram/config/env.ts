const STORAGE_KEY = 'mall_device_id'

const API_BASE_URLS = {
  develop: 'http://localhost:8080',
  trial: '',
  release: ''
}

function randomId() {
  return `mp-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 12)}`
}

function getDeviceId() {
  const existing = wx.getStorageSync(STORAGE_KEY)
  if (existing) return existing
  const id = randomId()
  wx.setStorageSync(STORAGE_KEY, id)
  return id
}

module.exports = {
  getApiBaseUrl() {
    const info = wx.getAccountInfoSync ? wx.getAccountInfoSync() : null
    const envVersion = info && info.miniProgram && info.miniProgram.envVersion || 'develop'
    const value = API_BASE_URLS[envVersion]
    if (!value) throw new Error(`${envVersion} 环境尚未配置 HTTPS API 域名`)
    return value
  },
  getDeviceId,
  getToken() {
    return wx.getStorageSync('mall_access_token') || ''
  },
  setToken(token: string) {
    wx.setStorageSync('mall_access_token', token || '')
  },
  clearToken() {
    wx.removeStorageSync('mall_access_token')
  }
}
