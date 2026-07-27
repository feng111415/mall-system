import http from './http'

export const cancelOrder = (orderId, reason) => http.post(`/mall/orders/${orderId}/cancel`, { reason })
