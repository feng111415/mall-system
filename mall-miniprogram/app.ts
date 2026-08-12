const { request } = require('./utils/request')

App({
  globalData: {
    config: require('./config/env'),
    request,
    member: null
  },

  onLaunch() {
    wx.setStorageSync('mall_device_id', this.globalData.config.getDeviceId())
  }
})
