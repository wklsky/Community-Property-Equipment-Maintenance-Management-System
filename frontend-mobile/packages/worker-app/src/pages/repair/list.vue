<template>
  <view class="page">
    <view class="tabs">
      <view
        v-for="tab in TABS"
        :key="tab.label"
        class="tab-item"
        :class="{ active: activeTab === tab.label }"
        @click="switchTab(tab)"
      >
        {{ tab.label }}
      </view>
    </view>

    <view v-if="error" class="empty">
      <text class="empty-text">{{ error }}，下拉可重试</text>
    </view>

    <view v-else-if="isEmpty" class="empty">
      <text class="empty-text">暂无相关工单</text>
    </view>

    <view v-else class="card-list">
      <view
        v-for="order in list"
        :key="order.id"
        class="order-card"
        @click="goDetail(order.id)"
      >
        <view class="card-header">
          <text class="order-no">{{ order.orderNo }}</text>
          <text class="status" :style="{ color: statusColor(order.status) }">
            {{ resolveStatusName(REPAIR_STATUS_NAME, order.status) }}
          </text>
        </view>

        <text class="fault">{{ order.faultDesc }}</text>

        <view class="card-footer">
          <text class="address">{{ order.address }}</text>
          <text class="time">{{ formatDateTime(order.createTime) }}</text>
        </view>
      </view>
    </view>

    <view v-if="loading || loadingMore" class="tip">加载中...</view>
    <view v-else-if="finished" class="tip">没有更多了</view>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/repair/list.vue
 * @Description: 维修工端工单列表，只展示派给当前维修工的工单
 */

import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import {
  REPAIR_STATUS,
  REPAIR_STATUS_NAME,
  formatDateTime,
  getAssignedOrders,
  resolveStatusName,
  usePagedList,
  type RepairOrder
} from '@community/shared'

/**
 * 维修工视角的分组。
 * 派单链路是 待处理(2) → 处理中(3) → 已完成(5)，
 * 待受理(0)/待派单(1) 属于管理员未派单阶段，维修工在此阶段无可执行动作，故不单列。
 *
 * 这里不提供"我提交的"分组：后端 create 接口仅对系统管理员与业主开放，
 * 维修工根本无法创建工单，该分组会永远为空，只会让用户以为功能坏了
 */
const TABS = [
  { label: '待处理', statuses: [REPAIR_STATUS.PENDING_PROCESS, REPAIR_STATUS.PROCESSING] },
  { label: '已完成', statuses: [REPAIR_STATUS.COMPLETED] },
  { label: '全部', statuses: [] as number[] }
] as const

const STATUS_COLOR: Record<number, string> = {
  [REPAIR_STATUS.PENDING_ACCEPT]: '#f59e0b',
  [REPAIR_STATUS.PENDING_ASSIGN]: '#f59e0b',
  [REPAIR_STATUS.PENDING_PROCESS]: '#0066ff',
  [REPAIR_STATUS.PROCESSING]: '#0066ff',
  [REPAIR_STATUS.PENDING_EVALUATE]: '#8b5cf6',
  [REPAIR_STATUS.COMPLETED]: '#10b981',
  [REPAIR_STATUS.CANCELLED]: '#94a3b8',
  [REPAIR_STATUS.TRANSFERRING]: '#ef4444'
}

const activeTab = ref<string>('待处理')

const { list, loading, loadingMore, finished, isEmpty, error, loadMore, setQuery, refresh } = usePagedList<
  RepairOrder,
  { statuses?: number[] }
>({
  fetcher: getAssignedOrders,
  baseQuery: { statuses: [REPAIR_STATUS.PENDING_PROCESS, REPAIR_STATUS.PROCESSING] }
})

const switchTab = (tab: (typeof TABS)[number]): void => {
  activeTab.value = tab.label
  void setQuery({ statuses: [...tab.statuses] })
}

const statusColor = (status: number): string => STATUS_COLOR[status] ?? '#64748b'

const goDetail = (id: number): void => {
  uni.navigateTo({ url: `/pages/repair/detail?id=${id}` })
}

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
  padding-bottom: 32rpx;
}

.tabs {
  display: flex;
  background: #ffffff;
  padding: 0 16rpx;
  border-bottom: 2rpx solid #f1f5f9;
  position: sticky;
  top: 0;
  z-index: 10;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 28rpx 0;
  font-size: 28rpx;
  color: #64748b;
}

.tab-item.active {
  color: #0066ff;
  font-weight: 600;
  border-bottom: 4rpx solid #0066ff;
}

.empty {
  padding: 160rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #94a3b8;
}

.card-list {
  padding: 24rpx;
}

.order-card {
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

.order-no {
  font-size: 26rpx;
  color: #94a3b8;
}

.status {
  font-size: 26rpx;
  font-weight: 600;
}

.fault {
  display: block;
  font-size: 30rpx;
  color: #1e293b;
  line-height: 1.5;
  margin-bottom: 20rpx;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address {
  flex: 1;
  font-size: 24rpx;
  color: #64748b;
  margin-right: 16rpx;
}

.time {
  font-size: 24rpx;
  color: #94a3b8;
}

.tip {
  text-align: center;
  padding: 24rpx 0;
  font-size: 24rpx;
  color: #94a3b8;
}
</style>
