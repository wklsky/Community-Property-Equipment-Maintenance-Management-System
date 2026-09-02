/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/constants/business.ts
 * @Description: 工单、设备、巡检、公告的业务状态常量，取值与后端枚举严格一一对应
 */

/**
 * 工单状态码，与后端 common/RepairOrderStatus 完全对齐。
 * 状态码一旦与后端不一致，列表筛选会把工单分到错误的分组，属于静默的数据错误
 */
export const REPAIR_STATUS = {
  PENDING_ACCEPT: 0,
  PENDING_ASSIGN: 1,
  PENDING_PROCESS: 2,
  PROCESSING: 3,
  PENDING_EVALUATE: 4,
  COMPLETED: 5,
  CANCELLED: 6,
  TRANSFERRING: 7
} as const

export type RepairStatus = (typeof REPAIR_STATUS)[keyof typeof REPAIR_STATUS]

export const REPAIR_STATUS_NAME: Record<number, string> = {
  [REPAIR_STATUS.PENDING_ACCEPT]: '待受理',
  [REPAIR_STATUS.PENDING_ASSIGN]: '待派单',
  [REPAIR_STATUS.PENDING_PROCESS]: '待处理',
  [REPAIR_STATUS.PROCESSING]: '处理中',
  [REPAIR_STATUS.PENDING_EVALUATE]: '待评价',
  [REPAIR_STATUS.COMPLETED]: '已完成',
  [REPAIR_STATUS.CANCELLED]: '已取消',
  [REPAIR_STATUS.TRANSFERRING]: '转单中'
}

/** 工单优先级：后端以 priority === 1 判定紧急，其余一律按普通处理 */
export const REPAIR_PRIORITY = {
  NORMAL: 0,
  URGENT: 1
} as const

export type RepairPriority = (typeof REPAIR_PRIORITY)[keyof typeof REPAIR_PRIORITY]

export const REPAIR_PRIORITY_NAME: Record<number, string> = {
  [REPAIR_PRIORITY.NORMAL]: '普通',
  [REPAIR_PRIORITY.URGENT]: '紧急'
}

/** 设备状态码，取值来自后端看板 deviceStats 的分组口径（1 正常 / 2 故障 / 3 维修中 / 4 停用） */
export const DEVICE_STATUS = {
  NORMAL: 1,
  FAULTY: 2,
  REPAIRING: 3,
  DISABLED: 4
} as const

export type DeviceStatus = (typeof DEVICE_STATUS)[keyof typeof DEVICE_STATUS]

export const DEVICE_STATUS_NAME: Record<number, string> = {
  [DEVICE_STATUS.NORMAL]: '正常',
  [DEVICE_STATUS.FAULTY]: '故障',
  [DEVICE_STATUS.REPAIRING]: '维修中',
  [DEVICE_STATUS.DISABLED]: '停用'
}

/** 巡检任务流转：0 待接单 → 1 进行中 → 2 已完成，后端按此顺序做状态前置校验 */
export const INSPECTION_TASK_STATUS = {
  PENDING: 0,
  PROCESSING: 1,
  COMPLETED: 2
} as const

export type InspectionTaskStatus =
  (typeof INSPECTION_TASK_STATUS)[keyof typeof INSPECTION_TASK_STATUS]

export const INSPECTION_TASK_STATUS_NAME: Record<number, string> = {
  [INSPECTION_TASK_STATUS.PENDING]: '待接单',
  [INSPECTION_TASK_STATUS.PROCESSING]: '进行中',
  [INSPECTION_TASK_STATUS.COMPLETED]: '已完成'
}

/** 巡检计划状态：0 未发布 / 1 已发布 / 2 已暂停 */
export const INSPECTION_PLAN_STATUS = {
  DRAFT: 0,
  PUBLISHED: 1,
  PAUSED: 2
} as const

export const INSPECTION_PLAN_STATUS_NAME: Record<number, string> = {
  [INSPECTION_PLAN_STATUS.DRAFT]: '未发布',
  [INSPECTION_PLAN_STATUS.PUBLISHED]: '已发布',
  [INSPECTION_PLAN_STATUS.PAUSED]: '已暂停'
}

/**
 * 巡检结果。
 * 后端以 result === 0 判定"异常"并自动生成一张维修工单，
 * 因此 0/1 不能按直觉反过来定义，否则巡检异常将无法触发工单
 */
export const INSPECTION_RESULT = {
  ABNORMAL: 0,
  NORMAL: 1
} as const

export type InspectionResult = (typeof INSPECTION_RESULT)[keyof typeof INSPECTION_RESULT]

export const INSPECTION_RESULT_NAME: Record<number, string> = {
  [INSPECTION_RESULT.ABNORMAL]: '异常',
  [INSPECTION_RESULT.NORMAL]: '正常'
}

/** 公告发布状态：0 草稿 / 1 已发布 / 2 定时发布 */
export const NOTICE_STATUS = {
  DRAFT: 0,
  PUBLISHED: 1,
  SCHEDULED: 2
} as const

export const NOTICE_STATUS_NAME: Record<number, string> = {
  [NOTICE_STATUS.DRAFT]: '草稿',
  [NOTICE_STATUS.PUBLISHED]: '已发布',
  [NOTICE_STATUS.SCHEDULED]: '待发布'
}

/** 分页默认值，与后端 @RequestParam 的 default 值保持一致 */
export const DEFAULT_PAGE_NUM = 1
export const DEFAULT_PAGE_SIZE = 10
