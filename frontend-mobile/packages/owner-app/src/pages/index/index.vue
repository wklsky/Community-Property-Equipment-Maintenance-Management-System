<template>
  <view class="page">
    <view class="header">
      <text class="greeting">{{ username || '业主' }}，你好</text>
      <text class="community">{{ tenantName || '智慧社区' }}</text>
    </view>

    <view class="stats">
      <view v-for="item in statCards" :key="item.label" class="stat-item">
        <text class="stat-value">{{ item.value }}</text>
        <text class="stat-label">{{ item.label }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">快捷服务</text>
      <view class="quick-grid">
        <view class="quick-item" @click="go('/pages/repair/create')">
          <text class="quick-text">我要报修</text>
        </view>
        <view class="quick-item" @click="go('/pages/notice/list')">
          <text class="quick-text">社区公告</text>
        </view>
        <view class="quick-item" @click="go('/pages/message/list')">
          <text class="quick-text">消息中心</text>
        </view>
        <view class="quick-item" @click="go('/pages/address/list')">
          <text class="quick-text">我的地址</text>
        </view>
        <view class="quick-item" @click="go('/pages/device/list')">
          <text class="quick-text">设备台账</text>
        </view>
        <view class="quick-item" @click="go('/pages/repair/list')">
          <text class="quick-text">我的工单</text>
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
 * @FilePath: frontend-mobile/packages/owner-app/src/pages/index/index.vue
 * @Description: 业主端首页，展示我的工单概览、快捷服务入口与最近工单
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
const tenantName = computed<string>(() => userStore.tenantName)

/**
 * 业主只看与自己相关的四个口径。
 * 看板 orderStats 还包含待派单、已取消等管理员视角的分组，
 * 对业主没有行动指引意义，故不全部罗列
 */
const statCards = computed<{ label: string; value: number }[]>(() => {
  const orderStats = stats.value?.orderStats
  return [
    { label: '待受理', value: orderStats?.pendingAccept ?? 0 },
    { label: '处理中', value: orderStats?.processing ?? 0 },
    { label: '待评价', value: orderStats?.pendingEvaluate ?? 0 },
    { label: '已完成', value: orderStats?.completed ?? 0 }
  ]
})

const loadStats = async (): Promise<void> => {
  try {
    const res = await getDashboardStats({ silent: true, quiet: true })
    stats.value = res.data
    recentOrders.value = res.data?.recentOrders ?? []
  } catch {
    // 首页统计是锦上添花：加载失败时保留上一次结果，不弹出错误打断用户
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
  padding: 40rpx 24rpx 40rpx;
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
