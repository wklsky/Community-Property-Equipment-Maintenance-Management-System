/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/api/inspection.ts
 * @Description: 巡检任务与巡检计划接口，主要由维修工端使用
 */

import { http } from '../utils/request'
import { DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE } from '../constants/business'
import { compactParams } from '../utils/query'
import type { ApiResponse, HttpOptions } from '../types/auth'
import type { PageResult } from '../types/common'
import type {
  CompleteInspectionPayload,
  InspectionPlan,
  InspectionRecord,
  InspectionTask,
  InspectionTaskQuery
} from '../types/inspection'

/**
 * 我的巡检任务。
 * 后端 /tasks/my 的口径是「派给我 或 未指派(抢单池)」，
 * 因此维修工在这一页既能看到指派任务，也能看到可抢的公共任务
 */
export const getMyInspectionTasks = (
  query?: InspectionTaskQuery,
  options?: HttpOptions
): Promise<ApiResponse<PageResult<InspectionTask>>> =>
  http.get<PageResult<InspectionTask>>(
    '/inspections/tasks/my',
    compactParams({
      pageNum: query?.pageNum ?? DEFAULT_PAGE_NUM,
      pageSize: query?.pageSize ?? DEFAULT_PAGE_SIZE,
      status: query?.status
    }),
    options
  )

export const getInspectionTasks = (
  query?: InspectionTaskQuery,
  options?: HttpOptions
): Promise<ApiResponse<PageResult<InspectionTask>>> =>
  http.get<PageResult<InspectionTask>>(
    '/inspections/tasks',
    compactParams({
      pageNum: query?.pageNum ?? DEFAULT_PAGE_NUM,
      pageSize: query?.pageSize ?? DEFAULT_PAGE_SIZE,
      status: query?.status,
      planId: query?.planId
    }),
    options
  )

export const getInspectionTask = (
  id: number,
  options?: HttpOptions
): Promise<ApiResponse<InspectionTask>> =>
  http.get<InspectionTask>(`/inspections/tasks/${id}`, undefined, options)

export const acceptInspectionTask = (
  id: number,
  options?: HttpOptions
): Promise<ApiResponse<null>> => http.post<null>(`/inspections/tasks/${id}/accept`, undefined, options)

/**
 * 提交巡检记录。
 * 后端以 @RequestBody 接收 InspectionRecord，因此走请求体而非 query；
 * result 传 0（异常）会触发生成维修工单，调用方需提前提示用户
 */
export const completeInspectionTask = (
  id: number,
  payload: CompleteInspectionPayload,
  options?: HttpOptions
): Promise<ApiResponse<null>> =>
  http.post<null>(`/inspections/tasks/${id}/complete`, payload, options)

export const getTaskRecords = (
  taskId: number,
  query?: { pageNum?: number; pageSize?: number },
  options?: HttpOptions
): Promise<ApiResponse<PageResult<InspectionRecord>>> =>
  http.get<PageResult<InspectionRecord>>(
    `/inspections/tasks/${taskId}/records`,
    compactParams({
      pageNum: query?.pageNum ?? DEFAULT_PAGE_NUM,
      pageSize: query?.pageSize ?? DEFAULT_PAGE_SIZE
    }),
    options
  )

export const getInspectionPlans = (
  query?: { pageNum?: number; pageSize?: number; status?: number | null },
  options?: HttpOptions
): Promise<ApiResponse<PageResult<InspectionPlan>>> =>
  http.get<PageResult<InspectionPlan>>(
    '/inspections/plans',
    compactParams({
      pageNum: query?.pageNum ?? DEFAULT_PAGE_NUM,
      pageSize: query?.pageSize ?? DEFAULT_PAGE_SIZE,
      status: query?.status
    }),
    options
  )

export const getInspectionPlan = (
  id: number,
  options?: HttpOptions
): Promise<ApiResponse<InspectionPlan>> =>
  http.get<InspectionPlan>(`/inspections/plans/${id}`, undefined, options)
