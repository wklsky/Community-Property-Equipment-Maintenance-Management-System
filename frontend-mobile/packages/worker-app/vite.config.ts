/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/worker-app/vite.config.ts
 * @Description: 维修工 App 构建配置，shared 包为工作区源码依赖，无需额外声明即可被转译
 */

import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  build: {
    // shared 是软链到 packages/shared 的源码包，必须让 rollup 跟随符号链接解析，
    // 否则会被当成 node_modules 中的产物依赖而跳过 TS 转译
    rollupOptions: {
      preserveSymlinks: true
    }
  }
})
