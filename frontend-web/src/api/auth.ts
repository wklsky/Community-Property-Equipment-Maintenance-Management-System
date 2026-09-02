/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/api/auth.ts
 * @Description: 认证相关接口，统一声明入参与返回值的类型契约
 */

import request from '@/utils/request'
import type { ApiResponse, HttpClient, RequestConfig } from '@/types/http'
import type {
  LoginResult,
  PasswordLoginPayload,
  ResetPasswordPayload,
  SendSmsPayload,
  SmsLoginPayload,
  TenantOption
} from '@/types/auth'

// utils/request 仍是历史 JS 模块且未提供 .d.ts，这里做一次显式收窄，
// 避免隐式 any 从请求层扩散到全部业务调用方
const http = request as unknown as HttpClient

/** 免认证接口，仅返回启用状态的租户 */
export function getTenants(config?: RequestConfig): Promise<ApiResponse<TenantOption[]>> {
  return http.get<TenantOption[]>('/public/tenants', undefined, config)
}

export function login(
  data: PasswordLoginPayload,
  config?: RequestConfig
): Promise<ApiResponse<LoginResult>> {
  return http.post<LoginResult>('/auth/login', data, config)
}

export function loginByCode(
  data: SmsLoginPayload,
  config?: RequestConfig
): Promise<ApiResponse<LoginResult>> {
  return http.post<LoginResult>('/auth/login-by-code', data, config)
}

export function sendCode(
  data: SendSmsPayload,
  config?: RequestConfig
): Promise<ApiResponse<null>> {
  return http.post<null>('/auth/send-code', data, config)
}

export function resetPassword(
  data: ResetPasswordPayload,
  config?: RequestConfig
): Promise<ApiResponse<null>> {
  return http.post<null>('/auth/reset-password', data, config)
}
