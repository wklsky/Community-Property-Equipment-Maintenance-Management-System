import request from '@/utils/request'

export function getRepairOrders(params) {
  return request.get('/repair-orders', params)
}

export function getMyOrders(params) {
  return request.get('/repair-orders/my', params)
}

export function getAssignedOrders(params) {
  return request.get('/repair-orders/assigned', params)
}

export function getRepairOrder(id) {
  return request.get(`/repair-orders/${id}`)
}

export function createRepairOrder(data) {
  return request.post('/repair-orders', data)
}

export function assignOrder(id, workerId) {
  return request.post(`/repair-orders/${id}/assign`, null, { params: { workerId } })
}

export function acceptOrder(id) {
  return request.post(`/repair-orders/${id}/accept`)
}

export function completeOrder(id, processDesc) {
  return request.post(`/repair-orders/${id}/complete`, null, { params: { processDesc } })
}

export function evaluateOrder(id, rating, comment) {
  return request.post(`/repair-orders/${id}/evaluate`, null, { params: { rating, comment } })
}

export function cancelOrder(id) {
  return request.post(`/repair-orders/${id}/cancel`)
}

export function approveOrder(id) {
  return request.post(`/repair-orders/${id}/approve`)
}

export function rejectOrder(id, reason) {
  return request.post(`/repair-orders/${id}/reject`, null, { params: { reason } })
}

export function transferOrder(id, workerId, reason) {
  return request.post(`/repair-orders/${id}/transfer`, null, { params: { workerId, reason } })
}

export function exportSingleOrder(id) {
  return request.download(`/repair-orders/${id}/export`, {}, `工单_${id}.xlsx`)
}

export function exportOrderList(params) {
  return request.download('/repair-orders/export', params, '工单列表.xlsx')
}

export function exportOrderStatistics() {
  return request.download('/repair-orders/export-statistics', {}, '工单统计报表.xlsx')
}
