/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/composables/useLogin.ts
 * @Description: 业主 App 与维修工 App 共用的登录业务逻辑，含租户解析、模式切换、验证码倒计时与越权拦截
 */

import { computed, onUnmounted, ref, type ComputedRef, type Ref } from 'vue'
import { getTenants, login, loginByCode, sendCode } from '../api/auth'
import {
  DEFAULT_LOGIN_ERROR_MESSAGE,
  LOGIN_DEBOUNCE_MS,
  LOGIN_ERROR_MESSAGES,
  SMS_COUNTDOWN_SECONDS
} from '../constants/auth'
import { useUserStore } from '../stores/user'
import { debounce, type DebouncedFn } from '../utils/debounce'
import { checkRoleAllowed } from '../utils/role'
import { validatePasswordLogin, validateSendSms, validateSmsLogin } from '../utils/validate'
import type {
  AppIdentity,
  LoginFormModel,
  LoginMode,
  LoginResult,
  TenantOption
} from '../types/auth'

export interface UseLoginOptions {
  /** 当前 App 的身份信息，决定允许登录的角色与越权提示 */
  identity: AppIdentity
  /** 登录成功且角色校验通过后的回调，由宿主决定跳转方式 */
  onSuccess?: (result: LoginResult) => void
}

export interface UseLoginReturn {
  form: Ref<LoginFormModel>
  mode: Ref<LoginMode>
  loading: Ref<boolean>
  tenants: Ref<TenantOption[]>
  /** 租户输入联想结果，供 UI 渲染候选列表 */
  tenantSuggestions: ComputedRef<TenantOption[]>
  smsCountdown: Ref<number>
  smsButtonText: ComputedRef<string>
  loadTenants: () => Promise<void>
  switchMode: (mode: LoginMode) => void
  pickTenant: (tenant: TenantOption) => void
  handleSendSmsCode: () => Promise<void>
  /** 已内置防抖，可直接绑定登录按钮 */
  submit: DebouncedFn<[]>
}

const createForm = (): LoginFormModel => ({
  tenantKeyword: '',
  tenantId: null,
  account: '',
  password: '',
  smsCode: ''
})

/** 从异常上读取后端业务码：request 已把 code 挂在异常实例上 */
const readBusinessCode = (error: unknown): number | null => {
  if (error && typeof error === 'object' && 'code' in error) {
    const code = (error as { code?: unknown }).code
    return typeof code === 'number' ? code : null
  }
  return null
}

const isNetworkError = (error: unknown): boolean =>
  !!error &&
  typeof error === 'object' &&
  (error as { isNetworkError?: boolean }).isNetworkError === true

