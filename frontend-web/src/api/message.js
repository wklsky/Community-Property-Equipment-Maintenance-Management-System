import request from '@/utils/request'

export function getMessages(params) {
  return request.get('/messages', params)
}

export function markMessageRead(id) {
  return request.post(`/messages/${id}/read`)
}

export function getUnreadCount() {
  return request.get('/messages/unread-count')
}
