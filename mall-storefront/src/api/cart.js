import http from './http'

export const getCart = () => http.get('/mall/cart')
export const addCartItem = (skuId, quantity = 1) => http.post('/mall/cart/items', { skuId, quantity })
export const updateCartQuantity = (skuId, quantity) => http.put(`/mall/cart/items/${skuId}`, { quantity })
export const updateCartSelected = (skuId, selected) => http.put(`/mall/cart/items/${skuId}/selected`, { selected })
export const removeCartItem = skuId => http.delete(`/mall/cart/items/${skuId}`)
