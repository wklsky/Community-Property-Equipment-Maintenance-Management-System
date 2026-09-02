/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/views/login/useResetPassword.ts
 * @Description: 忘记密码弹窗的业务逻辑：验证码倒计时、二次密码一致性校验与重置请求
 */

import { computed, onUnmounted, ref, type ComputedRef, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { resetPassword, sendCode } from '@/api/auth'
import {
  ACCOUNT_PATTERN,
  DEFAULT_LOGIN_ERROR_MESSAGE,
  PASSWORD_MAX_LENGTH,
  PASSWORD_MIN_LENGTH,
  SMS_CODE_PATTERN,
  SMS_COUNTDOWN_SECONDS
} from '@/constants/auth'

export interface ResetPasswordForm {
  phone: string
  code: string
  newPassword: string
  confirmPassword: string
}

export interface UseResetPasswordOptions {
  /**
   * 复用登录表单已解析出的租户 ID（响应式引用）。
   * 重置密码与登录共用同一个租户，要求用户再选一次既多余又容易选错
   */
  tenantId: Ref<number | null>
  /** 重置成功后回调，回传 { phone } 供登录表单回填账号 */
  onSuccess?: (payload: { phone: string }) => void
}

export interface UseResetPasswordReturn {
  visible: Ref<boolean>
  formRef: Ref<FormInstance | undefined>
  form: Ref<ResetPasswordForm>
  rules: FormRules<ResetPasswordForm>
  loading: Ref<boolean>
  sending: Ref<boolean>
  countdown: Ref<number>
  countdownText: ComputedRef<string>
  open: (presetPhone?: string) => void
  close: () => void
  handleSendCode: () => Promise<void>
  submit: () => Promise<void>
}

const createForm = (): ResetPasswordForm => ({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

export function useResetPassword(options: UseResetPasswordOptions): UseResetPasswordReturn {
  const visible = ref(false)
  const formRef = ref<FormInstance>()
  const form = ref<ResetPasswordForm>(createForm())
  const loading = ref(false)
  const sending = ref(false)
  const countdown = ref(0)

  let countdownTimer: ReturnType<typeof setInterval> | null = null

  const stopCountdown = (): void => {
    if (countdownTimer !== null) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }

  const countdownText = computed<string>(() =>
    countdown.value > 0 ? `${countdown.value}s 后重发` : '发送验证码'
  )

  const rules: FormRules<ResetPasswordForm> = {
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: ACCOUNT_PATTERN, message: '手机号格式不正确', trigger: 'blur' }
    ],
    code: [
      { required: true, message: '请输入验证码', trigger: 'blur' },
      { pattern: SMS_CODE_PATTERN, message: '验证码为 6 位数字', trigger: 'blur' }
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      {
        min: PASSWORD_MIN_LENGTH,
        max: PASSWORD_MAX_LENGTH,
        message: `密码长度需在 ${PASSWORD_MIN_LENGTH}-${PASSWORD_MAX_LENGTH} 位之间`,
        trigger: 'blur'
      }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入新密码', trigger: 'blur' },
      {
        // 后端只接收一次新密码，两次输入的一致性只能在前端拦截，
        // 否则用户会把"以为设成功的密码"记错，最终被锁在登录页外
        validator: (_rule, value, callback) => {
          if (value !== form.value.newPassword) {
            callback(new Error('两次输入的密码不一致'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }

  const open = (presetPhone = ''): void => {
    form.value = { ...createForm(), phone: presetPhone }
    visible.value = true
    formRef.value?.clearValidate()
  }

  const close = (): void => {
    visible.value = false
    stopCountdown()
    countdown.value = 0
    form.value = createForm()
  }

  const handleSendCode = async (): Promise<void> => {
    if (countdown.value > 0 || sending.value) return

    const tenantId = options.tenantId.value
    if (tenantId === null) {
      ElMessage.warning('请先在登录表单中填写正确的租户')
      return
    }

    if (!ACCOUNT_PATTERN.test(form.value.phone.trim())) {
      ElMessage.warning('请输入正确的手机号')
      return
    }

    sending.value = true
    try {
      await sendCode({ phone: form.value.phone.trim(), tenantId }, { silent: true })
      ElMessage.success('验证码已发送')

      countdown.value = SMS_COUNTDOWN_SECONDS
      stopCountdown()
      countdownTimer = setInterval(() => {
        countdown.value -= 1
        if (countdown.value <= 0) {
          stopCountdown()
        }
      }, 1000)
    } catch (error) {
      console.error('[reset-password] 发送验证码失败:', error)
      ElMessage.error(
        (error instanceof Error ? error.message : '') || '验证码发送失败，请稍后重试'
      )
    } finally {
      sending.value = false
    }
  }

  const submit = async (): Promise<void> => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()
    } catch {
      return
    }

    const tenantId = options.tenantId.value
    if (tenantId === null) {
      ElMessage.warning('请先在登录表单中填写正确的租户')
      return
    }

    loading.value = true
    try {
      await resetPassword(
        {
          phone: form.value.phone.trim(),
          code: form.value.code.trim(),
          tenantId,
          newPassword: form.value.newPassword
        },
        { silent: true }
      )
      ElMessage.success('密码重置成功，请使用新密码登录')
      options.onSuccess?.({ phone: form.value.phone.trim() })
      close()
    } catch (error) {
      console.error('[reset-password] 重置失败:', error)
      ElMessage.error(
        (error instanceof Error ? error.message : '') || DEFAULT_LOGIN_ERROR_MESSAGE
      )
    } finally {
      loading.value = false
    }
  }

  onUnmounted(stopCountdown)

  return {
    visible,
    formRef,
    form,
    rules,
    loading,
    sending,
    countdown,
    countdownText,
    open,
    close,
    handleSendCode,
    submit
  }
}
