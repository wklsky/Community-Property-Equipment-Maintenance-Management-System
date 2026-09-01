/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/router/guard.ts
 * @Description: 跨 App 复用的路由守卫，统一拦截未登录访问与角色越权（如维修工进入业主 App）
 */

/**
 * uni-app 没有 vue-router，因此不存在全局 beforeEach。
 * 这里用 uni.addInterceptor 拦截四类跳转 API，等价于路由守卫：
 *
 *   ① 白名单页面（登录页）             → 放行
 *   ② 未携带 Token                    → 携带 redirect 参数跳转登录页
 *   ③ 已登录却再次访问登录页           → 送回首页，避免重复登录产生多份会话
 *   ④ 角色不在本 App 白名单内（越权）  → 清除登录态 + 弹窗提示 + 强制回登录页
 *
 * 第 ④ 步是"维修工登录业主 App"的精准拦截点：
 * 登录接口只负责校验「账号密码是否正确」，它并不关心用户打开的是哪一个 App，
 * 所以 Token 合法 ≠ 有权进入当前 App，必须由前端按 App 身份补上这一道判定。
 * 判定规则与登录成功回调中的校验同源（都走 checkRoleAllowed），避免两处规则各自演化出现缝隙
 */

import type { AppId } from '../constants/roles'
import { useUserStore } from '../stores/user'

export interface GuardOptions {
  /** 当前 App 标识，用于查询该 App 允许登录的角色白名单 */
  appId: AppId
  /** 登录页完整路径，必须以 / 开头 */
  loginPath: string
  /** 首页路径，必须是 tabBar 页面 */
  homePath: string
  /** 免登录白名单，元素为 pages.json 中的页面路径（不含前导斜杠） */
  whiteList: readonly string[]
}

/** 统一把 "/pages/a/b?x=1" 规约为 "pages/a/b"，便于与白名单做精确比对 */
const toPath = (url: string): string => url.split('?')[0].replace(/^\//, '')

/** 需要被守卫覆盖的跳转方式，遗漏任何一个都会形成绕过口子 */
const NAV_METHODS = ['navigateTo', 'redirectTo', 'reLaunch', 'switchTab'] as const

export function setupRouteGuard(options: GuardOptions): void {
  const { appId, loginPath, homePath, whiteList } = options

  const canVisit = (url: string): boolean => {
    const path = toPath(url)

    // ① 白名单放行
    if (whiteList.includes(path)) {
      return true
    }

    const userStore = useUserStore()

    // ② 未登录：保留原始目标地址，登录成功后原路返回
    if (!userStore.token) {
      uni.navigateTo({ url: `${loginPath}?redirect=${encodeURIComponent(url)}` })
      return false
    }

    // ③ 已登录却要回登录页：直接送回首页
    if (path === toPath(loginPath)) {
      uni.switchTab({ url: homePath })
      return false
    }

    // ④ 角色越权判定
    const access = userStore.checkAppAccess(appId)
    if (!access.allowed) {
      // 必须同步清除登录态：只拦截跳转而不清理，下次冷启动仍会带着
      // 错误角色的 Token 进入，等于"拦了但没完全拦"
      userStore.logout()

      uni.showModal({
        title: '无权访问',
        content: access.message,
        showCancel: false,
        confirmText: '我知道了',
        success: () => {
          uni.reLaunch({ url: loginPath })
        }
      })
      return false
    }

    return true
  }

  NAV_METHODS.forEach((method) => {
    uni.addInterceptor(method, {
      // invoke 返回 false 即终止本次跳转
      invoke(args) {
        const url = (args as { url?: unknown }).url
        if (typeof url !== 'string') {
          return true
        }
        return canVisit(url)
      }
    })
  })
}
