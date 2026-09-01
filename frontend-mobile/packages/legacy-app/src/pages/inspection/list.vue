<template>
  <view class="inspection-list-page">
    <!-- Page Header -->
    <view class="page-header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="header-title">巡检任务</text>
      <view class="header-right"></view>
    </view>

    <!-- Tab Switcher -->
    <view class="tab-container">
      <view class="tab-wrapper">
        <view
          class="tab-item"
          :class="{ active: activeTab === 'pending' }"
          @click="switchTab('pending')"
        >
          <text class="tab-text">待接单</text>
          <view class="tab-count" v-if="tabCounts.pending > 0">{{ tabCounts.pending }}</view>
          <view class="tab-indicator" v-if="activeTab === 'pending'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'processing' }"
          @click="switchTab('processing')"
        >
          <text class="tab-text">进行中</text>
          <view class="tab-count" v-if="tabCounts.processing > 0">{{ tabCounts.processing }}</view>
          <view class="tab-indicator" v-if="activeTab === 'processing'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'completed' }"
          @click="switchTab('completed')"
        >
          <text class="tab-text">已完成</text>
          <view class="tab-indicator" v-if="activeTab === 'completed'"></view>
        </view>
      </view>
    </view>

    <!-- Task List -->
    <scroll-view
      scroll-y
      class="task-scroll"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="task-list">
        <view
          class="task-card"
          v-for="task in tasks"
          :key="task.id"
          @click="goToExecute(task)"
        >
          <!-- Task Header -->
          <view class="task-header">
            <view class="task-info">
              <text class="task-name">{{ task.planName || '巡检任务' }}</text>
              <view class="task-date">
                <text>📅 {{ formatDate(task.taskDate) }}</text>
              </view>
            </view>
            <view class="status-badge" :class="getStatusClass(task.status)">
              <text>{{ statusMap[task.status] }}</text>
            </view>
          </view>

          <!-- Task Body -->
          <view class="task-body">
            <view class="task-detail-item" v-if="task.buildingName">
              <text class="detail-icon">🏢</text>
              <text class="detail-text">{{ task.buildingName }}</text>
            </view>
            <view class="task-detail-item" v-if="task.categoryName">
              <text class="detail-icon">🔧</text>
              <text class="detail-text">{{ task.categoryName }}</text>
            </view>
          </view>

          <!-- Task Footer -->
          <view class="task-footer">
            <view class="task-time">
              <text class="time-icon">🕐</text>
              <text class="time-text">{{ formatTime(task.createTime) }}</text>
            </view>

            <view class="task-actions">
              <view
                class="action-btn accept"
                v-if="task.status === 0"
                @click.stop="handleAccept(task)"
              >
                <text>接单</text>
              </view>
              <view
                class="action-btn execute"
                v-if="task.status === 1"
                @click.stop="goToExecute(task)"
              >
                <text>执行巡检</text>
              </view>
              <view
                class="action-btn view"
                v-if="task.status === 2"
                @click.stop="goToExecute(task)"
              >
                <text>查看详情</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view class="empty-state" v-if="tasks.length === 0 && !loading">
        <view class="empty-icon">{{ getEmptyIcon() }}</view>
        <text class="empty-title">{{ getEmptyTitle() }}</text>
        <text class="empty-desc">{{ getEmptyDesc() }}</text>
      </view>

      <!-- Loading State -->
      <view class="loading-state" v-if="loading">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- Load More -->
      <view class="load-more" v-if="tasks.length > 0 && !hasMore && !loading">
        <text>— 已加载全部 —</text>
      </view>

      <!-- Bottom Spacing -->
      <view class="bottom-spacing"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyInspectionTasks, acceptInspectionTask } from '../../api/inspection'

const activeTab = ref('pending')
const tasks = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const hasMore = ref(true)

const tabCounts = reactive({
  pending: 0,
  processing: 0
})

const statusMap = {
  0: '待接单',
  1: '进行中',
  2: '已完成'
}

