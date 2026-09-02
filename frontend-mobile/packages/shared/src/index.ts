/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/index.ts
 * @Description: 共享包统一出口，业主 App 与维修工 App 均从此处引入公共能力
 */

// 常量
export * from './constants/roles'
export * from './constants/auth'
export * from './constants/business'

// 类型
export * from './types/auth'
export * from './types/common'
export * from './types/repair'
export * from './types/device'
export * from './types/inspection'
export * from './types/notice'
export * from './types/message'
export * from './types/address'
export * from './types/dashboard'
export * from './types/property'

// 工具
export * from './utils/validate'
export * from './utils/role'
export * from './utils/debounce'
export * from './utils/request'
export * from './utils/query'
export * from './utils/format'

// 接口
export * from './api/auth'
export * from './api/repair'
export * from './api/device'
export * from './api/inspection'
export * from './api/notice'
export * from './api/message'
export * from './api/address'
export * from './api/common'
export * from './api/dashboard'

// 状态与路由
export * from './stores/user'
export * from './composables/useLogin'
export * from './composables/usePagedList'
export * from './router/guard'

export { default as LoginForm } from './components/LoginForm.vue'
