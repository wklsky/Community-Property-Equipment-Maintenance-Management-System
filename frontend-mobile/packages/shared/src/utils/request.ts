/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/utils/request.ts
 * @Description: 移动端统一请求封装，处理 Token 注入、无感刷新、错误提示与全局 loading
 */

import type { ApiResponse, HttpOptions } from '../types/auth'

export interface HttpConfig {
  baseURL: string
  /**
   * 会话失效（刷新 Token 也失败）后的跳转钩子。
   * 两个 App 的登录页路径与跳转方式不同，必须由宿主注入，
   * 否则 shared 包会硬编码页面路由，直接破坏复用性
   */
  onUnauthorized?: () => void
  /** 清除本地登录态的钩子，默认清 token / refreshToken / userInfo */
  onSessionExpired?: () => void
}

let httpConfig: HttpConfig = {
  baseURL: '/api/v1'
}

/**
 * 合并配置时剔除 undefined 字段。
 * 宿主 App 未提供 VITE_APP_BASE_URL 时，import.meta.env 的取值就是 undefined，
 * 直接展开会把默认的 '/api/v1' 静默覆盖成 undefined，
 * 请求因此打到 "undefined/repair-orders/my" 且报的是网络错误，极难定位到配置缺失
 */
export const configureHttp = (config: Partial<HttpConfig>): void => {
  const next: Partial<HttpConfig> = {}
  const keys = Object.keys(config) as (keyof HttpConfig)[]
  keys.forEach((key) => {
    if (config[key] !== undefined) {
      // TS 无法推导"键名与值类型匹配"的动态赋值，这里用 Object.assign 收口，
      // 避免为绕开类型检查而使用 any 污染整个请求层
      Object.assign(next, { [key]: config[key] })
    }
  })
  httpConfig = { ...httpConfig, ...next }
}

export const getHttpConfig = (): HttpConfig => httpConfig

// uni 的 loading 是全局单例：若每个请求各自 show/hide，先返回的请求会提前
// 关掉其他在途请求的 loading，因此统一用计数器控制显隐
let loadingCount = 0

const showLoading = (options: HttpOptions): void => {
  if (options.quiet) return
  loadingCount += 1
  if (loadingCount === 1) {
    uni.showLoading({ title: '加载中...', mask: true })
  }
}

const hideLoading = (options: HttpOptions): void => {
  if (options.quiet) return
  if (loadingCount > 0) {
    loadingCount -= 1
  }
  if (loadingCount === 0) {
    uni.hideLoading()
  }
}

const HTTP_ERROR_MESSAGES: Record<number, string> = {
  400: '请求参数错误',
  401: '登录已过期',
  403: '没有权限访问',
  404: '请求的资源不存在',
  408: '请求超时',
  429: '操作过于频繁，请稍后再试',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂不可用',
  504: '网关超时'
}

let isRefreshing = false

interface RefreshWaiter {
  resolve: (token: string) => void
  reject: (error: unknown) => void
}

let refreshQueue: RefreshWaiter[] = []

/**
 * 结算等待刷新结果的并发请求。
 * 失败时必须一并 reject：只 resolve 成功分支会让等待中的 Promise 永远处于 pending，
 * 既泄漏内存，也让这些请求各自持有的 loading 再也没有机会关闭
 */
const flushRefreshQueue = (result: { ok: true; token: string } | { ok: false; error: unknown }): void => {
  const queue = refreshQueue
  refreshQueue = []
  queue.forEach((waiter) => {
    if (result.ok) {
      waiter.resolve(result.token)
      return
    }
    waiter.reject(result.error)
  })
}

const clearSession = (): void => {
  if (httpConfig.onSessionExpired) {
    httpConfig.onSessionExpired()
    return
  }
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
}

const refreshToken = (): Promise<string> => {
  const current = uni.getStorageSync('refreshToken') as string
  if (!current) {
    return Promise.reject(new Error('无可用刷新 Token'))
  }

  return new Promise<string>((resolve, reject) => {
    uni.request({
      url: `${httpConfig.baseURL}/auth/refresh`,
      method: 'POST',
      data: { refreshToken: current },
      header: { 'Content-Type': 'application/json' },
      success: (res) => {
        const body = res.data as ApiResponse<{ token: string; refreshToken: string }>
        if (res.statusCode === 200 && body?.code === 200) {
          uni.setStorageSync('token', body.data.token)
          uni.setStorageSync('refreshToken', body.data.refreshToken)
          resolve(body.data.token)
          return
        }
        reject(new Error(body?.message || 'Token 刷新失败'))
      },
      fail: (err) => reject(err)
    })
  })
}

