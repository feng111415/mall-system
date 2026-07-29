import http from './http'

export const sendSmsCode = phone => http.post('/mall/member/sms-code', { phone })
export const loginBySms = payload => http.post('/mall/member/login', payload)
export const getProfile = () => http.get('/mall/member/profile')
export const logoutMember = () => http.post('/mall/member/logout')
export const getAddresses = () => http.get('/mall/member/addresses')
export const addAddress = payload => http.post('/mall/member/addresses', payload)
export const updateAddress = (addressId, payload) => http.put(`/mall/member/addresses/${addressId}`, payload)
export const deleteAddress = addressId => http.delete(`/mall/member/addresses/${addressId}`)
