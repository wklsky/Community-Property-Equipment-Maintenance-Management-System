<template>
  <view class="order-list-page">
    <!-- Custom Header -->
    <view class="page-header">
      <text class="header-title">{{ isWorker ? '工单中心' : '我的工单' }}</text>
      <view class="header-badge" v-if="totalOrders > 0">
        <text>{{ totalOrders }}</text>
      </view>
    </view>

    <!-- Tab Switcher - 业主视图 -->
    <view class="tab-container owner-tabs" v-if="!isWorker">
      <view class="tab-wrapper">
        <view
          class="tab-item"
          :class="{ active: activeTab === 'processing' }"
          @click="switchTab('processing')"
        >
          <text class="tab-text">处理中</text>
          <view class="tab-count" v-if="ownerTabCounts.processing > 0">{{ ownerTabCounts.processing }}</view>
          <view class="tab-indicator" v-if="activeTab === 'processing'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'reviewing' }"
          @click="switchTab('reviewing')"
        >
          <text class="tab-text">待评价</text>
          <view class="tab-count highlight" v-if="ownerTabCounts.reviewing > 0">{{ ownerTabCounts.reviewing }}</view>
          <view class="tab-indicator" v-if="activeTab === 'reviewing'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'completed' }"
          @click="switchTab('completed')"
        >
          <text class="tab-text">已完成</text>
          <view class="tab-indicator" v-if="activeTab === 'completed'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'cancelled' }"
          @click="switchTab('cancelled')"
        >
          <text class="tab-text">已取消</text>
          <view class="tab-indicator" v-if="activeTab === 'cancelled'"></view>
        </view>
      </view>
    </view>

    <!-- Tab Switcher - 维修工视图 -->
    <view class="tab-container worker-tabs" v-if="isWorker">
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
          <text class="tab-text">处理中</text>
          <view class="tab-count" v-if="tabCounts.processing > 0">{{ tabCounts.processing }}</view>
          <view class="tab-indicator" v-if="activeTab === 'processing'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'reviewing' }"
          @click="switchTab('reviewing')"
        >
          <text class="tab-text">待评价</text>
          <view class="tab-count highlight" v-if="tabCounts.reviewing > 0">{{ tabCounts.reviewing }}</view>
          <view class="tab-indicator" v-if="activeTab === 'reviewing'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'completed' }"
          @click="switchTab('completed')"
        >
          <text class="tab-text">已完成</text>
          <view class="tab-indicator" v-if="activeTab === 'completed'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'cancelled' }"
          @click="switchTab('cancelled')"
        >
          <text class="tab-text">已取消</text>
          <view class="tab-indicator" v-if="activeTab === 'cancelled'"></view>
        </view>
      </view>
    </view>

    <!-- Order List -->
    <scroll-view
      scroll-y
      class="order-scroll"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="order-list">
        <view
          class="order-card"
          v-for="order in orders"
          :key="order.id"
          @click="goToDetail(order.id)"
        >
          <!-- Order Header -->
          <view class="order-header">
            <view class="order-info">
              <text class="order-no">{{ order.orderNo }}</text>
              <view class="priority-tag" :class="{ urgent: order.priority === 1 }" v-if="order.priority === 1">
                <text>紧急</text>
              </view>
            </view>
            <view class="status-badge" :class="getStatusClass(order.status)">
              <view class="status-dot"></view>
              <text>{{ statusMap[order.status] }}</text>
            </view>
          </view>

          <!-- Order Body -->
          <view class="order-body">
            <view class="order-location">
              <text class="location-icon">📍</text>
              <text class="location-text">{{ order.address }}</text>
            </view>
            <text class="order-desc">{{ order.faultDesc }}</text>
            <view class="order-evaluation" v-if="order.rating">
              <view class="eval-stars">
                <text v-for="i in 5" :key="i" class="eval-star" :class="{ active: i <= order.rating }">★</text>
              </view>
              <text class="eval-comment" v-if="order.comment">{{ order.comment }}</text>
            </view>
            <view class="order-reason" v-if="order.transferReason">
              <text class="reason-label">{{ order.status === 6 ? '拒绝/取消原因' : '转派原因' }}：</text>
              <text class="reason-text">{{ order.transferReason }}</text>
            </view>
          </view>

          <!-- Order Footer -->
          <view class="order-footer">
            <view class="order-time">
              <text class="time-icon">🕐</text>
              <text class="time-text">{{ formatTime(order.createTime) }}</text>
            </view>

            <view class="order-actions">
              <!-- 维修工：待接单状态显示接单按钮 -->
              <view
                class="action-btn accept"
                v-if="order.status === 2 && isWorker"
                @click.stop="handleAccept(order)"
              >
                <text>立即接单</text>
              </view>
              <!-- 维修工：处理中状态显示完成按钮 -->
              <view
                class="action-btn complete"
                v-if="order.status === 3 && isWorker"
                @click.stop="showComplete(order)"
              >
                <text>完成维修</text>
              </view>
              <!-- 业主：待评价状态显示评价按钮 -->
              <view
                class="action-btn evaluate"
                v-if="order.status === 4 && !isWorker"
                @click.stop="showEvaluate(order)"
              >
                <text>评价</text>
              </view>
              <!-- 业主：可取消状态显示取消按钮 -->
              <view
                class="action-btn cancel"
                v-if="[0,1,2].includes(order.status) && !isWorker"
                @click.stop="handleCancel(order)"
              >
                <text>取消</text>
              </view>
            </view>
          </view>

          <!-- Swipe Hint -->
          <view class="swipe-hint">
            <text>›</text>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view class="empty-state" v-if="orders.length === 0 && !loading">
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
      <view class="load-more" v-if="orders.length > 0 && !hasMore && !loading">
        <text>— 已加载全部 —</text>
      </view>

      <!-- Bottom Spacing -->
      <view class="bottom-spacing"></view>
    </scroll-view>

    <!-- Floating Action Button - 仅业主可见 -->
    <view class="fab" @click="goToCreate" v-if="!isWorker">
      <view class="fab-inner">
        <text class="fab-icon">+</text>
      </view>
      <view class="fab-ripple"></view>
    </view>

    <!-- 维修工快捷入口 -->
    <view class="worker-quick-actions" v-if="isWorker">
      <view class="quick-action-item" @click="goToInspection">
        <view class="quick-action-icon">🔍</view>
        <text class="quick-action-text">巡检任务</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyOrders, getAssignedOrders, acceptOrder, completeOrder, evaluateOrder, cancelOrder } from '../../api/repair'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isWorker = computed(() => userStore.userInfo?.roleName === '维修工')

