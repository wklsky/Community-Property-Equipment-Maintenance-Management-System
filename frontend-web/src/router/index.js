import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({
  showSpinner: false,
  easing: 'ease',
  speed: 500,
  minimum: 0.3
})

export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', hidden: true, noAuth: true }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', hidden: true, noAuth: true }
  }
]

export const asyncRoutes = [
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '工作台',
          icon: 'HomeFilled',
          affix: true
        }
      },
      {
        path: 'repair-orders',
        name: 'RepairOrders',
        component: () => import('@/views/repair/OrderList.vue'),
        meta: {
          title: '工单管理',
          icon: 'Tickets',
          permission: 'repair:view'
        }
      },
      {
        path: 'repair-orders/:id',
        name: 'RepairOrderDetail',
        component: () => import('@/views/repair/OrderDetail.vue'),
        meta: {
          title: '工单详情',
          hidden: true,
          activeMenu: '/repair-orders'
        }
      },
      {
        path: 'my-orders',
        redirect: '/repair-orders',
        meta: { hidden: true }
      },
      {
        path: 'devices',
        name: 'Devices',
        component: () => import('@/views/device/DeviceList.vue'),
        meta: {
          title: '设备管理',
          icon: 'Monitor',
          permission: 'device:view',
          roles: ['系统管理员']
        }
      },
      {
        path: 'inspections',
        name: 'Inspections',
        component: () => import('@/views/inspection/InspectionList.vue'),
        meta: {
          title: '巡检管理',
          icon: 'Checked',
          permission: 'inspection:view',
          roles: ['系统管理员', '维修工']
        }
      },
      {
        path: 'notices',
        name: 'Notices',
        component: () => import('@/views/notice/NoticeList.vue'),
        meta: {
          title: '公告管理',
          icon: 'Bell',
          permission: 'notice:view'
        }
      },
      {
        path: 'system',
        name: 'SystemUsers',
        component: () => import('@/views/system/SystemUsers.vue'),
        meta: {
          title: '用户管理',
          icon: 'Setting',
          permission: 'system:user:view',
          roles: ['系统管理员']
        }
      },
      {
        path: 'admin/tenants',
        name: 'TenantManagement',
        component: () => import('@/views/system/TenantList.vue'),
        meta: {
          title: '公司管理',
          icon: 'OfficeBuilding',
          permission: 'admin:tenant:view',
          roles: ['超级管理员']
        }
      }
    ]
  },

  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: (to, from, savedPosition) => {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0, behavior: 'smooth' }
  }
})

const whiteList = ['/login', '/404']

let hasAddedRoutes = false

function filterAsyncRoutes(routes, userStore) {
  const filteredRoutes = []

  routes.forEach(route => {
    const tmp = { ...route }

    if (hasPermission(tmp, userStore)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, userStore)
      }
      filteredRoutes.push(tmp)
    }
  })

  return filteredRoutes
}

function hasPermission(route, userStore) {
  const { meta } = route

  if (!meta) return true

  if (meta.roles && meta.roles.length > 0) {
    if (!userStore.hasRole(meta.roles)) {
      return false
    }
  }

  if (meta.permission) {
    if (!userStore.hasPermission(meta.permission)) {
      return false
    }
  }

  return true
}

function addDynamicRoutes(userStore) {
  const accessRoutes = filterAsyncRoutes(asyncRoutes, userStore)

  accessRoutes.forEach(route => {
    router.addRoute(route)
  })

  hasAddedRoutes = true
  return accessRoutes
}

export function resetRouter() {

  const routeNames = router.getRoutes().map(route => route.name)

  routeNames.forEach(name => {
    if (name && !constantRoutes.some(r => r.name === name)) {
      router.removeRoute(name)
    }
  })

  hasAddedRoutes = false
}

function getPageTitle(to) {
  const baseTitle = '社区物业设备维护管理系统'
  if (to.meta?.title) {
    return `${to.meta.title} - ${baseTitle}`
  }
  return baseTitle
}

router.beforeEach(async (to, from, next) => {

  NProgress.start()

  document.title = getPageTitle(to)

  const userStore = useUserStore()
  const appStore = useAppStore()

  if (to.meta?.title) {
    appStore.setPageTitle(to.meta.title)
  }

  if (whiteList.includes(to.path) || to.meta?.noAuth) {
    next()
    return
  }

  if (!userStore.token) {

    ElMessage.warning('请先登录')
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  if (to.path === '/login') {
    next({ path: '/' })
    return
  }

  if (!userStore.userInfo || !userStore.userInfo.userId) {

    try {

      const storedUserInfo = localStorage.getItem('userInfo')
      if (storedUserInfo) {
        userStore.setUserInfo(JSON.parse(storedUserInfo))
      } else {

        throw new Error('用户信息丢失')
      }
    } catch (error) {
      console.error('恢复用户信息失败:', error)
      userStore.resetState()
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }

  if (!hasAddedRoutes) {
    try {
      const accessRoutes = addDynamicRoutes(userStore)

      next({ ...to, replace: true })
      return
    } catch (error) {
      console.error('添加动态路由失败:', error)
      userStore.resetState()
      next('/login')
      return
    }
  }

  if (userStore.isSuperAdmin && to.path !== '/admin/tenants') {
    next('/admin/tenants')
    return
  }

  if (to.meta?.permission && !userStore.hasPermission(to.meta.permission)) {
    ElMessage.error('您没有权限访问该页面')
    next('/dashboard')
    return
  }

  if (to.meta?.roles && to.meta.roles.length > 0 && !userStore.hasRole(to.meta.roles)) {
    ElMessage.error('您的角色无权访问该页面')
    next('/dashboard')
    return
  }

  next()
})

router.afterEach((to, from) => {

  NProgress.done()

})

router.onError((error) => {
  console.error('路由错误:', error)
  NProgress.done()

  if (error.message.includes('Failed to fetch dynamically imported module')) {
    ElMessage.error('页面加载失败，请刷新重试')
  }
})

export default router
