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
        <text class="label">维修人员</text>
        <text class="value">{{ detail.assignToName || '待指派' }}</text>
      </view>
      <view class="row">
        <text class="label">提交时间</text>
        <text class="value">{{ formatDateTime(detail.createTime) }}</text>
      </view>

      <view class="block">
        <text class="label">故障描述</text>
        <text class="content">{{ detail.faultDesc }}</text>
      </view>

      <view v-if="detail.processDesc" class="block">
        <text class="label">处理说明</text>
        <text class="content">{{ detail.processDesc }}</text>
      </view>

      <view v-if="detail.transferReason" class="block">
        <text class="label">转单原因</text>
        <text class="content">{{ detail.transferReason }}</text>
      </view>

      <view v-if="detail.rating" class="block">
        <text class="label">我的评价</text>
        <text class="content">{{ renderStars(detail.rating) }} {{ detail.comment || '' }}</text>
      </view>
    </view>

    <view v-if="canEvaluate" class="card">
      <text class="card-title">服务评价</text>
      <view class="stars">
        <text
          v-for="n in 5"
          :key="n"
          class="star"
          :class="{ on: n <= rating }"
          @click="rating = n"
        >★</text>
      </view>
      <textarea
        v-model="comment"
        class="textarea"
        placeholder="说说本次维修服务（选填）"
        maxlength="200"
      />
      <button class="primary-btn" :disabled="submitting" @click="submitEvaluate">提交评价</button>
    </view>

    <button v-if="canCancel" class="danger-btn" @click="confirmCancel">取消工单</button>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/pages/repair/detail.vue
 * @Description: 业主端工单详情，承载取消工单与服务评价两类业主专属操作
 */

import { onLoad } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import {
  REPAIR_PRIORITY_NAME,
  REPAIR_STATUS,
  REPAIR_STATUS_NAME,
  cancelOrder,
  evaluateOrder,
  formatDateTime,
  getRepairOrder,
  resolveStatusName,
  type RepairOrderDetail
} from '@community/shared'

const orderId = ref<number>(0)
const detail = ref<RepairOrderDetail | null>(null)
const rating = ref<number>(5)
const comment = ref<string>('')
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

/**
 * 只允许取消"尚未被维修工接手"的工单。
 * 后端对 cancel 的状态前置校验同样如此，此处提前置灰按钮，
 * 避免用户点了之后才收到一条"状态不允许此操作"的报错
 */
const canCancel = computed<boolean>(() => {
  const status = detail.value?.status
  return status === REPAIR_STATUS.PENDING_ACCEPT || status === REPAIR_STATUS.PENDING_ASSIGN
})

const canEvaluate = computed<boolean>(() => detail.value?.status === REPAIR_STATUS.PENDING_EVALUATE)

const renderStars = (score: number): string => '★'.repeat(Math.max(0, Math.min(5, score)))

const confirmCancel = (): void => {
  uni.showModal({
    title: '取消工单',
    content: '确认取消该报修工单吗？取消后不可恢复',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await cancelOrder(orderId.value)
        uni.showToast({ title: '工单已取消', icon: 'success' })
        await loadDetail()
      } catch (error) {
        uni.showToast({
          title: error instanceof Error ? error.message : '取消失败',
          icon: 'none'
        })
      }
    }
  })
}

const submitEvaluate = async (): Promise<void> => {
  if (submitting.value) return
  submitting.value = true
  try {
    await evaluateOrder(orderId.value, { rating: rating.value, comment: comment.value.trim() })
    uni.showToast({ title: '评价成功', icon: 'success' })
    await loadDetail()
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '评价失败',
      icon: 'none'
    })
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

.stars {
  display: flex;
  margin-bottom: 20rpx;
}

.star {
  font-size: 48rpx;
  color: #e2e8f0;
  margin-right: 16rpx;
}

.star.on {
  color: #fbbf24;
}

.textarea {
  width: 100%;
  min-height: 160rpx;
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

.danger-btn {
  height: 88rpx;
  line-height: 88rpx;
  background: #ffffff;
  color: #e11d48;
  border: 2rpx solid #fecdd3;
  border-radius: 44rpx;
  font-size: 30rpx;
}
</style>