const getStatusClass = (status) => {
  if (status === 0) return 'pending'
  if (status === 1) return 'processing'
  return 'completed'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`

  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

const getEmptyIcon = () => {
  if (activeTab.value === 'pending') return '📋'
  if (activeTab.value === 'processing') return '🔍'
  return '✅'
}

const getEmptyTitle = () => {
  if (activeTab.value === 'pending') return '暂无待接单任务'
  if (activeTab.value === 'processing') return '暂无进行中任务'
  return '暂无已完成任务'
}

const getEmptyDesc = () => {
  if (activeTab.value === 'pending') return '当前没有需要接单的巡检任务'
  if (activeTab.value === 'processing') return '您还没有正在执行的巡检任务'
  return '完成的巡检任务将显示在这里'
}

onShow(() => {
  loadData()
  loadTabCounts()
})

const loadTabCounts = async () => {
  try {
    const [pendingRes, processingRes] = await Promise.all([
      getMyInspectionTasks({ pageNum: 1, pageSize: 1, status: 0 }),
      getMyInspectionTasks({ pageNum: 1, pageSize: 1, status: 1 })
    ])
    tabCounts.pending = pendingRes.data?.total || 0
    tabCounts.processing = processingRes.data?.total || 0
  } catch (e) {
    console.error('加载Tab数量失败', e)
  }
}

const loadData = async (reset = true) => {
  if (reset) {
    pageNum.value = 1
    hasMore.value = true
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: 10 }

    if (activeTab.value === 'pending') {
      params.status = 0
    } else if (activeTab.value === 'processing') {
      params.status = 1
    } else {
      params.status = 2
    }

    const res = await getMyInspectionTasks(params)

    const records = res.data?.records || []
    const total = res.data?.total || 0

    if (reset) {
      tasks.value = records
    } else {
      tasks.value = [...tasks.value, ...records]
    }

    hasMore.value = tasks.value.length < total
    pageNum.value++
  } catch (e) {
    console.error('加载巡检任务失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  refreshing.value = true
  loadData()
  loadTabCounts()
}

const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadData(false)
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  loadData()
}

const goBack = () => {
  uni.navigateBack()
}

const goToExecute = (task) => {
  uni.navigateTo({ url: `/pages/inspection/execute?id=${task.id}` })
}

const handleAccept = async (task) => {
  uni.showModal({
    title: '确认接单',
    content: '确定接受该巡检任务吗？',
    confirmColor: '#0066FF',
    success: async (res) => {
      if (res.confirm) {
        try {
          await acceptInspectionTask(task.id)
          uni.showToast({ title: '接单成功', icon: 'success' })

          const index = tasks.value.findIndex(t => t.id === task.id)
          if (index > -1) {
            tasks.value.splice(index, 1)
          }

          tabCounts.pending = Math.max(0, tabCounts.pending - 1)
          tabCounts.processing++

        } catch (e) {
          console.error('接单失败', e)
        }
      }
    }
  })
}
</script>

<style scoped>
.inspection-list-page {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}

.page-header {
  background: #FFFFFF;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2rpx solid #F1F5F9;
}

.header-left {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 48rpx;
  color: #1E293B;
  font-weight: 300;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1E293B;
}

.header-right {
  width: 60rpx;
}

.tab-container {
  background: #FFFFFF;
  padding: 0 32rpx;
  border-bottom: 2rpx solid #F1F5F9;
}

.tab-wrapper {
  display: flex;
  justify-content: space-around;
}

.tab-item {
  position: relative;
  padding: 28rpx 0;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.tab-text {
  font-size: 30rpx;
  color: #64748B;
  font-weight: 500;
}

.tab-item.active .tab-text {
  color: #0066FF;
  font-weight: 600;
}

.tab-count {
  min-width: 36rpx;
  height: 36rpx;
  background: #EF4444;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6rpx;
  background: #0066FF;
  border-radius: 3rpx;
}

.task-scroll {
  flex: 1;
  padding: 24rpx 32rpx;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.task-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
  transition: all 0.2s ease;
}

.task-card:active {
  transform: scale(0.98);
  background: #FAFBFC;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.task-info {
  flex: 1;
}

.task-name {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1E293B;
  margin-bottom: 8rpx;
}

.task-date {
  font-size: 24rpx;
  color: #64748B;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.status-badge.pending {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.status-badge.processing {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.status-badge.completed {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.task-body {
  margin-bottom: 20rpx;
}

.task-detail-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.task-detail-item:last-child {
  margin-bottom: 0;
}

.detail-icon {
  font-size: 24rpx;
}

.detail-text {
  font-size: 28rpx;
  color: #64748B;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 2rpx solid #F1F5F9;
}

.task-time {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.time-icon {
  font-size: 22rpx;
}

.time-text {
  font-size: 24rpx;
  color: #94A3B8;
}

.task-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  padding: 12rpx 28rpx;
  border-radius: 20rpx;
  font-size: 26rpx;
  font-weight: 600;
  transition: all 0.2s ease;
}

.action-btn:active {
  transform: scale(0.95);
}

.action-btn.accept {
  background: linear-gradient(135deg, #0066FF 0%, #0052CC 100%);
  box-shadow: 0 4rpx 16rpx rgba(0, 102, 255, 0.3);
}
.action-btn.accept text {
  color: #FFFFFF;
}

.action-btn.execute {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  box-shadow: 0 4rpx 16rpx rgba(16, 185, 129, 0.3);
}
.action-btn.execute text {
  color: #FFFFFF;
}

.action-btn.view {
  background: #F1F5F9;
}
.action-btn.view text {
  color: #64748B;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 32rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
  opacity: 0.5;
}

.empty-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #94A3B8;
  text-align: center;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx;
  gap: 16rpx;
}

.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #E2E8F0;
  border-top-color: #0066FF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 26rpx;
  color: #94A3B8;
}

.load-more {
  text-align: center;
  padding: 32rpx;
}

.load-more text {
  font-size: 24rpx;
  color: #CBD5E1;
}

.bottom-spacing {
  height: 60rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
