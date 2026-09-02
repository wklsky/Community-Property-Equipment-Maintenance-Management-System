/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/repair.ts
 * @Description: 报修工单的请求入参与响应结构，字段对齐后端 RepairOrder 与 RepairOrderVO
 */

import type { PageQuery } from './common'

/**
 * 工单列表项：分页接口直接返回 RepairOrder 实体，
 * 只有详情接口（RepairOrderVO）才带 statusName / 关联人等展示字段
 */
export interface RepairOrder {
  id: number
  tenantId: number
  orderNo: string
  userId: number
  deviceId?: number | null
  address: string
  faultDesc: string
  status: number
  priority: number
  assignTo?: number | null
  appointTime?: string | null
  finishTime?: string | null
  processDesc?: string | null
  resultImages?: string | null
  transferReason?: string | null
  createTime?: string | null
}

/** 工单详情：后端 getDetailById 返回的 RepairOrderVO，额外补齐展示用字段 */
export interface RepairOrderDetail extends RepairOrder {
  statusName?: string
  priorityName?: string
  userName?: string
  userPhone?: string
  deviceName?: string
  deviceLocation?: string
  assignToName?: string
  rating?: number | null
  comment?: string | null
}

/** 提交报修入参，与后端 RepairOrderCreateRequest 的校验约束一致 */
export interface RepairOrderCreatePayload {
  deviceId?: number | null
  /** 后端 @NotBlank @Size(max=200) */
  address: string
  /** 后端 @NotBlank @Size(max=500) */
  faultDesc: string
  /** 后端 @Min(0) @Max(1)，默认紧急 */
  priority?: number
  appointTime?: string
}

/**
 * 我的工单 / 派给我的工单的查询参数。
 * statuses 用数组表达多选，由 api 层 join 成逗号分隔串：
 * 后端 /repair-orders/my 与 /assigned 只接受 "1,2,3" 形式的字符串
 */
export interface RepairOrderQuery extends PageQuery {
  statuses?: number[]
}

export interface CompleteOrderPayload {
  processDesc: string
}

export interface EvaluateOrderPayload {
  rating: number
  comment?: string
}

export interface TransferOrderPayload {
  workerId: number
  reason?: string
}
