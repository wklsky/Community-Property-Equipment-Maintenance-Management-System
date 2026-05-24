<template>
  <view class="create-page">
    <!-- 表单卡片 -->
    <view class="form-card">
      <!-- 地址输入 -->
      <view class="form-item">
        <view class="label-row">
          <text class="label">报修地址</text>
          <text class="required">*</text>
          <view class="manage-link" v-if="savedAddresses.length > 0" @click="goToManageAddresses">
            <text>管理</text>
          </view>
        </view>

        <!-- 默认地址卡片 -->
        <view class="default-address-card" v-if="defaultAddress && !selectedAddressKey" @click="selectAddress(defaultAddress)">
          <view class="default-card-left">
            <view class="default-card-icon">🏠</view>
            <view class="default-card-info">
              <view class="default-card-label">
                <text class="default-badge-tag">默认地址</text>
              </view>
              <text class="default-card-text">{{ defaultAddress.label }}</text>
            </view>
          </view>
          <view class="default-card-right">
            <text class="default-card-check">✓</text>
            <text class="default-card-hint">已自动选择</text>
          </view>
        </view>

        <!-- 已保存地址快捷选择 -->
        <view class="address-chips" v-if="savedAddresses.length > 0">
          <text class="chips-label">已保存地址</text>
          <view class="chips-row">
            <view
              class="address-chip"
              :class="{ active: selectedAddressKey === (item.type === 'property' ? 'p' : 'c') + '-' + item.id }"
              v-for="item in savedAddresses"
              :key="item.type + '-' + item.id"
              @click="selectAddress(item)"
            >
              <text class="chip-icon">{{ item.type === 'property' ? '🏢' : '📍' }}</text>
              <text class="chip-text">{{ item.label }}</text>
              <text class="chip-check" v-if="selectedAddressKey === (item.type === 'property' ? 'p' : 'c') + '-' + item.id">✓</text>
            </view>
          </view>
        </view>

        <!-- 无保存地址提示 -->
        <view class="no-address-tip" v-if="savedAddresses.length === 0" @click="goToAddAddress">
          <text class="tip-icon">💡</text>
          <text class="tip-text">添加常用地址，报修时无需重复填写</text>
          <text class="tip-arrow">›</text>
        </view>

        <view class="input-wrapper" :class="{ error: errors.address }">
          <text class="input-icon">📍</text>
          <input
            v-model="form.address"
            placeholder="请输入详细地址（如：1栋2单元301室）"
            maxlength="100"
            @blur="validateAddress"
            @input="onAddressInput"
          />
        </view>
        <text class="error-tip" v-if="errors.address">{{ errors.address }}</text>
      </view>

      <!-- 期望上门时间 -->
      <view class="form-item">
        <view class="label-row">
          <text class="label">期望上门时间</text>
          <text class="optional">（选填）</text>
        </view>
        <view class="appoint-row">
          <picker mode="date" :value="form.appointDate" :start="today" @change="onDateChange">
            <view class="appoint-date" :class="{ filled: form.appointDate }">
              <text class="appoint-date-icon">📅</text>
              <text class="appoint-date-text">{{ form.appointDate || '选择日期' }}</text>
            </view>
          </picker>
          <view class="appoint-periods">
            <view
              class="period-btn"
              :class="{ active: form.appointPeriod === 'am' }"
              @click="form.appointPeriod = form.appointPeriod === 'am' ? '' : 'am'"
            >
              <text>上午</text>
            </view>
            <view
              class="period-btn"
              :class="{ active: form.appointPeriod === 'pm' }"
              @click="form.appointPeriod = form.appointPeriod === 'pm' ? '' : 'pm'"
            >
              <text>下午</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 故障描述 -->
      <view class="form-item">
        <view class="label-row">
          <text class="label">故障描述</text>
          <text class="required">*</text>
        </view>
        <textarea
          v-model="form.faultDesc"
          placeholder="请详细描述故障情况，以便维修人员快速定位问题"
          :maxlength="500"
          :class="{ error: errors.faultDesc }"
          @blur="validateFaultDesc"
        />
        <view class="textarea-footer">
          <text class="error-tip" v-if="errors.faultDesc">{{ errors.faultDesc }}</text>
          <text class="char-count">{{ form.faultDesc.length }}/500</text>
        </view>
      </view>

      <!-- 图片上传 -->
      <view class="form-item">
        <view class="label-row">
          <text class="label">故障照片</text>
          <text class="optional">（选填，最多3张）</text>
        </view>
        <view class="image-upload">
          <view
            class="image-item"
            v-for="(img, index) in imageList"
            :key="index"
          >
            <image :src="img.tempPath || img.url" mode="aspectFill" @click="previewImage(index)" />
            <view class="image-delete" @click.stop="removeImage(index)">
              <text>×</text>
            </view>
            <view class="image-loading" v-if="img.uploading">
              <view class="upload-progress">
                <view class="progress-spinner"></view>
              </view>
              <text>上传中...</text>
            </view>
            <view class="image-success" v-if="img.url && !img.uploading">
              <text>✓</text>
            </view>
          </view>
          <view
            class="image-add"
            v-if="imageList.length < 3"
            @click="showImageSourcePicker"
          >
            <text class="add-icon">+</text>
            <text class="add-text">添加照片</text>
          </view>
        </view>
        <text class="image-tip">支持从相册选择或拍照上传</text>
      </view>

      <!-- 设备选择（可选） -->
      <view class="form-item">
        <view class="label-row">
          <text class="label">关联设备</text>
          <text class="optional">（选填）</text>
        </view>
        <picker :range="devices" range-key="name" @change="onDeviceChange">
          <view class="picker-field">
            <text class="picker-icon">🔧</text>
            <text class="picker-text" :class="{ placeholder: !selectedDevice }">
              {{ selectedDevice ? selectedDevice.name : '选择故障设备（可选）' }}
            </text>
            <text class="picker-arrow">▾</text>
          </view>
        </picker>
        <text class="error-tip" v-if="errors.deviceId">{{ errors.deviceId }}</text>
      </view>

      <!-- 优先级选择 -->
      <view class="form-item">
        <view class="label-row">
          <text class="label">紧急程度</text>
          <text class="required">*</text>
        </view>
        <view class="priority-options">
          <view
            class="priority-item"
            :class="{ active: form.priority === 0 }"
            @click="form.priority = 0"
          >
            <view class="priority-icon normal">🟢</view>
            <view class="priority-info">
              <text class="priority-name">普通</text>
              <text class="priority-desc">正常处理</text>
            </view>
          </view>
          <view
            class="priority-item urgent"
            :class="{ active: form.priority === 1 }"
            @click="form.priority = 1"
          >
            <view class="priority-icon urgent">🔴</view>
            <view class="priority-info">
              <text class="priority-name">紧急</text>
              <text class="priority-desc">优先处理</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button
        class="submit-btn"
        :class="{ disabled: !canSubmit }"
        :loading="loading"
        :disabled="!canSubmit || loading"
        @click="handleSubmit"
      >
        <text v-if="!loading">提交报修</text>
        <text v-else>提交中...</text>
      </button>
      <text class="submit-tip">提交后将尽快安排维修人员处理</text>
    </view>

    <!-- 图片来源选择弹窗 -->
    <view class="image-source-modal" v-if="showSourcePicker" @click="showSourcePicker = false">
      <view class="modal-content" @click.stop>
        <view class="modal-title">选择图片来源</view>
        <view class="modal-options">
          <view class="modal-option" @click="chooseFromCamera">
            <text class="option-icon">📷</text>
            <text class="option-text">拍照</text>
          </view>
          <view class="modal-option" @click="chooseFromAlbum">
            <text class="option-icon">🖼️</text>
            <text class="option-text">从相册选择</text>
          </view>
        </view>
        <view class="modal-cancel" @click="showSourcePicker = false">
          <text>取消</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createRepairOrder } from '../../api/repair'
