<template>
  <LoginForm
    :app-id="APP_IDENTITY.appId"
    title="智慧社区·业主端"
    subtitle="在线报修 · 进度跟踪"
    @success="handleSuccess"
  />
</template>

<script setup lang="ts">
/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/pages/login/index.vue
 * @Description: 业主 App 登录页，复用共享登录组件并处理登录后的落地跳转
 */

import { LoginForm, type LoginResult } from '@community/shared'
import { APP_IDENTITY } from '../../config/app'

interface PageLike {
  options?: Record<string, string>
  $page?: { options?: Record<string, string> }
}

/** 读取路由守卫在拦截时写入的原始目标地址 */
const readRedirect = (): string => {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as unknown as PageLike
  const options = current?.options ?? current?.$page?.options ?? {}
  return options.redirect ?? ''
}

const handleSuccess = (_result: LoginResult): void => {
  const redirect = readRedirect()
  if (redirect) {
    // redirect 由守卫写入，指向被拦截的普通页面，直接回跳即可
    uni.redirectTo({ url: decodeURIComponent(redirect) })
    return
  }
  uni.switchTab({ url: APP_IDENTITY.homePath })
}
</script>
