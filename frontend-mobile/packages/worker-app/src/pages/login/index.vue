<template>
  <LoginForm
    :app-id="APP_IDENTITY.appId"
    title="智慧社区·维修端"
    subtitle="接单处理 · 巡检执行"
    @success="handleSuccess"
  />
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/login/index.vue
 * @Description: 维修工 App 登录页，复用共享登录组件并处理登录后的落地跳转
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
    uni.redirectTo({ url: decodeURIComponent(redirect) })
    return
  }
  uni.switchTab({ url: APP_IDENTITY.homePath })
}
</script>
