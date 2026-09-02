<template>
  <view class="page">
    <view class="header">
      <text class="greeting">{{ username || '维修师傅' }}，你好</text>
      <text class="community">{{ tenantName || '智慧社区' }} · {{ roleName || '维修工' }}</text>
    </view>

    <view class="stats">
      <view v-for="item in orderCards" :key="item.label" class="stat-item">
        <text class="stat-value">{{ item.value }}</text>
        <text class="stat-label">{{ item.label }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">巡检概览</text>
      <view class="inspect-row">
        <view v-for="item in inspectCards" :key="item.label" class="inspect-item">
          <text class="inspect-value">{{ item.value }}</text>
          <text class="inspect-label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">快捷服务</text>
      <view class="quick-grid">
        <view class="quick-item" @click="go('/pages/repair/list')">
          <text class="quick-text">我的工单</text>
        </view>
        <view class="quick-item" @click="go('/pages/inspection/list')">
          <text class="quick-text">巡检任务</text>
        </view>
        <view class="quick-item" @click="go('/pages/device/list')">
          <text class="quick-text">设备台账</text>
        </view>
        <view class="quick-item" @click="go('/pages/message/list')">
          <text class="quick-text">消息中心</text>
        </view>
        <view class="quick-item" @click="go('/pages/notice/list')">
          <text class="quick-text">社区公告</text>
        </view>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">最近工单</text>
      <view
        v-for="order in recentOrders"
        :key="order.id"
        class="recent-item"
        @click="goDetail(order.id)"
      >
        <text class="recent-desc">{{ order.faultDesc }}</text>
        <text class="recent-status">{{ order.statusName }}</text>
      </view>
      <view v-if="recentOrders.length === 0" class="empty-line">暂无工单记录</view>
    </view>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/index/index.vue
 * @Description: 维修工端首页，展示工单与巡检概览及快捷入口
 */

import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import {
  getDashboardStats,
  useUserStore,
  type DashboardStats,
  type RecentOrder
} from '@community/shared'
import { TAB_BAR_PATHS } from '../../config/app'

const userStore = useUserStore()

const stats = ref<DashboardStats | null>(null)
const recentOrders = ref<RecentOrder[]>([])

const username = computed<string>(() => userStore.username)
const roleName = computed<string>(() => userStore.roleName)
const tenantName = computed<string>(() => userStore.tenantName)

/**
 * 后端已按「派给当前维修工」裁剪过 orderStats，
 * 因此这里的数字就是本人待办量，前端不能再按 userId 过滤一次
 */
const orderCards = computed<{ label: string; value: number }[]>(() => {
  const orderStats = stats.value?.orderStats
  return [
    { label: '待处理', value: orderStats?.pending ?? 0 },
    { label: '处理中', value: orderStats?.processing ?? 0 },
    { label: '已完成', value: orderStats?.completed ?? 0 }
  ]
})

const inspectCards = computed<{ label: string; value: number }[]>(() => {
  const inspectStats = stats.value?.inspectionStats
  return [
    { label: '待接单', value: inspectStats?.pending ?? 0 },
    { label: '进行中', value: inspectStats?.processing ?? 0 },
    { label: '今日完成', value: inspectStats?.completedToday ?? 0 }
  ]
})

const loadStats = async (): Promise<void> => {
  try {
    const res = await getDashboardStats({ silent: true, quiet: true })
    stats.value = res.data
    recentOrders.value = res.data?.recentOrders ?? []
  } catch {
    // 首页统计加载失败时保留上一次结果，不弹出错误打断用户操作
  }
}

/** tabBar 页面必须走 switchTab，用 navigateTo 跳转会静默失败，表现为点击后毫无反应 */
const go = (url: string): void => {
  if (TAB_BAR_PATHS.includes(url)) {
    uni.switchTab({ url })
    return
  }
  uni.navigateTo({ url })
}

const goDetail = (id: number): void => {
  uni.navigateTo({ url: `/pages/repair/detail?id=${id}` })
}

onShow(() => {
  void loadStats()
})

onPullDownRefresh(() => {
  void loadStats().finally(() => uni.stopPullDownRefresh())
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 40rpx 24rpx;
}

.header {
  padding: 20rpx 8rpx 40rpx;
}

.greeting {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12rpx;
}

.community {
  display: block;
  font-size: 26rpx;
  color: #64748b;
}

.stats {
  display: flex;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx 0;
  margin-bottom: 32rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #0066ff;
  margin-bottom: 8rpx;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #64748b;
}

.panel {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 32rpx;
}

.panel-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 24rpx;
}

.inspect-row {
  display: flex;
}

.inspect-item {
  flex: 1;
  text-align: center;
}

.inspect-value {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8rpx;
}

.inspect-label {
  display: block;
  font-size: 24rpx;
  color: #64748b;
}

.quick-grid {
  display: flex;
  flex-wrap: wrap;
}

.quick-item {
  width: 33.33%;
  padding: 20rpx 0;
  text-align: center;
}

.quick-text {
  display: inline-block;
  width: 180rpx;
  height: 72rpx;
  line-height: 72rpx;
  background: #f1f5f9;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #334155;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #f8fafc;
}

.recent-desc {
  flex: 1;
  font-size: 28rpx;
  color: #1e293b;
  margin-right: 20rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-status {
  font-size: 24rpx;
  color: #0066ff;
  flex-shrink: 0;
}

.empty-line {
  text-align: center;
  font-size: 26rpx;
  color: #94a3b8;
  padding: 40rpx 0;
}
</style>
