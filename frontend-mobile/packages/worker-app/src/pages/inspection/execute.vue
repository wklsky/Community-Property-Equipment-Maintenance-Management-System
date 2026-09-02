<template>
  <view class="page">
    <view v-if="task" class="card">
      <view class="row">
        <text class="label">巡检计划</text>
        <text class="value">{{ task.planName }}</text>
      </view>
      <view class="row">
        <text class="label">任务日期</text>
        <text class="value">{{ formatDate(task.taskDate) || '—' }}</text>
      </view>
      <view class="row">
        <text class="label">设备ID</text>
        <text class="value">{{ task.deviceId || '—' }}</text>
      </view>
      <view class="row">
        <text class="label">当前状态</text>
        <text class="value status">
          {{ resolveStatusName(INSPECTION_TASK_STATUS_NAME, task.status) }}
        </text>
      </view>
    </view>

    <view class="card">
      <text class="card-title">巡检结果 <text class="required">*</text></text>
      <view class="seg">
        <text
          class="seg-item"
          :class="{ on: result === INSPECTION_RESULT.NORMAL }"
          @click="result = INSPECTION_RESULT.NORMAL"
        >正常</text>
        <text
          class="seg-item"
          :class="{ on: result === INSPECTION_RESULT.ABNORMAL }"
          @click="result = INSPECTION_RESULT.ABNORMAL"
        >异常</text>
      </view>

      <text v-if="result === INSPECTION_RESULT.ABNORMAL" class="warn">
        提交异常结果后，系统将自动生成一张维修工单并通知管理员
      </text>

      <textarea
        v-model="remark"
        class="textarea"
        placeholder="巡检备注（选填），异常时建议说明具体情况"
        maxlength="500"
      />

      <button class="primary-btn" :disabled="submitting" @click="submit">提交巡检记录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/inspection/execute.vue
 * @Description: 维修工端执行巡检，提交结果（异常将触发生成维修工单）
 */

import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import {
  INSPECTION_RESULT,
  INSPECTION_TASK_STATUS_NAME,
  completeInspectionTask,
  formatDate,
  getInspectionTask,
  resolveStatusName,
  type InspectionTask
} from '@community/shared'

const taskId = ref<number>(0)
const task = ref<InspectionTask | null>(null)
const result = ref<number>(INSPECTION_RESULT.NORMAL)
const remark = ref<string>('')
const submitting = ref<boolean>(false)

const loadTask = async (): Promise<void> => {
  try {
    const res = await getInspectionTask(taskId.value, { silent: true })
    task.value = res.data
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '任务加载失败',
      icon: 'none'
    })
  }
}

const submit = async (): Promise<void> => {
  if (submitting.value) return
  submitting.value = true
  try {
    await completeInspectionTask(taskId.value, {
      result: result.value,
      remark: remark.value.trim() || undefined
    })
    uni.showToast({ title: '巡检已提交', icon: 'success' })
    // 回列表页而非停留：任务已流转到已完成，继续停留会让用户重复提交同一条记录
    setTimeout(() => uni.navigateBack(), 800)
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '提交失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

onLoad((options?: Record<string, string>) => {
  const id = Number(options?.id)
  // 本页只能由巡检列表携带 id 跳入。缺失或非法时直接返回，
  // 否则会带着 NaN 请求 /inspections/tasks/NaN，得到一条用户看不懂的 404
  if (!Number.isFinite(id) || id <= 0) {
    uni.showToast({ title: '巡检任务参数无效', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 800)
    return
  }
  taskId.value = id
  void loadTask()
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

.card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20rpx;
}

.required {
  color: #e11d48;
}

.seg {
  display: flex;
  margin-bottom: 20rpx;
}

.seg-item {
  flex: 1;
  text-align: center;
  height: 80rpx;
  line-height: 80rpx;
  background: #f8fafc;
  color: #64748b;
  font-size: 29rpx;
  border-radius: 12rpx;
}

.seg-item.on {
  background: #0066ff;
  color: #ffffff;
}

.warn {
  display: block;
  background: #fff7ed;
  color: #c2410c;
  font-size: 25rpx;
  line-height: 1.6;
  padding: 20rpx;
  border-radius: 12rpx;
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
  height: 96rpx;
  line-height: 96rpx;
  background: #0066ff;
  color: #ffffff;
  border-radius: 48rpx;
  font-size: 32rpx;
}
</style>
