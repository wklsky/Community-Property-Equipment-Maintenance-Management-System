import { get, post, put, del } from '../utils/request'

export const getMyAddresses = () => get('/my-addresses')

export const setDefaultAddress = (type, id) => {
  if (type === 'property') {
    return put(`/my-properties/${id}/default`)
  }
  return put(`/user-addresses/${id}/default`)
}

export const addAddress = (data) => post('/user-addresses', data)

export const updateAddress = (id, data) => put(`/user-addresses/${id}`, data)

export const deleteAddress = (id) => del(`/user-addresses/${id}`)
