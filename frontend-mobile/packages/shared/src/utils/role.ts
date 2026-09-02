/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/utils/role.ts
 * @Description: 跨端越权判定，登录回调与路由守卫共用同一套结论
 */

import {
  APP_ALLOWED_ROLES,
  APP_ROLE_MISMATCH_MESSAGE,
  MOBILE_FORBIDDEN_MESSAGE,
  MOBILE_FORBIDDEN_ROLES,
  type AppId,
  type AppRole
} from '../constants/roles'
import type { RoleCheckResult } from '../types/auth'

/**
 * 判定某角色是否允许进入指定 App。
 * 这是"维修工不能登录业主 App"的唯一判定入口：
 * 登录成功回调与路由守卫都调用它，避免两处规则各自演化出现缝隙
 */
export const checkRoleAllowed = (role: string | undefined, appId: AppId): RoleCheckResult => {
  if (!role) {
    return { allowed: false, reason: 'app-mismatch', message: '登录信息不完整，请重新登录' }
  }

  // 管理员类角色即使是"合法账号"也不能进移动端，否则会绕过 PC 后台的审计能力
  if (MOBILE_FORBIDDEN_ROLES.includes(role as AppRole)) {
    return { allowed: false, reason: 'forbidden-on-mobile', message: MOBILE_FORBIDDEN_MESSAGE }
  }

  const allowed = APP_ALLOWED_ROLES[appId]
  if (!allowed.includes(role as AppRole)) {
    return {
      allowed: false,
      reason: 'app-mismatch',
      message: APP_ROLE_MISMATCH_MESSAGE[appId]
    }
  }

  return { allowed: true }
}
