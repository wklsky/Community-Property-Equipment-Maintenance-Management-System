<template>
  <view class="register-page">
    <!-- Background -->
    <view class="register-bg">
      <view class="bg-pattern"></view>
      <view class="bg-gradient"></view>
    </view>

    <!-- Header -->
    <view class="register-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="header-title">注册账号</text>
      <view class="placeholder"></view>
    </view>

    <!-- Register Card -->
    <view class="register-card">
      <text class="card-title">创建新账号</text>
      <text class="card-subtitle">注册后即可使用智慧社区服务</text>

      <!-- Community Selector -->
      <view class="form-group">
        <text class="form-label">选择社区</text>
        <picker :range="tenants" range-key="name" @change="onTenantChange">
          <view class="picker-field">
            <view class="picker-icon"><text>🏘️</text></view>
            <text class="picker-text" :class="{ placeholder: !selectedTenant }">
              {{ selectedTenant ? selectedTenant.name : '请选择您所在的社区' }}
            </text>
            <view class="picker-arrow"><text>▾</text></view>
          </view>
        </picker>
      </view>

      <!-- Phone Input -->
      <view class="form-group">
        <text class="form-label">手机号码</text>
        <view class="input-wrapper">
          <view class="input-icon"><text>📱</text></view>
          <input
            v-model="form.phone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号码"
            class="form-input"
          />
        </view>
      </view>

      <!-- Password Input -->
      <view class="form-group">
        <text class="form-label">设置密码</text>
        <view class="input-wrapper">
          <view class="input-icon"><text>🔒</text></view>
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="6-20位，建议字母+数字组合"
            class="form-input"
          />
          <view class="input-action" @click="showPassword = !showPassword">
            <text>{{ showPassword ? '🙈' : '👁️' }}</text>
          </view>
        </view>
      </view>

      <!-- Confirm Password Input -->
      <view class="form-group">
        <text class="form-label">确认密码</text>
        <view class="input-wrapper">
          <view class="input-icon"><text>✓</text></view>
          <input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            class="form-input"
          />
        </view>
      </view>

      <!-- Register Button -->
      <button
        class="register-btn"
        :class="{ loading: loading }"
        :loading="loading"
        :disabled="loading"
        @click="handleRegister"
      >
        <text v-if="!loading">注 册</text>
      </button>

      <!-- Back to Login -->
      <view class="login-link" @click="goBack">
        <text>已有账号？去登录</text>
      </view>
    </view>

    <!-- Footer -->
    <view class="register-footer">
      <text class="footer-text">智慧社区物业管理系统 v1.0</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getTenants, register } from '../../api/auth'
import { isValidPhone, isValidPassword, isNotEmpty } from '../../utils/validate'

const loading = ref(false)
const tenants = ref([])
const selectedTenant = ref(null)
const showPassword = ref(false)

const form = ref({
  phone: '',
  password: '',
  confirmPassword: '',
  tenantId: null
})

onLoad(async () => {
  try {
    const res = await getTenants()
    tenants.value = res.data || []
    if (tenants.value.length > 0) {
      selectedTenant.value = tenants.value[0]
      form.value.tenantId = tenants.value[0].id
    }
  } catch (e) {
    console.error('获取社区列表失败', e)
  }
})

const onTenantChange = (e) => {
  const index = e.detail.value
  selectedTenant.value = tenants.value[index]
  form.value.tenantId = tenants.value[index].id
}

const goBack = () => {
  uni.navigateBack({ delta: 1 })
}

const handleRegister = async () => {

  if (!form.value.tenantId) {
    uni.showToast({ title: '请选择社区', icon: 'none' })
    return
  }
  if (!isNotEmpty(form.value.phone)) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!isValidPhone(form.value.phone)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return
  }
  if (!isNotEmpty(form.value.password)) {
    uni.showToast({ title: '请设置密码', icon: 'none' })
    return
  }
  if (!isValidPassword(form.value.password)) {
    uni.showToast({ title: '密码长度需6-20位', icon: 'none' })
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await register({
      phone: form.value.phone,
      password: form.value.password,
      tenantId: form.value.tenantId
    })

    uni.showToast({ title: '注册成功', icon: 'success', duration: 1500 })

    setTimeout(() => {
      const pages = getCurrentPages()
      const prevPage = pages[pages.length - 2]
      if (prevPage && prevPage.route.includes('login')) {

        uni.$emit('fillCredentials', {
          phone: form.value.phone,
          password: form.value.password,
          tenantId: form.value.tenantId
        })
      }
      uni.navigateBack({ delta: 1 })
    }, 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '注册失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.register-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(165deg, #0066FF 0%, #0052CC 40%, #1E293B 100%);
}

.bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 60%;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 40%),
    radial-gradient(circle at 40% 80%, rgba(255,255,255,0.05) 0%, transparent 30%);
  z-index: 1;
}

.register-header {
  position: relative;
  z-index: 2;
  padding: 80rpx 36rpx 40rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10rpx);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.back-btn text {
  font-size: 32rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 2rpx;
}

.placeholder {
  width: 64rpx;
}

.register-card {
  position: relative;
  z-index: 2;
  margin: 0 32rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(40rpx);
  border-radius: 40rpx;
  padding: 40rpx 36rpx 36rpx;
  box-shadow:
    0 20rpx 60rpx rgba(0, 0, 0, 0.15),
    0 0 0 2rpx rgba(255, 255, 255, 0.1) inset;
  flex: 1;
}

.card-title {
  display: block;
  font-size: 38rpx;
  font-weight: 700;
  color: #1E293B;
  text-align: center;
  margin-bottom: 8rpx;
}

.card-subtitle {
  display: block;
  font-size: 26rpx;
  color: #94A3B8;
  text-align: center;
  margin-bottom: 40rpx;
}

.form-group {
  margin-bottom: 28rpx;
}

.form-label {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 14rpx;
  margin-left: 8rpx;
}

.picker-field,
.input-wrapper {
  display: flex;
  align-items: center;
  height: 100rpx;
  background: #F8FAFC;
  border: 2rpx solid #E2E8F0;
  border-radius: 18rpx;
  padding: 0 22rpx;
  transition: all 0.3s ease;
}

.picker-field:active,
.input-wrapper:focus-within {
  background: #FFFFFF;
  border-color: #0066FF;
  box-shadow: 0 0 0 6rpx rgba(0, 102, 255, 0.1);
}

.picker-icon,
.input-icon {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14rpx;
  font-size: 30rpx;
}

.picker-text {
  flex: 1;
  font-size: 28rpx;
  color: #1E293B;
}

.picker-text.placeholder {
  color: #94A3B8;
}

.picker-arrow {
  color: #94A3B8;
  font-size: 22rpx;
}

.form-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #1E293B;
  background: transparent;
}

.input-action {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 14rpx;
}

.register-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #0066FF 0%, #0052CC 100%);
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 8rpx;
  border: none;
  border-radius: 50rpx;
  box-shadow: 0 12rpx 32rpx rgba(0, 102, 255, 0.35);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10rpx;
}

.register-btn:active {
  transform: scale(0.96);
}

.register-btn.loading {
  opacity: 0.8;
}

.login-link {
  text-align: center;
  margin-top: 28rpx;
}

.login-link text {
  font-size: 26rpx;
  color: #0066FF;
  font-weight: 500;
}

.register-footer {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 50rpx;
  padding-bottom: calc(50rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(50rpx + env(safe-area-inset-bottom));
}

.footer-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.5);
}
</style>
