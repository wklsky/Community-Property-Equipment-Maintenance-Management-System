<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand">
        <div class="brand-icon">🏢</div>
        <h1 class="brand-title">社区物业设备维护管理系统</h1>
        <p class="brand-desc">高效、便捷、智能的物业管理解决方案</p>
      </div>
      <div class="features">
        <div class="feature-item">
          <el-icon><CircleCheck /></el-icon>
          <span>工单全流程管理</span>
        </div>
        <div class="feature-item">
          <el-icon><CircleCheck /></el-icon>
          <span>设备智能巡检</span>
        </div>
        <div class="feature-item">
          <el-icon><CircleCheck /></el-icon>
          <span>公告消息推送</span>
        </div>
        <div class="feature-item">
          <el-icon><CircleCheck /></el-icon>
          <span>多租户数据隔离</span>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-box">
        <h2 class="login-title">欢迎登录</h2>
        <p class="login-subtitle">请输入租户、账号与密码</p>

        <div class="login-tabs">
          <span
            class="tab-item"
            :class="{ active: mode === 'password' }"
            @click="switchMode('password')"
          >
            密码登录
          </span>
          <span class="tab-item" :class="{ active: mode === 'sms' }" @click="switchMode('sms')">
            短信登录
          </span>
        </div>

        <el-checkbox
          v-if="mode === 'password'"
          v-model="isSuperAdminLogin"
          class="super-admin-check"
          @change="toggleSuperAdmin"
        >
          超级管理员登录
        </el-checkbox>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="0"
          size="large"
        >
          <el-form-item v-if="!isSuperAdminLogin" prop="tenantKeyword">
            <el-autocomplete
              v-model="form.tenantKeyword"
              class="full-width"
              value-key="name"
              placeholder="请输入租户 / 社区名称"
              clearable
              :fetch-suggestions="queryTenants"
              :trigger-on-focus="true"
              @select="handleTenantSelect"
            >
              <template #prefix>
                <el-icon><OfficeBuilding /></el-icon>
              </template>
            </el-autocomplete>
          </el-form-item>

          <el-form-item prop="account">
            <el-input v-model="form.account" maxlength="11" placeholder="请输入账号(手机号)" clearable>
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="mode === 'password'" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              @keyup.enter="submit"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item v-else prop="smsCode">
            <div class="code-row">
              <el-input
                v-model="form.smsCode"
                maxlength="6"
                placeholder="请输入6位短信验证码"
                @keyup.enter="submit"
              >
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
              <el-button class="send-code-btn" :disabled="smsCountdown > 0" @click="handleSendSmsCode">
                {{ smsButtonText }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button class="submit-btn" type="primary" :loading="loading" @click="submit">
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div v-if="mode === 'password'" class="form-extra">
          <el-button link type="primary" @click="openResetDialog">忘记密码?</el-button>
        </div>

        <div class="login-footer">
          <span class="demo-account">租户、账号、密码均由物业管理员分配</span>
        </div>
      </div>
    </div>

    <CaptchaDialog
      v-model="captchaVisible"
      :image="captchaImage"
      @refresh="captcha.refresh"
      @confirm="handleCaptchaConfirm"
    />

    <ResetPasswordDialog
      ref="resetDialogRef"
      :tenant-id="form.tenantId"
      @success="handleResetSuccess"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/views/Login.vue
 * @Description: 登录页视图层，仅负责布局渲染与事件绑定，全部业务逻辑下沉到 useLogin / useResetPassword
 */

import { ref } from 'vue'
import { CircleCheck, Key, Lock, OfficeBuilding, User } from '@element-plus/icons-vue'
import CaptchaDialog from './login/CaptchaDialog.vue'
import ResetPasswordDialog from './login/ResetPasswordDialog.vue'
import { useLogin } from './login/useLogin'
import type { LoginMode } from '@/types/auth'

const {
  formRef,
  form,
  rules,
  mode,
  loading,
  isSuperAdminLogin,
  smsCountdown,
  smsButtonText,
  captchaVisible,
  captcha,
  switchMode,
  toggleSuperAdmin,
  queryTenants,
  handleTenantSelect,
  handleSendSmsCode,
  submit,
  handleCaptchaConfirm
} = useLogin()

// captcha 是一个持有多个 Ref 的普通对象，模板不会自动解包其内部的 Ref，
// 因此在这里单独取出 image，保证传给子组件的是字符串而非 Ref
const { image: captchaImage } = captcha

const resetDialogRef = ref<InstanceType<typeof ResetPasswordDialog> | null>(null)

const openResetDialog = (): void => {
  resetDialogRef.value?.open(form.value.account)
}

/** 重置成功后回填账号并切回密码模式，减少用户重复输入 */
const handleResetSuccess = (payload: { phone: string }): void => {
  form.value.account = payload.phone
  form.value.password = ''
  switchMode('password' as LoginMode)
  captcha.reset()
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  background: #f0f2f5;
}

.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.brand {
  margin-bottom: 60px;

  .brand-icon {
    font-size: 64px;
    margin-bottom: 24px;
  }

  .brand-title {
    font-size: 36px;
    font-weight: 700;
    margin-bottom: 16px;
    letter-spacing: 2px;
  }

  .brand-desc {
    font-size: 18px;
    opacity: 0.9;
  }
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;

  .feature-item {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 16px;
    opacity: 0.9;

    .el-icon {
      font-size: 20px;
    }
  }
}

.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  box-shadow: -8px 0 24px rgba(0, 0, 0, 0.1);
}

