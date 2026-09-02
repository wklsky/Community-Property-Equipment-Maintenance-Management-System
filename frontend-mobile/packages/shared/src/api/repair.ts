/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/api/repair.ts
 * @Description: 报修工单相关接口，业主端与维修工端共用同一份定义
 */

import { http } from '../utils/request'
import { DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE } from '../constants/business'
import { compactParams, joinIds, toQueryString } from '../utils/query'
import type { ApiResponse, HttpOptions } from '../types/auth'
import type { PageResult } from '../types/common'
import type {
  CompleteOrderPayload,
  EvaluateOrderPayload,
  RepairOrder,
  RepairOrderCreatePayload,
  RepairOrderDetail,
  RepairOrderQuery,
  TransferOrderPayload
} from '../types/repair'

const buildPageParams = (query?: RepairOrderQuery): Record<string, unknown> =>
  compactParams({
    pageNum: query?.pageNum ?? DEFAULT_PAGE_NUM,
    pageSize: query?.pageSize ?? DEFAULT_PAGE_SIZE,
    statuses: joinIds(query?.statuses)
  })

/** 我提交的工单（业主视角） */
export const getMyOrders = (
  query?: RepairOrderQuery,
  options?: HttpOptions
): Promise<ApiResponse<PageResult<RepairOrder>>> =>
  http.get<PageResult<RepairOrder>>('/repair-orders/my', buildPageParams(query), options)

/** 派给我的工单（维修工视角） */
export const getAssignedOrders = (
  query?: RepairOrderQuery,
  options?: HttpOptions
): Promise<ApiResponse<PageResult<RepairOrder>>> =>
  http.get<PageResult<RepairOrder>>('/repair-orders/assigned', buildPageParams(query), options)

export const getRepairOrder = (
  id: number,
  options?: HttpOptions
): Promise<ApiResponse<RepairOrderDetail>> =>
  http.get<RepairOrderDetail>(`/repair-orders/${id}`, undefined, options)

/** 提交报修，后端返回创建后的工单实体（含 orderNo 与初始状态 0） */
export const createRepairOrder = (
  data: RepairOrderCreatePayload,
  options?: HttpOptions
): Promise<ApiResponse<RepairOrder>> => http.post<RepairOrder>('/repair-orders', data, options)

export const acceptOrder = (id: number, options?: HttpOptions): Promise<ApiResponse<null>> =>
  http.post<null>(`/repair-orders/${id}/accept`, undefined, options)

/**
 * 完成工单。
 * processDesc 必须走 query：后端标注为 @RequestParam，
 * 而 uni.request 的 data 会被放进请求体，拼在 URL 上才是后端期望的位置
 */
export const completeOrder = (
  id: number,
  payload: CompleteOrderPayload,
  options?: HttpOptions
): Promise<ApiResponse<null>> =>
  http.post<null>(
    `/repair-orders/${id}/complete${toQueryString({ processDesc: payload.processDesc })}`,
    undefined,
    options
  )

export const evaluateOrder = (
  id: number,
  payload: EvaluateOrderPayload,
  options?: HttpOptions
): Promise<ApiResponse<null>> =>
  http.post<null>(
    `/repair-orders/${id}/evaluate${toQueryString({
      rating: payload.rating,
      comment: payload.comment ?? ''
    })}`,
    undefined,
    options
  )

export const cancelOrder = (id: number, options?: HttpOptions): Promise<ApiResponse<null>> =>
  http.post<null>(`/repair-orders/${id}/cancel`, undefined, options)

export const approveOrder = (id: number, options?: HttpOptions): Promise<ApiResponse<null>> =>
  http.post<null>(`/repair-orders/${id}/approve`, undefined, options)

export const rejectOrder = (
  id: number,
  reason: string,
  options?: HttpOptions
): Promise<ApiResponse<null>> =>
  http.post<null>(
    `/repair-orders/${id}/reject${toQueryString({ reason })}`,
    undefined,
    options
  )

export const transferOrder = (
  id: number,
  payload: TransferOrderPayload,
  options?: HttpOptions
): Promise<ApiResponse<null>> =>
  http.post<null>(
    `/repair-orders/${id}/transfer${toQueryString({
      workerId: payload.workerId,
      reason: payload.reason ?? ''
    })}`,
    undefined,
    options
  )

export const assignOrder = (
  id: number,
  workerId: number,
  options?: HttpOptions
): Promise<ApiResponse<null>> =>
  http.post<null>(`/repair-orders/${id}/assign${toQueryString({ workerId })}`, undefined, options)
