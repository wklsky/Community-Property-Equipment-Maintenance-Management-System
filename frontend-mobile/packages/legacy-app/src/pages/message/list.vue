<template>
  <view class="message-list-page">
    <!-- Page Header -->
    <view class="page-header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="header-title">消息中心</text>
      <view class="header-right" @click="markAllRead" v-if="hasUnread">
        <text class="mark-all">全部已读</text>
      </view>
      <view class="header-right" v-else></view>
    </view>

    <!-- Message List -->
    <scroll-view
      scroll-y
      class="message-scroll"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="message-list">
        <view
          class="message-item"
          v-for="msg in messages"
          :key="msg.id"
          :class="{ unread: msg.isRead === 0, read: msg.isRead === 1 }"
          @click="handleMessageClick(msg)"
        >
          <!-- Message Icon -->
          <view class="message-icon" :class="getMessageTypeClass(msg.type)">
            <text>{{ getMessageIcon(msg.type) }}</text>
          </view>

          <!-- Message Content -->
          <view class="message-content">
            <view class="message-header">
              <text class="message-type-label">{{ getMessageTypeLabel(msg.type) }}</text>
              <view class="unread-dot" v-if="msg.isRead === 0"></view>
            </view>
            <text class="message-text">{{ msg.content }}</text>
            <text class="message-time">{{ formatTime(msg.createTime) }}</text>
          </view>

          <!-- Arrow -->
          <view class="message-arrow" v-if="canNavigate(msg)">
            <text>›</text>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view class="empty-state" v-if="messages.length === 0 && !loading">
        <view class="empty-icon">📭</view>
        <text class="empty-title">暂无消息</text>
        <text class="empty-desc">新的消息通知将显示在这里</text>
      </view>

      <!-- Loading State -->
      <view class="loading-state" v-if="loading">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- Load More -->
      <view class="load-more" v-if="messages.length > 0 && !hasMore && !loading">
        <text>— 已加载全部 —</text>
      </view>

      <!-- Bottom Spacing -->
      <view class="bottom-spacing"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMessages, markMessageRead } from '../../api/message'

const messages = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const hasMore = ref(true)

const hasUnread = computed(() => {
  return messages.value.some(msg => msg.isRead === 0)
})

const messageTypeMap = {
  'ORDER': { label: '工单通知', icon: '🔧', class: 'order' },
  'NOTICE': { label: '公告通知', icon: '📢', class: 'notice' },
  'SYSTEM': { label: '系统消息', icon: '⚙️', class: 'system' },
  'INSPECTION': { label: '巡检通知', icon: '🔍', class: 'inspection' }
}

const getMessageTypeLabel = (type) => {
  return messageTypeMap[type]?.label || '系统消息'
}

const getMessageIcon = (type) => {
  return messageTypeMap[type]?.icon || '📬'
}

const getMessageTypeClass = (type) => {
  return messageTypeMap[type]?.class || 'system'
}

const canNavigate = (msg) => {
  return msg.type === 'ORDER' || msg.type === 'NOTICE' || msg.type === 'INSPECTION'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
}

onShow(() => {
  loadData()
})

const loadData = async (reset = true) => {
  if (reset) {
    pageNum.value = 1
    hasMore.value = true
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await getMessages({ pageNum: pageNum.value, pageSize: 20 })

    const records = res.data?.records || []
    const total = res.data?.total || 0

    if (reset) {
      messages.value = records
    } else {
      messages.value = [...messages.value, ...records]
    }

    hasMore.value = messages.value.length < total
    pageNum.value++
  } catch (e) {
    console.error('加载消息列表失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  refreshing.value = true
  loadData()
}

const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadData(false)
  }
}

const goBack = () => {
  uni.navigateBack()
}

const handleMessageClick = async (msg) => {

  if (msg.isRead === 0) {
    try {
      await markMessageRead(msg.id)
      msg.isRead = 1
    } catch (e) {
      console.error('标记已读失败', e)
    }
  }

  navigateByType(msg)
}

const navigateByType = (msg) => {
  switch (msg.type) {
    case 'ORDER':

      if (msg.relatedId) {
        uni.navigateTo({ url: `/pages/repair/detail?id=${msg.relatedId}` })
      } else {
        uni.switchTab({ url: '/pages/repair/list' })
      }
      break

    case 'NOTICE':

      if (msg.relatedId) {
        uni.navigateTo({ url: `/pages/notice/detail?id=${msg.relatedId}` })
      } else {
        uni.switchTab({ url: '/pages/notice/list' })
      }
      break

    case 'INSPECTION':

      if (msg.relatedId) {
        uni.navigateTo({ url: `/pages/inspection/execute?id=${msg.relatedId}` })
      } else {
        uni.navigateTo({ url: '/pages/inspection/list' })
      }
      break

    default:

      break
  }
}

const markAllRead = async () => {
  uni.showModal({
    title: '确认',
    content: '确定将所有消息标记为已读吗？',
    confirmColor: '#0066FF',
    success: async (res) => {
      if (res.confirm) {
        try {

          const unreadMessages = messages.value.filter(msg => msg.isRead === 0)
          await Promise.all(unreadMessages.map(msg => markMessageRead(msg.id)))

          messages.value.forEach(msg => {
            msg.isRead = 1
          })

          uni.showToast({ title: '已全部标记为已读', icon: 'success' })
        } catch (e) {
          console.error('批量标记已读失败', e)
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style scoped>
.message-list-page {
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
  position: sticky;
  top: 0;
  z-index: 100;
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
  min-width: 60rpx;
}

.mark-all {
  font-size: 26rpx;
  color: #0066FF;
  font-weight: 500;
}

.message-scroll {
  flex: 1;
  padding: 24rpx 32rpx;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
  transition: all 0.2s ease;
}

.message-item:active {
  transform: scale(0.98);
  background: #FAFBFC;
}

.message-item.unread {
  background: #F0F9FF;
  border-color: #BAE6FD;
}

.message-item.read {
  opacity: 0.8;
}

.message-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
}

.message-icon.order {
  background: rgba(59, 130, 246, 0.1);
}

.message-icon.notice {
  background: rgba(245, 158, 11, 0.1);
}

.message-icon.system {
  background: rgba(100, 116, 139, 0.1);
}

.message-icon.inspection {
  background: rgba(16, 185, 129, 0.1);
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.message-type-label {
  font-size: 26rpx;
  color: #0066FF;
  background: rgba(0, 102, 255, 0.1);
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  font-weight: 500;
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  background: #EF4444;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

.message-text {
  display: block;
  font-size: 30rpx;
  color: #1E293B;
  line-height: 1.5;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-item.read .message-text {
  color: #64748B;
}

.message-time {
  display: block;
  font-size: 24rpx;
  color: #94A3B8;
}

.message-arrow {
  flex-shrink: 0;
  color: #CBD5E1;
  font-size: 40rpx;
  font-weight: 300;
  align-self: center;
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