import { getDevices } from '../../api/device'
import { getMyAddresses } from '../../api/address'
import { BASE_URL } from '../../utils/config'

const loading = ref(false)
const devices = ref([])
const selectedDevice = ref(null)
const imageList = ref([])
const showSourcePicker = ref(false)

const savedAddresses = ref([])
const selectedAddressKey = ref('')
const defaultAddress = computed(() => savedAddresses.value.find(a => a.isDefault === 1) || null)

const form = reactive({
  address: '',
  faultDesc: '',
  priority: 0,
  deviceId: null,
  images: [],
  appointDate: '',
  appointPeriod: ''
})

const today = new Date().toISOString().split('T')[0]

const errors = reactive({
  address: '',
  faultDesc: '',
  deviceId: ''
})

const canSubmit = computed(() => {
  return form.address.trim().length >= 5 &&
         form.faultDesc.trim().length >= 10 &&
         !hasUploadingImages.value
})

const hasUploadingImages = computed(() => {
  return imageList.value.some(img => img.uploading)
})

onLoad(async () => {
  await Promise.all([loadDevices(), loadAddresses()])
})

const loadDevices = async () => {
  try {
    const res = await getDevices({ pageNum: 1, pageSize: 100 })
    devices.value = res.data?.records || []
  } catch (e) {
    console.error('加载设备列表失败', e)
  }
}

