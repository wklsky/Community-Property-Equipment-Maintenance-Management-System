<template>
  <view class="device-page">
    <!-- Page Header -->
    <view class="page-header">
      <text class="header-title">设备列表</text>
      <view class="header-badge" v-if="totalCount > 0">
        <text>{{ totalCount }} 台</text>
      </view>
    </view>

    <!-- 顶部分类标签 -->
    <view class="category-bar">
      <scroll-view scroll-x class="category-scroll">
        <view
          class="category-tag"
          :class="{ active: currentCategory === null }"
          @click="filterByCategory(null)"
        >全部({{ totalCount }})</view>
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="category-tag"
          :class="{ active: currentCategory === cat.id }"
          @click="filterByCategory(cat.id)"
        >{{ cat.name }}</view>
      </scroll-view>
    </view>

    <!-- 设备列表 -->
    <view class="device-list">
      <view
        class="device-item"
        v-for="device in devices"
        :key="device.id"
      >
        <view class="device-left">
          <view class="device-avatar" :class="getStatusClass(device.status)">
            <text>{{ getDeviceIcon(device, categories) }}</text>
          </view>
          <view class="device-info">
            <text class="device-name">{{ device.name }}</text>
            <text class="device-meta">{{ device.model || '' }} | {{ device.location || '' }}</text>
            <text class="device-date" v-if="device.installDate">安装日期: {{ device.installDate }}</text>
          </view>
        </view>
        <view class="device-right">
          <view class="status-badge" :class="getStatusBadgeClass(device.status)">
            {{ getStatusName(device.status) }}
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="loading && devices.length === 0">
        <text class="empty-text">加载中...</text>
      </view>

      <view class="empty-state" v-if="!loading && devices.length === 0">
        <text class="empty-icon">📦</text>
        <text class="empty-text">暂无设备数据</text>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore && devices.length > 0">
      <text class="load-text" :class="{ 'load-text-disabled': loadingMore }" @click="loadMore">
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </text>
    </view>

    <view class="bottom-spacing"></view>
  </view>
</template>

<script setup>
// ref 属于 Vue 响应式 API，@dcloudio/uni-app 只导出页面生命周期钩子，
// 混在一起导入会在 H5 构建时因找不到命名导出而直接编译失败
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDevices } from '../../api/device'
import { getCategories } from '../../api/common'

const devices = ref([])
const categories = ref([])
const currentCategory = ref(null)
const totalCount = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const pageSize = 20
const hasMore = ref(true)

const getStatusName = (status) => {
  const map = { 1: '正常', 2: '故障', 3: '维修中', 4: '停用' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 1: 'normal', 2: 'faulty', 3: 'repairing', 4: 'disabled' }
  return map[status] || 'normal'
}

const getStatusBadgeClass = (status) => {
  const map = { 1: 'badge-normal', 2: 'badge-faulty', 3: 'badge-repairing', 4: 'badge-disabled' }
  return map[status] || ''
}

const getDeviceIcon = (device, categories) => {
  const catName = (categories.value || []).find(c => c.id === device.categoryId)?.name || ''
  const iconMap = {
    '电梯': '🛗',
    '水泵': '💧',
    '消防': '🔥',
    '配电': '⚡',
    '监控': '📹',
    '门禁': '🚪',
    '空调': '❄️',
    '照明': '💡',
    '发电机': '⚙️',
    '变压器': '🔌'
  }
  for (const [key, icon] of Object.entries(iconMap)) {
    if (catName.includes(key) || device.name?.includes(key)) return icon
  }
  return '🔧'
}

const filterByCategory = (categoryId) => {
  currentCategory.value = categoryId
  pageNum.value = 1
  devices.value = []
  hasMore.value = true
  loadDevices()
}

const loadDevices = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize }
    if (currentCategory.value) params.categoryId = currentCategory.value
    const res = await getDevices(params)
    const records = res.data?.records || []
    if (pageNum.value === 1) {
      devices.value = records
    } else {
      devices.value.push(...records)
    }
    totalCount.value = res.data?.total || 0
    hasMore.value = devices.value.length < totalCount.value
  } catch (e) {
    console.error('加载设备失败', e)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  pageNum.value++
  loadDevices()
}

onLoad(async () => {

  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) {  }
  loadDevices()
})
</script>

<style scoped>
.device-page {
  min-height: 100vh;
  background: #F8FAFC;
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

.category-bar {
  background: #FFFFFF;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #F1F5F9;
  position: sticky;
  top: 0;
  z-index: 10;
}

.category-scroll {
  white-space: nowrap;
  padding: 0 32rpx;
}

.category-tag {
  display: inline-block;
  padding: 14rpx 28rpx;
  margin-right: 16rpx;
  font-size: 26rpx;
  color: #64748B;
  background: #F8FAFC;
  border-radius: 32rpx;
  transition: all 0.2s;
}

.category-tag.active {
  color: #FFFFFF;
  background: #0066FF;
  box-shadow: 0 4rpx 12rpx rgba(0, 102, 255, 0.3);
}

.device-list {
  padding: 24rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.device-item {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
  transition: all 0.2s ease;
}

.device-item:active {
  transform: scale(0.98);
  background: #FAFBFC;
}

.device-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex: 1;
  min-width: 0;
}

.device-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.device-avatar.normal { background: rgba(16, 185, 129, 0.1); }
.device-avatar.faulty { background: rgba(239, 68, 68, 0.1); }
.device-avatar.repairing { background: rgba(245, 158, 11, 0.1); }
.device-avatar.disabled { background: rgba(148, 163, 184, 0.1); }

.device-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  min-width: 0;
}

.device-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-meta {
  font-size: 24rpx;
  color: #64748B;
}

.device-date {
  font-size: 22rpx;
  color: #94A3B8;
}

.device-right {
  flex-shrink: 0;
  margin-left: 16rpx;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
}

.badge-normal { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.badge-faulty { background: rgba(239, 68, 68, 0.1); color: #EF4444; }
.badge-repairing { background: rgba(245, 158, 11, 0.1); color: #F59E0B; }
.badge-disabled { background: rgba(148, 163, 184, 0.1); color: #94A3B8; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 32rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #94A3B8;
}

.load-more {
  text-align: center;
  padding: 32rpx;
}

.load-text {
  font-size: 26rpx;
  color: #0066FF;
}

.load-text-disabled {
  color: #94A3B8;
  pointer-events: none;
}

.bottom-spacing {
  height: 40rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
