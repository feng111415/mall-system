import http from './http'

export const getCategories = () => http.get('/mall/catalog/categories')
export const getProducts = params => http.get('/mall/catalog/products', { params })
export const getProduct = id => http.get(`/mall/catalog/products/${id}`)
// http 已通过 baseURL=/api 代理到后端，这里只保留业务路径，避免请求成 /api/api/...
export const getHomepage = () => http.get('/mall/homepage')
