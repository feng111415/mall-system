import request from '@/utils/request'

export const listOrders = query => request({ url: '/mall/logistics/orders', method: 'get', params: query })
export const getShipment = shipmentId => request({ url: '/mall/logistics/shipments/' + shipmentId, method: 'get' })
export const shipOrder = (orderId, data) => request({ url: '/mall/logistics/orders/' + orderId + '/ship', method: 'post', data })
export const appendNode = (shipmentId, data) => request({ url: '/mall/logistics/shipments/' + shipmentId + '/nodes', method: 'post', data })
