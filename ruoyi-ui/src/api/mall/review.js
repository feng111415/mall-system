import request from '@/utils/request'

export function listProductReviews (params) { return request({ url: '/mall/reviews', method: 'get', params }) }
export function getProductReview (id) { return request({ url: `/mall/reviews/${id}`, method: 'get' }) }
export function auditProductReview (id, data) { return request({ url: `/mall/reviews/${id}/audit`, method: 'post', data }) }
