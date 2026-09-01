import axios from 'axios'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

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
  const userStore = useUserStore()
  const refreshToken = userStore.refreshToken
  if (!refreshToken) {
    throw new Error('无刷新Token')
  }

  const res = await axios.post('/api/v1/auth/refresh', { refreshToken })
  if (res.data.code === 200) {
    const data = res.data.data
    userStore.setToken(data.token)
    userStore.setRefreshToken(data.refreshToken)
    return data.token
  }
  throw new Error(res.data.message || 'Token刷新失败')
}

const ERROR_MESSAGES = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '没有权限访问',
  404: '请求的资源不存在',
  405: '请求方法不允许',
  408: '请求超时',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂不可用',
  504: '网关超时'
}

const BUSINESS_ERROR_MESSAGES = {
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
  6002: '巡检任务不存在'
}

const service = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  },

  withCredentials: false
})

// 在途请求的 AbortController 集合，key 为请求唯一 ID（见请求拦截器中写入的 requestId）
const pendingRequests = new Map()

let requestSeq = 0

const removePendingRequest = (config) => {
  const requestId = config?.metadata?.requestId
  if (requestId) {
    pendingRequests.delete(requestId)
  }
}

service.interceptors.request.use(
  (config) => {

    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }

    if (userStore.tenantId) {
      config.headers['X-Tenant-Id'] = userStore.tenantId
    }

    if (config.method === 'get' && !config.noCache) {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }

    config.metadata = { startTime: new Date() }

    // 为每个请求绑定独立的 AbortController，使 cancelAllRequests() 能真正中断在途请求。
    // 调用方已自带 signal 时不再托管，避免覆盖其取消语义；
    // allowRepeat 用于上传等允许并发重复的场景，登记后会被 cancelAllRequests 误伤，故跳过
    if (!config.signal && !config.allowRepeat) {
      const controller = new AbortController()
      const requestId = `${Date.now()}-${++requestSeq}`
      config.signal = controller.signal
      config.metadata.requestId = requestId
      pendingRequests.set(requestId, controller)
    }

    return config
  },
  (error) => {
    console.error('请求配置错误:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {

    removePendingRequest(response.config)

    if (import.meta.env.DEV && response.config.metadata) {
      const duration = new Date() - response.config.metadata.startTime
      if (duration > 1000) {
        console.warn(`慢请求警告: ${response.config.url} 耗时 ${duration}ms`)
      }
    }

    const res = response.data

    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }

    if (res.code === 200) {
      return res
    }

    const errorMessage = BUSINESS_ERROR_MESSAGES[res.code] || res.message || '请求失败'

    if (res.code === 401 || res.code === 1004 || res.code === 1005) {
      handleUnauthorized(errorMessage)
      return Promise.reject(new Error(errorMessage))
    }

    if (res.code === 403 || res.code === 2001 || res.code === 2003) {
      ElMessage.error('您没有权限执行此操作')
      return Promise.reject(new Error(errorMessage))
    }

    if ([3001, 3002, 3003].includes(res.code)) {
      ElMessage.error(errorMessage)

      if (res.code === 3002) {
        handleUnauthorized('租户已被禁用，请联系管理员')
      }
      return Promise.reject(new Error(errorMessage))
    }

    if (!response.config.silent) {
      ElMessage.error(errorMessage)
    }
    return Promise.reject(new Error(errorMessage))
  },
  async (error) => {

    if (error.config) {
      removePendingRequest(error.config)
    }

    // 主动取消（登出时 cancelAllRequests、组件卸载、防抖）属于预期行为，不向用户报错
    if (axios.isCancel(error)) {
      if (import.meta.env.DEV) {
        console.warn('请求已取消:', error.message)
      }
      return Promise.reject(error)
    }

    if (!error.response) {
      ElNotification({
        title: '网络错误',
        message: '无法连接到服务器，请检查网络连接',
        type: 'error',
        duration: 5000
      })
      return Promise.reject(error)
    }

    const status = error.response?.status
    const serverMessage = error.response?.data?.message || ''
    const errorMessage = serverMessage || ERROR_MESSAGES[status] || error.message || '网络错误'

    // silent 用于轮询、预检等无需打扰用户的场景。
    // 401 不受 silent 约束：会话失效必须显式告知，否则用户会对空白页面无感知
    const isSilent = error.config?.silent === true

    if (status === 401) {

      const originalRequest = error.config
      if (!originalRequest._retry) {
        originalRequest._retry = true

        if (!isRefreshing) {
          isRefreshing = true
          try {
            const newToken = await refreshTokenRequest()
            executeRefreshQueue(true, newToken)

            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return service(originalRequest)
          } catch (refreshErr) {
            executeRefreshQueue(false)
            handleUnauthorized(errorMessage)
            return Promise.reject(error)
          } finally {
            isRefreshing = false
          }
        } else {

          return new Promise((resolve, reject) => {
            refreshQueue.push({
              resolve: (newToken) => {
                originalRequest.headers.Authorization = `Bearer ${newToken}`
                resolve(service(originalRequest))
              },
              reject: () => {
                reject(error)
              }
            })
          })
        }
      } else {

        handleUnauthorized(errorMessage)
      }
    } else if (status === 403) {
      if (!isSilent) ElMessage.error('没有权限访问该资源')
    } else if (status === 404) {
      if (!isSilent) ElMessage.error('请求的资源不存在')
    } else if (status >= 500) {
      // 5xx 属于服务端故障，保留独立通知样式以便与业务错误区分
      if (!isSilent) {
        ElNotification({
          title: '服务器错误',
          message: errorMessage,
          type: 'error',
          duration: 5000
        })
      }
    } else if (!isSilent) {
      ElMessage.error(errorMessage)
    }

    return Promise.reject(error)
  }
)

