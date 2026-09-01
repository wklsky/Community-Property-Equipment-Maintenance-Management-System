<template>
  <view class="login-page">
    <view class="login-header">
      <text class="brand-title">{{ title }}</text>
      <text class="brand-subtitle">{{ subtitle }}</text>
    </view>

    <view class="login-card">
      <view class="tab-switch">
        <view class="tab-item" :class="{ active: mode === 'password' }" @click="switchMode('password')">
          <text>密码登录</text>
        </view>
        <view class="tab-item" :class="{ active: mode === 'sms' }" @click="switchMode('sms')">
          <text>验证码登录</text>
        </view>
      </view>

      <!-- 租户：手动输入 + 联想选择，最终解析成后端需要的 tenantId -->
      <view class="form-group">
        <text class="form-label">所在社区</text>
        <view class="input-wrapper">
          <input
            v-model="form.tenantKeyword"
            class="form-input"
            placeholder="请输入社区名称"
            :maxlength="30"
            @input="onTenantInput"
            @blur="onTenantBlur"
          />
        </view>
        <view v-if="suggestVisible && tenantSuggestions.length > 0" class="suggest-list">
          <view
            v-for="tenant in tenantSuggestions"
            :key="tenant.id"
            class="suggest-item"
            @click="handlePickTenant(tenant)"
          >
            <text>{{ tenant.name }}</text>
          </view>
        </view>
      </view>

      <view class="form-group">
        <text class="form-label">手机号码</text>
        <view class="input-wrapper">
          <input
            v-model="form.account"
            class="form-input"
            type="number"
            :maxlength="11"
            placeholder="请输入手机号码"
          />
        </view>
      </view>

      <view v-if="mode === 'password'" class="form-group">
        <text class="form-label">登录密码</text>
        <view class="input-wrapper">
          <input
            v-model="form.password"
            class="form-input"
            :password="!showPassword"
            :maxlength="20"
            placeholder="请输入登录密码"
          />
          <view class="input-action" @click="showPassword = !showPassword">
            <text>{{ showPassword ? '隐藏' : '显示' }}</text>
          </view>
        </view>
      </view>

      <view v-else class="form-group">
        <text class="form-label">验证码</text>
        <view class="input-wrapper">
          <input
            v-model="form.smsCode"
            class="form-input"
            type="number"
            :maxlength="6"
            placeholder="请输入6位验证码"
          />
          <view class="code-btn" :class="{ disabled: smsCountdown > 0 }" @click="handleSendSmsCode">
            <text>{{ smsButtonText }}</text>
          </view>
        </view>
      </view>

      <button class="login-btn" :loading="loading" :disabled="loading" @click="submit">
        <text>{{ loading ? '登录中...' : '登 录' }}</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/components/LoginForm.vue
 * @Description: 业主 App 与维修工 App 复用的登录表单组件，自身持有 useLogin 逻辑，宿主只需声明 App 身份
 */

import { computed, onMounted, ref } from 'vue'
import { useLogin } from '../composables/useLogin'
import { APP_ALLOWED_ROLES, APP_ID, APP_ROLE_MISMATCH_MESSAGE, type AppId } from '../constants/roles'
import type { LoginResult, TenantOption } from '../types/auth'

interface LoginFormProps {
  /** 当前 App 标识，决定允许登录的角色白名单 */
  appId: AppId
  /** 品牌主标题 */
  title?: string
  /** 品牌副标题 */
  subtitle?: string
}

const props = withDefaults(defineProps<LoginFormProps>(), {
  appId: APP_ID.OWNER,
  title: '智慧社区',
  subtitle: '物业设备维护管理平台'
})

// 使用调用签名式声明：当前 uni-app 锁定 vue 3.2.47，
// 元组式 defineEmits（Vue 3.3+）在该版本的类型定义中尚不可用
const emit = defineEmits<{
  /** 登录成功且角色校验通过后触发，回传完整的登录结果 */
  (e: 'success', result: LoginResult): void
}>()

