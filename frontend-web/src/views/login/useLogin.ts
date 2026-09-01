/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/views/login/useLogin.ts
 * @Description: 登录页全部业务逻辑：租户解析、模式切换、表单校验、验证码流程、登录请求与异常提示
 */

import { computed, onUnmounted, ref, type ComputedRef, type Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getTenants, login, loginByCode, sendCode } from '@/api/auth'
import { debounce, type DebouncedFn } from '@/utils/debounce'
import { useCaptcha, type UseCaptchaReturn } from './useCaptcha'
import {
  ACCOUNT_PATTERN,
  DEFAULT_HOME_PATH,
  DEFAULT_LOGIN_ERROR_MESSAGE,
  LOGIN_DEBOUNCE_MS,
  LOGIN_ERROR_MESSAGES,
  PASSWORD_MAX_LENGTH,
  PASSWORD_MIN_LENGTH,
  SMS_CODE_PATTERN,
  SMS_COUNTDOWN_SECONDS,
  SUPER_ADMIN_HOME_PATH
} from '@/constants/auth'
import type {
  LoginFormModel,
  LoginMode,
  LoginResult,
  PasswordLoginPayload,
  SmsLoginPayload,
  TenantOption,
  TenantResolveResult
} from '@/types/auth'
import type { ApiResponse } from '@/types/http'

export interface UseLoginOptions {
  /**
   * 登录成功后的落地页决策器。
   * PC 后台与移动端 H5 的落地规则不同，交给宿主注入，Hook 本身不耦合具体业务路由
   */
  resolveHomePath?: (result: LoginResult) => string
}

export interface UseLoginReturn {
  formRef: Ref<FormInstance | undefined>
  form: Ref<LoginFormModel>
  rules: ComputedRef<FormRules<LoginFormModel>>
  mode: Ref<LoginMode>
  loading: Ref<boolean>
  /** 超级管理员不属于任何租户，勾选后隐藏并跳过租户校验 */
  isSuperAdminLogin: Ref<boolean>
  tenants: Ref<TenantOption[]>
  smsCountdown: Ref<number>
  smsButtonText: ComputedRef<string>
  captchaVisible: Ref<boolean>
  captcha: UseCaptchaReturn
  switchMode: (mode: LoginMode) => void
  toggleSuperAdmin: (checked: boolean) => void
  queryTenants: (keyword: string, callback: (list: TenantOption[]) => void) => void
  handleTenantSelect: (tenant: TenantOption) => void
  handleSendSmsCode: () => Promise<void>
  /** 已内置防抖，可直接绑定按钮点击事件 */
  submit: DebouncedFn<[]>
  handleCaptchaConfirm: (code: string) => Promise<void>
}

const createForm = (): LoginFormModel => ({
  tenantKeyword: '',
  tenantId: null,
  account: '',
  password: '',
  smsCode: ''
})

/** 从异常对象上读取后端业务码；request 拦截器已把 code 挂在异常实例上 */
const readBusinessCode = (error: unknown): number | null => {
  if (error && typeof error === 'object' && 'code' in error) {
    const code = (error as { code?: unknown }).code
    return typeof code === 'number' ? code : null
  }
  return null
}

/** 判断是否请求根本没到达服务端（断网 / 后端未启动），与 5xx 区分提示 */
const isNetworkError = (error: unknown): boolean =>
  !!error && typeof error === 'object' && (error as { isNetworkError?: boolean }).isNetworkError === true

