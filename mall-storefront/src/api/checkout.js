import http from './http'

export const getCheckoutPreview = memberCouponId => http.get('/mall/checkout/preview', { params: memberCouponId ? { memberCouponId } : {} })
export const createOrder = payload => http.post('/mall/orders', payload)
