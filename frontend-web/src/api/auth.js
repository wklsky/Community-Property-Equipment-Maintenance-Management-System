import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function sendCode(data) {
  return request.post('/auth/send-code', data)
}

export function loginByCode(data) {
  return request.post('/auth/login-by-code', data)
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}

export function getTenants() {
  return request.get('/public/tenants')
}
