import request from '@/utils/request'

export function listCategory(query) {
  return request({
    url: '/knowledge/category/list',
    method: 'get',
    params: query
  })
}

export function getCategory(categoryId) {
  return request({
    url: '/knowledge/category/' + categoryId,
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/knowledge/category',
    method: 'post',
    data
  })
}

export function updateCategory(data) {
  return request({
    url: '/knowledge/category',
    method: 'put',
    data
  })
}

export function delCategory(categoryIds) {
  return request({
    url: '/knowledge/category/' + categoryIds,
    method: 'delete'
  })
}