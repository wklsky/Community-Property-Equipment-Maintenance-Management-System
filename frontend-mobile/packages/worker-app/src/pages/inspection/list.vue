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
      <text class="empty-text">暂无相关巡检任务</text>
    </view>

    <view v-else class="card-list">
      <view v-for="task in list" :key="task.id" class="task-card">
        <view class="card-header">
          <text class="plan-name">{{ task.planName }}</text>
          <text class="status" :style="{ color: statusColor(task.status) }">
            {{ resolveStatusName(INSPECTION_TASK_STATUS_NAME, task.status) }}
          </text>
        </view>

        <view class="meta-row">
          <text class="meta">任务日期：{{ formatDate(task.taskDate) || '—' }}</text>
          <text class="meta">设备ID：{{ task.deviceId || '—' }}</text>
        </view>

        <view class="actions">
          <text
            v-if="task.status === INSPECTION_TASK_STATUS.PENDING"
            class="action"
            @click="acceptTask(task.id)"
          >接单</text>
          <text
            v-if="task.status === INSPECTION_TASK_STATUS.PROCESSING"
            class="action"
            @click="goExecute(task.id)"
          >执行巡检</text>
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
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/inspection/list.vue
 * @Description: 维修工端巡检任务列表，含指派任务与可抢的公共任务
 */

import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import {
  INSPECTION_TASK_STATUS,
  INSPECTION_TASK_STATUS_NAME,
  acceptInspectionTask,
  formatDate,
  getMyInspectionTasks,
  resolveStatusName,
  usePagedList,
  type InspectionTask
} from '@community/shared'

/**
 * /inspections/tasks/my 的口径是「派给我 或 未指派」，
 * 因此待接单一栏既包含指派给自己的任务，也包含抢单池里的公共任务
 */
const TABS = [
  { label: '待接单', status: INSPECTION_TASK_STATUS.PENDING },
  { label: '进行中', status: INSPECTION_TASK_STATUS.PROCESSING },
  { label: '已完成', status: INSPECTION_TASK_STATUS.COMPLETED }
] as const

const STATUS_COLOR: Record<number, string> = {
  [INSPECTION_TASK_STATUS.PENDING]: '#f59e0b',
  [INSPECTION_TASK_STATUS.PROCESSING]: '#0066ff',
  [INSPECTION_TASK_STATUS.COMPLETED]: '#10b981'
}

const activeTab = ref<string>('待接单')

const { list, loading, loadingMore, finished, isEmpty, error, loadMore, setQuery, refresh } = usePagedList<
  InspectionTask,
  { status?: number | null }
>({
  fetcher: getMyInspectionTasks,
  baseQuery: { status: INSPECTION_TASK_STATUS.PENDING }
})

const switchTab = (tab: (typeof TABS)[number]): void => {
  activeTab.value = tab.label
  void setQuery({ status: tab.status })
}

const statusColor = (status: number): string => STATUS_COLOR[status] ?? '#64748b'

const acceptTask = async (id: number): Promise<void> => {
  try {
    await acceptInspectionTask(id, { silent: true })
    uni.showToast({ title: '接单成功', icon: 'success' })
    await refresh()
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '接单失败',
      icon: 'none'
    })
  }
}

const goExecute = (id: number): void => {
  uni.navigateTo({ url: `/pages/inspection/execute?id=${id}` })
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

.task-card {
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

.plan-name {
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
  margin-bottom: 16rpx;
}

.meta {
  font-size: 25rpx;
  color: #64748b;
}

.actions {
  display: flex;
  justify-content: flex-end;
  border-top: 2rpx solid #f8fafc;
  padding-top: 20rpx;
}

.action {
  margin-left: 32rpx;
  font-size: 27rpx;
  color: #0066ff;
}

.tip {
  text-align: center;
  padding: 24rpx 0;
  font-size: 24rpx;
  color: #94a3b8;
}
</style>
