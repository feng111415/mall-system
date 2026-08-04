import request from '@/utils/request'

export const listProductInventory = query => request({ url: '/mall/product-inventory/list', method: 'get', params: query })
export const getProductInventoryDetail = spuId => request({ url: '/mall/product-inventory/' + spuId, method: 'get' })
