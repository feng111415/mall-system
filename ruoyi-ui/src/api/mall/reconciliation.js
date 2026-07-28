import request from '@/utils/request'

export function listDiff(query) {
  return request({ url: '/mall/reconciliation/diffs', method: 'get', params: query })
}

export function ignoreDiff(diffId, remark) {
  return request({ url: '/mall/reconciliation/diffs/' + diffId + '/ignore', method: 'post', data: remark ? { remark } : {} })
}

export function createCompensation(diffId, remark) {
  return request({ url: '/mall/reconciliation/diffs/' + diffId + '/create-compensation', method: 'post', data: remark ? { remark } : {} })
}

export function listAlerts(query) {
  return request({ url: '/mall/reconciliation/diffs/alerts', method: 'get', params: query })
}

export function acknowledgeAlert(alertId) {
  return request({ url: '/mall/reconciliation/diffs/alerts/' + alertId + '/ack', method: 'post' })
}
