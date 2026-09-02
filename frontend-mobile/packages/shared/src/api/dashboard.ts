/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/api/dashboard.ts
 * @Description: 首页看板统计接口
 */

import { http } from '../utils/request'
import type { ApiResponse, HttpOptions } from '../types/auth'
import type { DashboardStats } from '../types/dashboard'

/**
 * 看板统计。
 * 后端已按角色裁剪数据范围（业主只看自己报修的，维修工只看派给自己的），
 * 前端不应再按 userId 二次过滤，否则会把公共抢单池的数据也过滤掉
 */
export const getDashboardStats = (options?: HttpOptions): Promise<ApiResponse<DashboardStats>> =>
  http.get<DashboardStats>('/dashboard/stats', undefined, options)
