import { get } from '../utils/request'

export const getDevices = (params) => get('/devices', params)

export const getDevice = (id) => get(`/devices/${id}`)

export const getDeviceCategories = () => get('/device-categories')
