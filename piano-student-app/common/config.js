export const BASE_URL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

export function assertBaseUrl() {
  if (!BASE_URL) {
    throw new Error('请在小程序构建环境中配置 VUE_APP_API_BASE_URL')
  }
}
