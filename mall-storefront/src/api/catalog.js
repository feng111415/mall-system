import http from './http'

export const getCategories = () => http.get('/mall/catalog/categories')
export const getProducts = params => http.get('/mall/catalog/products', { params })
export const getProduct = id => http.get(`/mall/catalog/products/${id}`)
