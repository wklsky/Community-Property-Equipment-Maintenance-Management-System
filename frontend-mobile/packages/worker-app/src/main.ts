/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/main.ts
 * @Description: 维修工 App 入口，装配 Pinia、请求配置与路由守卫
 */

import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import { configureHttp } from '@community/shared'
import App from './App.vue'
import { APP_IDENTITY } from './config/app'
import { setupAppGuard } from './router/guard'

export function createApp() {
  const app = createSSRApp(App)

  // Pinia 必须先于任何 Store 调用注册，守卫内部会在跳转时读取用户状态
  app.use(createPinia())

  configureHttp({
    baseURL: import.meta.env.VITE_APP_BASE_URL,
    onUnauthorized: () => {
      uni.reLaunch({ url: APP_IDENTITY.loginPath })
    }
  })

  setupAppGuard()

  return { app }
}
