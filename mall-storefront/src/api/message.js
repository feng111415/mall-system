import http from './http'

export const getMessageSummary = () => http.get('/mall/member/messages/summary')
export const getMessages = category => http.get('/mall/member/messages', { params: category ? { category } : {} })
export const getMessageDetail = messageId => http.get(`/mall/member/messages/${messageId}`)
export const markMessageRead = messageId => http.post(`/mall/member/messages/${messageId}/read`)
export const markAllMessagesRead = category => http.post('/mall/member/messages/read-all', null, { params: category ? { category } : {} })
