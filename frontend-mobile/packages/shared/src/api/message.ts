/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/api/message.ts
 * @Description: 站内消息接口，双端共用的消息中心数据源
 */

import { http } from '../utils/request'
import { DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE } from '../constants/business'
import { compactParams } from '../utils/query'
import type { ApiResponse, HttpOptions } from '../types/auth'
import type { PageResult } from '../types/common'
import type { Message, MessageQuery } from '../types/message'

export const getMessages = (
  query?: MessageQuery,
  options?: HttpOptions
): Promise<ApiResponse<PageResult<Message>>> =>
  http.get<PageResult<Message>>(
    '/messages',
    compactParams({
      pageNum: query?.pageNum ?? DEFAULT_PAGE_NUM,
      pageSize: query?.pageSize ?? DEFAULT_PAGE_SIZE,
      isRead: query?.isRead
    }),
    options
  )

export const markMessageRead = (id: number, options?: HttpOptions): Promise<ApiResponse<null>> =>
  http.post<null>(`/messages/${id}/read`, undefined, options)

/** 后端返回 Result<Long>，data 直接是未读条数，不是对象 */
export const getUnreadCount = (options?: HttpOptions): Promise<ApiResponse<number>> =>
  http.get<number>('/messages/unread-count', undefined, options)
