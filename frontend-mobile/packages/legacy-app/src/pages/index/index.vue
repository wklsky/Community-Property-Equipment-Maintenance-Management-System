<template>
  <view class="home-page">
    <!-- Custom Header with Greeting -->
    <view class="home-header">
      <view class="header-content">
        <view class="greeting-section">
          <text class="greeting-text">{{ greeting }}，</text>
          <text class="user-name">{{ userInfo.username }}</text>
        </view>
        <text class="greeting-subtitle">{{ currentDate }}</text>
      </view>
      <view class="header-actions">
        <view class="notification-btn" @click="goToNotifications">
          <text class="notification-icon">🔔</text>
          <view class="notification-badge" v-if="stats.unread > 0">
            <text>{{ stats.unread > 99 ? '99+' : stats.unread }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Summary Cards - Hero Section -->
    <view class="summary-section">
      <view class="summary-card urgent" @click="goToOrders">
        <view class="summary-icon">
          <text>⚠️</text>
        </view>
        <view class="summary-content">
          <text class="summary-value">{{ stats.pending }}</text>
          <text class="summary-label">待处理工单</text>
        </view>
        <view class="summary-indicator">
          <view class="indicator-dot urgent"></view>
        </view>
      </view>

      <view class="summary-card active" @click="goToOrders">
        <view class="summary-icon">
          <text>🔧</text>
        </view>
        <view class="summary-content">
          <text class="summary-value">{{ stats.processing }}</text>
          <text class="summary-label">处理中</text>
        </view>
        <view class="summary-indicator">
          <view class="indicator-dot active"></view>
        </view>
      </view>
    </view>

    <!-- Order Status Chart -->
    <view class="chart-section">
      <view class="section-header">
        <text class="section-title">工单状态分布</text>
      </view>

      <view class="chart-card">
        <!-- 完成率环形图 -->
        <view class="completion-ring">
          <view class="ring-container">
            <view class="ring-outer" :style="{ background: 'conic-gradient(#0066FF 0deg, #0066FF ' + (completionRate * 3.6) + 'deg, #F1F5F9 ' + (completionRate * 3.6) + 'deg, #F1F5F9 360deg)' }">
              <view class="ring-inner">
                <text class="ring-value">{{ completionRate }}%</text>
                <text class="ring-label">完成率</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 状态柱状条 -->
        <view class="bar-chart">
          <view class="bar-item" v-for="item in orderStatusBars" :key="item.label">
            <view class="bar-label">
              <text class="bar-name">{{ item.label }}</text>
              <text class="bar-count">{{ item.count }}</text>
            </view>
            <view class="bar-track">
              <view class="bar-fill" :class="item.colorClass" :style="{ width: item.percent + '%' }"></view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Quick Actions -->
    <view class="actions-section">
      <view class="section-header">
        <text class="section-title">快捷操作</text>
      </view>

      <view class="action-grid">
        <view class="action-item" @click="goToCreate">
          <view class="action-icon-wrapper gradient-primary">
            <text class="action-icon">📝</text>
          </view>
          <text class="action-text">提交报修</text>
        </view>

        <view class="action-item" @click="goToOrders">
          <view class="action-icon-wrapper gradient-info">
            <text class="action-icon">📋</text>
          </view>
          <text class="action-text">我的工单</text>
        </view>

        <view class="action-item" @click="goToNotifications">
          <view class="action-icon-wrapper gradient-warning">
            <text class="action-icon">🔔</text>
          </view>
          <text class="action-text">消息通知</text>
        </view>

        <view class="action-item" @click="goToAddress">
          <view class="action-icon-wrapper gradient-success">
            <text class="action-icon">🏠</text>
          </view>
          <text class="action-text">我的房产</text>
        </view>
      </view>
    </view>

    <!-- Recent Orders -->
    <view class="orders-section">
      <view class="section-header">
        <text class="section-title">最近工单</text>
        <text class="section-action" @click="goToOrders">查看全部</text>
      </view>

      <view class="order-list">
        <view
          class="order-card"
          v-for="order in recentOrders"
          :key="order.id"
          @click="goToDetail(order.id)"
        >
          <view class="order-left">
            <view class="order-type-icon" :class="getOrderTypeClass(order.status)">
              <text>{{ getOrderIcon(order.status) }}</text>
            </view>
          </view>
          <view class="order-content">
            <view class="order-header">
              <text class="order-no">{{ order.orderNo }}</text>
              <view class="order-status-tag" :class="getStatusClass(order.status)">
                <text>{{ statusMap[order.status] }}</text>
              </view>
            </view>
            <text class="order-desc">{{ order.faultDesc }}</text>
            <view class="order-meta">
              <text class="order-address">📍 {{ order.address }}</text>
              <text class="order-time">{{ formatTime(order.createTime) }}</text>
            </view>
          </view>
          <view class="order-arrow">
            <text>›</text>
          </view>
        </view>

        <view class="empty-state" v-if="recentOrders.length === 0">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无工单记录</text>
          <view class="empty-action" @click="goToCreate">
            <text>立即报修</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Bottom Spacing for TabBar -->
    <view class="bottom-spacing"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyOrders } from '../../api/repair'
