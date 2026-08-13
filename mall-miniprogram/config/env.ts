const STORAGE_KEY = 'mall_device_id'
const DEVELOP_API_OVERRIDE_KEY = 'mall_develop_api_base_url'

const API_BASE_URLS = {
  develop: 'http://localhost:8080',
  trial: '',
  release: ''
}

function randomId() {
  let value = ''
  while (value.length < 32) {
    value += Math.floor(Math.random() * 0x100000000).toString(16).padStart(8, '0')
  }
  return value.slice(0, 32)
}

function getDeviceId() {
  const existing = wx.getStorageSync(STORAGE_KEY)
  if (typeof existing === 'string' && /^[a-f0-9]{32}$/.test(existing)) return existing
  const id = randomId()
  wx.setStorageSync(STORAGE_KEY, id)
  return id
}

module.exports = {
  getApiBaseUrl() {
    const info = wx.getAccountInfoSync ? wx.getAccountInfoSync() : null
    const envVersion = info && info.miniProgram && info.miniProgram.envVersion || 'develop'
    if (envVersion === 'develop') {
      const override = wx.getStorageSync(DEVELOP_API_OVERRIDE_KEY)
      if (typeof override === 'string' && /^https:\/\/[^/]+$/.test(override)) return override
    }
    const value = API_BASE_URLS[envVersion]
    if (!value) throw new Error(`${envVersion} 环境尚未配置 HTTPS API 域名`)
    return value
  },
  getDeviceId,
  setDevelopApiBaseUrl(url: string) {
    const value = (url || '').trim().replace(/\/$/, '')
    if (value && !/^https:\/\/[^/]+$/.test(value)) throw new Error('联调地址必须是 HTTPS 域名且不能包含路径')
    if (value) wx.setStorageSync(DEVELOP_API_OVERRIDE_KEY, value)
    else wx.removeStorageSync(DEVELOP_API_OVERRIDE_KEY)
  },
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
