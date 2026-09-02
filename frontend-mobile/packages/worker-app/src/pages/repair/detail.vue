<template>
  <view class="page">
    <view v-if="detail" class="card">
      <view class="row">
        <text class="label">工单号</text>
        <text class="value">{{ detail.orderNo }}</text>
      </view>
      <view class="row">
        <text class="label">当前状态</text>
        <text class="value status">{{ resolveStatusName(REPAIR_STATUS_NAME, detail.status) }}</text>
      </view>
      <view class="row">
        <text class="label">紧急程度</text>
        <text class="value">{{ resolveStatusName(REPAIR_PRIORITY_NAME, detail.priority) }}</text>
      </view>
      <view class="row">
        <text class="label">报修地址</text>
        <text class="value">{{ detail.address }}</text>
      </view>
      <view class="row">
        <text class="label">关联设备</text>
        <text class="value">{{ detail.deviceName || '未关联' }}</text>
      </view>
      <view class="row">
        <text class="label">报修人</text>
        <text class="value">{{ detail.userName || '—' }}</text>
      </view>
      <view class="row">
        <text class="label">联系电话</text>
        <text class="value">{{ detail.userPhone || '—' }}</text>
      </view>
      <view class="row">
        <text class="label">预约时间</text>
        <text class="value">{{ formatDateTime(detail.appointTime) || '未预约' }}</text>
      </view>

      <view class="block">
        <text class="label">故障描述</text>
        <text class="content">{{ detail.faultDesc }}</text>
      </view>

      <view v-if="detail.processDesc" class="block">
        <text class="label">处理说明</text>
        <text class="content">{{ detail.processDesc }}</text>
      </view>
    </view>

    <view v-if="canComplete" class="card">
      <text class="card-title">填写处理说明</text>
      <textarea
        v-model="processDesc"
        class="textarea"
        placeholder="请描述处理过程与结果，业主将据此评价服务"
        maxlength="500"
      />
      <button class="primary-btn" :disabled="submitting" @click="submitComplete">完成工单</button>
    </view>

    <button v-if="canAccept" class="primary-btn" @click="submitAccept">接单</button>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/repair/detail.vue
 * @Description: 维修工端工单详情，承载接单与完成两类维修工专属操作
 */

import { onLoad } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import {
  REPAIR_PRIORITY_NAME,
  REPAIR_STATUS,
  REPAIR_STATUS_NAME,
  acceptOrder,
  completeOrder,
  formatDateTime,
  getRepairOrder,
  resolveStatusName,
  type RepairOrderDetail
} from '@community/shared'

const orderId = ref<number>(0)
const detail = ref<RepairOrderDetail | null>(null)
const processDesc = ref<string>('')
const submitting = ref<boolean>(false)

const loadDetail = async (): Promise<void> => {
  try {
    const res = await getRepairOrder(orderId.value, { silent: true })
    detail.value = res.data
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '工单加载失败',
      icon: 'none'
    })
  }
}

const showError = (error: unknown, fallback: string): void => {
  uni.showToast({ title: error instanceof Error ? error.message : fallback, icon: 'none' })
}

/**
 * 后端对两个动作的状态前置校验：
 * accept 要求 status=2（待处理），complete 要求已接单且 status=3（处理中）。
 * 此处按同一规则控制按钮显隐，避免用户点了才收到"状态不允许此操作"
 *
 * 转单与取消不在此提供：后端 transfer/cancel 仅对系统管理员与业主开放，
 * 维修工调用会被 403 拦下，放上来只会产生无效操作
 */
const canAccept = computed<boolean>(() => detail.value?.status === REPAIR_STATUS.PENDING_PROCESS)

const canComplete = computed<boolean>(() => detail.value?.status === REPAIR_STATUS.PROCESSING)

const submitAccept = async (): Promise<void> => {
  if (submitting.value) return
  submitting.value = true
  try {
    await acceptOrder(orderId.value)
    uni.showToast({ title: '接单成功', icon: 'success' })
    await loadDetail()
  } catch (error) {
    showError(error, '接单失败')
  } finally {
    submitting.value = false
  }
}

const submitComplete = async (): Promise<void> => {
  if (submitting.value) return
  if (!processDesc.value.trim()) {
    uni.showToast({ title: '请填写处理说明', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await completeOrder(orderId.value, { processDesc: processDesc.value.trim() })
    uni.showToast({ title: '工单已完成', icon: 'success' })
    await loadDetail()
  } catch (error) {
    showError(error, '提交失败')
  } finally {
    submitting.value = false
  }
}

onLoad((options?: Record<string, string>) => {
  const id = Number(options?.id)
  // 本页只能由工单列表携带 id 跳入。缺失或非法时直接返回，
  // 否则会带着 NaN 请求 /repair-orders/NaN，得到一条用户看不懂的 404
  if (!Number.isFinite(id) || id <= 0) {
    uni.showToast({ title: '工单参数无效', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 800)
    return
  }
  orderId.value = id
  void loadDetail()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 24rpx;
}

.card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #f8fafc;
}

.label {
  width: 180rpx;
  font-size: 26rpx;
  color: #64748b;
  flex-shrink: 0;
}

.value {
  flex: 1;
  text-align: right;
  font-size: 26rpx;
  color: #1e293b;
}

.status {
  color: #0066ff;
  font-weight: 600;
}

.block {
  padding: 20rpx 0;
  border-bottom: 2rpx solid #f8fafc;
}

.content {
  display: block;
  margin-top: 12rpx;
  font-size: 28rpx;
  color: #1e293b;
  line-height: 1.6;
}

.card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20rpx;
}

.textarea {
  width: 100%;
  min-height: 200rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
}

.primary-btn {
  height: 88rpx;
  line-height: 88rpx;
  background: #0066ff;
  color: #ffffff;
  border-radius: 44rpx;
  font-size: 30rpx;
}
</style>
