import request from '@/utils/request'

export function listTicket(module, query) {
  return request({
    url: `/ticket/${module}/list`,
    method: 'get',
    params: query
  })
}

export function getTicket(module, id) {
  return request({
    url: `/ticket/${module}/${id}`,
    method: 'get'
  })
}

export function addTicket(module, data) {
  return request({
    url: `/ticket/${module}`,
    method: 'post',
    data
  })
}

export function updateTicket(module, data) {
  return request({
    url: `/ticket/${module}`,
    method: 'put',
    data
  })
}

export function delTicket(module, id) {
  return request({
    url: `/ticket/${module}/${id}`,
    method: 'delete'
  })
}

export function rushOrder(data) {
  return request({
    url: '/ticket/rush/order',
    method: 'post',
    data
  })
}
