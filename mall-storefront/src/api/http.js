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
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export default http
