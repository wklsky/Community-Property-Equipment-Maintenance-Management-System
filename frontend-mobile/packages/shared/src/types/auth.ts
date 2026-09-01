/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/auth.ts
 * @Description: 移动端登录模块的类型定义
 */

import type { AppId, AppRole } from '../constants/roles'

/** 登录模式：密码登录与短信登录共用同一套表单，仅第二因子不同 */
export type LoginMode = 'password' | 'sms'

export interface TenantOption {
  id: number
  name: string
}

/**
 * 登录表单模型。
 * tenantKeyword 为用户手动键入的租户文本，tenantId 由解析逻辑得出：
 * 后端 /auth/login 只接受租户 ID，前端必须先完成「文本 → ID」的转换
 */
export interface LoginFormModel {
  tenantKeyword: string
  tenantId: number | null
  account: string
  password: string
  smsCode: string
}

export interface PasswordLoginPayload {
  phone: string
  password: string
  tenantId: number
}

export interface SmsLoginPayload {
  phone: string
  code: string
  tenantId: number
}

export interface SendSmsPayload {
  phone: string
  tenantId: number
}

export interface LoginResult {
  token: string
  refreshToken?: string
  userId: number
  username: string
  phone: string
  tenantId: number
  tenantName?: string
  roleName: AppRole | string
  isSuperAdmin?: boolean
}

export interface ValidationResult {
  valid: boolean
  message: string
}

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface HttpOptions {
  /** 为 true 时不弹全局错误 Toast，交由调用方按业务码自行提示 */
  silent?: boolean
  /** 为 true 时不展示 uni 全局 loading（轮询、预检等场景） */
  quiet?: boolean
  timeout?: number
}

/** 登录前的角色校验结论 */
export type RoleCheckResult =
  | { allowed: true }
  | { allowed: false; reason: 'forbidden-on-mobile' | 'app-mismatch'; message: string }

export interface AppIdentity {
  appId: AppId
  allowedRoles: readonly AppRole[]
  mismatchMessage: string
  homePath: string
}
