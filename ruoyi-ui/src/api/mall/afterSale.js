import request from '@/utils/request'

export const listAfterSales = params => request({ url: '/mall/after-sale/item-orders', method: 'get', params })
export const getAfterSale = id => request({ url: '/mall/after-sale/item-orders/' + id, method: 'get' })
export const approveAfterSale = id => request({ url: '/mall/after-sale/item-orders/' + id + '/approve', method: 'post' })
export const rejectAfterSale = (id, reason) => request({ url: '/mall/after-sale/item-orders/' + id + '/reject', method: 'post', data: { reason } })
export const refundAfterSale = id => request({ url: '/mall/after-sale/item-orders/' + id + '/refund', method: 'post' })
