import { get, post } from '../utils/request'

export const getTenants = () => get('/public/tenants')

export const login = (data) => post('/auth/login', data)

export const sendCode = (data) => post('/auth/send-code', data)

export const loginByCode = (data) => post('/auth/login-by-code', data)

export const resetPassword = (data) => post('/auth/reset-password', data)

export const register = (data) => post('/auth/register', data)
