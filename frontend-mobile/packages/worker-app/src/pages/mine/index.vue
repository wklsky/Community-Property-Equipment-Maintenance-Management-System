<template>
  <view class="page">
    <view class="header">
      <text class="username">{{ username || '未登录' }}</text>
      <text class="role">{{ roleName || '—' }} · {{ tenantName || '—' }}</text>
    </view>

    <view class="panel">
      <view class="menu-item" @click="go('/pages/inspection/list')">
        <text class="menu-text">巡检任务</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/repair/list')">
        <text class="menu-text">我的工单</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/device/list')">
        <text class="menu-text">设备台账</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/message/list')">
        <text class="menu-text">消息中心</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/notice/list')">
        <text class="menu-text">社区公告</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/mine/index.vue
 * @Description: 维修工端个人中心，聚合非 tabBar 业务入口并提供退出登录
 */

import { computed } from 'vue'
import { useUserStore } from '@community/shared'
import { APP_IDENTITY, TAB_BAR_PATHS } from '../../config/app'

const userStore = useUserStore()

const username = computed<string>(() => userStore.username)
const roleName = computed<string>(() => userStore.roleName)
const tenantName = computed<string>(() => userStore.tenantName)

/** tabBar 页面必须走 switchTab，用 navigateTo 跳转会静默失败，表现为点击后毫无反应 */
const go = (url: string): void => {
  if (TAB_BAR_PATHS.includes(url)) {
    uni.switchTab({ url })
    return
  }
  uni.navigateTo({ url })
}

const handleLogout = (): void => {
  userStore.logout()
  // 登录页不是 tabBar 页面，必须使用 reLaunch 而非 switchTab
  uni.reLaunch({ url: APP_IDENTITY.loginPath })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 40rpx 24rpx;
}

.header {
  padding: 40rpx 8rpx 48rpx;
}

.username {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12rpx;
}

.role {
  display: block;
  font-size: 26rpx;
  color: #64748b;
}

.panel {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 0 28rpx;
  margin-bottom: 40rpx;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 2rpx solid #f8fafc;
}

.menu-text {
  font-size: 29rpx;
  color: #1e293b;
}

.arrow {
  font-size: 32rpx;
  color: #cbd5e1;
}

.logout-btn {
  height: 96rpx;
  line-height: 96rpx;
  background: #ffffff;
  color: #e11d48;
  border: 2rpx solid #fecdd3;
  border-radius: 48rpx;
  font-size: 30rpx;
}
</style>
