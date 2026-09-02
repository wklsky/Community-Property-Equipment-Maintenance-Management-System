/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/stores/user.ts
 * @Description: 用户状态仓库，持有登录态与角色信息，供路由守卫做越权判定
 */

import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { AppId } from '../constants/roles'
import type { LoginResult } from '../types/auth'
import { checkRoleAllowed } from '../utils/role'

const readStorage = <T>(key: string, fallback: T): T => {
  const value = uni.getStorageSync(key)
  return value === '' || value === null || value === undefined ? fallback : (value as T)
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(readStorage<string>('token', ''))
  const refreshToken = ref<string>(readStorage<string>('refreshToken', ''))
  const userInfo = ref<Partial<LoginResult>>(readStorage<Partial<LoginResult>>('userInfo', {}))

  const isLoggedIn = computed<boolean>(() => !!token.value)
  const roleName = computed<string>(() => userInfo.value?.roleName ?? '')
  const tenantId = computed<number | null>(() => userInfo.value?.tenantId ?? null)
  const tenantName = computed<string>(() => userInfo.value?.tenantName ?? '')
  const userId = computed<number | null>(() => userInfo.value?.userId ?? null)
  const username = computed<string>(() => userInfo.value?.username ?? '')

  function setToken(next: string): void {
    token.value = next
    uni.setStorageSync('token', next)
  }

  function setRefreshToken(next: string): void {
    refreshToken.value = next
    uni.setStorageSync('refreshToken', next)
  }

  function setUserInfo(info: Partial<LoginResult>): void {
    userInfo.value = info
    uni.setStorageSync('userInfo', info)
  }

  function applyLoginResult(result: LoginResult): void {
    setToken(result.token)
    if (result.refreshToken) {
      setRefreshToken(result.refreshToken)
    }
    // 角色信息必须先落盘再跳转：路由守卫读取的就是这里的 roleName
    setUserInfo(result)
  }

  function logout(): void {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = {}
    uni.removeStorageSync('token')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('userInfo')
  }

  /** 当前登录账号是否允许停留在指定 App 中，越权判定复用同一套规则 */
  function isAllowedIn(appId: AppId): boolean {
    return checkRoleAllowed(roleName.value, appId).allowed
  }

  /** 返回越权原因与提示文案，供守卫直接展示 */
  function checkAppAccess(appId: AppId) {
    return checkRoleAllowed(roleName.value, appId)
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    roleName,
    tenantId,
    tenantName,
    userId,
    username,
    setToken,
    setRefreshToken,
    setUserInfo,
    applyLoginResult,
    logout,
    isAllowedIn,
    checkAppAccess
  }
})
