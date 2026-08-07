import request from '@/utils/request'

export function listCoupon (params) { return request({ url: '/mall/coupons', method: 'get', params }) }
export function getCoupon (id) { return request({ url: `/mall/coupons/${id}`, method: 'get' }) }
export function addCoupon (data) { return request({ url: '/mall/coupons', method: 'post', data }) }
export function updateCoupon (data) { return request({ url: '/mall/coupons', method: 'put', data }) }
export function changeCouponStatus (id, status) { return request({ url: `/mall/coupons/${id}/status`, method: 'post', params: { status } }) }
export function issueCoupon (id, memberId) { return request({ url: `/mall/coupons/${id}/issue`, method: 'post', params: { memberId } }) }