const loadAddresses = async () => {
  try {
    const res = await getMyAddresses()
    const merged = (res.data || []).map(a => ({
      id: a.id,
      type: a.type,
      label: a.address,
      isDefault: a.isDefault
    }))
    savedAddresses.value = merged

    if (!selectedAddressKey.value) {
      const def = merged.find(a => a.isDefault === 1)
      if (def) {
        form.address = def.label
      }
    }
  } catch (e) {
    console.error('加载地址失败', e)
  }
}

const goToManageAddresses = () => {
  uni.navigateTo({ url: '/pages/address/list' })
}

const goToAddAddress = () => {
  uni.navigateTo({ url: '/pages/address/list' })
}

const selectAddress = (item) => {
  selectedAddressKey.value = `${item.type === 'property' ? 'p' : 'c'}-${item.id}`
  form.address = item.label
  errors.address = ''
}

const onDateChange = (e) => {
  form.appointDate = e.detail.value
}

const onDeviceChange = (e) => {
  const index = e.detail.value
  selectedDevice.value = devices.value[index]
  form.deviceId = devices.value[index]?.id || null
  errors.deviceId = ''
}

const onAddressInput = () => {
  selectedAddressKey.value = ''
}

const validateAddress = () => {
  if (!form.address.trim()) {
    errors.address = '请输入报修地址'
    return false
  } else if (form.address.trim().length < 5) {
    errors.address = '地址至少5个字符'
    return false
  }
  errors.address = ''
  return true
}

const validateFaultDesc = () => {
  if (!form.faultDesc.trim()) {
    errors.faultDesc = '请输入故障描述'
    return false
  } else if (form.faultDesc.trim().length < 10) {
    errors.faultDesc = '故障描述至少10个字符'
    return false
  }
  errors.faultDesc = ''
  return true
}

const showImageSourcePicker = () => {
  showSourcePicker.value = true
}

const chooseFromCamera = () => {
  showSourcePicker.value = false
  chooseImage('camera')
}

const chooseFromAlbum = () => {
  showSourcePicker.value = false
  chooseImage('album')
}

const chooseImage = (sourceType) => {
  const remainCount = 3 - imageList.value.length
  if (remainCount <= 0) return

  uni.chooseImage({
    count: remainCount,
    sizeType: ['compressed'],
    sourceType: [sourceType],
    success: async (res) => {
      const tempFiles = res.tempFiles
      for (const file of tempFiles) {

        if (file.size > 10 * 1024 * 1024) {
          uni.showToast({ title: '图片大小不能超过10MB', icon: 'none' })
          continue
        }

        const imgItem = {
          tempPath: file.path,
          uploading: true,
          url: '',
          fileId: null
        }
        imageList.value.push(imgItem)

        try {
          const uploadRes = await uploadImage(file.path)
          imgItem.url = uploadRes.data?.url || uploadRes.data
          imgItem.fileId = uploadRes.data?.fileId || null
          imgItem.uploading = false
          form.images.push(imgItem.url)
        } catch (e) {
          console.error('图片上传失败', e)
          imgItem.uploading = false
          uni.showToast({ title: '图片上传失败', icon: 'none' })

          const idx = imageList.value.indexOf(imgItem)
          if (idx > -1) {
            imageList.value.splice(idx, 1)
          }
        }
      }
    },
    fail: (err) => {
      if (err.errMsg && !err.errMsg.includes('cancel')) {
        uni.showToast({ title: '选择图片失败', icon: 'none' })
      }
    }
  })
}

