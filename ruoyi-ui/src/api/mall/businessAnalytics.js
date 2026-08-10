import request from '@/utils/request'

export const getBusinessAnalytics = days => request({
  url: '/mall/business-analytics',
  method: 'get',
  params: { days }
})
