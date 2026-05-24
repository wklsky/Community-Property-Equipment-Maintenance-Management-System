import request from '@/utils/request'

export function getTenants(params) {
  return request.get('/admin/tenants', params)
}

export function getTenant(id) {
  return request.get(`/admin/tenants/${id}`)
}

export function createTenant(data) {
  return request.post('/admin/tenants', data)
}

export function updateTenant(id, data) {
  return request.put(`/admin/tenants/${id}`, data)
}

export function updateTenantStatus(id, status) {
  return request.put(`/admin/tenants/${id}/status`, null, { params: { status } })
}

export function getCrossTenantUsers(params) {
  return request.get('/admin/users', params)
}

export function getCrossTenantRoles(tenantId) {
  return request.get('/admin/roles', { tenantId })
}
