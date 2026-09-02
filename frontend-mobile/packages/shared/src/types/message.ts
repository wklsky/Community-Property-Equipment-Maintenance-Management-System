/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/message.ts
 * @Description: 站内消息的类型定义，字段对齐后端 Message 实体
 */

import type { PageQuery } from './common'

export interface Message {
  id: number
  tenantId: number
  userId: number
  /** 业务类型，由后端写入（如工单状态变更、巡检异常通知） */
  type: string
  content: string
  isRead: number
  createTime?: string | null
}

export interface MessageQuery extends PageQuery {
  isRead?: number | null
}

// /messages/unread-count 返回 Result<Long>，data 直接是数字而非包装对象，
// 故此处不定义响应类型，调用方直接使用 ApiResponse<number>
