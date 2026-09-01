import { get, post } from '../utils/request'

export const getMyInspectionTasks = (params) => get('/inspections/tasks/my', params)

export const getInspectionTask = (id) => get(`/inspections/tasks/${id}`)

export const acceptInspectionTask = (id) => post(`/inspections/tasks/${id}/accept`)

export const completeInspectionTask = (id, data) => post(`/inspections/tasks/${id}/complete`, data)

export const getTaskRecords = (id, params) => get(`/inspections/tasks/${id}/records`, params)

export const getInspectionPlans = (params) => get('/inspections/plans', params)

export const getInspectionPlan = (id) => get(`/inspections/plans/${id}`)
