/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/api/address.ts
 * @Description: 报修地址接口，合并系统房产与自定义地址两类数据源
 */

import { http } from '../utils/request'
import type { ApiResponse, HttpOptions } from '../types/auth'
import type { MyAddress, UserAddress, UserAddressPayload } from '../types/address'

/** 地址聚合列表，type 字段决定后续操作走哪套路由 */
export const getMyAddresses = (options?: HttpOptions): Promise<ApiResponse<MyAddress[]>> =>
  http.get<MyAddress[]>('/my-addresses', undefined, options)

/**
 * 设置默认地址。
 * 房产与自定义地址分属两套后端路由（/my-properties 与 /user-addresses），
 * 且两者的 id 自增序列相互独立，按 type 分发是唯一可靠的方式，用错会 404
 */
export const setDefaultAddress = (
  type: MyAddress['type'],
  id: number,
  options?: HttpOptions
): Promise<ApiResponse<null>> =>
  type === 'property'
    ? http.put<null>(`/my-properties/${id}/default`, undefined, options)
    : http.put<null>(`/user-addresses/${id}/default`, undefined, options)

export const addAddress = (
  data: UserAddressPayload,
  options?: HttpOptions
): Promise<ApiResponse<UserAddress>> => http.post<UserAddress>('/user-addresses', data, options)

export const updateAddress = (
  id: number,
  data: UserAddressPayload,
  options?: HttpOptions
): Promise<ApiResponse<null>> => http.put<null>(`/user-addresses/${id}`, data, options)

export const deleteAddress = (id: number, options?: HttpOptions): Promise<ApiResponse<null>> =>
  http.del<null>(`/user-addresses/${id}`, undefined, options)
