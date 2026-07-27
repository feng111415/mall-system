import http from './http'

export const getCheckoutPreview = () => http.get('/mall/checkout/preview')
