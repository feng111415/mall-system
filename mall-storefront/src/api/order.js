import http from './http'

export const cancelOrder = (orderId, reason) => http.post(`/mall/orders/${orderId}/cancel`, { reason })
export const getOrders = params => http.get('/mall/orders', { params })
export const getOrderDetail = orderId => http.get(`/mall/orders/${orderId}`)
export const getOrderLogistics = orderId => http.get(`/mall/orders/${orderId}/logistics`)
export const confirmReceipt = orderId => http.post(`/mall/orders/${orderId}/confirm-receipt`)
export const applyRefund = (orderId, reason) => http.post(`/mall/orders/${orderId}/refund`, { reason })
export const applyItemAfterSale = (orderId, data) => http.post(`/mall/orders/${orderId}/after-sales`, data)
export const getAfterSales = () => http.get('/mall/after-sales')
export const submitReturnTracking = (afterSaleId, data) => http.post(`/mall/after-sales/${afterSaleId}/return-tracking`, data)
