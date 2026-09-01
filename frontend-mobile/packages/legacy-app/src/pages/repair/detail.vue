<template>
  <view class="detail-page">
    <view class="detail-card" v-if="order.id">
      <view class="detail-header">
        <text class="order-no">{{ order.orderNo }}</text>
        <view class="order-status" :class="'status-' + order.status">
          {{ statusMap[order.status] }}
        </view>
      </view>

      <view class="detail-section">
        <view class="detail-item">
          <text class="label">地址</text>
          <text class="value">{{ order.address }}</text>
        </view>
        <view class="detail-item">
          <text class="label">优先级</text>
          <text class="value" :class="order.priority === 1 ? 'urgent' : ''">
            {{ order.priority === 0 ? '普通' : '紧急' }}
          </text>
        </view>
        <view class="detail-item" v-if="order.assignToName">
          <text class="label">维修工</text>
          <text class="value">{{ order.assignToName }}</text>
        </view>
        <view class="detail-item">
          <text class="label">故障描述</text>
          <text class="value">{{ order.faultDesc }}</text>
        </view>
        <view class="detail-item" v-if="order.transferReason">
          <text class="label">{{ order.status === 6 ? '拒绝/取消原因' : '转派/备注原因' }}</text>
          <text class="value reason-text">{{ order.transferReason }}</text>
        </view>
        <view class="detail-item" v-if="order.processDesc">
          <text class="label">处理说明</text>
          <text class="value">{{ order.processDesc }}</text>
        </view>
        <view class="detail-item">
          <text class="label">创建时间</text>
          <text class="value">{{ order.createTime }}</text>
        </view>
        <view class="detail-item" v-if="order.finishTime">
          <text class="label">完成时间</text>
          <text class="value">{{ order.finishTime }}</text>
        </view>

        <!-- 已评价信息展示 -->
        <view class="evaluation-display" v-if="order.rating">
          <view class="eval-divider"></view>
          <text class="label">我的评价</text>
          <view class="eval-stars-display">
            <text v-for="i in 5" :key="i" class="star-display" :class="{ active: i <= order.rating }">★</text>
          </view>
          <text class="eval-comment-display" v-if="order.comment">{{ order.comment }}</text>
        </view>
      </view>
    </view>

    <!-- 完成工单表单 -->
    <view class="action-card" v-if="action === 'complete'">
      <text class="action-title">完成工单</text>
      <textarea v-model="processDesc" placeholder="请输入处理说明（必填）" />
      <button class="btn-primary" @click="handleComplete">确认完成</button>
    </view>

    <!-- 评价表单 -->
    <view class="action-card" v-if="action === 'evaluate'">
      <text class="action-title">评价工单</text>
      <view class="rating-section">
        <text class="rating-label">评分</text>
        <view class="stars-input">
          <text
            v-for="i in 5"
            :key="i"
            class="star-icon"
            :class="{ active: i <= rating }"
            @click="rating = i"
          >★</text>
        </view>
        <text class="rating-hint">{{ ratingHint }}</text>
      </view>
      <textarea v-model="comment" placeholder="请输入评价内容（选填）" />
      <button class="btn-primary" @click="handleEvaluate">提交评价</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getRepairOrder, completeOrder, evaluateOrder } from '../../api/repair'

const order = ref({})
const action = ref('')
const processDesc = ref('')
const rating = ref(5)
const comment = ref('')
const orderId = ref('')

const statusMap = {
  0: '待受理', 1: '待派单', 2: '待处理', 3: '处理中', 4: '待评价', 5: '已完成', 6: '已取消', 7: '转单中'
}

const ratingHint = computed(() => {
  const hints = ['', '非常不满意', '不满意', '一般', '满意', '非常满意']
  return hints[rating.value] || ''
})

onLoad((options) => {
  orderId.value = options.id
  action.value = options.action || ''
  loadOrderDetail()
})

const loadOrderDetail = async () => {
  if (!orderId.value) return
  try {
    const res = await getRepairOrder(orderId.value)
    order.value = res.data
  } catch (e) {
    console.error('获取工单详情失败', e)
    uni.showToast({ title: '获取工单详情失败', icon: 'none' })
  }
}

const handleComplete = async () => {
  if (!processDesc.value) {
    uni.showToast({ title: '请输入处理说明', icon: 'none' })
    return
  }
  try {
    await completeOrder(order.value.id, processDesc.value)
    uni.showToast({ title: '完成成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    console.error('完成工单失败', e)
  }
}

const handleEvaluate = async () => {
  try {
    await evaluateOrder(order.value.id, rating.value, comment.value)
    uni.showToast({ title: '评价成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    console.error('评价失败', e)
  }
}
</script>

<style scoped>
.detail-page {
  padding: 20rpx;
}

.detail-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 30rpx;
}

.order-no {
  font-size: 32rpx;
  font-weight: bold;
  color: #303133;
}

.order-status {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.status-0, .status-1, .status-2, .status-7 { background: #e6a23c20; color: #e6a23c; }
.status-3 { background: #409EFF20; color: #409EFF; }
.status-4, .status-5 { background: #67c23a20; color: #67c23a; }
.status-6 { background: #f56c6c20; color: #f56c6c; }

.detail-item {
  margin-bottom: 24rpx;
}

.detail-item .label {
  display: block;
  font-size: 24rpx;
  color: #909399;
  margin-bottom: 8rpx;
}

.detail-item .value {
  display: block;
  font-size: 28rpx;
  color: #303133;
}

.detail-item .value.urgent {
  color: #f56c6c;
}

.detail-item .value.reason-text {
  color: #f56c6c;
  background: #fef0f0;
  padding: 12rpx 16rpx;
  border-radius: 8rpx;
  border-left: 6rpx solid #f56c6c;
}

.action-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.action-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #303133;
  margin-bottom: 30rpx;
}

.action-card textarea {
  width: 100%;
  height: 200rpx;
  border: 1rpx solid #e4e7ed;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  margin-bottom: 30rpx;
}

.evaluation-display {
  margin-top: 20rpx;
}

.eval-divider {
  height: 2rpx;
  background: #F1F5F9;
  margin-bottom: 24rpx;
}

.eval-stars-display {
  display: flex;
  gap: 6rpx;
  margin-top: 12rpx;
  margin-bottom: 8rpx;
}

.star-display {
  font-size: 36rpx;
  color: #E2E8F0;
}

.star-display.active {
  color: #F59E0B;
}

.eval-comment-display {
  display: block;
  font-size: 26rpx;
  color: #475569;
  line-height: 1.5;
  padding: 16rpx;
  background: #F8FAFC;
  border-radius: 12rpx;
  margin-top: 8rpx;
}

.rating-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

.rating-label {
  font-size: 28rpx;
  color: #303133;
  font-weight: 500;
  margin-bottom: 16rpx;
}

.stars-input {
  display: flex;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.star-icon {
  font-size: 56rpx;
  color: #E2E8F0;
  transition: all 0.15s ease;
}

.star-icon:active {
  transform: scale(1.2);
}

.star-icon.active {
  color: #F59E0B;
}

.rating-hint {
  font-size: 24rpx;
  color: #94A3B8;
}
</style>
