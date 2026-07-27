import axios from 'axios'

/**
 * 管理端 HTTP 客户端。
 *
 * 后台接口地址通过 VITE_API_BASE_URL 注入，避免把环境地址写入业务页面。
 */
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10_000
})

http.interceptors.request.use(config => {
  const token = sessionStorage.getItem('mall-admin-token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(response => response, error => {
  if (error.response?.status === 401 || error.response?.data?.code === 401) {
    sessionStorage.removeItem('mall-admin-token')
    if (window.location.pathname !== '/login') window.location.assign('/login')
  }
  return Promise.reject(error)
})

export default http
