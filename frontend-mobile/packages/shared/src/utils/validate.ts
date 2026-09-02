/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/utils/validate.ts
 * @Description: 与 UI 框架无关的纯校验函数，供两个 App 与任意表单组件复用
 */

import {
  ACCOUNT_PATTERN,
  PASSWORD_MAX_LENGTH,
  PASSWORD_MIN_LENGTH,
  SMS_CODE_PATTERN
} from '../constants/auth'
import type { ValidationResult } from '../types/auth'

const ok = (): ValidationResult => ({ valid: true, message: '' })
const fail = (message: string): ValidationResult => ({ valid: false, message })

export const isNotEmpty = (value: unknown): boolean =>
  value !== null && value !== undefined && String(value).trim() !== ''

export const isValidAccount = (value: string): boolean => ACCOUNT_PATTERN.test(value.trim())

export const isValidSmsCode = (value: string): boolean => SMS_CODE_PATTERN.test(value.trim())

export const isValidPassword = (value: string): boolean =>
  value.length >= PASSWORD_MIN_LENGTH && value.length <= PASSWORD_MAX_LENGTH

/** 校验租户是否已解析出 ID：后端只接受 tenantId，未解析成功不允许发请求 */
export const validateTenant = (tenantId: number | null): ValidationResult =>
  tenantId === null ? fail('请选择或输入正确的社区') : ok()

export const validateAccount = (account: string): ValidationResult => {
  if (!isNotEmpty(account)) return fail('请输入手机号')
  if (!isValidAccount(account)) return fail('手机号格式不正确')
  return ok()
}

export const validatePassword = (password: string): ValidationResult => {
  if (!isNotEmpty(password)) return fail('请输入密码')
  if (!isValidPassword(password)) {
    return fail(`密码长度需 ${PASSWORD_MIN_LENGTH}-${PASSWORD_MAX_LENGTH} 位`)
  }
  return ok()
}

export const validateSmsCode = (code: string): ValidationResult => {
  if (!isNotEmpty(code)) return fail('请输入验证码')
  if (!isValidSmsCode(code)) return fail('验证码为 6 位数字')
  return ok()
}

/**
 * 按顺序执行多个校验，返回第一个失败结果。
 * 不能直接用 `a() || b()`：ValidationResult 是对象，永远为真，
 * 短路运算会把第一个校验的结果原样返回，后续校验根本不会执行
 */
const firstFailure = (...results: ValidationResult[]): ValidationResult =>
  results.find((result) => !result.valid) ?? ok()

/** 密码登录的整体校验：按表单字段自上而下的顺序，保证首个错误提示贴近对应输入框 */
export const validatePasswordLogin = (form: {
  tenantId: number | null
  account: string
  password: string
}): ValidationResult =>
  firstFailure(
    validateTenant(form.tenantId),
    validateAccount(form.account),
    validatePassword(form.password)
  )

/** 短信登录的整体校验 */
export const validateSmsLogin = (form: {
  tenantId: number | null
  account: string
  smsCode: string
}): ValidationResult =>
  firstFailure(
    validateTenant(form.tenantId),
    validateAccount(form.account),
    validateSmsCode(form.smsCode)
  )

/** 发送短信验证码前的校验：不校验密码，只确认租户与手机号可用 */
export const validateSendSms = (form: {
  tenantId: number | null
  account: string
}): ValidationResult => firstFailure(validateTenant(form.tenantId), validateAccount(form.account))
