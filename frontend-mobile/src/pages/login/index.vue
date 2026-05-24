<template>
  <view class="login-page">
    <!-- Background -->
    <view class="login-bg">
      <view class="bg-pattern"></view>
      <view class="bg-gradient"></view>
    </view>

    <!-- Logo & Branding -->
    <view class="login-header">
      <view class="logo-container">
        <view class="logo-icon">
          <text style="font-size: 56rpx;">🏢</text>
        </view>
        <view class="logo-glow"></view>
      </view>
      <text class="brand-title">智慧社区</text>
      <text class="brand-subtitle">物业设备维护管理平台</text>
    </view>

    <!-- Login Card -->
    <view class="login-card">
      <!-- Tab Switch -->
      <view class="tab-switch">
        <view
          class="tab-item"
          :class="{ active: activeTab === 'password' }"
          @click="switchTab('password')"
        >
          <text>密码登录</text>
          <view class="tab-underline" v-if="activeTab === 'password'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'code' }"
          @click="switchTab('code')"
        >
          <text>验证码登录</text>
          <view class="tab-underline" v-if="activeTab === 'code'"></view>
        </view>
      </view>

      <!-- Tenant Selector -->
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

      <!-- ========== 密码登录模式 ========== -->
      <template v-if="activeTab === 'password'">
        <!-- Password Input -->
        <view class="form-group">
          <text class="form-label">登录密码</text>
          <view class="input-wrapper">
            <view class="input-icon"><text>🔒</text></view>
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入登录密码"
              class="form-input"
            />
            <view class="input-action" @click="showPassword = !showPassword">
              <text>{{ showPassword ? '🙈' : '👁️' }}</text>
            </view>
          </view>
        </view>

        <!-- Remember Password & Forgot Password -->
        <view class="options-row">
          <view class="remember-row" @click="rememberPwd = !rememberPwd">
            <view class="checkbox" :class="{ checked: rememberPwd }">
              <text v-if="rememberPwd">✓</text>
            </view>
            <text class="option-text">记住密码</text>
          </view>
          <text class="forgot-link" @click="showResetModal = true">忘记密码?</text>
        </view>
      </template>

      <!-- ========== 验证码登录模式 ========== -->
      <template v-if="activeTab === 'code'">
        <!-- Code Input -->
        <view class="form-group">
          <text class="form-label">验证码</text>
          <view class="input-wrapper">
            <view class="input-icon"><text>💬</text></view>
            <input
              v-model="form.code"
              type="number"
              maxlength="6"
              placeholder="请输入6位验证码"
              class="form-input"
            />
            <view
              class="code-btn"
              :class="{ disabled: codeCountdown > 0 }"
              @click="handleSendCode"
            >
              <text v-if="codeCountdown === 0">获取验证码</text>
              <text v-else>{{ codeCountdown }}s</text>
            </view>
          </view>
        </view>
      </template>

      <!-- Login Button -->
      <button
        class="login-btn"
        :class="{ loading: loading }"
        :loading="loading"
        :disabled="loading"
        @click="handleLogin"
      >
        <text v-if="!loading">{{ activeTab === 'password' ? '登 录' : '验证并登录' }}</text>
      </button>

      <!-- Register Button -->
      <view class="register-link" @click="goToRegister">
        <text>还没有账号？立即注册</text>
      </view>
    </view>

    <!-- Footer -->
    <view class="login-footer">
      <text class="footer-text">智慧社区物业管理系统 v1.0</text>
    </view>

    <!-- ========== 忘记密码弹窗 ========== -->
    <view class="modal-overlay" v-if="showResetModal" @click="closeResetModal">
      <view class="modal-card" @click.stop>
        <view class="modal-header">
          <text class="modal-title">重置密码</text>
          <view class="modal-close" @click="closeResetModal">
            <text>✕</text>
          </view>
        </view>
        <view class="modal-body">
          <!-- Step 1: 验证身份 -->
          <view class="modal-step" v-if="resetStep === 1">
            <text class="step-hint">请输入手机号并获取验证码</text>
            <view class="form-group">
              <view class="input-wrapper">
                <view class="input-icon"><text>📱</text></view>
                <input
                  v-model="resetForm.phone"
                  type="number"
                  maxlength="11"
                  placeholder="请输入手机号码"
                  class="form-input"
                />
              </view>
            </view>
            <view class="form-group">
              <view class="input-wrapper">
                <view class="input-icon"><text>💬</text></view>
                <input
                  v-model="resetForm.code"
                  type="number"
                  maxlength="6"
                  placeholder="请输入验证码"
                  class="form-input"
                />
                <view
                  class="code-btn"
                  :class="{ disabled: resetCodeCountdown > 0 }"
                  @click="handleSendResetCode"
                >
                  <text v-if="resetCodeCountdown === 0">获取验证码</text>
                  <text v-else>{{ resetCodeCountdown }}s</text>
                </view>
              </view>
            </view>
            <button class="modal-btn" @click="goToSetPassword">下一步</button>
          </view>

          <!-- Step 2: 设置新密码 -->
          <view class="modal-step" v-if="resetStep === 2">
            <text class="step-hint">请设置6-20位的新密码</text>
            <view class="form-group">
              <view class="input-wrapper">
                <view class="input-icon"><text>🔒</text></view>
                <input
                  v-model="resetForm.newPassword"
                  :type="showResetPwd ? 'text' : 'password'"
                  placeholder="请输入新密码"
                  class="form-input"
                />
                <view class="input-action" @click="showResetPwd = !showResetPwd">
                  <text>{{ showResetPwd ? '🙈' : '👁️' }}</text>
                </view>
              </view>
            </view>
            <view class="form-group">
              <view class="input-wrapper">
                <view class="input-icon"><text>✓</text></view>
                <input
                  v-model="resetForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  class="form-input"
                />
              </view>
            </view>
            <button class="modal-btn" :loading="resetLoading" @click="handleResetPassword">
              确认重置
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getTenants, login, sendCode, loginByCode, resetPassword } from '../../api/auth'
import { useUserStore } from '../../store/user'
import { validator } from '../../utils/validate'

