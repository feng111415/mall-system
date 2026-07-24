import request from '@/utils/request'

// 查询钢琴作业今日工作台
export function getPianoDashboard() {
  return request({
    url: '/piano/homework/dashboard',
    method: 'get'
  })
}
