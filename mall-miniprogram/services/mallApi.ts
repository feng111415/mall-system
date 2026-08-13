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

const mallApi = {
  getHome() {
    return request({ url: '/api/mall/homepage' })
  },
  getCatalog(params?: Record<string, string | number>) {
    return request({ url: '/api/mall/catalog/products', data: params })
  },
  getCart() {
    return request({ url: '/api/mall/cart' })
  },
  getMessages(params?: Record<string, string | number>) {
    return request({ url: '/api/mall/member/messages', data: params })
  },
  getProfile() {
    return request<MallMemberProfile>({ url: '/api/mall/member/profile' })
  },
  getOrders(params?: Record<string, string | number>) {
    return request({ url: '/api/mall/orders', data: params })
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