const userStore = useUserStore()

const loading = ref(false)
const tenants = ref([])
const selectedTenant = ref(null)
const showPassword = ref(false)
const activeTab = ref('password')

const rememberPwd = ref(false)

const form = ref({
  phone: '',
  password: '',
  code: '',
  tenantId: null
})

const codeCountdown = ref(0)
let codeTimer = null

const showResetModal = ref(false)
const resetStep = ref(1)
const resetLoading = ref(false)
const showResetPwd = ref(false)
const resetCodeCountdown = ref(0)
let resetCodeTimer = null
const resetForm = ref({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

onLoad(async () => {
  await loadTenants()

  loadRememberedAccount()

  uni.$on('fillCredentials', (data) => {
    form.value.phone = data.phone
    form.value.password = data.password

    if (data.tenantId) {
      const tenant = tenants.value.find(t => t.id === data.tenantId)
      if (tenant) {
        selectedTenant.value = tenant
        form.value.tenantId = tenant.id
      }
    }
    activeTab.value = 'password'
  })
})

onShow(() => {

  loadRememberedAccount()
})

const loadRememberedAccount = () => {
  try {
    const saved = uni.getStorageSync('rememberedAccount')
    if (saved) {
      form.value.phone = saved.phone || ''
      form.value.password = saved.password || ''
      form.value.tenantId = saved.tenantId || null
      rememberPwd.value = true

      if (saved.tenantName) {
        selectedTenant.value = { id: saved.tenantId, name: saved.tenantName }
      }
    }
  } catch (e) {

  }
}

const loadTenants = async () => {
  try {
    const res = await getTenants()
    tenants.value = res.data || []

    if (!rememberPwd.value && tenants.value.length > 0) {
      selectedTenant.value = tenants.value[0]
      form.value.tenantId = tenants.value[0].id
    } else if (!form.value.tenantId && tenants.value.length > 0) {
      selectedTenant.value = tenants.value[0]
      form.value.tenantId = tenants.value[0].id
    }
  } catch (e) {
    console.error('获取租户列表失败', e)
  }
}

const onTenantChange = (e) => {
  const index = e.detail.value
  selectedTenant.value = tenants.value[index]
  form.value.tenantId = tenants.value[index].id
}

const switchTab = (tab) => {
  activeTab.value = tab

  form.value.code = ''
  codeCountdown.value = 0
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
}

const toggleTab = () => {
  switchTab(activeTab.value === 'password' ? 'code' : 'password')
}

const goToRegister = () => {
  uni.navigateTo({ url: '/pages/register/index' })
}

const startCountdown = () => {
  codeCountdown.value = 60
  codeTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(codeTimer)
      codeTimer = null
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (codeCountdown.value > 0) return

  const { valid, message } = validator.validateSendCode(form.value)
  if (!valid) {
    uni.showToast({ title: message, icon: 'none' })
    return
  }

  try {
    await sendCode({
      phone: form.value.phone,
      tenantId: form.value.tenantId
    })
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    startCountdown()
  } catch (e) {
    uni.showToast({ title: e.message || '发送失败', icon: 'none' })
  }
}

const handleLogin = async () => {
  const isPasswordLogin = activeTab.value === 'password'

  const { valid, message } = isPasswordLogin
    ? validator.validateLogin(form.value)
    : validator.validateCodeLogin(form.value)

  if (!valid) {
    uni.showToast({ title: message, icon: 'none' })
    return
  }

  loading.value = true
  try {
    let res
    if (isPasswordLogin) {
      res = await login({
        phone: form.value.phone,
        password: form.value.password,
        tenantId: form.value.tenantId
      })
    } else {
      res = await loginByCode({
        phone: form.value.phone,
        code: form.value.code,
        tenantId: form.value.tenantId
      })
    }

    if (res.data.roleName === '系统管理员') {
      uni.showModal({
        title: '提示',
        content: '管理员请使用 PC 端管理后台登录',
        showCancel: false,
        confirmText: '知道了',
        confirmColor: '#0066FF'
      })
      return
    }

    if (isPasswordLogin && rememberPwd.value) {
      uni.setStorageSync('rememberedAccount', {
        phone: form.value.phone,
        password: form.value.password,
        tenantId: form.value.tenantId,
        tenantName: selectedTenant.value?.name || ''
      })
    } else if (!rememberPwd.value) {
      uni.removeStorageSync('rememberedAccount')
    }

    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    if (res.data.refreshToken) {
      uni.setStorageSync('refreshToken', res.data.refreshToken)
    }

    uni.showToast({ title: '登录成功', icon: 'success', duration: 1500 })

    setTimeout(() => {
      const homePath = userStore.getHomePath()
      if (homePath.includes('/pages/repair/list')) {
        uni.switchTab({ url: '/pages/repair/list' })
      } else {
        uni.switchTab({ url: '/pages/index/index' })
      }
    }, 1500)
  } finally {
    loading.value = false
  }
}

const closeResetModal = () => {
  showResetModal.value = false
  resetStep.value = 1
  resetForm.value = { phone: '', code: '', newPassword: '', confirmPassword: '' }
  resetCodeCountdown.value = 0
  if (resetCodeTimer) {
    clearInterval(resetCodeTimer)
    resetCodeTimer = null
  }
}

const handleSendResetCode = async () => {
  if (resetCodeCountdown.value > 0) return
  if (!form.value.tenantId) {
    uni.showToast({ title: '请先在主页选择社区', icon: 'none' })
    return
  }
  const phone = resetForm.value.phone
  if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  try {
    await sendCode({ phone, tenantId: form.value.tenantId })
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    resetCodeCountdown.value = 60
    resetCodeTimer = setInterval(() => {
      resetCodeCountdown.value--
      if (resetCodeCountdown.value <= 0) {
        clearInterval(resetCodeTimer)
        resetCodeTimer = null
      }
    }, 1000)
  } catch (e) {
    uni.showToast({ title: e.message || '发送失败', icon: 'none' })
  }
}

const goToSetPassword = () => {
  const phone = resetForm.value.phone
  const code = resetForm.value.code
  if (!phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!code || code.length !== 6) {
    uni.showToast({ title: '请输入6位验证码', icon: 'none' })
    return
  }
  resetStep.value = 2
}

const handleResetPassword = async () => {
  const { newPassword, confirmPassword } = resetForm.value
  if (!newPassword || newPassword.length < 6 || newPassword.length > 20) {
    uni.showToast({ title: '密码长度需6-20位', icon: 'none' })
    return
  }
  if (newPassword !== confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }

  resetLoading.value = true
  try {
    await resetPassword({
      phone: resetForm.value.phone,
      code: resetForm.value.code,
      tenantId: form.value.tenantId,
      newPassword
    })
    uni.showToast({ title: '密码重置成功，请登录', icon: 'success' })

    form.value.password = newPassword
    form.value.phone = resetForm.value.phone
    activeTab.value = 'password'
    closeResetModal()
  } catch (e) {
    uni.showToast({ title: e.message || '重置失败', icon: 'none' })
  } finally {
    resetLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.login-bg {
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

.login-header {
  position: relative;
  z-index: 2;
  padding: 100rpx 60rpx 50rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-container {
  position: relative;
  margin-bottom: 28rpx;
}

.logo-icon {
  width: 130rpx;
  height: 130rpx;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20rpx);
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 190rpx;
  height: 190rpx;
  background: radial-gradient(circle, rgba(0, 102, 255, 0.4) 0%, transparent 70%);
  z-index: -1;
}

.brand-title {
  font-size: 50rpx;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 4rpx;
  margin-bottom: 10rpx;
}

.brand-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 2rpx;
}

.login-card {
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

.tab-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 40rpx;
  gap: 60rpx;
}

.tab-item {
  position: relative;
  padding-bottom: 16rpx;
}

.tab-item text {
  font-size: 32rpx;
  color: #94A3B8;
  font-weight: 500;
  transition: color 0.3s;
}

.tab-item.active text {
  color: #1E293B;
  font-weight: 700;
}

.tab-underline {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 6rpx;
  background: linear-gradient(90deg, #0066FF, #0052CC);
  border-radius: 3rpx;
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

.options-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
  padding: 0 8rpx;
}

.remember-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 3rpx solid #CBD5E1;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.checkbox.checked {
  background: #0066FF;
  border-color: #0066FF;
}

.checkbox text {
  font-size: 22rpx;
  color: #FFFFFF;
}

.option-text {
  font-size: 26rpx;
  color: #64748B;
  line-height: 36rpx;
}

.forgot-link {
  font-size: 26rpx;
  color: #0066FF;
  font-weight: 500;
}

.code-btn {
  flex-shrink: 0;
  height: 56rpx;
  padding: 0 20rpx;
  background: linear-gradient(135deg, #0066FF, #0052CC);
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 16rpx;
}

.code-btn text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 500;
  white-space: nowrap;
}

.code-btn.disabled {
  background: #CBD5E1;
}

.login-btn {
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

.login-btn:active {
  transform: scale(0.96);
}

.login-btn.loading {
  opacity: 0.8;
}

.register-link {
  text-align: center;
  margin-top: 28rpx;
  padding: 16rpx 0;
}

.register-link text {
  font-size: 28rpx;
  color: #0066FF;
  font-weight: 600;
}

.switch-mode-text {
  text-align: center;
  margin-top: 28rpx;
}

.switch-mode-text text {
  font-size: 26rpx;
  color: #0066FF;
  font-weight: 500;
}

.demo-hint {
  margin-top: 36rpx;
}

.hint-divider {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.divider-line {
  flex: 1;
  height: 2rpx;
  background: #E2E8F0;
}

.divider-text {
  padding: 0 24rpx;
  font-size: 24rpx;
  color: #94A3B8;
}

.hint-text {
  display: block;
  text-align: center;
  font-size: 24rpx;
  color: #64748B;
  background: #F8FAFC;
  padding: 18rpx;
  border-radius: 12rpx;
}

.login-footer {
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

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60rpx;
}

.modal-card {
  width: 100%;
  background: #FFFFFF;
  border-radius: 28rpx;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 36rpx 36rpx 0;
}

.modal-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1E293B;
}

.modal-close {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #F1F5F9;
}

.modal-close text {
  font-size: 28rpx;
  color: #64748B;
}

.modal-body {
  padding: 32rpx 36rpx 40rpx;
}

.step-hint {
  display: block;
  font-size: 28rpx;
  color: #64748B;
  margin-bottom: 32rpx;
  text-align: center;
}

.modal-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #0066FF, #0052CC);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  border-radius: 45rpx;
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body .form-group {
  margin-bottom: 24rpx;
}

.modal-body .input-wrapper {
  height: 90rpx;
}
</style>
