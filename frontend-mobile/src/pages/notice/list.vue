<template>
  <view class="notification-page">
    <!-- Custom Header -->
    <view class="page-header">
      <text class="header-title">消息通知</text>
      <view class="header-right" @click="markAllRead" v-if="activeTab === 'messages' && hasUnread">
        <text class="mark-all">全部已读</text>
      </view>
    </view>

    <!-- Tab Switcher -->
    <view class="tab-container">
      <view class="tab-wrapper">
        <view
          class="tab-item"
          :class="{ active: activeTab === 'notices' }"
          @click="switchTab('notices')"
        >
          <text class="tab-text">公告通知</text>
          <view class="tab-indicator" v-if="activeTab === 'notices'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'messages' }"
          @click="switchTab('messages')"
        >
          <text class="tab-text">消息提醒</text>
          <view class="tab-count" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
          <view class="tab-indicator" v-if="activeTab === 'messages'"></view>
        </view>
      </view>
    </view>

    <!-- Notice List -->
    <scroll-view
      v-if="activeTab === 'notices'"
      scroll-y
      class="content-scroll"
      @scrolltolower="loadMoreNotices"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="notice-list">
        <view class="notice-card" v-for="notice in notices" :key="notice.id" @click="goToNoticeDetail(notice)">
          <view class="notice-card-header">
            <view class="notice-icon-wrap">
              <text class="notice-icon">📢</text>
            </view>
            <view class="notice-card-content">
              <text class="notice-card-title">{{ notice.title }}</text>
              <text class="notice-card-time">{{ notice.createTime }}</text>
            </view>
            <view class="notice-card-arrow">
              <text>›</text>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="notices.length === 0 && !loadingNotices">
        <view class="empty-icon">📢</view>
        <text class="empty-title">暂无公告</text>
        <text class="empty-desc">社区公告和通知将显示在这里</text>
      </view>

      <view class="loading-state" v-if="loadingNotices">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <view class="load-more" v-if="notices.length > 0 && !hasMoreNotices && !loadingNotices">
        <text>— 已加载全部 —</text>
      </view>
    </scroll-view>

    <!-- Message List -->
    <scroll-view
      v-if="activeTab === 'messages'"
      scroll-y
      class="content-scroll"
      @scrolltolower="loadMoreMessages"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="message-list">
        <view
          class="message-item"
          v-for="msg in messages"
          :key="msg.id"
          :class="{ unread: msg.isRead === 0 }"
          @click="handleMessageClick(msg)"
        >
          <view class="message-icon" :class="getMessageTypeClass(msg.type)">
            <text>{{ getMessageIcon(msg.type) }}</text>
          </view>

          <view class="message-content">
            <view class="message-header">
              <text class="message-type-label">{{ getMessageTypeLabel(msg.type) }}</text>
              <view class="unread-dot" v-if="msg.isRead === 0"></view>
            </view>
            <text class="message-text">{{ msg.content }}</text>
            <text class="message-time">{{ formatTime(msg.createTime) }}</text>
          </view>

          <view class="message-arrow" v-if="canNavigate(msg)">
            <text>›</text>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="messages.length === 0 && !loadingMessages">
        <view class="empty-icon">📭</view>
        <text class="empty-title">暂无消息</text>
        <text class="empty-desc">工单进度、巡检任务等消息将显示在这里</text>
      </view>

      <view class="loading-state" v-if="loadingMessages">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <view class="load-more" v-if="messages.length > 0 && !hasMoreMessages && !loadingMessages">
        <text>— 已加载全部 —</text>
      </view>

      <view class="bottom-spacing"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getNotices } from '../../api/notice'
import { getMessages, markMessageRead, getUnreadCount } from '../../api/message'

const activeTab = ref('notices')

const notices = ref([])
const messages = ref([])
const loadingNotices = ref(false)
const loadingMessages = ref(false)
const refreshing = ref(false)
const noticePageNum = ref(1)
const messagePageNum = ref(1)
const hasMoreNotices = ref(true)
const hasMoreMessages = ref(true)
const unreadCount = ref(0)

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
  return `${month}月${day}日`
}

onShow(() => {
  loadData()
})

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'messages' && messages.value.length === 0) {
    loadMessages()
  } else if (tab === 'notices' && notices.value.length === 0) {
    loadNotices()
  }
}

