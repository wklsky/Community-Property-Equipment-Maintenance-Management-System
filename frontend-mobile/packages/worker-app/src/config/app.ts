/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/config/app.ts
 * @Description: 维修工 App 的身份声明：允许的登录角色、登录页与首页路径
 */

import { APP_ID, type AppId } from '@community/shared'

export const APP_IDENTITY = {
  /** 本 App 只允许"维修工"角色登录，业主账号会在登录回调与路由守卫两处被拦截 */
  appId: APP_ID.WORKER as AppId,
  loginPath: '/pages/login/index',
  homePath: '/pages/index/index'
} as const

/** 免登录白名单，填写 pages.json 中的页面路径（不含前导斜杠，与 uni 跳转 url 保持一致） */
export const ROUTE_WHITE_LIST: readonly string[] = ['pages/login/index']

/**
 * tabBar 页面路径集合。
 * uni 的 navigateTo 无法跳转到 tabBar 页面且不会给出有效提示，必须改用 switchTab；
 * 菜单与快捷入口中同时存在两类页面，跳转前必须据此分流，否则点击后毫无反应
 */
export const TAB_BAR_PATHS: readonly string[] = [
  '/pages/index/index',
  '/pages/repair/list',
  '/pages/inspection/list',
  '/pages/mine/index'
]
