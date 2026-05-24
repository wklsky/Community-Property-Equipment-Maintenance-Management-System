import request from '@/utils/request'

export function getInspectionPlans(params) {
  return request.get('/inspections/plans', params)
}

export function getInspectionPlan(id) {
  return request.get(`/inspections/plans/${id}`)
}

export function createInspectionPlan(data) {
  return request.post('/inspections/plans', data)
}

export function updateInspectionPlan(id, data) {
  return request.put(`/inspections/plans/${id}`, data)
}

export function deleteInspectionPlan(id) {
  return request.delete(`/inspections/plans/${id}`)
}

export function publishInspectionPlan(id) {
  return request.post(`/inspections/plans/${id}/publish`)
}

export function pauseInspectionPlan(id) {
  return request.post(`/inspections/plans/${id}/pause`)
}

export function resumeInspectionPlan(id) {
  return request.post(`/inspections/plans/${id}/resume`)
}

export function getInspectionTasks(params) {
  return request.get('/inspections/tasks', params)
}

export function getMyInspectionTasks(params) {
  return request.get('/inspections/tasks/my', params)
}

export function acceptInspectionTask(id) {
  return request.post(`/inspections/tasks/${id}/accept`)
}

export function getInspectionTask(id) {
  return request.get(`/inspections/tasks/${id}`)
}

export function completeInspectionTask(id, data) {
  return request.post(`/inspections/tasks/${id}/complete`, data)
}

export function getTaskRecords(id, params) {
  return request.get(`/inspections/tasks/${id}/records`, params)
}
