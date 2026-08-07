import http from './http'

export const getProductReviews = (spuId, params) => http.get(`/mall/reviews/products/${spuId}`, { params })
export const getOrderReviewItems = orderId => http.get(`/mall/reviews/orders/${orderId}/items`)
export const submitReview = data => http.post('/mall/reviews', data)
export const uploadReviewImage = file => {
  const body = new FormData()
  body.append('file', file)
  return http.post('/mall/reviews/images', body, { headers: { 'Content-Type': 'multipart/form-data' } })
}
