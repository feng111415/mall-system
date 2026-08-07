import request from '@/utils/request'
export const listMembers = query => request({ url: '/mall/member/list', method: 'get', params: query })
export const getMember = id => request({ url: '/mall/member/' + id, method: 'get' })
export const getMemberAccountLifecycle = id => request({ url: '/mall/member/' + id + '/account-lifecycle', method: 'get' })
export const updateMember = data => request({ url: '/mall/member', method: 'put', data })
export const delMember = ids => request({ url: '/mall/member/' + ids, method: 'delete' })