const activeTab = ref('processing')
const orders = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const hasMore = ref(true)
const totalOrders = ref(0)

const tabCounts = reactive({
  pending: 0,
  processing: 0,
  reviewing: 0,
  completed: 0
})

const ownerTabCounts = reactive({
  processing: 0,
  reviewing: 0,
  completed: 0,
  cancelled: 0
})

const statusMap = {
  0: '待受理',
  1: '待派单',
  2: '待处理',
  3: '处理中',
  4: '待评价',
  5: '已完成',
  6: '已取消',
  7: '转单中'
}

const getStatusClass = (status) => {
  if ([0, 1, 2].includes(status)) return 'warning'
  if (status === 3) return 'processing'
  if ([4, 5].includes(status)) return 'success'
  if (status === 7) return 'warning'
  return 'cancelled'
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

const getEmptyIcon = () => {
  if (isWorker.value) {
    if (activeTab.value === 'pending') return '📭'
    if (activeTab.value === 'processing') return '🔧'
    if (activeTab.value === 'reviewing') return '⭐'
    if (activeTab.value === 'cancelled') return '🚫'
    return '✅'
  }
  if (activeTab.value === 'processing') return '🔧'
  if (activeTab.value === 'reviewing') return '⭐'
  if (activeTab.value === 'cancelled') return '🚫'
  return '✅'
}

const getEmptyTitle = () => {
  if (isWorker.value) {
    if (activeTab.value === 'pending') return '暂无待接单工单'
    if (activeTab.value === 'processing') return '暂无处理中工单'
    if (activeTab.value === 'reviewing') return '暂无待评价工单'
    if (activeTab.value === 'cancelled') return '暂无已取消工单'
    return '暂无已完成工单'
  }
  if (activeTab.value === 'processing') return '暂无处理中工单'
  if (activeTab.value === 'reviewing') return '暂无待评价工单'
  if (activeTab.value === 'cancelled') return '暂无已取消工单'
  return '暂无已完成工单'
}

const getEmptyDesc = () => {
  if (isWorker.value) {
    if (activeTab.value === 'pending') return '当前没有需要处理的工单'
    if (activeTab.value === 'processing') return '您还没有正在处理的工单'
    if (activeTab.value === 'reviewing') return '等待业主评价的工单将显示在这里'
    if (activeTab.value === 'cancelled') return '已取消的工单将显示在这里'
    return '已完成的工单将显示在这里'
  }
  if (activeTab.value === 'processing') return '您的报修工单正在处理中'
  if (activeTab.value === 'reviewing') return '维修完成后，在这里对工单进行评价'
  if (activeTab.value === 'cancelled') return '已取消的工单将显示在这里'
  return '已完成的工单将显示在这里'
}

onShow(() => {

  loadData()
  loadTabCounts()
})

const loadTabCounts = async () => {
  try {
    if (isWorker.value) {
      const [pendingRes, processingRes, reviewingRes] = await Promise.all([
        getAssignedOrders({ pageNum: 1, pageSize: 1, statuses: [2] }),
        getAssignedOrders({ pageNum: 1, pageSize: 1, statuses: [3] }),
        getAssignedOrders({ pageNum: 1, pageSize: 1, statuses: [4] })
      ])
      tabCounts.pending = pendingRes.data?.total || 0
      tabCounts.processing = processingRes.data?.total || 0
      tabCounts.reviewing = reviewingRes.data?.total || 0
    } else {
      const [processingRes, reviewingRes, completedRes, cancelledRes] = await Promise.all([
        getMyOrders({ pageNum: 1, pageSize: 1, statuses: [0, 1, 2, 3] }),
        getMyOrders({ pageNum: 1, pageSize: 1, statuses: [4] }),
        getMyOrders({ pageNum: 1, pageSize: 1, statuses: [5] }),
        getMyOrders({ pageNum: 1, pageSize: 1, statuses: [6] })
      ])
      ownerTabCounts.processing = processingRes.data?.total || 0
      ownerTabCounts.reviewing = reviewingRes.data?.total || 0
      ownerTabCounts.completed = completedRes.data?.total || 0
      ownerTabCounts.cancelled = cancelledRes.data?.total || 0
    }
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
    let api, params = { pageNum: pageNum.value, pageSize: 10 }

    if (isWorker.value) {
      api = getAssignedOrders

      if (activeTab.value === 'pending') {
        params.statuses = [2]
      } else if (activeTab.value === 'processing') {
        params.statuses = [3]
      } else if (activeTab.value === 'reviewing') {
        params.statuses = [4]
      } else if (activeTab.value === 'completed') {
        params.statuses = [5]
      } else if (activeTab.value === 'cancelled') {
        params.statuses = [6]
      }
    } else {
      api = getMyOrders

      if (activeTab.value === 'processing') {
        params.statuses = [0, 1, 2, 3]
      } else if (activeTab.value === 'reviewing') {
        params.statuses = [4]
      } else if (activeTab.value === 'completed') {
        params.statuses = [5]
      } else if (activeTab.value === 'cancelled') {
        params.statuses = [6]
      }
    }

    const res = await api(params)

    const records = res.data?.records || []
    const total = res.data?.total || 0

    totalOrders.value = total

    if (reset) {
      orders.value = records
    } else {
      orders.value = [...orders.value, ...records]
    }

    hasMore.value = orders.value.length < total
    pageNum.value++
  } catch (e) {
    console.error('加载工单列表失败', e)
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

const goToDetail = (id) => uni.navigateTo({ url: `/pages/repair/detail?id=${id}` })
const goToCreate = () => uni.navigateTo({ url: '/pages/repair/create' })
const goToInspection = () => uni.navigateTo({ url: '/pages/inspection/list' })

const handleAccept = async (order) => {
  uni.showModal({
    title: '确认接单',
    content: `确定接受工单 ${order.orderNo} 吗？`,
    confirmColor: '#0066FF',
    success: async (res) => {
      if (res.confirm) {
        try {
          await acceptOrder(order.id)
          uni.showToast({ title: '接单成功', icon: 'success' })

          const index = orders.value.findIndex(o => o.id === order.id)
          if (index > -1) {
            orders.value.splice(index, 1)
            totalOrders.value--
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

const showComplete = (order) => {
  uni.navigateTo({ url: `/pages/repair/detail?id=${order.id}&action=complete` })
}

const showEvaluate = (order) => {
  uni.navigateTo({ url: `/pages/repair/detail?id=${order.id}&action=evaluate` })
}

const handleCancel = async (order) => {
  uni.showModal({
    title: '确认取消',
    content: '确定取消该工单吗？取消后无法恢复。',
    confirmColor: '#EF4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelOrder(order.id)
          uni.showToast({ title: '取消成功', icon: 'success' })
          loadData()
        } catch (e) {
          console.error('取消失败', e)
        }
      }
    }
  })
}
</script>

<style scoped>
.order-list-page {
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
  gap: 16rpx;
  border-bottom: 2rpx solid #F1F5F9;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1E293B;
}

.header-badge {
  background: #0066FF;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.header-badge text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.tab-container {
  background: #FFFFFF;
  padding: 0 32rpx;
  border-bottom: 2rpx solid #F1F5F9;
}

.tab-container.worker-tabs .tab-wrapper,
.tab-container.owner-tabs .tab-wrapper {
  justify-content: space-around;
}

.tab-count.highlight {
  background: #F59E0B;
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

.order-scroll {
  flex: 1;
  padding: 24rpx 32rpx;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.order-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 28rpx;
  position: relative;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
  transition: all 0.2s ease;
}

.order-card:active {
  transform: scale(0.98);
  background: #FAFBFC;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.order-no {
  font-size: 30rpx;
  font-weight: 700;
  color: #1E293B;
}

.priority-tag {
  background: rgba(239, 68, 68, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.priority-tag.urgent {
  background: rgba(239, 68, 68, 0.1);
}

.priority-tag text {
  font-size: 22rpx;
  color: #EF4444;
  font-weight: 600;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.status-badge .status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}

.status-badge.warning {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}
.status-badge.warning .status-dot {
  background: #F59E0B;
}

.status-badge.processing {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}
.status-badge.processing .status-dot {
  background: #3B82F6;
  animation: pulse 1.5s infinite;
}

.status-badge.success {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}
.status-badge.success .status-dot {
  background: #10B981;
}

.status-badge.cancelled {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}
.status-badge.cancelled .status-dot {
  background: #EF4444;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

.order-body {
  margin-bottom: 20rpx;
}

.order-location {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.location-icon {
  font-size: 24rpx;
}

.location-text {
  font-size: 26rpx;
  color: #64748B;
}

.order-desc {
  display: block;
  font-size: 28rpx;
  color: #1E293B;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.order-evaluation {
  margin-top: 12rpx;
  padding: 16rpx;
  background: #FFFBEB;
  border-radius: 12rpx;
  border-left: 6rpx solid #F59E0B;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.eval-stars {
  display: flex;
  gap: 4rpx;
}

.eval-star {
  font-size: 28rpx;
  color: #E2E8F0;
}

.eval-star.active {
  color: #F59E0B;
}

.eval-comment {
  font-size: 24rpx;
  color: #64748B;
  line-height: 1.4;
}

.order-reason {
  margin-top: 12rpx;
  padding: 12rpx 16rpx;
  background: #FEF2F2;
  border-radius: 12rpx;
  border-left: 6rpx solid #EF4444;
}

.reason-label {
  font-size: 22rpx;
  color: #EF4444;
  font-weight: 600;
}

.reason-text {
  font-size: 24rpx;
  color: #64748B;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 2rpx solid #F1F5F9;
}

.order-time {
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

.order-actions {
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

.action-btn.complete {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  box-shadow: 0 4rpx 16rpx rgba(16, 185, 129, 0.3);
}
.action-btn.complete text {
  color: #FFFFFF;
}

.action-btn.evaluate {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
  box-shadow: 0 4rpx 16rpx rgba(245, 158, 11, 0.3);
}
.action-btn.evaluate text {
  color: #FFFFFF;
}

.action-btn.cancel {
  background: #FEF2F2;
  border: 2rpx solid #FECACA;
}
.action-btn.cancel text {
  color: #EF4444;
}

.swipe-hint {
  position: absolute;
  right: 16rpx;
  top: 50%;
  transform: translateY(-50%);
  color: #CBD5E1;
  font-size: 48rpx;
  font-weight: 300;
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
  height: 200rpx;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 200rpx;
  bottom: calc(200rpx + constant(safe-area-inset-bottom));
  bottom: calc(200rpx + env(safe-area-inset-bottom));
  z-index: 100;
}

.fab-inner {
  width: 112rpx;
  height: 112rpx;
  background: linear-gradient(135deg, #0066FF 0%, #0052CC 100%);
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 8rpx 32rpx rgba(0, 102, 255, 0.4),
    0 0 0 8rpx rgba(0, 102, 255, 0.1);
  transition: all 0.2s ease;
}

.fab:active .fab-inner {
  transform: scale(0.9);
}

.fab-icon {
  font-size: 56rpx;
  color: #FFFFFF;
  font-weight: 300;
  line-height: 1;
}

.fab-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140rpx;
  height: 140rpx;
  background: rgba(0, 102, 255, 0.2);
  border-radius: 40rpx;
  z-index: -1;
  animation: ripple 2s infinite;
}

@keyframes ripple {
  0% { transform: translate(-50%, -50%) scale(0.8); opacity: 1; }
  100% { transform: translate(-50%, -50%) scale(1.2); opacity: 0; }
}

.worker-quick-actions {
  position: fixed;
  right: 40rpx;
  bottom: 200rpx;
  bottom: calc(200rpx + constant(safe-area-inset-bottom));
  bottom: calc(200rpx + env(safe-area-inset-bottom));
  z-index: 100;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  border: 2rpx solid #F1F5F9;
}

.quick-action-item:active {
  transform: scale(0.95);
}

.quick-action-icon {
  font-size: 40rpx;
}

.quick-action-text {
  font-size: 22rpx;
  color: #64748B;
  font-weight: 500;
}
</style>
