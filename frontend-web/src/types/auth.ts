/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/types/auth.ts
 * @Description: 登录模块的类型定义，含租户解析结果与两种登录模式的载荷
 */

/** 登录模式：密码登录需过图形验证码，短信登录由短信验证码本身完成校验 */
export type LoginMode = 'password' | 'sms'

/** /public/tenants 返回的租户选项，仅暴露 id 与 name（后端刻意裁剪过的公开字段） */
export interface TenantOption {
  id: number
  name: string
}

/**
 * 登录表单模型。
 * tenantKeyword 是用户手动键入的租户文本，tenantId 由 resolveTenant() 解析得出：
 * 后端 /auth/login 只接受租户 ID（Long），前端必须先完成「文本 → ID」的转换
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

export interface ResetPasswordPayload {
  phone: string
  code: string
  tenantId: number
  newPassword: string
}

export interface LoginResult {
  token: string
  refreshToken?: string
  userId: number
  username: string
  phone: string
  tenantId: number
  tenantName?: string
  roleName: string
  isSuperAdmin?: boolean
}

export type TenantResolveError = 'empty' | 'not-found' | 'ambiguous'

export type TenantResolveResult =
  | { ok: true; tenant: TenantOption }
  | { ok: false; reason: TenantResolveError; message: string }
