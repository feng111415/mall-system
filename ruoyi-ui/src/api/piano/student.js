import request from '@/utils/request'

export function listStudent(query) {
  return request({
    url: '/system/testStudent/list',
    method: 'get',
    params: query
  })
}

export function getStudent(studentId) {
  return request({
    url: '/system/testStudent/' + studentId,
    method: 'get'
  })
}

export function addStudent(data) {
  return request({
    url: '/system/testStudent',
    method: 'post',
    data
  })
}

export function updateStudent(data) {
  return request({
    url: '/system/testStudent',
    method: 'put',
    data
  })
}

export function delStudent(studentId) {
  return request({
    url: '/system/testStudent/' + studentId,
    method: 'delete'
  })
}
