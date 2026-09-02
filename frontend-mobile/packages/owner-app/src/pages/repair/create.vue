<template>
  <view class="page">
    <view class="card">
      <view class="form-item">
        <text class="label">报修地址 <text class="required">*</text></text>
        <input v-model="form.address" class="input" placeholder="请输入报修地址" maxlength="200" />
        <text class="link" @click="chooseAddress">从我的地址选择</text>
      </view>

      <view class="form-item">
        <text class="label">故障描述 <text class="required">*</text></text>
        <textarea
          v-model="form.faultDesc"
          class="textarea"
          placeholder="请描述故障现象，便于维修工提前准备工具"
          maxlength="500"
        />
      </view>

      <view class="form-item">
        <text class="label">关联设备</text>
        <picker :range="deviceNames" :value="deviceIndex" @change="onDeviceChange">
          <view class="picker">{{ deviceIndex >= 0 ? deviceNames[deviceIndex] : '请选择设备（选填）' }}</view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">紧急程度</text>
        <view class="seg">
          <text
            class="seg-item"
            :class="{ on: form.priority === REPAIR_PRIORITY.NORMAL }"
            @click="form.priority = REPAIR_PRIORITY.NORMAL"
          >普通</text>
          <text
            class="seg-item"
            :class="{ on: form.priority === REPAIR_PRIORITY.URGENT }"
            @click="form.priority = REPAIR_PRIORITY.URGENT"
          >紧急</text>
        </view>
      </view>

      <view class="form-item">
        <text class="label">预约上门</text>
        <picker mode="date" :value="form.appointDate" @change="onDateChange">
          <view class="picker">{{ form.appointDate || '请选择日期（选填）' }}</view>
        </picker>
      </view>
    </view>

    <button class="primary-btn" :disabled="submitting" @click="submit">提交报修</button>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/pages/repair/create.vue
 * @Description: 业主端提交报修，字段与后端 RepairOrderCreateRequest 的校验约束对齐
 */

import { onMounted, reactive, ref, computed } from 'vue'
import {
  REPAIR_PRIORITY,
  createRepairOrder,
  getDevices,
  getMyAddresses,
  type Device
} from '@community/shared'

/** 设备下拉一次拉取的条数上限，超出后不再分页：社区设备量级有限，分页会打断填写流程 */
const DEVICE_FETCH_SIZE = 200

const form = reactive({
  deviceId: null as number | null,
  address: '',
  faultDesc: '',
  priority: REPAIR_PRIORITY.NORMAL as number,
  appointDate: ''
})

const devices = ref<Device[]>([])
const deviceIndex = ref<number>(-1)
const submitting = ref<boolean>(false)

const deviceNames = computed<string[]>(() => devices.value.map((device) => device.name))

const loadDevices = async (): Promise<void> => {
  try {
    const res = await getDevices({ pageSize: DEVICE_FETCH_SIZE }, { silent: true, quiet: true })
    devices.value = res.data?.records ?? []
  } catch {
    // 设备是选填项，列表拉取失败不阻断报修，用户仍可直接填写地址与描述
  }
}

const chooseAddress = async (): Promise<void> => {
  try {
    const res = await getMyAddresses({ silent: true })
    const addresses = res.data ?? []
    if (addresses.length === 0) {
      uni.showToast({ title: '暂无可选地址，请手动输入', icon: 'none' })
      return
    }
    uni.showActionSheet({
      itemList: addresses.map((item) => item.address),
      success: (result) => {
        const picked = addresses[result.tapIndex]
        if (picked) {
          form.address = picked.address
        }
      }
    })
  } catch {
    uni.showToast({ title: '地址加载失败', icon: 'none' })
  }
}

const onDeviceChange = (event: { detail: { value: number | string } }): void => {
  deviceIndex.value = Number(event.detail.value)
  form.deviceId = devices.value[deviceIndex.value]?.id ?? null
}

const onDateChange = (event: { detail: { value: string } }): void => {
  form.appointDate = event.detail.value ?? ''
}

const validate = (): boolean => {
  if (!form.address.trim()) {
    uni.showToast({ title: '请输入报修地址', icon: 'none' })
    return false
  }
  if (!form.faultDesc.trim()) {
    uni.showToast({ title: '请输入故障描述', icon: 'none' })
    return false
  }
  return true
}

const submit = async (): Promise<void> => {
  if (submitting.value || !validate()) return
  submitting.value = true
  try {
    await createRepairOrder({
      deviceId: form.deviceId,
      address: form.address.trim(),
      faultDesc: form.faultDesc.trim(),
      priority: form.priority,
      // 日期选择器产出 "YYYY-MM-DD"，后端 appointTime 为字符串类型，直接透传
      appointTime: form.appointDate || undefined
    })
    uni.showToast({ title: '报修提交成功', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/repair/list' }), 800)
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '提交失败，请重试',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void loadDevices()
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
  padding: 12rpx 28rpx 28rpx;
  margin-bottom: 32rpx;
}

.form-item {
  padding: 24rpx 0;
  border-bottom: 2rpx solid #f8fafc;
}

.label {
  display: block;
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 16rpx;
}

.required {
  color: #e11d48;
}

.input,
.picker {
  width: 100%;
  min-height: 72rpx;
  line-height: 72rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #1e293b;
  box-sizing: border-box;
}

.textarea {
  width: 100%;
  min-height: 200rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.link {
  display: inline-block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #0066ff;
}

.seg {
  display: flex;
}

.seg-item {
  flex: 1;
  text-align: center;
  height: 72rpx;
  line-height: 72rpx;
  background: #f8fafc;
  color: #64748b;
  font-size: 28rpx;
  border-radius: 12rpx;
}

.seg-item.on {
  background: #0066ff;
  color: #ffffff;
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
