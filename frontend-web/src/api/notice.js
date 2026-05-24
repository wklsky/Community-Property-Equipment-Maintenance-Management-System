import request from '@/utils/request'

export function getNotices(params) {
  return request.get('/notices', params)
}

export function getAllNotices(params) {
  return request.get('/notices/all', params)
}

export function getNotice(id) {
  return request.get(`/notices/${id}`)
}

export function createNotice(data) {
  return request.post('/notices', data)
}

export function updateNotice(id, data) {
  return request.put(`/notices/${id}`, data)
}

export function deleteNotice(id) {
  return request.delete(`/notices/${id}`)
}

export function publishNotice(id) {
  return request.post(`/notices/${id}/publish`)
}

export function scheduleNotice(id, scheduledTime) {
  return request.post(`/notices/${id}/schedule`, null, { params: { scheduledTime } })
}

export function markNoticeRead(id) {
  return request.post(`/notices/${id}/read`)
}
