import request from '@/utils/request'

// 查询钢琴课后作业列表
export function listHomework(query) {
  return request({
    url: '/piano/homework/list',
    method: 'get',
    params: query
  })
}

// 查询钢琴课后作业详细
export function getHomework(homeworkId) {
  return request({
    url: '/piano/homework/' + homeworkId,
    method: 'get'
  })
}

// 新增钢琴课后作业
export function addHomework(data) {
  return request({
    url: '/piano/homework',
    method: 'post',
    data
  })
}

// 修改钢琴课后作业
export function updateHomework(data) {
  return request({
    url: '/piano/homework',
    method: 'put',
    data
  })
}

// 删除钢琴课后作业
export function delHomework(homeworkId) {
  return request({
    url: '/piano/homework/' + homeworkId,
    method: 'delete'
  })
}
