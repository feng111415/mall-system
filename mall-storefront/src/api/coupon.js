import http from './http'

export const getMemberCoupons = () => http.get('/mall/coupons')
export const getClaimableCoupons = () => http.get('/mall/coupons/claimable')
export const claimCoupon = couponId => http.post(`/mall/coupons/${couponId}/claim`)
