<template>
  <view class="page">
    <view class="section">
      <text class="section-title">系统房产</text>
      <view v-for="item in properties" :key="`p-${item.id}`" class="addr-card">
        <view class="addr-main">
          <text class="addr-text">{{ item.address }}</text>
          <text v-if="item.isDefault === 1" class="badge">默认</text>
        </view>
        <text v-if="item.isDefault !== 1" class="action" @click="setDefault(item)">设为默认</text>
      </view>
      <view v-if="properties.length === 0" class="empty-line">暂无关联房产</view>
    </view>

    <view class="section">
      <text class="section-title">自定义地址</text>
      <view v-for="item in customs" :key="`c-${item.id}`" class="addr-card">
        <view class="addr-main">
          <text class="addr-text">{{ item.address }}</text>
          <text v-if="item.isDefault === 1" class="badge">默认</text>
        </view>
        <view class="actions">
          <text v-if="item.isDefault !== 1" class="action" @click="setDefault(item)">设为默认</text>
          <text class="action" @click="editAddress(item)">编辑</text>
          <text class="action danger" @click="removeAddress(item)">删除</text>
        </view>
      </view>
      <view v-if="customs.length === 0" class="empty-line">暂无自定义地址</view>
    </view>

    <button class="primary-btn" @click="createAddress">新增地址</button>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/pages/address/list.vue
 * @Description: 业主端地址簿，系统房产与自定义地址分属两套后端路由，操作能力不同
 */

import { onShow } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import {
  addAddress,
  deleteAddress,
  getMyAddresses,
  setDefaultAddress,
  updateAddress,
  type MyAddress
} from '@community/shared'

const addresses = ref<MyAddress[]>([])

const properties = computed<MyAddress[]>(() =>
  addresses.value.filter((item) => item.type === 'property')
)

const customs = computed<MyAddress[]>(() =>
  addresses.value.filter((item) => item.type === 'custom')
)

const load = async (): Promise<void> => {
  try {
    const res = await getMyAddresses({ silent: true })
    addresses.value = res.data ?? []
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '地址加载失败',
      icon: 'none'
    })
  }
}

/**
 * 按 type 分发到不同后端路由。
 * 房产由物业维护，业主没有删除权限，因此只有自定义地址提供编辑与删除
 */
const setDefault = async (item: MyAddress): Promise<void> => {
  try {
    await setDefaultAddress(item.type, item.id)
    await load()
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '设置失败',
      icon: 'none'
    })
  }
}

const createAddress = (): void => {
  uni.showModal({
    title: '新增地址',
    editable: true,
    placeholderText: '请输入详细地址',
    success: async (res) => {
      if (!res.confirm) return
      const value = (res.content ?? '').trim()
      if (!value) {
        uni.showToast({ title: '地址不能为空', icon: 'none' })
        return
      }
      try {
        await addAddress({ address: value })
        await load()
      } catch (error) {
        uni.showToast({
          title: error instanceof Error ? error.message : '新增失败',
          icon: 'none'
        })
      }
    }
  })
}

const editAddress = (item: MyAddress): void => {
  uni.showModal({
    title: '编辑地址',
    editable: true,
    placeholderText: item.address,
    success: async (res) => {
      if (!res.confirm) return
      const value = (res.content ?? '').trim()
      if (!value) {
        uni.showToast({ title: '地址不能为空', icon: 'none' })
        return
      }
      try {
        await updateAddress(item.id, { address: value })
        await load()
      } catch (error) {
        uni.showToast({
          title: error instanceof Error ? error.message : '修改失败',
          icon: 'none'
        })
      }
    }
  })
}

const removeAddress = (item: MyAddress): void => {
  uni.showModal({
    title: '删除地址',
    content: `确认删除「${item.address}」？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteAddress(item.id)
        await load()
      } catch (error) {
        uni.showToast({
          title: error instanceof Error ? error.message : '删除失败',
          icon: 'none'
        })
      }
    }
  })
}

onShow(() => {
  void load()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 24rpx;
}

.section {
  margin-bottom: 40rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20rpx;
}

.addr-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.addr-main {
  flex: 1;
  margin-right: 20rpx;
}

.addr-text {
  font-size: 29rpx;
  color: #1e293b;
  line-height: 1.5;
}

.badge {
  display: inline-block;
  margin-left: 12rpx;
  padding: 2rpx 12rpx;
  background: #e0f2fe;
  color: #0066ff;
  font-size: 22rpx;
  border-radius: 6rpx;
}

.actions {
  display: flex;
  flex-shrink: 0;
}

.action {
  font-size: 26rpx;
  color: #0066ff;
  margin-left: 20rpx;
}

.action.danger {
  color: #e11d48;
}

.empty-line {
  font-size: 26rpx;
  color: #94a3b8;
  padding: 24rpx 0;
  text-align: center;
}

.primary-btn {
  height: 96rpx;
  line-height: 96rpx;
  background: #0066ff;
  color: #ffffff;
  border-radius: 48rpx;
  font-size: 32rpx;
}
</style>
