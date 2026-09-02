/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/api/notice.ts
 * @Description: 社区公告接口，双端共用，只消费已发布列表
 */

import { http } from '../utils/request'
import { DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE } from '../constants/business'
import { compactParams } from '../utils/query'
import type { ApiResponse, HttpOptions } from '../types/auth'
import type { PageResult } from '../types/common'
import type { Notice, NoticeQuery } from '../types/notice'

/**
 * 公告列表。
 * 移动端固定调用 /notices（后端已过滤 publishStatus=1），
 * 不使用 /notices/all：草稿与定时公告属于管理员视角，对业主/维修工无意义
 */
export const getNotices = (
  query?: NoticeQuery,
  options?: HttpOptions
): Promise<ApiResponse<PageResult<Notice>>> =>
  http.get<PageResult<Notice>>(
    '/notices',
    compactParams({
      pageNum: query?.pageNum ?? DEFAULT_PAGE_NUM,
      pageSize: query?.pageSize ?? DEFAULT_PAGE_SIZE
    }),
    options
  )

export const getNotice = (id: number, options?: HttpOptions): Promise<ApiResponse<Notice>> =>
  http.get<Notice>(`/notices/${id}`, undefined, options)

export const markNoticeRead = (id: number, options?: HttpOptions): Promise<ApiResponse<null>> =>
  http.post<null>(`/notices/${id}/read`, undefined, options)
