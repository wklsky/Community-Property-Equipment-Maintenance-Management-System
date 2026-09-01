/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-mobile/packages/shared/src/index.ts
 * @Description: 共享包统一出口，业主 App 与维修工 App 均从此处引入公共能力
 */

export * from './constants/roles'
export * from './constants/auth'
export * from './types/auth'

export * from './utils/validate'
export * from './utils/role'
export * from './utils/debounce'
export * from './utils/request'

export * from './api/auth'
export * from './stores/user'
export * from './composables/useLogin'
export * from './router/guard'

export { default as LoginForm } from './components/LoginForm.vue'
