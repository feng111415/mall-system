import request from '@/utils/request'

export function listHomeContent (query) {
  return request({ url: '/mall/content/homepage', method: 'get', params: query })
}

export function getHomeContent (id) {
  return request({ url: `/mall/content/homepage/${id}`, method: 'get' })
}

export function addHomeContent (data) {
  return request({ url: '/mall/content/homepage', method: 'post', data })
}

export function updateHomeContent (data) {
  return request({ url: '/mall/content/homepage', method: 'put', data })
}

export function disableHomeContent (id) {
  return request({ url: `/mall/content/homepage/${id}/disable`, method: 'put' })
}
