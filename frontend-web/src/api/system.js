import request from '@/utils/request'

export function getUsers(params) {
  return request.get('/system/users', params)
}

export function getUser(id) {
  return request.get(`/system/users/${id}`)
}

export function createUser(data) {
  return request.post('/system/users', data)
}

export function updateUser(id, data) {
  return request.put(`/system/users/${id}`, data)
}

export function updateUserStatus(id, status) {
  return request.put(`/system/users/${id}/status`, null, { params: { status } })
}

export function getRoles() {
  return request.get('/system/roles')
}
