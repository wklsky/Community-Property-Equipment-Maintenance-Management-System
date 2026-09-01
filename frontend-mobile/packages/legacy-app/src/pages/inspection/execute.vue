<template>
  <view class="execute-page">
    <!-- Page Header -->
    <view class="page-header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="header-title">巡检执行</text>
      <view class="header-right"></view>
    </view>

    <!-- Task Info Card -->
    <view class="task-info-card" v-if="task">
      <view class="info-header">
        <text class="info-title">{{ task.planName || '巡检任务' }}</text>
        <view class="status-badge" :class="getStatusClass(task.status)">
          <text>{{ statusMap[task.status] }}</text>
        </view>
      </view>

      <view class="info-details">
        <view class="detail-row">
          <text class="detail-label">📅 巡检日期</text>
          <text class="detail-value">{{ formatDate(task.taskDate) }}</text>
        </view>
        <view class="detail-row" v-if="task.buildingName">
          <text class="detail-label">🏢 巡检楼栋</text>
          <text class="detail-value">{{ task.buildingName }}</text>
        </view>
        <view class="detail-row" v-if="task.categoryName">
          <text class="detail-label">🔧 设备类型</text>
          <text class="detail-value">{{ task.categoryName }}</text>
        </view>
        <view class="detail-row" v-if="task.deviceName">
          <text class="detail-label">⚙️ 巡检设备</text>
          <text class="detail-value">{{ task.deviceName }}</text>
        </view>
      </view>
    </view>

    <!-- 已完成状态：显示巡检记录 -->
    <view class="record-section" v-if="task && task.status === 2">
      <view class="section-title">
        <text>巡检记录</text>
      </view>

      <view class="record-card" v-for="record in records" :key="record.id">
        <view class="record-header">
          <view class="result-badge" :class="record.result === 0 ? 'normal' : 'abnormal'">
            <text>{{ record.result === 0 ? '✓ 正常' : '⚠ 异常' }}</text>
          </view>
          <text class="record-time">{{ formatTime(record.createTime) }}</text>
        </view>
        <view class="record-remark" v-if="record.remark">
          <text class="remark-label">备注：</text>
          <text class="remark-text">{{ record.remark }}</text>
        </view>
      </view>

      <view class="empty-record" v-if="records.length === 0">
        <text>暂无巡检记录</text>
      </view>
    </view>

    <!-- 执行巡检表单 -->
    <view class="execute-form" v-if="task && task.status !== 2">
      <view class="section-title">
        <text>巡检结果</text>
      </view>

      <!-- 巡检结果选择 -->
      <view class="form-item">
        <view class="label-row">
          <text class="label">设备状态</text>
          <text class="required">*</text>
        </view>
        <view class="result-options">
          <view
            class="result-option normal"
            :class="{ active: form.result === 0 }"
            @click="form.result = 0"
          >
            <view class="option-icon">✓</view>
            <view class="option-content">
              <text class="option-title">正常</text>
              <text class="option-desc">设备运行正常，无异常情况</text>
            </view>
            <view class="option-check" v-if="form.result === 0">
              <text>✓</text>
            </view>
          </view>
          <view
            class="result-option abnormal"
            :class="{ active: form.result === 1 }"
            @click="form.result = 1"
          >
            <view class="option-icon">⚠</view>
            <view class="option-content">
              <text class="option-title">异常</text>
              <text class="option-desc">发现问题，需要维修或关注</text>
            </view>
            <view class="option-check" v-if="form.result === 1">
              <text>✓</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 备注输入 -->
      <view class="form-item">
        <view class="label-row">
          <text class="label">巡检备注</text>
          <text class="optional" v-if="form.result === 0">（选填）</text>
          <text class="required" v-else>*</text>
        </view>
        <textarea
          v-model="form.remark"
          :placeholder="form.result === 1 ? '请详细描述发现的异常情况' : '可填写巡检过程中的观察记录'"
          :maxlength="500"
          :class="{ error: errors.remark }"
        />
        <view class="textarea-footer">
          <text class="error-tip" v-if="errors.remark">{{ errors.remark }}</text>
          <text class="char-count">{{ form.remark.length }}/500</text>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section" v-if="task && task.status !== 2">
      <button
        class="submit-btn"
        :class="{ disabled: !canSubmit }"
        :loading="loading"
        :disabled="!canSubmit || loading"
        @click="handleSubmit"
      >
        <text v-if="!loading">提交巡检结果</text>
        <text v-else>提交中...</text>
      </button>
      <text class="submit-tip">提交后将同步更新巡检记录</text>
    </view>

    <!-- 返回按钮（已完成状态） -->
    <view class="back-section" v-if="task && task.status === 2">
      <button class="back-btn" @click="goBack">
        <text>返回列表</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getInspectionTask, completeInspectionTask, getTaskRecords } from '../../api/inspection'

const taskId = ref(null)
const task = ref(null)
const records = ref([])
const loading = ref(false)

const form = reactive({
  result: null,
  remark: ''
})

const errors = reactive({
  remark: ''
})

const statusMap = {
  0: '待接单',
  1: '进行中',
  2: '已完成'
}

const getStatusClass = (status) => {
  if (status === 0) return 'pending'
  if (status === 1) return 'processing'
  return 'completed'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
}

const canSubmit = computed(() => {
  if (form.result === null) return false

  if (form.result === 1 && !form.remark.trim()) return false
  return true
})

onLoad((options) => {
  if (options.id) {
    taskId.value = options.id
    loadTask()
  }
})

