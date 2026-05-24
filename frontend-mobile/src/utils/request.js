import { BASE_URL } from './config'

const REQUEST_TIMEOUT = 15000

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
    const sendRequest = (overrideToken) => {
      const token = overrideToken || uni.getStorageSync('token')

      if (options.showLoading !== false) {
        uni.showLoading({ title: '加载中...', mask: true })
      }

      let requestTask = null
      let isTimeout = false

      const timeoutId = setTimeout(() => {
        isTimeout = true
        if (requestTask) {
          requestTask.abort()
        }
        uni.hideLoading()
        uni.showToast({ title: '请求超时，请重试', icon: 'none' })
        reject(new Error('请求超时'))
      }, REQUEST_TIMEOUT)

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
          if (isTimeout) return

          uni.hideLoading()

          if (res.statusCode === 200) {
            if (res.data.code === 200) {
              resolve(res.data)
            } else {

              const errorMsg = getErrorMessage(res.data.code) || res.data.message || '请求失败'
              if (options.showError !== false) {
                uni.showToast({ title: errorMsg, icon: 'none' })
              }
              reject(res.data)
            }
          } else if (res.statusCode === 401) {

            if (options._retry) {
              handleUnauthorized()
              reject(res)
              return
            }
            options._retry = true

            if (!isRefreshing) {
              isRefreshing = true
              try {
                const newToken = await refreshTokenRequest()
                executeRefreshQueue(true, newToken)

                sendRequest(newToken)
                return
              } catch (refreshErr) {
                executeRefreshQueue(false)
                handleUnauthorized()
                reject(res)
                return
              } finally {
                isRefreshing = false
              }
            } else {

              refreshQueue.push({
                resolve: (newToken) => {
                  sendRequest(newToken)
                },
                reject: () => {
                  handleUnauthorized()
                  reject(res)
                }
              })
            }
          } else {
            if (options.showError !== false) {
              const httpMsg = HTTP_ERROR_MESSAGES[res.statusCode] || `请求失败(${res.statusCode})`
              uni.showToast({ title: httpMsg, icon: 'none' })
            }
            reject(res)
          }
        },
        fail: (err) => {
          clearTimeout(timeoutId)
          if (isTimeout) return

          uni.hideLoading()
          if (options.showError !== false) {
            uni.showToast({ title: '网络连接失败', icon: 'none' })
          }
          reject(err)
        }
      })
    }

    sendRequest()
  })
}

const handleUnauthorized = () => {
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
