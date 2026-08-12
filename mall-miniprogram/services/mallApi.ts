const { request } = require('../utils/request')

const mallApi = {
  getHome() {
    return request({ url: '/api/mall/homepage' })
  },
  getCatalog(params?: Record<string, string | number>) {
    return request({ url: '/api/mall/catalog/products', data: params })
  },
  getCart() {
    return request({ url: '/api/mall/cart' })
  },
  getMessages(params?: Record<string, string | number>) {
    return request({ url: '/api/mall/member/messages', data: params })
  },
  getProfile() {
    return request({ url: '/api/mall/member/profile' })
  },
  getOrders(params?: Record<string, string | number>) {
    return request({ url: '/api/mall/orders', data: params })
  }
}

module.exports = mallApi
