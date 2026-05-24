import request from '@/utils/request'

export function getDevices(params) {
  return request.get('/devices', params)
}

export function getDevice(id) {
  return request.get(`/devices/${id}`)
}

export function createDevice(data) {
  return request.post('/devices', data)
}

export function updateDevice(id, data) {
  return request.put(`/devices/${id}`, data)
}

export function updateDeviceStatus(id, status) {
  return request.put(`/devices/${id}/status`, null, { params: { status } })
}

export function deleteDevice(id) {
  return request.delete(`/devices/${id}`)
}

export function getDeviceCategories() {
  return request.get('/device-categories')
}
