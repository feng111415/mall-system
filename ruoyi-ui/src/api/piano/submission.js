import request from '@/utils/request'

// 查询钢琴作业提交点评列表
export function listSubmission(query) {
  return request({
    url: '/piano/submission/list',
    method: 'get',
    params: query
  })
}

// 查询钢琴作业提交点评详细
export function getSubmission(submissionId) {
  return request({
    url: '/piano/submission/' + submissionId,
    method: 'get'
  })
}

// 查询某份作业的提交历史
export function listSubmissionHistory(homeworkId) {
  return request({
    url: '/piano/submission/history/' + homeworkId,
    method: 'get'
  })
}

// 新增提交或订正记录
export function addSubmission(data) {
  return request({
    url: '/piano/submission',
    method: 'post',
    data
  })
}

// 修改提交记录
export function updateSubmission(data) {
  return request({
    url: '/piano/submission',
    method: 'put',
    data
  })
}

// 点评提交记录
export function reviewSubmission(data) {
  return request({
    url: '/piano/submission/review',
    method: 'put',
    data
  })
}

// 删除提交记录
export function delSubmission(submissionId) {
  return request({
    url: '/piano/submission/' + submissionId,
    method: 'delete'
  })
}