const uploadImage = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    const baseUrl = BASE_URL.replace('/api/v1', '')

    uni.uploadFile({
      url: `${baseUrl}/api/v1/files/upload`,
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data)
            } else {
              reject(new Error(data.message || '上传失败'))
            }
          } catch (e) {
            reject(new Error('解析响应失败'))
          }
        } else if (res.statusCode === 401) {
          uni.showToast({ title: '登录已过期', icon: 'none' })
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/index' })
          }, 1500)
          reject(new Error('未授权'))
        } else {
          reject(new Error('上传失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

const previewImage = (index) => {
  const urls = imageList.value.map(img => img.tempPath || img.url)
  uni.previewImage({
    current: urls[index],
    urls: urls
  })
}

const removeImage = (index) => {
  const img = imageList.value[index]
  imageList.value.splice(index, 1)

  if (img.url) {
    const urlIndex = form.images.indexOf(img.url)
    if (urlIndex > -1) {
      form.images.splice(urlIndex, 1)
    }
  }
}

const validateForm = () => {
  const addressValid = validateAddress()
  const faultDescValid = validateFaultDesc()
  return addressValid && faultDescValid
}

const handleSubmit = async () => {
  if (!validateForm()) {
    uni.showToast({ title: '请完善表单信息', icon: 'none' })
    return
  }

  if (hasUploadingImages.value) {
    uni.showToast({ title: '请等待图片上传完成', icon: 'none' })
    return
  }

  loading.value = true
  try {
    const submitData = {
      address: form.address.trim(),
      faultDesc: form.faultDesc.trim(),
      priority: form.priority,
      deviceId: form.deviceId,
      images: form.images
    }

    if (form.appointDate && form.appointPeriod) {
      const hour = form.appointPeriod === 'am' ? '09:00:00' : '14:00:00'
      submitData.appointTime = `${form.appointDate} ${hour}`
    }

    await createRepairOrder(submitData)

    uni.showToast({
      title: '提交成功',
      icon: 'success',
      duration: 1500
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    console.error('提交报修失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.create-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding: 24rpx;
  padding-bottom: calc(200rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(200rpx + env(safe-area-inset-bottom));
}

.form-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid #F1F5F9;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-item:last-child {
  margin-bottom: 0;
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

.input-wrapper {
  display: flex;
  align-items: center;
  height: 100rpx;
  background: #F8FAFC;
  border: 2rpx solid #E2E8F0;
  border-radius: 20rpx;
  padding: 0 24rpx;
  transition: all 0.2s ease;
}

.input-wrapper:focus-within {
  background: #FFFFFF;
  border-color: #0066FF;
  box-shadow: 0 0 0 6rpx rgba(0, 102, 255, 0.1);
}

.input-wrapper.error {
  border-color: #EF4444;
  background: #FEF2F2;
}

.input-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.input-wrapper input {
  flex: 1;
  height: 100%;
  font-size: 30rpx;
  color: #1E293B;
}

.manage-link {
  margin-left: auto;
  padding: 8rpx 20rpx;
  background: #F1F5F9;
  border-radius: 20rpx;
}

.manage-link text {
  font-size: 24rpx;
  color: #0066FF;
  font-weight: 500;
}

.manage-link:active {
  background: #E2E8F0;
}

.default-address-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background: linear-gradient(135deg, #E6F0FF, #F0F7FF);
  border: 2rpx solid #0066FF;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
}

.default-address-card:active {
  opacity: 0.85;
}

.default-card-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}

.default-card-icon {
  font-size: 40rpx;
  flex-shrink: 0;
}

.default-card-info {
  flex: 1;
  min-width: 0;
}

.default-card-label {
  margin-bottom: 6rpx;
}

.default-badge-tag {
  font-size: 20rpx;
  color: #0066FF;
  font-weight: 600;
  background: rgba(0, 102, 255, 0.1);
  padding: 4rpx 14rpx;
  border-radius: 12rpx;
}

.default-card-text {
  display: block;
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.default-card-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
  flex-shrink: 0;
}

.default-card-check {
  width: 40rpx;
  height: 40rpx;
  background: #0066FF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 700;
}

.default-card-hint {
  font-size: 20rpx;
  color: #0066FF;
}

.chips-label {
  display: block;
  font-size: 24rpx;
  color: #94A3B8;
  margin-bottom: 12rpx;
}

.chips-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.no-address-tip {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx;
  background: #FFF8ED;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid rgba(245, 158, 11, 0.2);
}

.no-address-tip:active {
  background: #FFF3E0;
}

.tip-icon {
  font-size: 30rpx;
  flex-shrink: 0;
}

.tip-text {
  flex: 1;
  font-size: 26rpx;
  color: #92400E;
}

.tip-arrow {
  font-size: 32rpx;
  color: #F59E0B;
  font-weight: 700;
}

.address-chips {
  margin-bottom: 20rpx;
}

.address-chip {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 22rpx;
  background: #F8FAFC;
  border: 2rpx solid #E2E8F0;
  border-radius: 16rpx;
  transition: all 0.2s;
  max-width: 100%;
}

.address-chip.active {
  background: #E6F0FF;
  border-color: #0066FF;
}

.chip-icon {
  font-size: 26rpx;
  flex-shrink: 0;
}

.chip-text {
  font-size: 26rpx;
  color: #1E293B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chip-check {
  font-size: 22rpx;
  color: #0066FF;
  font-weight: 700;
  flex-shrink: 0;
}

.appoint-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.appoint-date {
  display: flex;
  align-items: center;
  gap: 12rpx;
  height: 80rpx;
  background: #F8FAFC;
  border: 2rpx solid #E2E8F0;
  border-radius: 16rpx;
  padding: 0 20rpx;
  white-space: nowrap;
}

.appoint-date.filled {
  border-color: #0066FF;
  background: #E6F0FF;
}

.appoint-date-icon {
  font-size: 30rpx;
}

.appoint-date-text {
  font-size: 28rpx;
  color: #94A3B8;
}

.appoint-date.filled .appoint-date-text {
  color: #1E293B;
  font-weight: 500;
}

.appoint-periods {
  display: flex;
  gap: 16rpx;
}

.period-btn {
  padding: 14rpx 32rpx;
  background: #F8FAFC;
  border: 2rpx solid #E2E8F0;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #64748B;
  transition: all 0.2s;
}

.period-btn.active {
  background: #E6F0FF;
  border-color: #0066FF;
  color: #0066FF;
  font-weight: 600;
}

.form-item textarea {
  width: 100%;
  height: 200rpx;
  background: #F8FAFC;
  border: 2rpx solid #E2E8F0;
  border-radius: 20rpx;
  padding: 24rpx;
  font-size: 30rpx;
  color: #1E293B;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.form-item textarea:focus {
  background: #FFFFFF;
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

.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.image-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.image-item image {
  width: 100%;
  height: 100%;
}

.image-delete {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 44rpx;
  height: 44rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.image-delete text {
  color: #FFFFFF;
  font-size: 32rpx;
  line-height: 1;
}

.image-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.upload-progress {
  width: 48rpx;
  height: 48rpx;
}

.progress-spinner {
  width: 100%;
  height: 100%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #FFFFFF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.image-loading text {
  color: #FFFFFF;
  font-size: 24rpx;
}

.image-success {
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  width: 36rpx;
  height: 36rpx;
  background: #10B981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-success text {
  color: #FFFFFF;
  font-size: 24rpx;
}

.image-add {
  width: 200rpx;
  height: 200rpx;
  background: #F8FAFC;
  border: 2rpx dashed #CBD5E1;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  transition: all 0.2s ease;
}

.image-add:active {
  background: #F1F5F9;
  border-color: #0066FF;
}

.add-icon {
  font-size: 56rpx;
  color: #94A3B8;
  line-height: 1;
}

.add-text {
  font-size: 24rpx;
  color: #94A3B8;
}

.image-tip {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 12rpx;
}

.picker-field {
  display: flex;
  align-items: center;
  height: 100rpx;
  background: #F8FAFC;
  border: 2rpx solid #E2E8F0;
  border-radius: 20rpx;
  padding: 0 24rpx;
}

.picker-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.picker-text {
  flex: 1;
  font-size: 30rpx;
  color: #1E293B;
}

.picker-text.placeholder {
  color: #94A3B8;
}

.picker-arrow {
  color: #94A3B8;
  font-size: 24rpx;
}

.priority-options {
  display: flex;
  gap: 24rpx;
}

.priority-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx;
  background: #F8FAFC;
  border: 2rpx solid #E2E8F0;
  border-radius: 20rpx;
  transition: all 0.2s ease;
}

.priority-item.active {
  background: #E6F0FF;
  border-color: #0066FF;
}

.priority-item.urgent.active {
  background: #FEF2F2;
  border-color: #EF4444;
}

.priority-icon {
  font-size: 36rpx;
}

.priority-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.priority-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
}

.priority-desc {
  font-size: 24rpx;
  color: #64748B;
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
  background: linear-gradient(135deg, #0066FF 0%, #0052CC 100%);
  color: #FFFFFF;
  font-size: 34rpx;
  font-weight: 600;
  letter-spacing: 4rpx;
  border: none;
  border-radius: 50rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 102, 255, 0.3);
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

.image-source-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  background: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx;
  padding-bottom: calc(32rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.modal-title {
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 32rpx;
}

.modal-options {
  display: flex;
  gap: 24rpx;
  margin-bottom: 24rpx;
}

.modal-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 32rpx;
  background: #F8FAFC;
  border-radius: 20rpx;
  transition: all 0.2s ease;
}

.modal-option:active {
  background: #E6F0FF;
}

.option-icon {
  font-size: 48rpx;
}

.option-text {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
}

.modal-cancel {
  width: 100%;
  height: 96rpx;
  background: #F1F5F9;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-cancel text {
  font-size: 30rpx;
  color: #64748B;
  font-weight: 500;
}
</style>
