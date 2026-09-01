/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/constants/auth.ts
 * @Description: 登录模块的业务常量，集中管理防抖时长、倒计时、验证码规格等阈值
 */

/** 登录按钮防抖窗口(ms)：覆盖一次表单校验 + 网络往返，避免连点产生重复会话 */
export const LOGIN_DEBOUNCE_MS = 400

/** 短信验证码冷却时长(秒) */
export const SMS_COUNTDOWN_SECONDS = 60

/** 图形验证码字符个数 */
export const CAPTCHA_LENGTH = 4

/** 图形验证码画布尺寸(px) */
export const CAPTCHA_WIDTH = 120
export const CAPTCHA_HEIGHT = 44

/** 连续输错图形验证码达到该次数立即换图，防止在同一张图有效期内被暴力枚举 */
export const CAPTCHA_MAX_FAIL_COUNT = 3

/** 后端 LoginRequest 对账号的约束是 11 位手机号，前端保持一致避免无效请求 */
export const ACCOUNT_PATTERN = /^1[3-9]\d{9}$/

/** 短信验证码为 6 位纯数字 */
export const SMS_CODE_PATTERN = /^\d{6}$/

/** 密码长度区间，与后端 ResetPasswordRequest 的 @Size(min=6, max=20) 对齐 */
export const PASSWORD_MIN_LENGTH = 6
export const PASSWORD_MAX_LENGTH = 20

/** 业务码缺失或未知时的兜底文案 */
export const DEFAULT_LOGIN_ERROR_MESSAGE = '登录失败，请稍后重试'

/** 超级管理员不属于任何租户，登录后直接落到租户管理页 */
export const SUPER_ADMIN_HOME_PATH = '/admin/tenants'

/** 普通角色的默认落地页 */
export const DEFAULT_HOME_PATH = '/'

/**
 * 后端业务码到用户可读文案的映射。
 * 登录页必须区分「租户不存在」与「密码错误」：前者要引导用户改租户，
 * 后者要引导用户改密码，混为一谈会让用户反复试错
 */
export const LOGIN_ERROR_MESSAGES: Record<number, string> = {
  1001: '账号不存在，请确认账号与所属租户是否匹配',
  1002: '账号已被禁用，请联系物业管理人员',
  1003: '密码错误，请重新输入',
  3001: '租户不存在，请检查输入的租户名称',
  3002: '该租户已被停用，请联系平台管理员',
  3003: '账号与租户不匹配，请确认所属社区'
}
