const { request } = require('./utils/request')

App({
  globalData: {
    config: require('./config/env'),
    request,
    member: null as MallMemberProfile | null
  },

  onLaunch() {
    wx.setStorageSync('mall_device_id', this.globalData.config.getDeviceId())
    if (this.globalData.config.getToken()) {
      this.globalData.request({ url: '/api/mall/member/profile' })
        .then((member: unknown) => { this.globalData.member = member as MallMemberProfile })
        .catch(() => { this.globalData.member = null })
    }
  }
})
