import http from './http'

export const createPayment = (orderId, idempotencyKey) => http.post(`/mall/orders/${orderId}/payment`, { idempotencyKey })
export const mockPaymentSuccess = paymentNo => http.post(`/mall/payments/${paymentNo}/mock-success`)
export const getOrderPayments = orderId => http.get(`/mall/orders/${orderId}/payments`)