const handleUnauthorized = (): void => {
  loadingCount = 0
  uni.hideLoading()
  clearSession()
  httpConfig.onUnauthorized?.()
}

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE'
type Settler = (value: unknown) => void

const doRequest = <T>(
  method: Method,
  url: string,
  data?: unknown,
  options: HttpOptions = {}
): Promise<ApiResponse<T>> =>
  new Promise<ApiResponse<T>>((resolve, reject) => {
    // 重试标记必须是本次请求私有的：若挂在共享 options 上，
    // 复用 options 对象的后续请求会直接跳过刷新流程
    let retried = false

    const send = (overrideToken?: string): void => {
      const token = (overrideToken ?? uni.getStorageSync('token')) as string
      let settled = false

      const settle = (fn: Settler, value: unknown): void => {
        if (settled) return
        settled = true
        hideLoading(options)
        fn(value)
      }

      const resolveWith = resolve as unknown as Settler
      const rejectWith = reject as unknown as Settler

      showLoading(options)

      uni.request({
        url: `${httpConfig.baseURL}${url}`,
        method,
        data: data as never,
        header: {
          'Content-Type': 'application/json',
          Authorization: token ? `Bearer ${token}` : ''
        },
        timeout: options.timeout ?? 15000,
        success: (res) => {
          if (settled) return

          const body = res.data as ApiResponse<T>

          if (res.statusCode === 200 && body?.code === 200) {
            settle(resolveWith, body)
            return
          }

          if (res.statusCode === 401) {
            if (retried) {
              handleUnauthorized()
              settle(rejectWith, Object.assign(new Error('登录已过期'), { code: body?.code ?? 401 }))
              return
            }
            retried = true

            if (!isRefreshing) {
              isRefreshing = true
              refreshToken()
                .then((newToken) => {
                  flushRefreshQueue({ ok: true, token: newToken })
                  settled = true
                  hideLoading(options)
                  send(newToken)
                })
                .catch((error: unknown) => {
                  flushRefreshQueue({ ok: false, error })
                  handleUnauthorized()
                  settle(rejectWith, new Error('登录已过期'))
                })
                .finally(() => {
                  isRefreshing = false
                })
              return
            }

            refreshQueue.push({
              resolve: (newToken) => {
                settled = true
                hideLoading(options)
                send(newToken)
              },
              reject: (error: unknown) => {
                settle(rejectWith, error)
              }
            })
            return
          }

          const message =
            body?.message || HTTP_ERROR_MESSAGES[res.statusCode] || `请求失败(${res.statusCode})`

          if (!options.silent) {
            uni.showToast({ title: message, icon: 'none' })
          }

          // 把后端业务码挂在异常上：登录页要据此区分「租户不存在」与「密码错误」，
          // 仅凭 message 文案映射在后端调整措辞后就会失效
          settle(rejectWith, Object.assign(new Error(message), { code: body?.code ?? null }))
        },
        fail: (err) => {
          if (!options.silent) {
            uni.showToast({ title: '网络连接失败，请检查网络', icon: 'none' })
          }
          settle(
            rejectWith,
            Object.assign(new Error('网络连接失败'), { isNetworkError: true, cause: err })
          )
        }
      })
    }

    send()
  })

export const http = {
  get<T>(
    url: string,
    params?: Record<string, unknown>,
    options?: HttpOptions
  ): Promise<ApiResponse<T>> {
    return doRequest<T>('GET', url, params, options)
  },
  post<T>(url: string, data?: unknown, options?: HttpOptions): Promise<ApiResponse<T>> {
    return doRequest<T>('POST', url, data, options)
  },
  put<T>(url: string, data?: unknown, options?: HttpOptions): Promise<ApiResponse<T>> {
    return doRequest<T>('PUT', url, data, options)
  },
  del<T>(url: string, data?: unknown, options?: HttpOptions): Promise<ApiResponse<T>> {
    return doRequest<T>('DELETE', url, data, options)
  }
}
