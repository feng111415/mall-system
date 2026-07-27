import http from './http'

export const sendSmsCode = phone => http.post('/mall/member/sms-code', { phone })
export const loginBySms = payload => http.post('/mall/member/login', payload)
export const getProfile = () => http.get('/mall/member/profile')
export const logoutMember = () => http.post('/mall/member/logout')
