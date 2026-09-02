/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/api/device.ts
 * @Description: 设备查询接口，业主端用于选择报修设备，维修工端用于查看设备状态
 */

import { http } from '../utils/request'
import { DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE } from '../constants/business'
import { compactParams, toQueryString } from '../utils/query'
import type { ApiResponse, HttpOptions } from '../types/auth'
import type { PageResult } from '../types/common'
import type { Device, DeviceQuery } from '../types/device'

export const getDevices = (
  query?: DeviceQuery,
  options?: HttpOptions
): Promise<ApiResponse<PageResult<Device>>> =>
  http.get<PageResult<Device>>(
    '/devices',
    compactParams({
      pageNum: query?.pageNum ?? DEFAULT_PAGE_NUM,
      pageSize: query?.pageSize ?? DEFAULT_PAGE_SIZE,
      categoryId: query?.categoryId,
      buildingId: query?.buildingId,
      status: query?.status
    }),
    options
  )

export const getDevice = (id: number, options?: HttpOptions): Promise<ApiResponse<Device>> =>
  http.get<Device>(`/devices/${id}`, undefined, options)

/** 设备状态变更走 query 参数，与后端 @RequestParam Integer status 对齐 */
export const updateDeviceStatus = (
  id: number,
  status: number,
  options?: HttpOptions
): Promise<ApiResponse<null>> =>
  http.put<null>(`/devices/${id}/status${toQueryString({ status })}`, undefined, options)
