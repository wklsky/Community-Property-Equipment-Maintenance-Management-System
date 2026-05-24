<template>
  <view class="mine-page">
    <!-- Profile Header -->
    <view class="profile-header">
      <view class="header-bg">
        <view class="bg-pattern"></view>
      </view>

      <view class="profile-content">
        <view class="avatar-section">
          <view class="avatar-wrapper">
            <view class="avatar">
              <text class="avatar-text">{{ userInfo.username ? userInfo.username.charAt(0).toUpperCase() : 'U' }}</text>
            </view>
            <view class="avatar-badge">
              <text>✓</text>
            </view>
          </view>
        </view>

        <view class="user-info">
          <text class="user-name">{{ userInfo.username || '用户' }}</text>
          <view class="user-role-badge" :class="isWorker ? 'worker' : 'owner'">
            <text>{{ isWorker ? '维修工' : '业主' }}</text>
          </view>
        </view>

        <!-- 租户信息 -->
        <view class="tenant-info" v-if="tenantName">
          <text class="tenant-icon">🏢</text>
          <text class="tenant-name">{{ tenantName }}</text>
        </view>

        <!-- 业主统计 -->
        <view class="user-stats" v-if="!isWorker">
          <view class="stat-item" @click="goToOrders">
            <text class="stat-value">{{ ownerStats.total }}</text>
            <text class="stat-label">全部工单</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goToOrders">
            <text class="stat-value">{{ ownerStats.completed }}</text>
            <text class="stat-label">已完成</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goToNotifications">
            <text class="stat-value">{{ messageStats.unread }}</text>
            <text class="stat-label">未读消息</text>
          </view>
        </view>

        <!-- 维修工统计 -->
        <view class="user-stats worker-stats" v-if="isWorker">
          <view class="stat-item" @click="goToOrders">
            <text class="stat-value">{{ workerStats.pending }}</text>
            <text class="stat-label">待处理</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goToOrders">
            <text class="stat-value">{{ workerStats.completed }}</text>
            <text class="stat-label">已完成</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goToInspection">
            <text class="stat-value">{{ workerStats.inspection }}</text>
            <text class="stat-label">巡检任务</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Quick Actions - 业主 -->
    <view class="quick-section" v-if="!isWorker">
      <view class="quick-grid">
        <view class="quick-item" @click="goToOrders">
          <view class="quick-icon">
            <text>📋</text>
          </view>
          <text class="quick-text">我的工单</text>
        </view>
        <view class="quick-item" @click="goToNotifications">
          <view class="quick-icon">
            <text>🔔</text>
          </view>
          <text class="quick-text">消息通知</text>
          <view class="quick-badge" v-if="messageStats.unread > 0">
            <text>{{ messageStats.unread > 99 ? '99+' : messageStats.unread }}</text>
          </view>
        </view>
        <view class="quick-item" @click="goToAddress">
          <view class="quick-icon">
            <text>🏠</text>
          </view>
          <text class="quick-text">我的房产</text>
        </view>
        <view class="quick-item" @click="goToCreate">
          <view class="quick-icon">
            <text>📝</text>
          </view>
          <text class="quick-text">提交报修</text>
        </view>
      </view>
    </view>

    <!-- Quick Actions - 维修工 -->
    <view class="quick-section" v-if="isWorker">
      <view class="quick-grid">
        <view class="quick-item" @click="goToOrders">
          <view class="quick-icon worker">
            <text>🔧</text>
          </view>
          <text class="quick-text">工单中心</text>
          <view class="quick-badge" v-if="workerStats.pending > 0">
            <text>{{ workerStats.pending }}</text>
          </view>
        </view>
        <view class="quick-item" @click="goToInspection">
          <view class="quick-icon worker">
            <text>🔍</text>
          </view>
          <text class="quick-text">巡检任务</text>
          <view class="quick-badge" v-if="workerStats.inspection > 0">
            <text>{{ workerStats.inspection }}</text>
          </view>
        </view>
        <view class="quick-item" @click="goToNotifications">
          <view class="quick-icon worker">
            <text>🔔</text>
          </view>
          <text class="quick-text">消息通知</text>
          <view class="quick-badge" v-if="messageStats.unread > 0">
            <text>{{ messageStats.unread }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Menu List - 业主 -->
    <view class="menu-section" v-if="!isWorker">
      <view class="section-title">
        <text>我的服务</text>
      </view>
      <view class="menu-group">
        <view class="menu-item" @click="goToOrders">
          <view class="menu-left">
            <view class="menu-icon orders">
              <text>📋</text>
            </view>
            <text class="menu-text">报修记录</text>
          </view>
          <view class="menu-right">
            <text class="menu-value" v-if="ownerStats.total > 0">{{ ownerStats.total }}条</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>

        <view class="menu-item" @click="showPropertyInfo">
          <view class="menu-left">
            <view class="menu-icon property">
              <text>🏠</text>
            </view>
            <text class="menu-text">我的房产</text>
          </view>
          <view class="menu-right">
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Menu List - 维修工 -->
    <view class="menu-section" v-if="isWorker">
      <view class="section-title">
        <text>工作统计</text>
      </view>
      <view class="menu-group">
        <view class="menu-item" @click="goToOrders">
          <view class="menu-left">
            <view class="menu-icon orders">
              <text>📊</text>
            </view>
            <text class="menu-text">工单统计</text>
          </view>
          <view class="menu-right">
            <text class="menu-value">已完成 {{ workerStats.completed }} 单</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>

        <view class="menu-item" @click="goToInspection">
          <view class="menu-left">
            <view class="menu-icon inspection">
              <text>📝</text>
            </view>
            <text class="menu-text">巡检记录</text>
          </view>
          <view class="menu-right">
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Common Menu -->
    <view class="menu-section">
      <view class="section-title">
        <text>设置</text>
      </view>
      <view class="menu-group">
        <view class="menu-item" @click="showAccountInfo">
          <view class="menu-left">
            <view class="menu-icon account">
              <text>👤</text>
            </view>
            <text class="menu-text">账户信息</text>
          </view>
          <view class="menu-right">
            <text class="menu-arrow">›</text>
          </view>
        </view>

        <view class="menu-item" @click="showSettings">
          <view class="menu-left">
            <view class="menu-icon settings">
              <text>⚙️</text>
            </view>
            <text class="menu-text">系统设置</text>
          </view>
          <view class="menu-right">
            <text class="menu-arrow">›</text>
          </view>
        </view>

        <view class="menu-item" @click="showHelp">
          <view class="menu-left">
            <view class="menu-icon help">
              <text>❓</text>
            </view>
            <text class="menu-text">帮助与反馈</text>
          </view>
          <view class="menu-right">
            <text class="menu-arrow">›</text>
          </view>
        </view>

        <view class="menu-item" @click="showAbout">
          <view class="menu-left">
            <view class="menu-icon about">
              <text>ℹ️</text>
            </view>
            <text class="menu-text">关于我们</text>
          </view>
          <view class="menu-right">
            <text class="menu-value">v1.0.0</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Logout Button -->
    <view class="logout-section">
      <view class="logout-btn" @click="handleLogout">
        <text class="logout-icon">🚪</text>
        <text class="logout-text">退出登录</text>
      </view>
    </view>

    <!-- Footer -->
    <view class="footer">
      <text class="footer-text">智慧社区物业管理系统</text>
      <text class="footer-version">Version 1.0.0</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user'
import { getMyOrders, getAssignedOrders } from '../../api/repair'
import { getUnreadCount } from '../../api/message'
import { getMyInspectionTasks } from '../../api/inspection'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo || {})
const isWorker = computed(() => userStore.isWorker)
const tenantName = computed(() => userStore.tenantName)

