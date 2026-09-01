<template>
  <view class="page">
    <text class="page-title">待办工单</text>
    <text class="page-desc">当前登录角色：{{ roleName || '未知' }}　社区：{{ tenantName || '未知' }}</text>
    <button class="primary-btn" @click="goInspection">巡检任务</button>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/index/index.vue
 * @Description: 维修工 App 首页占位，用于验证登录态与角色隔离
 */

import { computed } from 'vue'
import { useUserStore } from '@community/shared'

const userStore = useUserStore()

const roleName = computed(() => userStore.roleName)
const tenantName = computed(() => userStore.tenantName)

const goInspection = (): void => {
  uni.showToast({ title: '巡检任务待迁移', icon: 'none' })
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

.primary-btn {
  height: 96rpx;
  background: #0066ff;
  color: #ffffff;
  border-radius: 48rpx;
  font-size: 30rpx;
}
</style>