.login-box {
  width: 360px;
  padding: 20px;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin-bottom: 24px;
}

.login-tabs {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;

  .tab-item {
    font-size: 15px;
    color: #909399;
    cursor: pointer;
    padding-bottom: 12px;
    margin-bottom: -13px;
    border-bottom: 2px solid transparent;
    transition: all 0.2s;

    &:hover {
      color: #667eea;
    }

    &.active {
      color: #667eea;
      font-weight: 600;
      border-bottom-color: #667eea;
    }
  }
}

.super-admin-check {
  margin-bottom: 16px;
  margin-top: -8px;
}

.full-width {
  width: 100%;
}

.code-row {
  display: flex;
  gap: 12px;
  width: 100%;

  .send-code-btn {
    flex-shrink: 0;
    width: 120px;
    height: 40px;
    border-radius: 8px;
    font-size: 13px;
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
}

.form-extra {
  text-align: right;
  margin-top: -8px;
  margin-bottom: 8px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;

  .demo-account {
    font-size: 13px;
    color: #909399;
  }
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;

  &:hover {
    box-shadow: 0 0 0 1px #c0c4cc inset;
  }

  &.is-focus {
    box-shadow: 0 0 0 1px #667eea inset;
  }
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 4px;

  &:hover {
    background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
  }
}

@media (max-width: 900px) {
  .login-left {
    padding: 40px;

    .brand-title {
      font-size: 26px;
    }

    .brand-desc {
      font-size: 14px;
    }

    .features {
      gap: 14px;

      .feature-item {
        font-size: 14px;
      }
    }
  }

  .login-right {
    width: 400px;
  }

  .login-box {
    width: 320px;
  }
}

@media (max-width: 640px) {
  .login-container {
    flex-direction: column;
  }

  .login-left {
    flex: none;
    padding: 32px 24px;
    align-items: center;
    text-align: center;

    .brand {
      margin-bottom: 24px;

      .brand-icon {
        font-size: 48px;
        margin-bottom: 16px;
      }

      .brand-title {
        font-size: 22px;
        letter-spacing: 1px;
      }

      .brand-desc {
        font-size: 13px;
      }
    }

    .features {
      display: none;
    }
  }

  .login-right {
    width: 100%;
    flex: 1;
    box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.06);
    border-radius: 24px 24px 0 0;
  }

  .login-box {
    width: 100%;
    max-width: 360px;
    padding: 16px;
  }

  .login-title {
    font-size: 24px;
  }

  .login-subtitle {
    margin-bottom: 24px;
  }
}
</style>
