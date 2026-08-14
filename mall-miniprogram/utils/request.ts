const env = require('../config/env')

type RequestMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

interface ApiEnvelope<T> {
  code?: number
  msg?: string
  message?: string
  data?: T
}

interface RequestOptions {
  url: string
  method?: RequestMethod
  data?: WechatMiniprogram.IAnyObject
  header?: Record<string, string>
  timeout?: number
}

interface UploadOptions {
  url: string
  filePath: string
  name?: string
  formData?: WechatMiniprogram.IAnyObject
  timeout?: number
}

class MallRequestError extends Error {
  code?: number | string
  statusCode?: number
  body?: ApiEnvelope<unknown>
}

function normalizeError(response: WechatMiniprogram.RequestSuccessCallbackResult): MallRequestError {
  const body = response.data as ApiEnvelope<unknown> | undefined
  const error = new MallRequestError(body?.msg || body?.message || `请求失败（HTTP ${response.statusCode || 0}）`)
  error.code = body?.code
  error.statusCode = response.statusCode
  error.body = body
  return error
}

function request<T = unknown>(options: RequestOptions): Promise<T> {
  let apiBaseUrl: string
  try {
    apiBaseUrl = env.getApiBaseUrl()
  } catch (error) {
    return Promise.reject(error)
  }
  const token = env.getToken()
  const header: Record<string, string> = Object.assign({
    'content-type': 'application/json',
    'X-Mall-Device-Id': env.getDeviceId()
  }, options.header || {})
  if (token) header['X-Mall-Authorization'] = `Bearer ${token}`

  return new Promise<T>((resolve, reject) => {
    wx.request({
      url: `${apiBaseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header,
      timeout: options.timeout || 10000,
      success(response) {
        const body = response.data as ApiEnvelope<T> | undefined
        if (response.statusCode >= 200 && response.statusCode < 300 && (!body || body.code === undefined || body.code === 200)) {
          resolve(body?.data !== undefined ? body.data : response.data as T)
          return
        }
        if (response.statusCode === 401 || body?.code === 401) env.clearToken()
        reject(normalizeError(response))
      },
      fail(error) {
        const wrapped = new MallRequestError(error.errMsg || '网络连接失败，请稍后重试')
        wrapped.code = 'NETWORK_ERROR'
        reject(wrapped)
      }
    })
  })
}

function upload<T = unknown>(options: UploadOptions): Promise<T> {
  let apiBaseUrl: string
  try {
    apiBaseUrl = env.getApiBaseUrl()
  } catch (error) {
    return Promise.reject(error)
  }
  const token = env.getToken()
  const header: Record<string, string> = {
    'X-Mall-Device-Id': env.getDeviceId()
  }
  if (token) header['X-Mall-Authorization'] = `Bearer ${token}`

  return new Promise<T>((resolve, reject) => {
    wx.uploadFile({
      url: `${apiBaseUrl}${options.url}`,
      filePath: options.filePath,
      name: options.name || 'file',
      formData: options.formData,
      header,
      timeout: options.timeout || 30000,
      success(response) {
        let body: ApiEnvelope<T> | undefined
        try {
          body = JSON.parse(response.data) as ApiEnvelope<T>
        } catch (_) {
          body = undefined
        }
        if (!body) {
          const error = new MallRequestError('上传响应格式异常，请稍后重试')
          error.statusCode = response.statusCode
          reject(error)
          return
        }
        if (response.statusCode >= 200 && response.statusCode < 300 && (body.code === undefined || body.code === 200)) {
          resolve(body.data !== undefined ? body.data : body as T)
          return
        }
        if (response.statusCode === 401 || body?.code === 401) env.clearToken()
        const error = new MallRequestError(body?.msg || body?.message || `上传失败（HTTP ${response.statusCode || 0}）`)
        error.code = body?.code
        error.statusCode = response.statusCode
        error.body = body
        reject(error)
      },
      fail(error) {
        const wrapped = new MallRequestError(error.errMsg || '图片上传失败，请稍后重试')
        wrapped.code = 'NETWORK_ERROR'
        reject(wrapped)
      }
    })
  })
}

module.exports = { request, upload }
