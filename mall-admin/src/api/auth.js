import http from './http'

export const getCaptcha = () => http.get('/captchaImage')
export const loginAdmin = data => http.post('/login', data)
export const getAdminInfo = () => http.get('/getInfo')
