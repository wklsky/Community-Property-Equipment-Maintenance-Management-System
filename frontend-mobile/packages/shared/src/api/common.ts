/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/api/common.ts
 * @Description: 公共基础数据接口，为报修表单与筛选器提供下拉数据源
 */

import { http } from '../utils/request'
import { compactParams } from '../utils/query'
import type { ApiResponse, HttpOptions } from '../types/auth'
import type { DeviceCategory } from '../types/device'
import type { Building, Community, DictItem, Room, WorkerOption } from '../types/property'

export const getCommunities = (options?: HttpOptions): Promise<ApiResponse<Community[]>> =>
  http.get<Community[]>('/communities', undefined, options)

export const getBuildings = (
  communityId?: number,
  options?: HttpOptions
): Promise<ApiResponse<Building[]>> =>
  http.get<Building[]>('/buildings', compactParams({ communityId }), options)

export const getRooms = (
  buildingId?: number,
  options?: HttpOptions
): Promise<ApiResponse<Room[]>> => http.get<Room[]>('/rooms', compactParams({ buildingId }), options)

export const getDeviceCategories = (options?: HttpOptions): Promise<ApiResponse<DeviceCategory[]>> =>
  http.get<DeviceCategory[]>('/device-categories', undefined, options)

/** 维修工名单，转单时用于选择接手人 */
export const getWorkers = (options?: HttpOptions): Promise<ApiResponse<WorkerOption[]>> =>
  http.get<WorkerOption[]>('/workers', undefined, options)

export const getDicts = (
  dictType: string,
  options?: HttpOptions
): Promise<ApiResponse<DictItem[]>> => http.get<DictItem[]>('/dicts', { dictType }, options)
