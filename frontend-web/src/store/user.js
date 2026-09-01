import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'
import router, { resetRouter } from '@/router'

export const useUserStore = defineStore('user', () => {

  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(safeJsonParse(localStorage.getItem('userInfo'), {}))
  const permissions = ref(safeJsonParse(localStorage.getItem('permissions'), []))
  const roles = ref(safeJsonParse(localStorage.getItem('roles'), []))

  function safeJsonParse(str, fallback) {
    if (str == null || str === '') return fallback
    try { return JSON.parse(str) } catch (e) { return fallback }
  }

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value.userId)
  const username = computed(() => userInfo.value.username)
  const tenantId = computed(() => userInfo.value.tenantId)
  const roleName = computed(() => userInfo.value.roleName)

  const isSuperAdmin = computed(() => userInfo.value.isSuperAdmin === true)

  const isAdmin = computed(() => roleName.value === '系统管理员')
  const isWorker = computed(() => roleName.value === '维修工')
  const isOwner = computed(() => roleName.value === '业主')

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setRefreshToken(newToken) {
    refreshToken.value = newToken
    localStorage.setItem('refreshToken', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))

    const rolePermissions = getRolePermissions(info.roleName)
    setPermissions(rolePermissions)
    setRoles([info.roleName])
  }

  function setPermissions(perms) {
    permissions.value = perms
    localStorage.setItem('permissions', JSON.stringify(perms))
  }

  function setRoles(roleList) {
    roles.value = roleList
    localStorage.setItem('roles', JSON.stringify(roleList))
  }

  function getRolePermissions(role) {
    const permissionMap = {
      '系统管理员': [
        'repair:view', 'repair:create', 'repair:assign', 'repair:accept',
        'repair:complete', 'repair:evaluate', 'repair:cancel',
        'device:view', 'device:create', 'device:update', 'device:delete',
        'inspection:view', 'inspection:plan:create', 'inspection:plan:update',
        'inspection:plan:delete', 'inspection:plan:publish',
        'inspection:task:accept', 'inspection:task:complete',
        'notice:view', 'notice:create', 'notice:update', 'notice:delete', 'notice:publish',
        'message:view',
        'system:user:view', 'system:user:create', 'system:user:update', 'system:user:delete',
        'system:role:view', 'system:role:create', 'system:role:update', 'system:role:delete'
      ],
      '维修工': [
        'repair:view', 'repair:accept', 'repair:complete',
        'inspection:view', 'inspection:task:accept', 'inspection:task:complete',
        'device:view', 'notice:view', 'message:view'
      ],
      '业主': [
        'repair:view', 'repair:create', 'repair:evaluate', 'repair:cancel',
        'device:view', 'notice:view', 'message:view'
      ]
    }

    if (role === '超级管理员') {
      return [
        'repair:view', 'repair:create', 'repair:assign', 'repair:accept',
        'repair:complete', 'repair:evaluate', 'repair:cancel',
        'device:view', 'device:create', 'device:update', 'device:delete',
        'inspection:view', 'inspection:plan:create', 'inspection:plan:update',
        'inspection:plan:delete', 'inspection:plan:publish',
        'inspection:task:accept', 'inspection:task:complete',
        'notice:view', 'notice:create', 'notice:update', 'notice:delete', 'notice:publish',
        'message:view',
        'system:user:view', 'system:user:create', 'system:user:update', 'system:user:delete',
        'system:role:view', 'system:role:create', 'system:role:update', 'system:role:delete',
        'admin:tenant:view', 'admin:tenant:create', 'admin:tenant:update', 'admin:tenant:delete'
      ]
    }

    if (!permissionMap[role]) {
      console.warn(`未知角色: ${role}，使用默认权限`)

      return ['repair:view', 'device:view', 'notice:view', 'message:view']
    }
    return permissionMap[role]
  }

  function hasPermission(permission) {
    if (!permission) return true
    if (Array.isArray(permission)) {
      return permission.some(p => permissions.value.includes(p))
    }
    return permissions.value.includes(permission)
  }

  function hasRole(role) {
    if (!role) return true
    if (Array.isArray(role)) {
      return role.some(r => roles.value.includes(r))
    }
    return roles.value.includes(role)
  }

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    setToken(res.data.token)
    if (res.data.refreshToken) {
      setRefreshToken(res.data.refreshToken)
    }
    setUserInfo(res.data)
    return res.data
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = {}
    permissions.value = []
    roles.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
    localStorage.removeItem('roles')

    // 必须同步移除按权限动态注入的路由：同一浏览器内切换到低权限账号时，
    // 上一账号的路由（如超管的 /admin/tenants）会残留在路由表中，造成越权访问
    resetRouter()
  }

  function resetState() {
    logout()
    router.push('/login')
  }

  return {
    token,
    refreshToken,
    userInfo,
    permissions,
    roles,
    isLoggedIn,
    userId,
    username,
    tenantId,
    roleName,
    isSuperAdmin,
    isAdmin,
    isWorker,
    isOwner,
    setToken,
    setRefreshToken,
    setUserInfo,
    setPermissions,
    setRoles,
    hasPermission,
    hasRole,
    login,
    logout,
    resetState
  }
})
