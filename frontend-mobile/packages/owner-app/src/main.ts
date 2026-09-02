/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/main.ts
 * @Description: 业主 App 入口，装配 Pinia、请求配置与路由守卫
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

  // 会话失效后的跳转由宿主注入，shared 包不感知具体页面路径
  configureHttp({
    baseURL: import.meta.env.VITE_APP_BASE_URL,
    onUnauthorized: () => {
      uni.reLaunch({ url: APP_IDENTITY.loginPath })
    }
  })

  setupAppGuard()

  return { app }
}
