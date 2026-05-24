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
        <p class="login-subtitle">请输入您的账号信息</p>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <span class="tab-item" :class="{ active: loginMode === 'password' }" @click="switchMode('password')">密码登录</span>
          <span class="tab-item" :class="{ active: loginMode === 'code' }" @click="switchMode('code')">验证码登录</span>
        </div>

        <div class="super-admin-check" v-if="loginMode === 'password'">
          <el-checkbox v-model="isSuperAdminLogin" @change="onSuperAdminChange">超级管理员登录</el-checkbox>
        </div>

        <!-- 密码登录表单 -->
        <el-form v-if="loginMode === 'password'" ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="0" size="large">
          <el-form-item prop="tenantId" v-if="!isSuperAdminLogin">
            <el-select v-model="passwordForm.tenantId" placeholder="请选择租户" style="width: 100%">
              <template #prefix>
                <el-icon><OfficeBuilding /></el-icon>
              </template>
              <el-option v-for="t in tenants" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="passwordForm.phone" placeholder="请输入手机号">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="passwordForm.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handlePasswordLogin">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%; height: 48px;" :loading="loading" @click="handlePasswordLogin">
              登 录
            </el-button>
          </el-form-item>
          <div class="form-extra">
            <el-button link type="primary" @click="showResetDialog">忘记密码?</el-button>
          </div>
        </el-form>

        <!-- 验证码登录表单 -->
        <el-form v-if="loginMode === 'code'" ref="codeFormRef" :model="codeForm" :rules="codeRules" label-width="0" size="large">
          <el-form-item prop="tenantId">
            <el-select v-model="codeForm.tenantId" placeholder="请选择租户" style="width: 100%">
              <template #prefix>
                <el-icon><OfficeBuilding /></el-icon>
              </template>
              <el-option v-for="t in tenants" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="codeForm.phone" placeholder="请输入手机号">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="code">
            <div class="code-row">
              <el-input v-model="codeForm.code" placeholder="请输入验证码">
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
              <el-button class="send-code-btn" :disabled="countdown > 0" @click="handleSendCode">
                {{ countdown > 0 ? countdown + 's' : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%; height: 48px;" :loading="loading" @click="handleCodeLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 重置密码对话框 -->
      <el-dialog v-model="resetDialogVisible" title="重置密码" width="420px" :close-on-click-modal="false">
        <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="80px" size="large">
          <el-form-item label="租户" prop="tenantId">
            <el-select v-model="resetForm.tenantId" placeholder="请选择租户" style="width: 100%">
              <el-option v-for="t in tenants" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="resetForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <div class="code-row">
              <el-input v-model="resetForm.code" placeholder="请输入验证码" />
              <el-button class="send-code-btn" :disabled="resetCountdown > 0" @click="handleResetSendCode">
                {{ resetCountdown > 0 ? resetCountdown + 's' : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码(6-20位)" show-password />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="resetDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">确认重置</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, OfficeBuilding, User, Lock, Key } from '@element-plus/icons-vue'
import { login, loginByCode, sendCode, resetPassword, getTenants } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const tenants = ref([])
const loginMode = ref('password')
const isSuperAdminLogin = ref(false)

const passwordFormRef = ref()
const passwordForm = ref({ tenantId: null, phone: '', password: '' })
const passwordRules = {
  tenantId: [{ required: true, message: '请选择租户', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const codeFormRef = ref()
const codeForm = ref({ tenantId: null, phone: '', code: '' })
const codeRules = {
  tenantId: [{ required: true, message: '请选择租户', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}
const countdown = ref(0)
let countdownTimer = null

const resetDialogVisible = ref(false)
const resetFormRef = ref()
const resetLoading = ref(false)
const resetForm = ref({ tenantId: null, phone: '', code: '', newPassword: '' })
const resetRules = {
  tenantId: [{ required: true, message: '请选择租户', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20位之间', trigger: 'blur' }
  ]
}
const resetCountdown = ref(0)
let resetCountdownTimer = null

onMounted(async () => {
  await loadTenants()
})

const loadTenants = async () => {
  try {
    const res = await getTenants()
    if (res.data && Array.isArray(res.data)) {
      tenants.value = res.data
      if (tenants.value.length > 0) {
        passwordForm.value.tenantId = tenants.value[0].id
        codeForm.value.tenantId = tenants.value[0].id
      }
    } else {
      ElMessage.error('加载租户列表失败，请刷新重试')
    }
  } catch (e) {
    ElMessage.error('无法连接服务器，请检查后端服务是否启动')
  }
}

const switchMode = (mode) => {
  loginMode.value = mode
  codeForm.value.code = ''
  countdown.value = 0
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
}

const onSuperAdminChange = (val) => {
  if (val) {
    passwordForm.value.tenantId = null
  } else if (tenants.value.length > 0) {
    passwordForm.value.tenantId = tenants.value[0].id
  }
}

const doLogin = (res) => {
  userStore.setToken(res.data.token)
  if (res.data.refreshToken) {
    userStore.setRefreshToken(res.data.refreshToken)
  }
  userStore.setUserInfo(res.data)
  ElMessage.success('登录成功')
  const redirect = route.query.redirect
  if (redirect) {
    router.push(redirect)
  } else if (userStore.isSuperAdmin) {
    router.push('/admin/tenants')
  } else {
    router.push('/')
  }
}

const handlePasswordLogin = async () => {
  try {
    await passwordFormRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const res = await login(passwordForm.value)
    doLogin(res)
  } catch (e) {
    console.error('登录失败:', e.message)
  } finally {
    loading.value = false
  }
}

const handleSendCode = async () => {
  if (!codeForm.value.tenantId) { ElMessage.warning('请先选择租户'); return }
  if (!codeForm.value.phone || !/^1[3-9]\d{9}$/.test(codeForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号'); return
  }
  try {
    await sendCode({ phone: codeForm.value.phone, tenantId: codeForm.value.tenantId })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch (e) {
    console.error('发送验证码失败:', e.message)
  }
}

const handleCodeLogin = async () => {
  try {
    await codeFormRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const res = await loginByCode(codeForm.value)
    doLogin(res)
  } catch (e) {
    console.error('登录失败:', e.message)
  } finally {
    loading.value = false
  }
}

const showResetDialog = () => {
  resetForm.value = { tenantId: tenants.value[0]?.id || null, phone: '', code: '', newPassword: '' }
  resetCountdown.value = 0
  if (resetCountdownTimer) { clearInterval(resetCountdownTimer); resetCountdownTimer = null }
  resetDialogVisible.value = true
}

const handleResetSendCode = async () => {
  if (!resetForm.value.tenantId) { ElMessage.warning('请先选择租户'); return }
  if (!resetForm.value.phone || !/^1[3-9]\d{9}$/.test(resetForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号'); return
  }
  try {
    await sendCode({ phone: resetForm.value.phone, tenantId: resetForm.value.tenantId })
    ElMessage.success('验证码已发送')
    resetCountdown.value = 60
    resetCountdownTimer = setInterval(() => {
      resetCountdown.value--
      if (resetCountdown.value <= 0) {
        clearInterval(resetCountdownTimer)
        resetCountdownTimer = null
      }
    }, 1000)
  } catch (e) {
    console.error('发送验证码失败:', e.message)
  }
}

const handleResetPassword = async () => {
  try {
    await resetFormRef.value.validate()
  } catch (e) {
    return
  }
  resetLoading.value = true
  try {
    await resetPassword(resetForm.value)
    ElMessage.success('密码重置成功，请使用新密码登录')
    resetDialogVisible.value = false
    passwordForm.value.phone = resetForm.value.phone
    passwordForm.value.password = ''
    loginMode.value = 'password'
  } catch (e) {
    console.error('重置密码失败:', e.message)
  } finally {
    resetLoading.value = false
  }
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
  margin-bottom: 24px;
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

    &:hover { color: #667eea; }

    &.active {
      color: #667eea;
      font-weight: 600;
      border-bottom-color: #667eea;
    }
  }
}

.code-row {
  display: flex;
  gap: 12px;

  .send-code-btn {
    flex-shrink: 0;
    width: 120px;
    height: 40px;
    border-radius: 8px;
    font-size: 13px;
  }
}

.super-admin-check {
  margin-bottom: 16px;
  margin-top: -8px;
}

.form-extra {
  text-align: right;
  margin-top: -8px;
  margin-bottom: 8px;
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

:deep(.el-select__wrapper) {
  border-radius: 8px;
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

.login-footer {
  text-align: center;
  margin-top: 24px;

  .demo-account {
    font-size: 13px;
    color: #909399;
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
