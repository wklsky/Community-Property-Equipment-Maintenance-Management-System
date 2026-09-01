<template>
  <view class="page">
    <text class="page-title">我的</text>
    <text class="page-desc">{{ username || '未登录' }}</text>
    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/mine/index.vue
 * @Description: 维修工 App 个人中心占位，演示退出登录回到登录页
 */

import { computed } from 'vue'
import { useUserStore } from '@community/shared'
import { APP_IDENTITY } from '../../config/app'

const userStore = useUserStore()

const username = computed(() => userStore.username)

const handleLogout = (): void => {
  userStore.logout()
  // 登录页不是 tabBar 页面，必须使用 reLaunch 而非 switchTab
  uni.reLaunch({ url: APP_IDENTITY.loginPath })
}
</script>

<style scoped>
.page {
  padding: 60rpx 40rpx;
}

.page-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16rpx;
}

.page-desc {
  display: block;
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 60rpx;
}

.logout-btn {
  height: 96rpx;
  background: #ffffff;
  color: #e11d48;
  border: 2rpx solid #fecdd3;
  border-radius: 48rpx;
  font-size: 30rpx;
}
</style>
