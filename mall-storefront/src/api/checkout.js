import http from './http'

export const getCheckoutPreview = () => http.get('/mall/checkout/preview')
export const createOrder = payload => http.post('/mall/orders', payload)
