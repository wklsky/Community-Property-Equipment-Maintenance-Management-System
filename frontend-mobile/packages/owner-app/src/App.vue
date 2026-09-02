<script setup lang="ts">
/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/App.vue
 * @Description: 业主 App 根组件，承载全局生命周期
 */

import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('[owner-app] 启动')
})
</script>

<style>
page {
  background-color: #f8fafc;
}
</style>
