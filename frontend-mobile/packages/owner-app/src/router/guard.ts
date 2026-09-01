/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/router/guard.ts
 * @Description: 业主 App 的守卫装配，将本 App 的身份信息注入共享守卫
 */

import { setupRouteGuard } from '@community/shared'
import { APP_IDENTITY, ROUTE_WHITE_LIST } from '../config/app'

/**
 * 只把业主 App 的身份注入共享守卫，不再复制一份拦截逻辑。
 * 未来若业主端需要额外的准入规则（如房屋认证），在此处追加即可
 */
export function setupAppGuard(): void {
  setupRouteGuard({
    appId: APP_IDENTITY.appId,
    loginPath: APP_IDENTITY.loginPath,
    homePath: APP_IDENTITY.homePath,
    whiteList: ROUTE_WHITE_LIST
  })
}
