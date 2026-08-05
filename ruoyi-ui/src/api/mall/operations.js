import request from '@/utils/request'

export const getOperationsDashboard = () => request({
  url: '/mall/operations/dashboard', method: 'get'
})