const loadData = async () => {
  if (activeTab.value === 'notices') {
    await loadNotices()
  } else {
    await loadMessages()
  }
  loadUnreadCount()
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) {
    console.error('加载未读消息数失败', e)
  }
}

const loadNotices = async (reset = true) => {
  if (reset) {
    noticePageNum.value = 1
    hasMoreNotices.value = true
  }
  if (!hasMoreNotices.value) return

  loadingNotices.value = true
  try {
    const res = await getNotices({ pageNum: noticePageNum.value, pageSize: 10 })

    const records = res.data?.records || []
    const total = res.data?.total || 0

    if (reset) {
      notices.value = records
    } else {
      notices.value = [...notices.value, ...records]
    }

    hasMoreNotices.value = notices.value.length < total
    noticePageNum.value++
  } catch (e) {
    console.error('加载公告列表失败', e)
  } finally {
    loadingNotices.value = false
    refreshing.value = false
  }
}

const loadMessages = async (reset = true) => {
  if (reset) {
    messagePageNum.value = 1
    hasMoreMessages.value = true
  }
  if (!hasMoreMessages.value) return

  loadingMessages.value = true
  try {
    const res = await getMessages({ pageNum: messagePageNum.value, pageSize: 20 })

    const records = res.data?.records || []
    const total = res.data?.total || 0

    if (reset) {
      messages.value = records
    } else {
      messages.value = [...messages.value, ...records]
    }

    hasMoreMessages.value = messages.value.length < total
    messagePageNum.value++
  } catch (e) {
    console.error('加载消息列表失败', e)
  } finally {
    loadingMessages.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  refreshing.value = true
  loadData()
}

const loadMoreNotices = () => {
  if (!loadingNotices.value && hasMoreNotices.value) {
    loadNotices(false)
  }
}

const loadMoreMessages = () => {
  if (!loadingMessages.value && hasMoreMessages.value) {
    loadMessages(false)
  }
}

const goToNoticeDetail = (notice) => {
  uni.navigateTo({ url: `/pages/notice/detail?id=${notice.id}` })
}

const handleMessageClick = async (msg) => {
  if (msg.isRead === 0) {
    try {
      await markMessageRead(msg.id)
      msg.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {
      console.error('标记已读失败', e)
    }
  }

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
        switchTab('notices')
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

          messages.value.forEach(msg => { msg.isRead = 1 })
          unreadCount.value = 0

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
.notification-page {
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

.tab-container {
  background: #FFFFFF;
  padding: 0 32rpx;
  border-bottom: 2rpx solid #F1F5F9;
}

.tab-wrapper {
  display: flex;
  gap: 48rpx;
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
  transition: color 0.2s ease;
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
}

.tab-count text {
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

.content-scroll {
  flex: 1;
  padding: 24rpx 32rpx;
}

/* Notice Cards */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.notice-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
}

.notice-card:active {
  transform: scale(0.98);
  background: #FAFBFC;
}

.notice-card-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.notice-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  background: rgba(245, 158, 11, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
}

.notice-card-content {
  flex: 1;
  min-width: 0;
}

.notice-card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-card-time {
  font-size: 24rpx;
  color: #94A3B8;
}

.notice-card-arrow {
  flex-shrink: 0;
  color: #CBD5E1;
  font-size: 40rpx;
  font-weight: 300;
}

/* Message Items */
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
}

.message-item:active {
  transform: scale(0.98);
  background: #FAFBFC;
}

.message-item.unread {
  background: #F0F9FF;
  border-color: #BAE6FD;
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

.message-icon.order { background: rgba(59, 130, 246, 0.1); }
.message-icon.notice { background: rgba(245, 158, 11, 0.1); }
.message-icon.system { background: rgba(100, 116, 139, 0.1); }
.message-icon.inspection { background: rgba(16, 185, 129, 0.1); }

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
  font-size: 24rpx;
  color: #0066FF;
  background: rgba(0, 102, 255, 0.1);
  padding: 4rpx 14rpx;
  border-radius: 10rpx;
  font-weight: 500;
}

.unread-dot {
  width: 14rpx;
  height: 14rpx;
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
  font-size: 28rpx;
  color: #1E293B;
  line-height: 1.5;
  margin-bottom: 10rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-time {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
}

.message-arrow {
  flex-shrink: 0;
  color: #CBD5E1;
  font-size: 40rpx;
  font-weight: 300;
  align-self: center;
}

/* Empty / Loading / Load More */
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
