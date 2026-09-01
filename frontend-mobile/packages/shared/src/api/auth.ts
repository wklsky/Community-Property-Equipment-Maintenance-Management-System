/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/api/auth.ts
 * @Description: 认证相关接口，两个 App 共用同一份请求定义
 */

import { http } from '../utils/request'
import type {
  ApiResponse,
  HttpOptions,
  LoginResult,
  PasswordLoginPayload,
  SendSmsPayload,
  SmsLoginPayload,
  TenantOption
} from '../types/auth'

/** 免认证接口，只返回启用状态的租户 */
export const getTenants = (options?: HttpOptions): Promise<ApiResponse<TenantOption[]>> =>
  http.get<TenantOption[]>('/public/tenants', undefined, options)

export const login = (
  data: PasswordLoginPayload,
  options?: HttpOptions
): Promise<ApiResponse<LoginResult>> => http.post<LoginResult>('/auth/login', data, options)

export const loginByCode = (
  data: SmsLoginPayload,
  options?: HttpOptions
): Promise<ApiResponse<LoginResult>> => http.post<LoginResult>('/auth/login-by-code', data, options)

export const sendCode = (
  data: SendSmsPayload,
  options?: HttpOptions
): Promise<ApiResponse<null>> => http.post<null>('/auth/send-code', data, options)