export function useLogin(options: UseLoginOptions): UseLoginReturn {
  const { identity, onSuccess } = options
  const userStore = useUserStore()

  const form = ref<LoginFormModel>(createForm())
  const mode = ref<LoginMode>('password')
  const loading = ref(false)
  const tenants = ref<TenantOption[]>([])
  const smsCountdown = ref(0)

  let countdownTimer: ReturnType<typeof setInterval> | null = null

  // ---------- 租户 ----------

  const loadTenants = async (): Promise<void> => {
    try {
      const res = await getTenants({ silent: true })
      tenants.value = Array.isArray(res.data) ? res.data : []
    } catch (error) {
      console.error('[login] 加载租户列表失败:', error)
      uni.showToast({ title: '租户列表加载失败，请检查网络', icon: 'none' })
    }
  }

  const tenantSuggestions = computed<TenantOption[]>(() => {
    const keyword = form.value.tenantKeyword.trim()
    if (!keyword) return []
    return tenants.value.filter((tenant) => tenant.name.includes(keyword)).slice(0, 8)
  })

  const pickTenant = (tenant: TenantOption): void => {
    form.value.tenantKeyword = tenant.name
    form.value.tenantId = tenant.id
  }

  /**
   * 把用户键入的租户文本解析成租户 ID。
   * 后端只接受 tenantId，且不允许歧义匹配：同名租户若被随意解析，
   * 用户可能登录到别的社区，属于数据越权
   */
  const resolveTenant = (): boolean => {
    if (form.value.tenantId !== null) return true

    const keyword = form.value.tenantKeyword.trim()
    if (!keyword) {
      uni.showToast({ title: '请输入社区名称', icon: 'none' })
      return false
    }

    const exact = tenants.value.filter((tenant) => tenant.name === keyword)
    if (exact.length === 1) {
      form.value.tenantId = exact[0].id
      return true
    }

    const fuzzy = tenants.value.filter((tenant) => tenant.name.includes(keyword))
    if (fuzzy.length === 1) {
      form.value.tenantId = fuzzy[0].id
      return true
    }

    if (fuzzy.length > 1) {
      uni.showToast({ title: '匹配到多个社区，请补全名称后选择', icon: 'none' })
      return false
    }

    uni.showToast({ title: '社区不存在，请检查输入', icon: 'none' })
    return false
  }

  // ---------- 模式切换与倒计时 ----------

  const stopCountdown = (): void => {
    if (countdownTimer !== null) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }

  const switchMode = (next: LoginMode): void => {
    if (mode.value === next) return
    mode.value = next
    // 切换模式时清空另一模式的凭据，避免残留值被误提交
    form.value.password = ''
    form.value.smsCode = ''
    stopCountdown()
    smsCountdown.value = 0
  }

  const startCountdown = (): void => {
    smsCountdown.value = SMS_COUNTDOWN_SECONDS
    stopCountdown()
    countdownTimer = setInterval(() => {
      smsCountdown.value -= 1
      if (smsCountdown.value <= 0) {
        stopCountdown()
      }
    }, 1000)
  }

  const smsButtonText = computed<string>(() =>
    smsCountdown.value > 0 ? `${smsCountdown.value}s` : '获取验证码'
  )

  // ---------- 登录 ----------

  const handleFailure = (error: unknown): void => {
    console.error('[login] 登录失败:', error)

    const code = readBusinessCode(error)
    if (code !== null && LOGIN_ERROR_MESSAGES[code]) {
      uni.showToast({ title: LOGIN_ERROR_MESSAGES[code], icon: 'none' })
      // 密码错误必须清空输入框，否则用户会反复提交同一错误凭据触发风控
      if (code === 1003) {
        form.value.password = ''
      }
      return
    }

    if (isNetworkError(error)) {
      uni.showToast({ title: '无法连接到服务器，请检查网络', icon: 'none' })
      return
    }

    uni.showToast({
      title: (error instanceof Error ? error.message : '') || DEFAULT_LOGIN_ERROR_MESSAGE,
      icon: 'none'
    })
  }

  /**
   * 登录成功后的角色校验。
   * 必须与路由守卫使用同一套规则：维修工账号在业主 App 里即便拿到合法 Token，
   * 也不允许落盘——一旦写入本地，守卫读取到的角色会被"已登录"状态掩盖
   */
  const handleSuccess = (result: LoginResult): void => {
    const check = checkRoleAllowed(result.roleName, identity.appId)
    if (!check.allowed) {
      userStore.logout()
      uni.showModal({
        title: '无法登录',
        content: check.message,
        showCancel: false,
        confirmText: '我知道了'
      })
      return
    }

    userStore.applyLoginResult(result)
    uni.showToast({ title: '登录成功', icon: 'success', duration: 1500 })
    onSuccess?.(result)
  }

  const requestLogin = async (
    requestFn: () => Promise<{ data: LoginResult }>
  ): Promise<void> => {
    loading.value = true
    try {
      const res = await requestFn()
      handleSuccess(res.data)
    } catch (error) {
      handleFailure(error)
    } finally {
      loading.value = false
    }
  }

  const runSubmit = async (): Promise<void> => {
    if (loading.value) return

    if (!resolveTenant()) return

    const tenantId = form.value.tenantId as number

    if (mode.value === 'password') {
      const result = validatePasswordLogin({
        tenantId,
        account: form.value.account,
        password: form.value.password
      })
      if (!result.valid) {
        uni.showToast({ title: result.message, icon: 'none' })
        return
      }
      await requestLogin(() =>
        login(
          { phone: form.value.account.trim(), password: form.value.password, tenantId },
          { silent: true }
        )
      )
      return
    }

    const result = validateSmsLogin({
      tenantId,
      account: form.value.account,
      smsCode: form.value.smsCode
    })
    if (!result.valid) {
      uni.showToast({ title: result.message, icon: 'none' })
      return
    }
    await requestLogin(() =>
      loginByCode(
        { phone: form.value.account.trim(), code: form.value.smsCode.trim(), tenantId },
        { silent: true }
      )
    )
  }

  const submit = debounce(() => {
    void runSubmit()
  }, LOGIN_DEBOUNCE_MS)

  const handleSendSmsCode = async (): Promise<void> => {
    if (smsCountdown.value > 0) return
    if (!resolveTenant()) return

    const result = validateSendSms({
      tenantId: form.value.tenantId,
      account: form.value.account
    })
    if (!result.valid) {
      uni.showToast({ title: result.message, icon: 'none' })
      return
    }

    try {
      await sendCode(
        { phone: form.value.account.trim(), tenantId: form.value.tenantId as number },
        { silent: true }
      )
      uni.showToast({ title: '验证码已发送', icon: 'success' })
      startCountdown()
    } catch (error) {
      console.error('[login] 发送验证码失败:', error)
      uni.showToast({
        title: (error instanceof Error ? error.message : '') || '验证码发送失败，请稍后重试',
        icon: 'none'
      })
    }
  }

  onUnmounted(() => {
    stopCountdown()
    submit.cancel()
  })

  return {
    form,
    mode,
    loading,
    tenants,
    tenantSuggestions,
    smsCountdown,
    smsButtonText,
    loadTenants,
    switchMode,
    pickTenant,
    handleSendSmsCode,
    submit
  }
}