import { getUnreadCount } from '../../api/message'
import { getDashboardStats } from '../../api/dashboard'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo || {})

const stats = ref({
  pending: 0,
  processing: 0,
  completed: 0,
  unread: 0
})

const recentOrders = ref([])

const orderStats = ref({
  pendingAccept: 0,
  pendingAssign: 0,
  pending: 0,
  processing: 0,
  pendingEvaluate: 0,
  completed: 0,
  cancelled: 0,
  total: 0
})

const orderStatusBars = computed(() => {
  const items = [
    { label: '待受理', count: orderStats.value.pendingAccept, colorClass: 'bar-warning' },
    { label: '待派单', count: orderStats.value.pendingAssign, colorClass: 'bar-warning' },
    { label: '待处理', count: orderStats.value.pending, colorClass: 'bar-info' },
    { label: '处理中', count: orderStats.value.processing, colorClass: 'bar-processing' },
    { label: '待评价', count: orderStats.value.pendingEvaluate, colorClass: 'bar-accent' },
    { label: '已完成', count: orderStats.value.completed, colorClass: 'bar-success' },
    { label: '已取消', count: orderStats.value.cancelled, colorClass: 'bar-danger' }
  ]
  const max = Math.max(...items.map(i => i.count), 1)
  return items.map(item => ({
    ...item,
    percent: Math.round(item.count / max * 100)
  }))
})

const completionRate = computed(() => {
  const validTotal = orderStats.value.total - orderStats.value.cancelled
  if (validTotal <= 0) return 0
  return Math.round(orderStats.value.completed / validTotal * 100)
})

const statusMap = {
  0: '待受理',
  1: '待派单',
  2: '待处理',
  3: '处理中',
  4: '待评价',
  5: '已完成',
  6: '已取消'
}

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

