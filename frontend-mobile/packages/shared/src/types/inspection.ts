/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/inspection.ts
 * @Description: 巡检计划、巡检任务与巡检记录的类型定义
 */

import type { PageQuery } from './common'

export interface InspectionTask {
  id: number
  tenantId: number
  planId: number
  planName: string
  buildingId?: number | null
  categoryId?: number | null
  deviceId?: number | null
  assignedTo?: number | null
  taskDate?: string | null
  status: number
  createTime?: string | null
}

export interface InspectionPlan {
  id: number
  tenantId: number
  name: string
  buildingId?: number | null
  categoryId?: number | null
  deviceId?: number | null
  cycle: number
  nextTime?: string | null
  status: number
  createTime?: string | null
}

export interface InspectionRecord {
  id: number
  tenantId: number
  taskId: number
  deviceId?: number | null
  result: number
  remark?: string | null
  createTime?: string | null
}

export interface InspectionTaskQuery extends PageQuery {
  status?: number | null
  planId?: number | null
}

/**
 * 提交巡检记录的入参。
 * result 传 0（异常）时后端会自动生成一张维修工单并通知管理员，
 * 提交前必须向用户明确该后果
 */
export interface CompleteInspectionPayload {
  result: number
  remark?: string
}
