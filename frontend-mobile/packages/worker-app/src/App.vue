<script setup lang="ts">
/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/App.vue
 * @Description: 维修工 App 根组件，承载全局生命周期
 */

import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('[worker-app] 启动')
})
</script>

<style>
page {
  background-color: #f8fafc;
}
</style>