const currentDate = computed(() => {
  const now = new Date()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${month}月${day}日 ${weekdays[now.getDay()]}`
})

const getStatusClass = (status) => {
  if ([0, 1, 2].includes(status)) return 'warning'
  if (status === 3) return 'info'
  if ([4, 5].includes(status)) return 'success'
  return 'danger'
}

const getOrderTypeClass = (status) => {
  if ([0, 1, 2].includes(status)) return 'pending'
  if (status === 3) return 'processing'
  if ([4, 5].includes(status)) return 'completed'
  return 'cancelled'
}

const getOrderIcon = (status) => {
  if ([0, 1, 2].includes(status)) return '⏳'
  if (status === 3) return '🔧'
  if ([4, 5].includes(status)) return '✅'
  return '❌'
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

  return `${date.getMonth() + 1}/${date.getDate()}`
}

onShow(async () => {
  await loadData()
})

const loadData = async () => {
  try {
    const [ordersRes, unreadRes, dashboardRes] = await Promise.all([
      getMyOrders({ pageNum: 1, pageSize: 5 }),
      getUnreadCount(),
      getDashboardStats()
    ])

    recentOrders.value = ordersRes.data?.records || []
    stats.value.unread = unreadRes.data || 0

    if (dashboardRes.data) {
      const d = dashboardRes.data
      stats.value.pending = (d.orderStats?.pendingAccept || 0) +
                           (d.orderStats?.pendingAssign || 0) +
                           (d.orderStats?.pending || 0)
      stats.value.processing = d.orderStats?.processing || 0
      stats.value.completed = d.orderStats?.completed || 0

      if (d.orderStats) {
        orderStats.value = {
          pendingAccept: d.orderStats.pendingAccept || 0,
          pendingAssign: d.orderStats.pendingAssign || 0,
          pending: d.orderStats.pending || 0,
          processing: d.orderStats.processing || 0,
          pendingEvaluate: d.orderStats.pendingEvaluate || 0,
          completed: d.orderStats.completed || 0,
          cancelled: d.orderStats.cancelled || 0,
          total: d.orderStats.total || 0
        }
      }

      if (d.recentOrders && d.recentOrders.length > 0) {
        recentOrders.value = d.recentOrders.map(o => ({
          id: o.id,
          orderNo: o.orderNo,
          faultDesc: o.faultDesc,
          address: o.address,
          status: o.status,
          createTime: o.createTime
        }))
      }
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

const goToCreate = () => uni.navigateTo({ url: '/pages/repair/create' })
const goToOrders = () => uni.switchTab({ url: '/pages/repair/list' })
const goToNotifications = () => uni.switchTab({ url: '/pages/notice/list' })
const goToAddress = () => uni.navigateTo({ url: '/pages/address/list' })
const goToDetail = (id) => uni.navigateTo({ url: `/pages/repair/detail?id=${id}` })
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.home-header {
  background: linear-gradient(135deg, #0066FF 0%, #0052CC 100%);
  padding: 60rpx 32rpx 80rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
}

.home-header::after {
  content: '';
  position: absolute;
  bottom: -40rpx;
  left: 0;
  right: 0;
  height: 80rpx;
  background: #F8FAFC;
  border-radius: 40rpx 40rpx 0 0;
}

.header-content {
  flex: 1;
}

.greeting-section {
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
}

.greeting-text {
  font-size: 32rpx;
  color: rgba(255, 255, 255, 0.8);
}

.user-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #FFFFFF;
}

.greeting-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.6);
}

.notification-btn {
  position: relative;
  width: 88rpx;
  height: 88rpx;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20rpx);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-icon {
  font-size: 40rpx;
}

.notification-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 36rpx;
  height: 36rpx;
  background: #EF4444;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
  border: 4rpx solid #0066FF;
}

.notification-badge text {
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.summary-section {
  display: flex;
  gap: 24rpx;
  padding: 0 32rpx;
  margin-top: -20rpx;
  position: relative;
  z-index: 10;
}

.summary-card {
  flex: 1;
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 28rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  border: 2rpx solid #F1F5F9;
}

.summary-card:active {
  transform: scale(0.98);
}

.summary-icon {
  width: 72rpx;
  height: 72rpx;
  background: #F8FAFC;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
}

.summary-content {
  flex: 1;
}

.summary-value {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #1E293B;
  line-height: 1.2;
}

.summary-label {
  display: block;
  font-size: 24rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.summary-indicator {
  padding: 8rpx;
}

.indicator-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
}

.indicator-dot.urgent {
  background: #F59E0B;
  box-shadow: 0 0 12rpx rgba(245, 158, 11, 0.5);
  animation: pulse 2s infinite;
}

.indicator-dot.active {
  background: #0066FF;
  box-shadow: 0 0 12rpx rgba(0, 102, 255, 0.5);
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1E293B;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  width: 8rpx;
  height: 32rpx;
  background: #0066FF;
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.section-action {
  font-size: 26rpx;
  color: #0066FF;
  font-weight: 500;
}

.chart-section {
  padding: 40rpx 32rpx 0;
}

.chart-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
  display: flex;
  gap: 32rpx;
}

.completion-ring {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-container {
  width: 180rpx;
  height: 180rpx;
}

.ring-outer {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx;
}

.ring-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #FFFFFF;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.ring-value {
  font-size: 40rpx;
  font-weight: 700;
  color: #0066FF;
  line-height: 1.2;
}

.ring-label {
  font-size: 22rpx;
  color: #94A3B8;
}

.bar-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  min-width: 0;
}

.bar-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.bar-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bar-name {
  font-size: 24rpx;
  color: #64748B;
}

.bar-count {
  font-size: 24rpx;
  font-weight: 600;
  color: #1E293B;
}

.bar-track {
  width: 100%;
  height: 12rpx;
  background: #F1F5F9;
  border-radius: 6rpx;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.6s ease;
  min-width: 8rpx;
}

.bar-warning { background: #F59E0B; }
.bar-info { background: #3B82F6; }
.bar-processing { background: #0066FF; }
.bar-accent { background: #8B5CF6; }
.bar-success { background: #10B981; }
.bar-danger { background: #EF4444; }

.actions-section {
  padding: 40rpx 32rpx 0;
}

.action-grid {
  display: flex;
  justify-content: space-between;
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 32rpx 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.action-item:active {
  opacity: 0.7;
}

.action-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon-wrapper.gradient-primary {
  background: linear-gradient(135deg, #0066FF 0%, #0052CC 100%);
  box-shadow: 0 8rpx 24rpx rgba(0, 102, 255, 0.3);
}

.action-icon-wrapper.gradient-info {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.3);
}

.action-icon-wrapper.gradient-warning {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
  box-shadow: 0 8rpx 24rpx rgba(245, 158, 11, 0.3);
}

.action-icon-wrapper.gradient-success {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  box-shadow: 0 8rpx 24rpx rgba(16, 185, 129, 0.3);
}

.action-icon {
  font-size: 40rpx;
}

.action-text {
  font-size: 24rpx;
  color: #64748B;
  font-weight: 500;
}

.orders-section {
  padding: 40rpx 32rpx 0;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.order-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
}

.order-card:active {
  transform: scale(0.98);
  background: #F8FAFC;
}

.order-left {
  flex-shrink: 0;
}

.order-type-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.order-type-icon.pending {
  background: rgba(245, 158, 11, 0.1);
}

.order-type-icon.processing {
  background: rgba(59, 130, 246, 0.1);
}

.order-type-icon.completed {
  background: rgba(16, 185, 129, 0.1);
}

.order-type-icon.cancelled {
  background: rgba(239, 68, 68, 0.1);
}

.order-content {
  flex: 1;
  min-width: 0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.order-no {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}

.order-status-tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.order-status-tag.warning {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.order-status-tag.info {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.order-status-tag.success {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.order-status-tag.danger {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.order-desc {
  display: block;
  font-size: 26rpx;
  color: #64748B;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-address {
  font-size: 22rpx;
  color: #94A3B8;
}

.order-time {
  font-size: 22rpx;
  color: #94A3B8;
}

.order-arrow {
  flex-shrink: 0;
  color: #CBD5E1;
  font-size: 40rpx;
  font-weight: 300;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  border: 2rpx solid #F1F5F9;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #94A3B8;
  margin-bottom: 32rpx;
}

.empty-action {
  background: linear-gradient(135deg, #0066FF 0%, #0052CC 100%);
  padding: 20rpx 48rpx;
  border-radius: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 102, 255, 0.3);
}

.empty-action text {
  color: #FFFFFF;
  font-size: 28rpx;
  font-weight: 600;
}

.bottom-spacing {
  height: 40rpx;
}
</style>
