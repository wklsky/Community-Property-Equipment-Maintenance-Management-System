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
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/pages/repair/list.vue
 * @Description: 业主端「我的工单」列表，按业主视角合并后端状态码分组展示
 */

import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import {
  REPAIR_STATUS,
  REPAIR_STATUS_NAME,
  formatDateTime,
  getMyOrders,
  resolveStatusName,
  usePagedList,
  type RepairOrder
} from '@community/shared'

/**
 * 业主视角的状态分组。
 * 后端 8 种状态码对业主而言过细：待派单(1)与待受理(0)都只是"物业还没处理"，
 * 待处理(2)与处理中(3)都是"维修工已介入"，合并展示才能减少无意义的空分组
 */
const TABS = [
  { label: '全部', statuses: [] as number[] },
  { label: '待受理', statuses: [REPAIR_STATUS.PENDING_ACCEPT, REPAIR_STATUS.PENDING_ASSIGN] },
  { label: '处理中', statuses: [REPAIR_STATUS.PENDING_PROCESS, REPAIR_STATUS.PROCESSING] },
  { label: '待评价', statuses: [REPAIR_STATUS.PENDING_EVALUATE] },
  { label: '已完成', statuses: [REPAIR_STATUS.COMPLETED] }
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

const activeTab = ref<string>('全部')

const { list, loading, loadingMore, finished, isEmpty, error, loadMore, setQuery, refresh } = usePagedList<
  RepairOrder,
  { statuses?: number[] }
>({
  fetcher: getMyOrders,
  baseQuery: { statuses: [] }
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
