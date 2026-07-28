import request from '@/utils/request'
export const listStocks = query => request({ url: '/mall/inventory/list', method: 'get', params: query })
export const getStock = id => request({ url: '/mall/inventory/' + id, method: 'get' })
export const listStockLogs = (id, params) => request({ url: '/mall/inventory/' + id + '/logs', method: 'get', params })
export const adjustStock = (id, data) => request({ url: '/mall/inventory/' + id + '/adjust', method: 'put', data })