const ownerStats = reactive({
  total: 0,
  completed: 0
})

const workerStats = reactive({
  pending: 0,
  completed: 0,
  inspection: 0
})

const messageStats = reactive({
  unread: 0
})

onShow(async () => {
  await loadStats()
})

const loadStats = async () => {
  try {

    const unreadRes = await getUnreadCount()
    messageStats.unread = unreadRes.data || 0

    if (isWorker.value) {

      const [pendingRes, completedRes, inspectionRes] = await Promise.all([
        getAssignedOrders({ pageNum: 1, pageSize: 1, statuses: [2] }),
        getAssignedOrders({ pageNum: 1, pageSize: 1, statuses: [5] }),
        getMyInspectionTasks({ pageNum: 1, pageSize: 1, status: 0 })
      ])
      workerStats.pending = pendingRes.data?.total || 0
      workerStats.completed = completedRes.data?.total || 0
      workerStats.inspection = inspectionRes.data?.total || 0
    } else {

      const ordersRes = await getMyOrders({ pageNum: 1, pageSize: 100 })
      const orders = ordersRes.data?.records || []
      ownerStats.total = ordersRes.data?.total || 0
      ownerStats.completed = orders.filter(o => o.status === 5).length
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const goToOrders = () => uni.switchTab({ url: '/pages/repair/list' })
const goToNotifications = () => uni.switchTab({ url: '/pages/notice/list' })
const goToAddress = () => uni.navigateTo({ url: '/pages/address/list' })
const goToCreate = () => uni.navigateTo({ url: '/pages/repair/create' })
const goToInspection = () => uni.navigateTo({ url: '/pages/inspection/list' })

const showPropertyInfo = () => {
  uni.navigateTo({ url: '/pages/address/list' })
}

const showAccountInfo = () => {
  uni.showModal({
    title: '账户信息',
    content: `用户名：${userInfo.value.username || '-'}\n身份：${userInfo.value.roleName || '-'}\n所属社区：${tenantName.value || '-'}`,
    showCancel: false,
    confirmText: '知道了',
    confirmColor: '#0066FF'
  })
}

const showSettings = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const showHelp = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const showAbout = () => {
  uni.showModal({
    title: '关于我们',
    content: '智慧社区物业设备维护管理系统\n\n版本: 1.0.0\n\n为您提供便捷的物业服务体验',
    showCancel: false,
    confirmText: '知道了',
    confirmColor: '#0066FF'
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    confirmColor: '#EF4444',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.reLaunch({ url: '/pages/login/index' })
      }
    }
  })
}
</script>

<style scoped>
.mine-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding-bottom: calc(120rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.profile-header {
  position: relative;
  padding-bottom: 32rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 360rpx;
  background: linear-gradient(135deg, #0066FF 0%, #0052CC 100%);
  overflow: hidden;
}

.bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 30%, rgba(255,255,255,0.08) 0%, transparent 40%);
}

.profile-content {
  position: relative;
  z-index: 10;
  padding: 48rpx 32rpx 0;
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24rpx;
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  background: linear-gradient(135deg, #FFFFFF 0%, #F1F5F9 100%);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 8rpx 32rpx rgba(0, 0, 0, 0.15),
    0 0 0 8rpx rgba(255, 255, 255, 0.2);
}

.avatar-text {
  font-size: 64rpx;
  font-weight: 700;
  color: #0066FF;
}

.avatar-badge {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  width: 48rpx;
  height: 48rpx;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #FFFFFF;
  box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.4);
}

.avatar-badge text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 700;
}

