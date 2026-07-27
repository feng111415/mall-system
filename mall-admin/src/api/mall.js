import http from './http'

export const getMembers = params => http.get('/mall/member/list', { params })
export const updateMember = data => http.put('/mall/member', data)
export const getAdminProducts = params => http.get('/mall/catalog/products', { params })
export const getAdminProduct = id => http.get(`/mall/catalog/products/${id}`)
export const saveProduct = data => http.request({ url: '/mall/catalog/products', method: data.spuId ? 'put' : 'post', data })
export const publishProduct = (id, status) => http.put(`/mall/catalog/products/${id}/publish/${status}`)
export const getAdminCategories = () => http.get('/mall/catalog/categories')
export const saveCategory = data => http.request({ url: '/mall/catalog/categories', method: data.categoryId ? 'put' : 'post', data })
export const getAdminBrands = () => http.get('/mall/catalog/brands')
export const saveBrand = data => http.request({ url: '/mall/catalog/brands', method: data.brandId ? 'put' : 'post', data })
