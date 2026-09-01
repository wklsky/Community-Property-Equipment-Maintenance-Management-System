<template>
  <view class="address-page">
    <!-- Header -->
    <view class="page-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <view class="header-center">
        <text class="header-title">我的房产</text>
        <text class="header-count" v-if="addresses.length > 0">{{ addresses.length }}个地址</text>
      </view>
      <view class="header-action" @click="goAdd">
        <text>+</text>
      </view>
    </view>

    <!-- Tip Banner -->
    <view class="tip-banner" v-if="addresses.length === 0">
      <view class="tip-icon">💡</view>
      <view class="tip-content">
        <text class="tip-title">添加您的房产地址</text>
        <text class="tip-desc">系统会自动关联您名下的房产，也可手动添加其他地址</text>
      </view>
    </view>

    <!-- Unified Address List with pull-refresh -->
    <scroll-view
      class="address-scroll"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="address-list" v-if="addresses.length > 0">
        <view
          v-for="item in addresses"
          :key="item.type + item.id"
          class="address-card"
          :class="{ 'is-default': item.isDefault === 1 }"
        >
          <view class="card-main" @click="setDefault(item)">
            <view class="card-icon" :class="item.type === 'custom' ? 'custom-icon' : ''">
              <text>{{ item.type === 'custom' ? '📍' : '🏠' }}</text>
            </view>
            <view class="card-body">
              <view class="address-row">
                <text class="address-text">{{ item.address }}</text>
                <view class="default-tag" v-if="item.isDefault === 1">
                  <text>默认</text>
                </view>
              </view>
              <text class="address-sub" v-if="item.type === 'property' && item.communityName">
                {{ item.communityName }} · {{ item.buildingName }} · {{ item.roomNo }}
              </text>
              <text class="address-hint" v-if="item.isDefault !== 1">点击设为默认地址</text>
              <text class="address-hint default-hint" v-else>报修时将自动使用此地址</text>
            </view>
            <view class="card-check-wrap">
              <view class="card-radio" v-if="item.isDefault !== 1">
                <view class="radio-dot"></view>
              </view>
              <view class="card-check" v-else>
                <text>✓</text>
              </view>
            </view>
          </view>
          <view class="card-divider" v-if="item.type === 'custom'"></view>
          <view class="card-footer" v-if="item.type === 'custom'">
            <view class="footer-action edit" @click="goEdit(item)">
              <text class="footer-icon">✎</text>
              <text>编辑</text>
            </view>
            <view class="footer-action delete" @click="handleDelete(item)">
              <text class="footer-icon">✕</text>
              <text>删除</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Empty -->
      <view class="empty-state" v-if="addresses.length === 0 && !loading">
        <view class="empty-icon-wrap">
          <text class="empty-icon">🏘️</text>
        </view>
        <text class="empty-title">还没有添加房产地址</text>
        <text class="empty-desc">系统会自动关联您名下的房产，也可以手动添加地址</text>
        <view class="empty-btn" @click="goAdd">
          <text>+ 添加地址</text>
        </view>
      </view>
    </scroll-view>

    <!-- Floating Add Button -->
    <view class="float-add" v-if="addresses.length > 0" @click="goAdd">
      <text>+</text>
    </view>

    <!-- Add/Edit Custom Address Modal -->
    <view class="modal-overlay" v-if="showModal" @click="closeModal">
      <view class="modal-card" @click.stop>
        <view class="modal-handle">
          <view class="handle-bar"></view>
        </view>
        <view class="modal-header">
          <text class="modal-title">{{ editingId ? '修改地址' : '添加地址' }}</text>
          <text class="modal-subtitle">手动输入您的其他房产地址</text>
        </view>
        <view class="modal-body">
          <view class="form-group">
            <text class="form-label">详细地址</text>
            <input
              v-model="form.address"
              placeholder="如：阳光花园1栋1单元101室"
              class="form-input"
              maxlength="200"
              :focus="true"
            />
          </view>
          <view class="form-group">
            <view class="default-row" @click="form.isDefault = form.isDefault === 1 ? 0 : 1">
              <view class="checkbox" :class="{ checked: form.isDefault === 1 }">
                <text v-if="form.isDefault === 1">✓</text>
              </view>
              <view class="default-info">
                <text class="default-label">设为默认地址</text>
                <text class="default-desc">报修时将自动选择此地址</text>
              </view>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <view class="btn-cancel" @click="closeModal">
            <text>取消</text>
          </view>
          <view class="btn-confirm" :class="{ disabled: !form.address.trim() }" @click="handleSubmit">
            <text>{{ editingId ? '保存' : '添加' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getMyAddresses, setDefaultAddress,
  addAddress, updateAddress, deleteAddress
} from '../../api/address'

const addresses = ref([])
const showModal = ref(false)
const editingId = ref(null)
const loading = ref(false)
const refreshing = ref(false)

const form = ref({
  address: '',
  isDefault: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyAddresses()
    addresses.value = res.data || []
  } catch (e) {
    console.error('加载地址失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

onShow(() => { loadData() })

const onRefresh = async () => {
  refreshing.value = true
  await loadData()
}

const goBack = () => { uni.navigateBack({ delta: 1 }) }

const setDefault = async (item) => {
  if (item.isDefault === 1) return
  try {
    await setDefaultAddress(item.type, item.id)
    uni.showToast({ title: '已设为默认地址', icon: 'success' })
    loadData()
  } catch (e) {
    uni.showToast({ title: e.message || '设置失败', icon: 'none' })
  }
}

const goAdd = () => {
  editingId.value = null
  form.value = { address: '', isDefault: addresses.value.length === 0 ? 1 : 0 }
  showModal.value = true
}

const goEdit = (item) => {
  editingId.value = item.id
  form.value = { address: item.address, isDefault: item.isDefault }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingId.value = null
}

const handleSubmit = async () => {
  if (!form.value.address.trim()) {
    uni.showToast({ title: '请输入地址', icon: 'none' })
    return
  }
  loading.value = true
  try {
    if (editingId.value) {
      await updateAddress(editingId.value, form.value)
    } else {
      await addAddress(form.value)
    }
    uni.showToast({ title: editingId.value ? '修改成功' : '添加成功', icon: 'success' })
    closeModal()
    loadData()
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleDelete = (item) => {
  uni.showModal({
    title: '删除地址',
    content: `确定要删除「${item.address}」吗？`,
    confirmText: '删除',
    confirmColor: '#EF4444',
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteAddress(item.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadData()
        } catch (e) {
          uni.showToast({ title: e.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style scoped>
.address-page {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 2rpx solid #F1F5F9;
  padding-top: calc(20rpx + constant(safe-area-inset-top));
  padding-top: calc(20rpx + env(safe-area-inset-top));
  z-index: 100;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  color: #1E293B;
  border-radius: 50%;
  background: #F8FAFC;
}

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1E293B;
}

.header-count {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 2rpx;
}

.header-action {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  color: #FFFFFF;
  font-weight: 400;
  background: linear-gradient(135deg, #0066FF, #0052CC);
  border-radius: 50%;
  box-shadow: 0 4rpx 12rpx rgba(0, 102, 255, 0.3);
}

.tip-banner {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin: 24rpx 32rpx;
  padding: 24rpx;
  background: linear-gradient(135deg, #E6F0FF, #F0F7FF);
  border-radius: 20rpx;
  border: 2rpx solid rgba(0, 102, 255, 0.1);
}

.tip-icon {
  font-size: 36rpx;
  flex-shrink: 0;
}

.tip-content {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.tip-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}

.tip-desc {
  font-size: 24rpx;
  color: #64748B;
  line-height: 1.5;
}

.address-scroll {
  flex: 1;
  height: 0;
}

.address-list {
  padding: 24rpx 32rpx 0;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.address-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.04);
  border: 2rpx solid #E2E8F0;
  overflow: hidden;
  transition: all 0.2s;
}

.address-card.is-default {
  border-color: #0066FF;
  box-shadow: 0 4rpx 20rpx rgba(0,102,255,0.1);
}

.card-main {
  display: flex;
  align-items: center;
  padding: 24rpx 24rpx 20rpx;
  gap: 16rpx;
}

.card-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  background: #F8FAFC;
  flex-shrink: 0;
}

.is-default .card-icon {
  background: linear-gradient(135deg, #E6F0FF, #D6E8FF);
}

.custom-icon {
  background: #FFF8ED;
}

.is-default .custom-icon {
  background: linear-gradient(135deg, #E6F0FF, #D6E8FF);
}

.card-body {
  flex: 1;
  min-width: 0;
}

.address-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.address-text {
  font-size: 30rpx;
  color: #1E293B;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.default-tag {
  background: linear-gradient(135deg, #0066FF, #0052CC);
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.default-tag text {
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.address-sub {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 4rpx;
}

.address-hint {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 6rpx;
}

.address-hint.default-hint {
  color: #0066FF;
}

.card-check-wrap {
  flex-shrink: 0;
}

.card-radio {
  width: 44rpx;
  height: 44rpx;
  border: 3rpx solid #CBD5E1;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radio-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
}

.card-check {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #0066FF, #0052CC);
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-check text {
  color: #FFFFFF;
  font-size: 24rpx;
  font-weight: 700;
}

.card-divider {
  height: 2rpx;
  background: #F1F5F9;
  margin: 0 24rpx;
}

.card-footer {
  display: flex;
  padding: 16rpx 24rpx;
  gap: 32rpx;
}

.footer-action {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #64748B;
  padding: 8rpx 0;
}

.footer-action.edit:active { color: #0066FF; }
.footer-action.delete:active { color: #EF4444; }
.footer-icon { font-size: 22rpx; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx;
}

.empty-icon-wrap {
  width: 160rpx;
  height: 160rpx;
  background: linear-gradient(135deg, #F1F5F9, #E2E8F0);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.empty-icon { font-size: 72rpx; }

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #94A3B8;
  text-align: center;
  line-height: 1.5;
  margin-bottom: 40rpx;
}

.empty-btn {
  padding: 20rpx 48rpx;
  background: linear-gradient(135deg, #0066FF, #0052CC);
  border-radius: 48rpx;
  box-shadow: 0 8rpx 24rpx rgba(0,102,255,0.3);
}

.empty-btn text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.float-add {
  position: fixed;
  right: 32rpx;
  bottom: 80rpx;
  width: 104rpx;
  height: 104rpx;
  background: linear-gradient(135deg, #0066FF, #0052CC);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(0,102,255,0.4);
  z-index: 50;
}

.float-add:active { transform: scale(0.95); }

.float-add text {
  font-size: 48rpx;
  color: #FFFFFF;
  font-weight: 300;
  line-height: 1;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-card {
  width: 100%;
  background: #FFFFFF;
  border-radius: 40rpx 40rpx 0 0;
  padding-bottom: calc(40rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.modal-handle {
  display: flex;
  justify-content: center;
  padding: 20rpx 0 8rpx;
}

.handle-bar {
  width: 64rpx;
  height: 6rpx;
  background: #E2E8F0;
  border-radius: 3rpx;
}

.modal-header {
  padding: 16rpx 36rpx 8rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1E293B;
}

.modal-subtitle {
  display: block;
  font-size: 26rpx;
  color: #94A3B8;
  margin-top: 8rpx;
}

.modal-body {
  padding: 24rpx 36rpx;
}

.form-group {
  margin-bottom: 28rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 14rpx;
}

.form-input {
  height: 100rpx;
  background: #F8FAFC;
  border: 2rpx solid #E2E8F0;
  border-radius: 18rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #1E293B;
}

.form-input:focus {
  border-color: #0066FF;
  background: #FFFFFF;
}

.default-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #F8FAFC;
  border-radius: 18rpx;
}

.checkbox {
  width: 48rpx;
  height: 48rpx;
  border: 3rpx solid #CBD5E1;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.checkbox.checked {
  background: #0066FF;
  border-color: #0066FF;
}

.checkbox text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 700;
}

.default-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.default-label {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
}

.default-desc {
  font-size: 22rpx;
  color: #94A3B8;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 16rpx 36rpx 0;
}

.btn-cancel {
  flex: 1;
  height: 96rpx;
  background: #F1F5F9;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-cancel text {
  font-size: 30rpx;
  color: #64748B;
  font-weight: 500;
}

.btn-cancel:active { background: #E2E8F0; }

.btn-confirm {
  flex: 2;
  height: 96rpx;
  background: linear-gradient(135deg, #0066FF, #0052CC);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-confirm text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.btn-confirm:active { opacity: 0.85; }

.btn-confirm.disabled {
  background: #CBD5E1;
  box-shadow: none;
}
</style>
