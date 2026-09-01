/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/constants/auth.ts
 * @Description: 登录模块的业务常量与错误码映射
 */

/** 登录按钮防抖窗口(ms)：移动端弱网下响应更慢，窗口略大于 Web 端 */
export const LOGIN_DEBOUNCE_MS = 500

/** 短信验证码冷却时长(秒) */
export const SMS_COUNTDOWN_SECONDS = 60

/** 后端 LoginRequest 约束账号为 11 位手机号 */
export const ACCOUNT_PATTERN = /^1[3-9]\d{9}$/

/** 短信验证码为 6 位纯数字 */
export const SMS_CODE_PATTERN = /^\d{6}$/

/** 密码长度区间，与后端 @Size(min=6, max=20) 对齐 */
export const PASSWORD_MIN_LENGTH = 6
export const PASSWORD_MAX_LENGTH = 20

export const DEFAULT_LOGIN_ERROR_MESSAGE = '登录失败，请稍后重试'

/**
 * 后端业务码 → 用户可读文案。
 * "租户不存在"与"密码错误"必须分开提示：前者引导改租户，后者引导改密码
 */
export const LOGIN_ERROR_MESSAGES: Record<number, string> = {
  1001: '账号不存在，请确认账号与所属租户是否匹配',
  1002: '账号已被禁用，请联系物业管理人员',
  1003: '密码错误，请重新输入',
  3001: '租户不存在，请检查输入的租户名称',
  3002: '该租户已被停用，请联系平台管理员',
  3003: '账号与租户不匹配，请确认所属社区'
}