// 越权规则完全由 appId 推导，两个 App 不需要各自维护一份白名单
const identity = computed(() => ({
  appId: props.appId,
  allowedRoles: APP_ALLOWED_ROLES[props.appId],
  mismatchMessage: APP_ROLE_MISMATCH_MESSAGE[props.appId],
  homePath: ''
}))

const showPassword = ref(false)
// 联想列表的显隐由组件自身控制：选中或失焦时必须收起，
// 否则选中后 form.tenantKeyword 仍命中关键词，列表会一直挂在下方
const suggestVisible = ref(false)

const {
  form,
  mode,
  loading,
  tenantSuggestions,
  smsCountdown,
  smsButtonText,
  loadTenants,
  switchMode,
  pickTenant,
  handleSendSmsCode,
  submit
} = useLogin({
  identity: identity.value,
  // 跳转交给宿主页面：不同 App 的首页类型（tabBar 页 / 普通页）与
  // 是否需要处理 redirect 回跳都不一样，写在组件里会剥夺宿主的决策权
  onSuccess: (result) => emit('success', result)
})

// 用户重新输入时旧的 tenantId 已失效，必须清空，否则会带着上一个社区的 ID 提交
const onTenantInput = (): void => {
  form.value.tenantId = null
  suggestVisible.value = true
}

// 点击联想项时 input 会先触发 blur，若立即隐藏列表则点击无法命中，
// 因此延迟收起，给点击腾出响应时间
const onTenantBlur = (): void => {
  setTimeout(() => {
    suggestVisible.value = false
  }, 150)
}

const handlePickTenant = (tenant: TenantOption): void => {
  pickTenant(tenant)
  suggestVisible.value = false
}

// 页面级生命周期（onLoad）只在 page 组件中生效，本组件作为子组件被复用，
// 因此必须使用 Vue 标准的 onMounted 拉取租户列表
onMounted(() => {
  void loadTenants()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(165deg, #0066ff 0%, #0052cc 40%, #1e293b 100%);
  display: flex;
  flex-direction: column;
}

.login-header {
  padding: 100rpx 60rpx 50rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.brand-title {
  font-size: 50rpx;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 4rpx;
  margin-bottom: 10rpx;
}

.brand-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
}

.login-card {
  margin: 0 32rpx;
  background: #ffffff;
  border-radius: 40rpx;
  padding: 40rpx 36rpx 36rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
}

.tab-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 40rpx;
  gap: 60rpx;
}

.tab-item text {
  font-size: 32rpx;
  color: #94a3b8;
  font-weight: 500;
}

.tab-item.active text {
  color: #1e293b;
  font-weight: 700;
}

.form-group {
  margin-bottom: 28rpx;
  position: relative;
}

.form-label {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 14rpx;
  margin-left: 8rpx;
}

.input-wrapper {
  display: flex;
  align-items: center;
  height: 100rpx;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
  border-radius: 18rpx;
  padding: 0 22rpx;
}

.form-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #1e293b;
}

.input-action {
  padding-left: 20rpx;
  font-size: 24rpx;
  color: #0066ff;
}

.suggest-list {
  position: absolute;
  left: 0;
  right: 0;
  top: 100%;
  z-index: 10;
  background: #ffffff;
  border: 2rpx solid #e2e8f0;
  border-radius: 18rpx;
  overflow: hidden;
}

.suggest-item {
  padding: 20rpx 22rpx;
  font-size: 28rpx;
  color: #1e293b;
  border-bottom: 2rpx solid #f1f5f9;
}

.code-btn {
  flex-shrink: 0;
  height: 56rpx;
  padding: 0 20rpx;
  background: linear-gradient(135deg, #0066ff, #0052cc);
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 16rpx;
}

.code-btn text {
  font-size: 24rpx;
  color: #ffffff;
}

.code-btn.disabled {
  background: #cbd5e1;
}

.login-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #0066ff 0%, #0052cc 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 8rpx;
  border: none;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10rpx;
}
</style>
