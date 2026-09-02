/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/dashboard.ts
 * @Description: 首页看板统计的类型定义，字段对齐后端 DashboardVO
 */

export interface OrderStats {
  pendingAccept: number
  pendingAssign: number
  pending: number
  processing: number
  pendingEvaluate: number
  completed: number
  cancelled: number
  total: number
}

export interface DeviceStats {
  normal: number
  faulty: number
  repairing: number
  disabled: number
  total: number
}

export interface InspectionStats {
  pending: number
  processing: number
  completedToday: number
  total: number
}

export interface OrderTrendItem {
  date: string
  count: number
  completed: number
}

export interface RecentOrder {
  id: number
  orderNo: string
  faultDesc: string
  address: string
  status: number
  statusName: string
  createTime: string | null
}

/**
 * 看板聚合数据。
 * 后端已按角色裁剪：业主只看自己报修的工单，维修工只看派给自己的，
 * 因此移动端无需再按 userId 过滤一次
 */
export interface DashboardStats {
  orderStats: OrderStats
  deviceStats: DeviceStats
  inspectionStats: InspectionStats
  orderTrend: OrderTrendItem[]
  recentOrders: RecentOrder[]
}
