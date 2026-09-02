<template>
  <view class="page">
    <view v-if="isEmpty" class="empty">
      <text class="empty-text">暂无设备信息</text>
    </view>

    <view v-else class="card-list">
      <view v-for="device in list" :key="device.id" class="device-card">
        <view class="card-header">
          <text class="name">{{ device.name }}</text>
          <text class="status" :style="{ color: statusColor(device.status) }">
            {{ resolveStatusName(DEVICE_STATUS_NAME, device.status) }}
          </text>
        </view>
        <view class="meta-row">
          <text class="meta">型号：{{ device.model || '—' }}</text>
          <text class="meta">位置：{{ device.location || '—' }}</text>
        </view>
      </view>
    </view>

    <view v-if="loading" class="tip">加载中...</view>
    <view v-else-if="finished" class="tip">没有更多了</view>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/pages/device/list.vue
 * @Description: 业主端设备浏览，只读展示本社区设备台账
 */

import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import {
  DEVICE_STATUS_NAME,
  getDevices,
  resolveStatusName,
  usePagedList,
  type Device
} from '@community/shared'

const STATUS_COLOR: Record<number, string> = {
  1: '#10b981',
  2: '#ef4444',
  3: '#f59e0b',
  4: '#94a3b8'
}

const { list, loading, finished, isEmpty, loadMore, refresh } = usePagedList<Device, Record<string, never>>({
  fetcher: getDevices
})

const statusColor = (status: number): string => STATUS_COLOR[status] ?? '#64748b'

onShow(() => {
  void refresh()
})

onPullDownRefresh(() => {
  void refresh().finally(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  void loadMore()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 24rpx;
}

.empty {
  padding: 200rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #94a3b8;
}

.device-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
}

.status {
  font-size: 26rpx;
  font-weight: 600;
}

.meta-row {
  display: flex;
  justify-content: space-between;
}

.meta {
  font-size: 25rpx;
  color: #64748b;
}

.tip {
  text-align: center;
  padding: 24rpx 0;
  font-size: 24rpx;
  color: #94a3b8;
}
</style>
