import { get, post } from '../utils/request'

export const getNotices = (params) => get('/notices', params)

export const getNotice = (id) => get(`/notices/${id}`)

export const markNoticeRead = (id) => post(`/notices/${id}/read`)
