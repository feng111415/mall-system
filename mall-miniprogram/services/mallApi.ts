type RequestOptions = {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, unknown>
}

type MallRequest = <T = unknown>(options: RequestOptions) => Promise<T>

const request = require('../utils/request').request as MallRequest
const env = require('../config/env')

interface SmsCodeResponse {
  challengeRequired?: boolean
  expiresIn?: number
  resendAfter?: number
}

interface SmsChallenge {
  challengeId: string
  pieceX: number
  positionScale: number
  sceneImage: string
  pieceImage: string
  expiresIn: number
}

interface SmsChallengeTicket {
  ticket: string
  expiresIn: number
}

interface LoginResponse {
  token: string
  expiresIn: number
  newMember: boolean
  member: MallMemberProfile
}

interface MallHomeResponse {
  hero?: Record<string, unknown>
  tickerItems?: Array<Record<string, unknown>>
  recommendation?: Record<string, unknown>
  recommendationSpuIds?: number[]
}

interface MallProductResponse {
  spuId: number
  productName: string
  subtitle?: string
  mainImage?: string
  priceMin?: number
  priceMax?: number
  salesCount?: number
}

const mallApi = {
  getHome() {
    return request<MallHomeResponse>({ url: '/api/mall/homepage' })
  },
  getCatalog(params?: Record<string, string | number>) {
    return request<MallProductResponse[]>({ url: '/api/mall/catalog/products', data: params })
  },
  getCategories() {
    return request<Array<Record<string, unknown>>>({ url: '/api/mall/catalog/categories' })
  },
  getProduct(spuId: number) {
    return request<Record<string, unknown> | null>({ url: `/api/mall/catalog/products/${spuId}` })
  },
  getProductReviews(spuId: number, params?: Record<string, string | number>) {
    return request<Record<string, unknown>>({ url: `/api/mall/reviews/products/${spuId}`, data: params })
  },
  getCart() {
    return request<Record<string, any>>({ url: '/api/mall/cart' })
  },
  addCartItem(skuId: number, quantity = 1) {
    return request<Record<string, any>>({
      url: '/api/mall/cart/items',
      method: 'POST',
      data: { skuId, quantity }
    })
  },
  updateCartQuantity(skuId: number, quantity: number) {
    return request<Record<string, any>>({
      url: `/api/mall/cart/items/${skuId}`,
      method: 'PUT',
      data: { quantity }
    })
  },
  updateCartSelected(skuId: number, selected: boolean) {
    return request<Record<string, any>>({
      url: `/api/mall/cart/items/${skuId}/selected`,
      method: 'PUT',
      data: { selected }
    })
  },
  removeCartItem(skuId: number) {
    return request<Record<string, any>>({
      url: `/api/mall/cart/items/${skuId}`,
      method: 'DELETE'
    })
  },
  getCheckoutPreview(memberCouponId?: number) {
    return request<Record<string, any>>({
      url: '/api/mall/checkout/preview',
      data: memberCouponId ? { memberCouponId } : undefined
    })
  },
  createOrder(data: { idempotencyKey: string; addressId: number; memberCouponId?: number; remark?: string }) {
    return request<Record<string, any>>({
      url: '/api/mall/orders',
      method: 'POST',
      data
    })
  },
  addAddress(data: Record<string, unknown>) {
    return request<Record<string, any>>({
      url: '/api/mall/member/addresses',
      method: 'POST',
      data
    })
  },
  getAddresses() {
    return request<Array<Record<string, any>>>({ url: '/api/mall/member/addresses' })
  },
  updateAddress(addressId: number, data: Record<string, unknown>) {
    return request<Record<string, any>>({
      url: `/api/mall/member/addresses/${addressId}`,
      method: 'PUT',
      data
    })
  },
  deleteAddress(addressId: number) {
    return request<Record<string, any>>({
      url: `/api/mall/member/addresses/${addressId}`,
      method: 'DELETE'
    })
  },
  getMessages(params?: Record<string, string | number>) {
    return request({ url: '/api/mall/member/messages', data: params })
  },
  getMessageSummary() {
    return request<Array<Record<string, any>>>({ url: '/api/mall/member/messages/summary' })
  },
  getMessage(messageId: number) {
    return request<Record<string, any>>({ url: `/api/mall/member/messages/${messageId}` })
  },
  markMessageRead(messageId: number) {
    return request<Record<string, any>>({ url: `/api/mall/member/messages/${messageId}/read`, method: 'POST' })
  },
  markAllMessagesRead(category?: string) {
    return request<number>({
      url: `/api/mall/member/messages/read-all${category ? `?category=${encodeURIComponent(category)}` : ''}`,
      method: 'POST'
    })
  },
  getCoupons() {
    return request<Array<Record<string, any>>>({ url: '/api/mall/coupons' })
  },
  getClaimableCoupons() {
    return request<Array<Record<string, any>>>({ url: '/api/mall/coupons/claimable' })
  },
  claimCoupon(couponId: number) {
    return request<Record<string, any>>({ url: `/api/mall/coupons/${couponId}/claim`, method: 'POST' })
  },
  getProfile() {
    return request<MallMemberProfile>({ url: '/api/mall/member/profile' })
  },
  getOrders(params?: Record<string, string | number>) {
    return request<Array<Record<string, unknown>>>({ url: '/api/mall/orders', data: params })
  },
  getOrder(orderId: number) {
    return request<Record<string, any>>({ url: `/api/mall/orders/${orderId}` })
  },
  cancelOrder(orderId: number, reason?: string) {
    return request<Record<string, any>>({ url: `/api/mall/orders/${orderId}/cancel`, method: 'POST', data: { reason } })
  },
  getOrderPayments(orderId: number) {
    return request<Array<Record<string, any>>>({ url: `/api/mall/orders/${orderId}/payments` })
  },
  createPayment(orderId: number, idempotencyKey: string) {
    return request<Record<string, any>>({ url: `/api/mall/orders/${orderId}/payment`, method: 'POST', data: { idempotencyKey } })
  },
  mockPaymentSuccess(paymentNo: string) {
    return request<Record<string, any>>({ url: `/api/mall/payments/${paymentNo}/mock-success`, method: 'POST' })
  },
  getOrderLogistics(orderId: number) {
    return request<Record<string, any>>({ url: `/api/mall/orders/${orderId}/logistics` })
  },
  confirmReceipt(orderId: number) {
    return request<Record<string, any>>({ url: `/api/mall/orders/${orderId}/confirm-receipt`, method: 'POST' })
  },
  getAfterSales() {
    return request<Array<Record<string, any>>>({ url: '/api/mall/after-sales' })
  },
  getAfterSale(afterSaleId: number) {
    return request<Record<string, any>>({ url: `/api/mall/after-sales/${afterSaleId}` })
  },
  applyAfterSale(orderId: number, data: Record<string, unknown>) {
    return request<Record<string, any>>({ url: `/api/mall/orders/${orderId}/after-sales`, method: 'POST', data })
  },
  submitReturnTracking(afterSaleId: number, companyCode: string, trackingNo: string) {
    return request<Record<string, any>>({
      url: `/api/mall/after-sales/${afterSaleId}/return-tracking`,
      method: 'POST',
      data: { companyCode, trackingNo }
    })
  },
  getProductActivitySummary() {
    return request<Record<string, number>>({ url: '/api/mall/member/product-activity/summary' })
  },
  sendSmsCode(phone: string, challengeTicket = '') {
    return request<SmsCodeResponse>({
      url: '/api/mall/member/sms-code',
      method: 'POST',
      data: { phone, challengeTicket }
    })
  },
  createSmsChallenge(phone: string) {
    return request<SmsChallenge>({ url: '/api/mall/member/sms-challenge', method: 'POST', data: { phone } })
  },
  verifySmsChallenge(challengeId: string, position: number) {
    return request<SmsChallengeTicket>({
      url: '/api/mall/member/sms-challenge/verify',
      method: 'POST',
      data: { challengeId, position }
    })
  },
  loginBySms(phone: string, code: string) {
    return request<LoginResponse>({
      url: '/api/mall/member/login',
      method: 'POST',
      data: {
        phone,
        code,
        agreed: true,
        userAgreementVersion: '1.0',
        privacyPolicyVersion: '1.0',
        deviceId: env.getDeviceId()
      }
    })
  },
  logout() {
    return request({ url: '/api/mall/member/logout', method: 'POST' })
  }
}

module.exports = mallApi
