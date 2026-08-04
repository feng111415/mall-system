import request from '@/utils/request'

export const listOrderCenter = query => request({ url: '/mall/order-center', method: 'get', params: query })
export const getOrderCenterDetail = orderId => request({ url: '/mall/order-center/' + orderId, method: 'get' })
