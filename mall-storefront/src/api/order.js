import http from './http'

export const cancelOrder = (orderId, reason) => http.post(`/mall/orders/${orderId}/cancel`, { reason })
export const getOrders = params => http.get('/mall/orders', { params })
export const getOrderDetail = orderId => http.get(`/mall/orders/${orderId}`)
