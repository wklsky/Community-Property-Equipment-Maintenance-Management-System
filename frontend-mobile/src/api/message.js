import { get, post } from '../utils/request'

export const getMessages = (params) => get('/messages', params)

export const markMessageRead = (id) => post(`/messages/${id}/read`)

export const getUnreadCount = () => get('/messages/unread-count')