const loadTask = async () => {
  try {
    const res = await getInspectionTask(taskId.value)
    task.value = res.data

    if (task.value.status === 2) {
      loadRecords()
    }
  } catch (e) {
    console.error('加载任务详情失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const loadRecords = async () => {
  try {
    const res = await getTaskRecords(taskId.value, { pageNum: 1, pageSize: 10 })
    records.value = res.data?.records || []
  } catch (e) {
    console.error('加载巡检记录失败', e)
  }
}

const validateForm = () => {
  let valid = true

  if (form.result === null) {
    uni.showToast({ title: '请选择设备状态', icon: 'none' })
    return false
  }

  if (form.result === 1 && !form.remark.trim()) {
    errors.remark = '发现异常时必须填写备注说明'
    valid = false
  } else {
    errors.remark = ''
  }

  return valid
}

const handleSubmit = async () => {
  if (!validateForm()) return

  loading.value = true
  try {
    await completeInspectionTask(taskId.value, {
      result: form.result,
      remark: form.remark.trim()
    })

    uni.showToast({
      title: '提交成功',
      icon: 'success',
      duration: 1500
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.execute-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding-bottom: calc(200rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(200rpx + env(safe-area-inset-bottom));
}

.page-header {
  background: #FFFFFF;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2rpx solid #F1F5F9;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 48rpx;
  color: #1E293B;
  font-weight: 300;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1E293B;
}

.header-right {
  width: 60rpx;
}

.task-info-card {
  margin: 24rpx 32rpx;
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #F1F5F9;
}

.info-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1E293B;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.status-badge.pending {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.status-badge.processing {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.status-badge.completed {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.info-details {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-size: 28rpx;
  color: #64748B;
}

.detail-value {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 32rpx;
  font-weight: 700;
  color: #1E293B;
  margin: 32rpx 32rpx 24rpx;
}

.section-title::before {
  content: '';
  width: 8rpx;
  height: 32rpx;
  background: #0066FF;
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.record-section {
  padding: 0 32rpx;
}

.record-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  border: 2rpx solid #F1F5F9;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.result-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 26rpx;
  font-weight: 600;
}

.result-badge.normal {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.result-badge.abnormal {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.record-time {
  font-size: 24rpx;
  color: #94A3B8;
}

.record-remark {
  background: #F8FAFC;
  border-radius: 12rpx;
  padding: 16rpx;
}

.remark-label {
  font-size: 24rpx;
  color: #64748B;
}

.remark-text {
  font-size: 26rpx;
  color: #1E293B;
}

.empty-record {
  text-align: center;
  padding: 48rpx;
  color: #94A3B8;
  font-size: 28rpx;
}

.execute-form {
  padding: 0 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.label-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.label {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
}

.required {
  color: #EF4444;
  margin-left: 8rpx;
}

.optional {
  font-size: 24rpx;
  color: #94A3B8;
  margin-left: 8rpx;
}

.result-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.result-option {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  background: #FFFFFF;
  border: 2rpx solid #E2E8F0;
  border-radius: 20rpx;
  transition: all 0.2s ease;
}

.result-option.active {
  border-color: #0066FF;
  background: #E6F0FF;
}

.result-option.normal.active {
  border-color: #10B981;
  background: rgba(16, 185, 129, 0.1);
}

.result-option.abnormal.active {
  border-color: #EF4444;
  background: rgba(239, 68, 68, 0.1);
}

.option-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  background: #F8FAFC;
}

.result-option.normal .option-icon {
  color: #10B981;
}

.result-option.abnormal .option-icon {
  color: #EF4444;
}

.option-content {
  flex: 1;
}

.option-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 4rpx;
}

.option-desc {
  display: block;
  font-size: 24rpx;
  color: #64748B;
}

.option-check {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #FFFFFF;
}

.result-option.normal .option-check {
  background: #10B981;
}

.result-option.abnormal .option-check {
  background: #EF4444;
}

.form-item textarea {
  width: 100%;
  height: 200rpx;
  background: #FFFFFF;
  border: 2rpx solid #E2E8F0;
  border-radius: 20rpx;
  padding: 24rpx;
  font-size: 30rpx;
  color: #1E293B;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.form-item textarea:focus {
  border-color: #0066FF;
  box-shadow: 0 0 0 6rpx rgba(0, 102, 255, 0.1);
}

.form-item textarea.error {
  border-color: #EF4444;
  background: #FEF2F2;
}

.textarea-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
}

.char-count {
  font-size: 24rpx;
  color: #94A3B8;
}

.error-tip {
  font-size: 24rpx;
  color: #EF4444;
}

.submit-section {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #FFFFFF;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.submit-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  color: #FFFFFF;
  font-size: 34rpx;
  font-weight: 600;
  letter-spacing: 4rpx;
  border: none;
  border-radius: 50rpx;
  box-shadow: 0 8rpx 24rpx rgba(16, 185, 129, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-btn.disabled {
  background: #CBD5E1;
  box-shadow: none;
}

.submit-btn:active:not(.disabled) {
  transform: scale(0.98);
}

.submit-tip {
  display: block;
  text-align: center;
  font-size: 24rpx;
  color: #94A3B8;
  margin-top: 16rpx;
}

.back-section {
  padding: 32rpx;
}

.back-btn {
  width: 100%;
  height: 96rpx;
  background: #F1F5F9;
  color: #64748B;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn:active {
  background: #E2E8F0;
}
</style>