let isShowingLoginExpired = false
let logoutTimer = null

const handleUnauthorized = (message = '登录已过期，请重新登录') => {
  if (isShowingLoginExpired) return

  isShowingLoginExpired = true
  const userStore = useUserStore()

  if (logoutTimer) {
    clearTimeout(logoutTimer)
  }

  ElMessageBox.confirm(message, '提示', {
    confirmButtonText: '重新登录',
    cancelButtonText: '取消',
    type: 'warning',
    closeOnClickModal: false,
    closeOnPressEscape: false
  }).then(() => {

    userStore.logout()

    cancelAllRequests()

    const currentPath = router.currentRoute.value.fullPath
    router.push(`/login?redirect=${encodeURIComponent(currentPath)}`)
  }).catch(() => {

    logoutTimer = setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 5000)
  }).finally(() => {
    isShowingLoginExpired = false
  })
}

const request = {

  get(url, params, config = {}) {
    return service.get(url, { params, ...config })
  },

  post(url, data, config = {}) {
    return service.post(url, data, config)
  },

  put(url, data, config = {}) {
    return service.put(url, data, config)
  },

  delete(url, params, config = {}) {
    return service.delete(url, { params, ...config })
  },

  patch(url, data, config = {}) {
    return service.patch(url, data, config)
  },

  upload(url, file, onProgress, config = {}) {
    const formData = file instanceof FormData ? file : new FormData()
    if (!(file instanceof FormData)) {
      formData.append('file', file)
    }

    return service.post(url, formData, {
      timeout: 60000,
      allowRepeat: true,
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(percent)
        }
      },
      ...config
    })
  },

  download(url, params, filename, config = {}) {
    return service.get(url, {
      params,
      responseType: 'blob',
      timeout: 60000,
      ...config
    }).then((response) => {

      const contentDisposition = response.headers['content-disposition']
      let downloadFilename = filename
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
        if (filenameMatch && filenameMatch[1]) {
          downloadFilename = decodeURIComponent(filenameMatch[1].replace(/['"]/g, ''))
        }
      }

      const blob = new Blob([response.data])
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = downloadFilename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(link.href)

      return response
    })
  },

  silent(method, url, data) {
    const config = { silent: true }
    switch (method.toLowerCase()) {
      case 'get':
        return this.get(url, data, config)
      case 'post':
        return this.post(url, data, config)
      case 'put':
        return this.put(url, data, config)
      case 'delete':
        return this.delete(url, data, config)
      default:
        return this.get(url, data, config)
    }
  }
}

export const cancelAllRequests = (reason = '取消所有请求') => {
  pendingRequests.forEach((controller) => {
    controller.abort(reason)
  })
  pendingRequests.clear()
}

export const getAxiosInstance = () => service

export default request