export function useLogin(options: UseLoginOptions = {}): UseLoginReturn {
  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()

  const formRef = ref<FormInstance>()
  const form = ref<LoginFormModel>(createForm())
  const mode = ref<LoginMode>('password')
  const loading = ref(false)
  const isSuperAdminLogin = ref(false)
  const tenants = ref<TenantOption[]>([])
  const smsCountdown = ref(0)
  const captchaVisible = ref(false)
  const captcha = useCaptcha()

  let countdownTimer: ReturnType<typeof setInterval> | null = null

  // ---------- 租户 ----------

  const loadTenants = async (): Promise<void> => {
    try {
      const res = await getTenants({ silent: true })
      tenants.value = Array.isArray(res.data) ? res.data : []
    } catch (error) {
      console.error('[login] 加载租户列表失败:', error)
      // 租户列表是登录的前置依赖，加载失败时给出可操作的指引，
      // 而不是让用户在一个无法提交的表单里反复尝试
      ElMessage.error('租户列表加载失败，请刷新页面或检查后端服务')
    }
  }

  void loadTenants()

  /**
   * 把用户键入的租户文本解析成租户 ID。
   * 后端 /auth/login 只接受 tenantId（Long），且不允许歧义匹配：
   * 同名租户若被随意解析，用户可能登录到别的社区，属于数据越权
   */
  const resolveTenant = (): TenantResolveResult => {
    const keyword = form.value.tenantKeyword.trim()
    if (!keyword) {
      return { ok: false, reason: 'empty', message: '请输入租户名称' }
    }

    const exact = tenants.value.filter((tenant) => tenant.name === keyword)
    if (exact.length === 1) {
      return { ok: true, tenant: exact[0] }
    }
    if (exact.length > 1) {
      return { ok: false, reason: 'ambiguous', message: '存在多个同名租户，请联系管理员处理' }
    }

    const fuzzy = tenants.value.filter((tenant) => tenant.name.includes(keyword))
    if (fuzzy.length === 1) {
      return { ok: true, tenant: fuzzy[0] }
    }
    if (fuzzy.length > 1) {
      return { ok: false, reason: 'ambiguous', message: '匹配到多个租户，请补全租户全称' }
    }

    return { ok: false, reason: 'not-found', message: '租户不存在，请检查输入的租户名称' }
  }

  const queryTenants = (keyword: string, callback: (list: TenantOption[]) => void): void => {
    const trimmed = keyword.trim()
    // 空关键词时返回全量，便于用户先浏览再选择
    callback(trimmed ? tenants.value.filter((t) => t.name.includes(trimmed)) : [...tenants.value])
  }

  const handleTenantSelect = (tenant: TenantOption): void => {
    form.value.tenantKeyword = tenant.name
    form.value.tenantId = tenant.id
  }

  // ---------- 表单校验 ----------

  const rules = computed<FormRules<LoginFormModel>>(() => ({
    tenantKeyword: isSuperAdminLogin.value
      ? []
      : [
          { required: true, message: '请输入租户名称', trigger: ['blur', 'change'] },
          {
            // 租户最终要落到 tenantId 上，这里只校验"能否唯一解析"，
            // 避免用户输入一半就提交、由后端返回晦涩的参数错误
            validator: (_rule, value, callback) => {
              const result = resolveTenant()
              if (result.ok) {
                callback()
                return
              }
              callback(new Error(result.message))
            },
            trigger: ['blur', 'change']
          }
        ],
    account: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { pattern: ACCOUNT_PATTERN, message: '账号为 11 位手机号', trigger: 'blur' }
    ],
    password:
      mode.value === 'password'
        ? [
            { required: true, message: '请输入密码', trigger: 'blur' },
            {
              min: PASSWORD_MIN_LENGTH,
              max: PASSWORD_MAX_LENGTH,
              message: `密码长度需在 ${PASSWORD_MIN_LENGTH}-${PASSWORD_MAX_LENGTH} 位之间`,
              trigger: 'blur'
            }
          ]
        : [],
    smsCode:
      mode.value === 'sms'
        ? [
            { required: true, message: '请输入短信验证码', trigger: 'blur' },
            { pattern: SMS_CODE_PATTERN, message: '验证码为 6 位数字', trigger: 'blur' }
          ]
        : []
  }))

  const validateForm = async (): Promise<boolean> => {
    if (!formRef.value) {
      console.error('[login] 表单实例未挂载，跳过校验')
      return false
    }
    try {
      await formRef.value.validate()
      return true
    } catch {
      // el-form 校验失败会 reject，这里只关心结果，具体错误已由表单自身渲染到字段下方
      return false
    }
  }

  // ---------- 模式切换 ----------

  const stopCountdown = (): void => {
    if (countdownTimer !== null) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }

  const switchMode = (next: LoginMode): void => {
    if (mode.value === next) return
    mode.value = next
    // 切换模式时清空另一模式的凭据，避免残留的密码/验证码被误提交
    form.value.password = ''
    form.value.smsCode = ''
    stopCountdown()
    smsCountdown.value = 0
    captchaVisible.value = false
    formRef.value?.clearValidate()
  }

  const toggleSuperAdmin = (checked: boolean): void => {
    isSuperAdminLogin.value = checked
    // 超级管理员不属于任何租户，切换时必须清掉上一次的租户选择
    form.value.tenantKeyword = ''
    form.value.tenantId = null
    formRef.value?.clearValidate(['tenantKeyword'])
  }

  // ---------- 短信验证码 ----------

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
    smsCountdown.value > 0 ? `${smsCountdown.value}s 后重发` : '发送验证码'
  )

  const handleSendSmsCode = async (): Promise<void> => {
    if (smsCountdown.value > 0) return

    let tenantId = form.value.tenantId
    if (!isSuperAdminLogin.value) {
      const resolved = resolveTenant()
      if (!resolved.ok) {
        ElMessage.warning(resolved.message)
        return
      }
      tenantId = resolved.tenant.id
      form.value.tenantId = tenantId
    }

    const phone = form.value.account.trim()
    if (!ACCOUNT_PATTERN.test(phone)) {
      ElMessage.warning('请输入正确的手机号后再获取验证码')
      return
    }

    try {
      await sendCode({ phone, tenantId: tenantId as number }, { silent: true })
      ElMessage.success('验证码已发送，请注意查收')
      startCountdown()
    } catch (error) {
      console.error('[login] 发送短信验证码失败:', error)
      ElMessage.error(readBusinessCode(error) ? (error as Error).message : '验证码发送失败，请稍后重试')
    }
  }

  // ---------- 登录 ----------

  const handleSuccess = (result: LoginResult): void => {
    userStore.setToken(result.token)
    if (result.refreshToken) {
      userStore.setRefreshToken(result.refreshToken)
    }
    // setUserInfo 内部会依据 roleName 推导权限码与菜单，必须先于路由跳转执行
    userStore.setUserInfo(result)
    ElMessage.success('登录成功')

    const redirect = route.query.redirect
    if (typeof redirect === 'string' && redirect) {
      router.push(redirect)
      return
    }

    router.push(options.resolveHomePath?.(result) ?? resolveDefaultHomePath(result))
  }

  const resolveDefaultHomePath = (result: LoginResult): string =>
    result.isSuperAdmin === true ? SUPER_ADMIN_HOME_PATH : DEFAULT_HOME_PATH

  const handleFailure = (error: unknown): void => {
    console.error('[login] 登录失败:', error)

    const code = readBusinessCode(error)
    if (code !== null && LOGIN_ERROR_MESSAGES[code]) {
      ElMessage.error(LOGIN_ERROR_MESSAGES[code])
      // 密码类错误必须让用户重新输入：保留旧密码会诱导重复提交同一错误凭据
      if (code === 1003) {
        form.value.password = ''
      }
      return
    }

    if (isNetworkError(error)) {
      ElMessage.error('无法连接到服务器，请检查网络或确认后端服务已启动')
      return
    }

    ElMessage.error(
      (error instanceof Error ? error.message : '') || DEFAULT_LOGIN_ERROR_MESSAGE
    )
  }

  const requestLogin = async (
    requestFn: () => Promise<ApiResponse<LoginResult>>
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

  const loginByPassword = async (): Promise<void> => {
    const payload: PasswordLoginPayload = {
      phone: form.value.account.trim(),
      password: form.value.password,
      tenantId: form.value.tenantId as number
    }
    await requestLogin(() => login(payload, { silent: true }))
  }

  const loginBySms = async (): Promise<void> => {
    const payload: SmsLoginPayload = {
      phone: form.value.account.trim(),
      code: form.value.smsCode.trim(),
      tenantId: form.value.tenantId as number
    }
    await requestLogin(() => loginByCode(payload, { silent: true }))
  }

  /**
   * 提交流程：
   * 模式 A（密码）先做完表单校验与租户解析，再弹出图形验证码，验证通过后才请求登录；
   * 模式 B（短信）的验证码本身就是第二因子，无需再叠图形验证码，直接登录
   */
  const runSubmit = async (): Promise<void> => {
    if (loading.value) return

    const valid = await validateForm()
    if (!valid) return

    if (!isSuperAdminLogin.value) {
      const resolved = resolveTenant()
      if (!resolved.ok) {
        ElMessage.warning(resolved.message)
        return
      }
      form.value.tenantId = resolved.tenant.id
    } else {
      form.value.tenantId = null
    }

    if (mode.value === 'password') {
      // 每次触发登录都换一张图，防止上一轮的验证码被重放
      captcha.reset()
      captchaVisible.value = true
      return
    }

    await loginBySms()
  }

  const submit = debounce(() => {
    void runSubmit()
  }, LOGIN_DEBOUNCE_MS)

  const handleCaptchaConfirm = async (code: string): Promise<void> => {
    if (!captcha.verify(code)) {
      ElMessage.error('验证码不正确，请重新输入')
      return
    }
    captchaVisible.value = false
    await loginByPassword()
  }

  onUnmounted(() => {
    stopCountdown()
    submit.cancel()
  })

  return {
    formRef,
    form,
    rules,
    mode,
    loading,
    isSuperAdminLogin,
    tenants,
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
  }
}
