/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 11:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 11:20
 * @FilePath: frontend-mobile/src/utils/request.js
 * @Description: 移动端统一请求封装，处理 Token 注入、无感刷新、错误提示与 loading 管理
 */

import { BASE_URL, REQUEST_TIMEOUT } from './config'

// 并发请求计数：uni 的 loading 是全局单例，若每个请求各自 show/hide，
// 先返回的请求会提前关掉其他在途请求的 loading，故统一由计数器控制显隐
let loadingCount = 0

const showLoading = (options) => {
  if (options.showLoading === false) return
  loadingCount += 1
  if (loadingCount === 1) {
    uni.showLoading({ title: '加载中...', mask: true })
  }
}

const hideLoading = (options) => {
  if (options.showLoading === false) return
  if (loadingCount > 0) {
    loadingCount -= 1
  }
  if (loadingCount === 0) {
    uni.hideLoading()
  }
}

let isRefreshing = false

let refreshQueue = []

const executeRefreshQueue = (success, newToken) => {
  refreshQueue.forEach(({ resolve, reject }) => {
    if (success) {
      resolve(newToken)
    } else {
      reject(new Error('Token刷新失败'))
    }
  })
  refreshQueue = []
}

const refreshTokenRequest = async () => {
  const refreshToken = uni.getStorageSync('refreshToken')
  if (!refreshToken) {
    throw new Error('无刷新Token')
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + '/auth/refresh',
      method: 'POST',
      data: { refreshToken },
      header: { 'Content-Type': 'application/json' },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          const data = res.data.data
          uni.setStorageSync('token', data.token)
          uni.setStorageSync('refreshToken', data.refreshToken)
          resolve(data.token)
        } else {
          reject(new Error('Token刷新失败'))
        }
      },
      fail: (err) => reject(err)
    })
  })
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 重试标记必须是请求私有的：原先写在 options._retry 上会污染调用方传入的对象，
    // 该对象若被复用会导致后续请求直接跳过刷新流程
    let retried = false

    const sendRequest = (overrideToken) => {
      const token = overrideToken || uni.getStorageSync('token')
      const timeout = options.timeout || REQUEST_TIMEOUT

      showLoading(options)

      let requestTask = null
      let isSettled = false

      const settle = (fn, value) => {
        if (isSettled) return
        isSettled = true
        hideLoading(options)
        fn(value)
      }

      const timeoutId = setTimeout(() => {
        // H5 端 uni.request 的返回值不保证实现 abort，需可选调用避免二次抛错
        requestTask?.abort?.()
        settle(reject, new Error('请求超时'))
        uni.showToast({ title: '请求超时，请重试', icon: 'none' })
      }, timeout)

      requestTask = uni.request({
        url: BASE_URL + options.url,
        method: options.method || 'GET',
        data: options.data,
        header: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : '',
          ...options.header
        },
        success: async (res) => {
          clearTimeout(timeoutId)
          if (isSettled) return

          if (res.statusCode === 200) {
            if (res.data.code === 200) {
              settle(resolve, res.data)
              return
            }

            const errorMsg = getErrorMessage(res.data.code) || res.data.message || '请求失败'
            if (options.showError !== false) {
              uni.showToast({ title: errorMsg, icon: 'none' })
            }
            settle(reject, res.data)
            return
          }

          if (res.statusCode === 401) {
            if (retried) {
              handleUnauthorized()
              settle(reject, res)
              return
            }
            retried = true

            if (!isRefreshing) {
              isRefreshing = true
              try {
                const newToken = await refreshTokenRequest()
                executeRefreshQueue(true, newToken)
                clearTimeout(timeoutId)
                isSettled = true
                hideLoading(options)
                sendRequest(newToken)
                return
              } catch (refreshErr) {
                executeRefreshQueue(false)
                handleUnauthorized()
                settle(reject, res)
                return
              } finally {
                isRefreshing = false
              }
            } else {
              refreshQueue.push({
                resolve: (newToken) => {
                  clearTimeout(timeoutId)
                  isSettled = true
                  hideLoading(options)
                  sendRequest(newToken)
                },
                reject: () => {
                  handleUnauthorized()
                  settle(reject, res)
                }
              })
              return
            }
          }

          if (options.showError !== false) {
            const httpMsg = HTTP_ERROR_MESSAGES[res.statusCode] || `请求失败(${res.statusCode})`
            uni.showToast({ title: httpMsg, icon: 'none' })
          }
          settle(reject, res)
        },
        fail: (err) => {
          clearTimeout(timeoutId)
          if (isSettled) return

          if (options.showError !== false) {
            uni.showToast({ title: '网络连接失败', icon: 'none' })
          }
          settle(reject, err)
        }
      })
    }

    sendRequest()
  })
}

const handleUnauthorized = () => {
  loadingCount = 0
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
  uni.showModal({
    title: '提示',
    content: '登录已过期，请重新登录',
    showCancel: false,
    success: () => {
      uni.reLaunch({ url: '/pages/login/index' })
    }
  })
}

const ERROR_MESSAGES = {
  1001: '用户不存在',
  1002: '用户已被禁用',
  1003: '密码错误',
  1004: 'Token无效或已过期',
  1005: 'Token已过期',
  2001: '无操作权限',
  2003: '权限不足',
  3001: '租户不存在',
  3002: '租户已被禁用',
  3003: '租户数据不匹配',
  4001: '工单不存在',
  4002: '工单状态不允许此操作',
  4003: '工单已被分配',
  5001: '设备不存在',
  5002: '设备已停用',
  6001: '巡检计划不存在',
  6002: '巡检任务不存在',
  422: '参数校验失败'
}

const HTTP_ERROR_MESSAGES = {
  400: '请求参数错误',
  401: '登录已过期',
  403: '没有权限访问',
  404: '请求的资源不存在',
  405: '请求方法不允许',
  408: '请求超时',
  429: '请求过于频繁',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂不可用',
  504: '网关超时'
}

const getErrorMessage = (code) => ERROR_MESSAGES[code]

export const get = (url, data, options = {}) => request({ url, method: 'GET', data, ...options })
export const post = (url, data, options = {}) => request({ url, method: 'POST', data, ...options })
export const put = (url, data, options = {}) => request({ url, method: 'PUT', data, ...options })
export const del = (url, data, options = {}) => request({ url, method: 'DELETE', data, ...options })

export default request
