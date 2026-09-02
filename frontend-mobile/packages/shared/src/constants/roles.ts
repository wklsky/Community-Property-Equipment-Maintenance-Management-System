/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/constants/roles.ts
 * @Description: 角色与 App 身份常量，定义"哪个 App 允许哪些角色登录"的唯一事实来源
 */

/**
 * 后端 sys_role 表中的固定角色名。
 * 前端权限判断必须与后端字符串完全一致，任何改名都要先改后端再同步此处
 */
export const ROLE = {
  SUPER_ADMIN: '超级管理员',
  ADMIN: '系统管理员',
  WORKER: '维修工',
  OWNER: '业主'
} as const

export type AppRole = (typeof ROLE)[keyof typeof ROLE]

/** 拆分后的两个移动端 App 标识 */
export const APP_ID = {
  OWNER: 'owner-app',
  WORKER: 'worker-app'
} as const

export type AppId = (typeof APP_ID)[keyof typeof APP_ID]

/**
 * 每个 App 允许登录的角色白名单。
 * 这是越权拦截的唯一事实来源：维修工账号登录业主 App 属于跨端越权，
 * 必须在路由守卫与登录回调两处同时校验，不能只依赖后端
 */
export const APP_ALLOWED_ROLES: Record<AppId, readonly AppRole[]> = {
  [APP_ID.OWNER]: [ROLE.OWNER],
  [APP_ID.WORKER]: [ROLE.WORKER]
}

/** 越权时的用户提示，直接告诉用户应该去哪个 App，而不是笼统的"无权限" */
export const APP_ROLE_MISMATCH_MESSAGE: Record<AppId, string> = {
  [APP_ID.OWNER]: '当前账号为维修工账号，请使用「维修工 App」登录',
  [APP_ID.WORKER]: '当前账号为业主账号，请使用「业主 App」登录'
}

/** 系统管理员类角色禁止登录移动端，只能使用 PC 管理后台 */
export const MOBILE_FORBIDDEN_ROLES: readonly AppRole[] = [ROLE.ADMIN, ROLE.SUPER_ADMIN]

export const MOBILE_FORBIDDEN_MESSAGE = '管理员账号请使用 PC 端管理后台登录'
