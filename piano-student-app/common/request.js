import { BASE_URL, assertBaseUrl } from './config'

export function request(options) {
  assertBaseUrl()
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'content-type': 'application/json',
        ...(options.header || {})
      },
      success: (res) => {
        const body = res.data || {}
        if (res.statusCode === 200 && body.code === 200) {
          resolve(body)
          return
        }
        const message = body.msg || '请求失败，请稍后重试'
        uni.showToast({ title: message, icon: 'none' })
        reject(new Error(message))
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}
