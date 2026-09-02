/**
 * @Author: kian
 * @Date: 2026-09-01 10:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 10:20
 * @FilePath: frontend-mobile/src/utils/config.js
 * @Description: 移动端全局环境配置，统一从 Vite 环境变量注入后端服务地址
 */

// 环境变量优先级：VITE_APP_BASE_URL（自定义部署）> 内置默认值
// 真机联调时后端必须绑定局域网 IP（0.0.0.0），localhost 在手机上指向手机自身
const FALLBACK_BASE_URL = 'http://localhost:8080/api/v1'

/**
 * 解析后端服务地址
 * uni-app 编译到小程序/App 时 window 不存在，需先做存在性判断避免 ReferenceError
 */
const resolveBaseUrl = () => {
  const fromEnv = import.meta.env?.VITE_APP_BASE_URL
  if (fromEnv) {
    return String(fromEnv).replace(/\/$/, '')
  }
  if (typeof window !== 'undefined' && window.__APP_BASE_URL__) {
    return String(window.__APP_BASE_URL__).replace(/\/$/, '')
  }
  return FALLBACK_BASE_URL
}

export const BASE_URL = resolveBaseUrl()

export const IS_DEV = import.meta.env?.DEV ?? true

// 请求超时时间（毫秒）
export const REQUEST_TIMEOUT = 15000

// 上传接口超时时间，图片压缩后仍可能较大，需单独放宽
export const UPLOAD_TIMEOUT = 60000

const config = {
  BASE_URL,
  IS_DEV,
  REQUEST_TIMEOUT,
  UPLOAD_TIMEOUT
}

export default config
