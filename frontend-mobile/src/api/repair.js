import { get, post } from '../utils/request'

export const getMyOrders = (params) => {
  const p = { ...params }
  if (Array.isArray(p.statuses)) {
    p.statuses = p.statuses.join(',')
  }
  return get('/repair-orders/my', p)
}

export const getAssignedOrders = (params) => {
  const p = { ...params }
  if (Array.isArray(p.statuses)) {
    p.statuses = p.statuses.join(',')
  }
  return get('/repair-orders/assigned', p)
}

export const getRepairOrder = (id) => get(`/repair-orders/${id}`)

export const createRepairOrder = (data) => post('/repair-orders', data)

export const acceptOrder = (id) => post(`/repair-orders/${id}/accept`)

export const completeOrder = (id, processDesc) => post(`/repair-orders/${id}/complete?processDesc=${encodeURIComponent(processDesc)}`)

export const evaluateOrder = (id, rating, comment) => post(`/repair-orders/${id}/evaluate?rating=${rating}&comment=${encodeURIComponent(comment || '')}`)

export const cancelOrder = (id) => post(`/repair-orders/${id}/cancel`)

export const approveOrder = (id) => post(`/repair-orders/${id}/approve`)

export const rejectOrder = (id, reason) => post(`/repair-orders/${id}/reject?reason=${encodeURIComponent(reason || '')}`)

export const transferOrder = (id, workerId, reason) => post(`/repair-orders/${id}/transfer?workerId=${workerId}&reason=${encodeURIComponent(reason || '')}`)
