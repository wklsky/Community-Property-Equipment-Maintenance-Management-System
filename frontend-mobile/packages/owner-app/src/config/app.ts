/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/config/app.ts
 * @Description: 业主 App 的身份声明：允许的登录角色、登录页与首页路径
 */

import { APP_ID, type AppId } from '@community/shared'

export const APP_IDENTITY = {
  /** 本 App 只允许"业主"角色登录，维修工账号会在登录回调与路由守卫两处被拦截 */
  appId: APP_ID.OWNER as AppId,
  loginPath: '/pages/login/index',
  homePath: '/pages/index/index'
} as const

/** 免登录白名单，填写 pages.json 中的页面路径（不含前导斜杠，与 uni 跳转 url 保持一致） */
export const ROUTE_WHITE_LIST: readonly string[] = ['pages/login/index']
