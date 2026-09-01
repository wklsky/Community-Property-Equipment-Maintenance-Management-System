<template>
  <el-dialog
    :model-value="visible"
    title="重置密码"
    width="420px"
    append-to-body
    :close-on-click-modal="false"
    @update:model-value="handleVisibleChange"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px" size="large">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" maxlength="11" placeholder="请输入手机号" />
      </el-form-item>

      <el-form-item label="验证码" prop="code">
        <div class="code-row">
          <el-input v-model="form.code" maxlength="6" placeholder="请输入6位验证码" />
          <el-button :disabled="sending" @click="handleSendCode">{{ countdownText }}</el-button>
        </div>
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          show-password
          placeholder="请输入新密码(6-20位)"
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleVisibleChange(false)">取 消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">确认重置</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/views/login/ResetPasswordDialog.vue
 * @Description: 忘记密码弹窗，自身持有 useResetPassword 逻辑并通过 open() 对外暴露唤起入口
 */

import { toRef } from 'vue'
import { useResetPassword } from './useResetPassword'

interface ResetPasswordDialogProps {
  /** 登录表单已解析出的租户 ID，重置密码与登录必须落在同一租户下 */
  tenantId: number | null
}

const props = withDefaults(defineProps<ResetPasswordDialogProps>(), {
  tenantId: null
})

const emit = defineEmits<{
  /** 重置成功，回传 { phone } 供登录表单回填账号 */
  success: [payload: { phone: string }]
}>()

const {
  visible,
  formRef,
  form,
  rules,
  loading,
  sending,
  countdownText,
  open,
  close,
  handleSendCode,
  submit
} = useResetPassword({
  tenantId: toRef(props, 'tenantId'),
  onSuccess: (payload) => emit('success', payload)
})

const handleVisibleChange = (next: boolean): void => {
  // 关闭统一走 Hook 的 close()，保证倒计时定时器一并被清理
  if (next) return
  close()
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.code-row {
  display: flex;
  gap: 12px;
  width: 100%;

  .el-button {
    flex-shrink: 0;
    width: 120px;
  }
}
</style>
