import axios from 'axios'

/**
 * 用户端 HTTP 客户端。
 *
 * API 地址通过 VITE_API_BASE_URL 注入，业务页面不直接写死环境地址。
 */
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10_000
})

http.interceptors.request.use(config => {
  const token = sessionStorage.getItem('mall-user-token')
  if (token) config.headers['X-Mall-Authorization'] = `Bearer ${token}`
  return config
})

http.interceptors.response.use(response => {
  const body = response.data
  if (body && typeof body.code === 'number' && body.code !== 200) {
    if (body.code === 401) sessionStorage.removeItem('mall-user-token')
    const error = new Error(body.msg || '请求失败')
    error.response = response
    return Promise.reject(error)
  }
  return response
}, error => Promise.reject(error))

export default http