.user-info {
  text-align: center;
  margin-bottom: 16rpx;
}

.user-name {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #FFFFFF;
  margin-bottom: 12rpx;
}

.user-role-badge {
  display: inline-flex;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10rpx);
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
}

.user-role-badge.worker {
  background: rgba(16, 185, 129, 0.3);
  border-color: rgba(16, 185, 129, 0.5);
}

.user-role-badge.owner {
  background: rgba(59, 130, 246, 0.3);
  border-color: rgba(59, 130, 246, 0.5);
}

.user-role-badge text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.tenant-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-bottom: 24rpx;
}

.tenant-icon {
  font-size: 24rpx;
}

.tenant-name {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.user-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 32rpx 24rpx;
  margin: 0 16rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-item:active {
  opacity: 0.7;
}

.stat-value {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #1E293B;
  line-height: 1.2;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #64748B;
  margin-top: 8rpx;
}

.stat-divider {
  width: 2rpx;
  height: 60rpx;
  background: #E2E8F0;
}

.quick-section {
  padding: 32rpx;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 32rpx 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  position: relative;
}

.quick-item:active {
  opacity: 0.7;
}

.quick-icon {
  width: 80rpx;
  height: 80rpx;
  background: #F8FAFC;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
}

.quick-icon.worker {
  background: rgba(16, 185, 129, 0.1);
}

.quick-text {
  font-size: 24rpx;
  color: #64748B;
  font-weight: 500;
}

.quick-badge {
  position: absolute;
  top: -8rpx;
  right: 8rpx;
  min-width: 32rpx;
  height: 32rpx;
  background: #EF4444;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.quick-badge text {
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.menu-section {
  padding: 0 32rpx 24rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #64748B;
  margin-bottom: 16rpx;
  padding-left: 8rpx;
}

.menu-group {
  background: #FFFFFF;
  border-radius: 28rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid #F8FAFC;
  transition: background 0.2s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #F8FAFC;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.menu-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.menu-icon.orders {
  background: rgba(59, 130, 246, 0.1);
}

.menu-icon.property {
  background: rgba(245, 158, 11, 0.1);
}

.menu-icon.inspection {
  background: rgba(16, 185, 129, 0.1);
}

.menu-icon.account {
  background: rgba(59, 130, 246, 0.1);
}

.menu-icon.settings {
  background: rgba(100, 116, 139, 0.1);
}

.menu-icon.help {
  background: rgba(245, 158, 11, 0.1);
}

.menu-icon.about {
  background: rgba(16, 185, 129, 0.1);
}

.menu-text {
  font-size: 30rpx;
  color: #1E293B;
  font-weight: 500;
}

.menu-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.menu-value {
  font-size: 26rpx;
  color: #94A3B8;
}

.menu-arrow {
  font-size: 36rpx;
  color: #CBD5E1;
  font-weight: 300;
}

.logout-section {
  padding: 32rpx;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #FECACA;
  transition: all 0.2s ease;
}

.logout-btn:active {
  background: #FEF2F2;
  transform: scale(0.98);
}

.logout-icon {
  font-size: 32rpx;
}

.logout-text {
  font-size: 30rpx;
  color: #EF4444;
  font-weight: 600;
}

.footer {
  padding: 48rpx 32rpx;
  text-align: center;
}

.footer-text {
  display: block;
  font-size: 24rpx;
  color: #94A3B8;
  margin-bottom: 8rpx;
}

.footer-version {
  display: block;
  font-size: 22rpx;
  color: #CBD5E1;
}
</style>
