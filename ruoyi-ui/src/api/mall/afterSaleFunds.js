import request from '@/utils/request'

export const getAfterSaleFundsCenter = query => request({
  url: '/mall/after-sale-funds/center', method: 'get', params: query
})

export const retryCompensationTask = taskId => request({
  url: '/mall/compensation/tasks/' + taskId + '/retry', method: 'post'
})

export const runCompensationTasks = () => request({
  url: '/mall/compensation/tasks/run', method: 'post'
})
