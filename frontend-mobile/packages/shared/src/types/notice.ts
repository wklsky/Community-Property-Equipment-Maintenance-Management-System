/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/notice.ts
 * @Description: 社区公告的类型定义，字段对齐后端 Notice 实体
 */

import type { PageQuery } from './common'

export interface Notice {
  id: number
  tenantId: number
  title: string
  content: string
  /** 0 草稿 / 1 已发布 / 2 定时发布，取值见 NOTICE_STATUS */
  publishStatus: number
  scheduledTime?: string | null
  createTime?: string | null
}

/**
 * 公告查询参数。
 * 移动端只消费 /notices（已发布列表），不带状态筛选：
 * 草稿与定时公告对业主/维修工无意义，/notices/all 是管理员专属接口
 */
export interface NoticeQuery extends PageQuery {
  publishStatus?: number | null
}
