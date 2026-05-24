import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(uni.getStorageSync('userInfo') || {})

  const isOwner = computed(() => userInfo.value?.roleName === '业主')
  const isWorker = computed(() => userInfo.value?.roleName === '维修工')
  const isAdmin = computed(() => userInfo.value?.roleName === '系统管理员')

  const roleName = computed(() => userInfo.value?.roleName || '')

  const tenantName = computed(() => userInfo.value?.tenantName || '')

  const userId = computed(() => userInfo.value?.userId || null)

  const username = computed(() => userInfo.value?.username || '')

  function setToken(newToken) {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    uni.setStorageSync('userInfo', info)
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    uni.removeStorageSync('token')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('userInfo')
  }

  function canLoginMobile() {
    const role = userInfo.value?.roleName
    return role === '业主' || role === '维修工'
  }

  function getHomePath() {
    if (isWorker.value) {
      return '/pages/repair/list'
    }
    return '/pages/index/index'
  }

  return {

    token,
    userInfo,

    isOwner,
    isWorker,
    isAdmin,
    roleName,
    tenantName,
    userId,
    username,

    setToken,
    setUserInfo,
    logout,
    canLoginMobile,
    getHomePath
  }
})
