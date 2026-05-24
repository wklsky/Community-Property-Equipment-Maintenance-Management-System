<template>
  <el-container class="layout-container">
    <!-- Mobile sidebar overlay -->
    <div class="sidebar-overlay" :class="{ visible: !sidebarCollapsed && isMobile }" @click="sidebarCollapsed = true"></div>

    <!-- Semi-transparent Dark Navigation Pane -->
    <el-aside :width="isMobile ? '260px' : (sidebarCollapsed ? '72px' : '260px')" class="aside" :class="{ collapsed: sidebarCollapsed, 'mobile-open': !sidebarCollapsed && isMobile }">
      <!-- Logo Section -->
      <div class="logo-section">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="2" y="8" width="12" height="20" rx="2" stroke="currentColor" stroke-width="2"/>
              <rect x="18" y="4" width="12" height="24" rx="2" stroke="currentColor" stroke-width="2"/>
              <rect x="5" y="12" width="3" height="3" rx="0.5" fill="currentColor"/>
              <rect x="5" y="18" width="3" height="3" rx="0.5" fill="currentColor"/>
              <rect x="21" y="8" width="3" height="3" rx="0.5" fill="currentColor"/>
              <rect x="21" y="14" width="3" height="3" rx="0.5" fill="currentColor"/>
              <rect x="21" y="20" width="3" height="3" rx="0.5" fill="currentColor"/>
              <rect x="26" y="8" width="2" height="2" rx="0.5" fill="currentColor" opacity="0.5"/>
              <rect x="26" y="14" width="2" height="2" rx="0.5" fill="currentColor" opacity="0.5"/>
            </svg>
          </div>
          <div class="logo-text">
            <span class="logo-title">智慧社区</span>
            <span class="logo-subtitle">物业设备管理平台</span>
          </div>
        </div>
      </div>

      <!-- Navigation Menu -->
      <nav class="nav-menu">
        <div class="nav-section" v-if="!isSuperAdmin">
          <span class="nav-section-title">主导航</span>

          <router-link to="/dashboard" class="nav-item" :class="{ active: route.path === '/dashboard' }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="2" y="2" width="7" height="7" rx="1.5"/>
                <rect x="11" y="2" width="7" height="7" rx="1.5"/>
                <rect x="2" y="11" width="7" height="7" rx="1.5"/>
                <rect x="11" y="11" width="7" height="7" rx="1.5"/>
              </svg>
            </span>
            <span class="nav-text">工作台</span>
          </router-link>

          <router-link to="/repair-orders" class="nav-item" :class="{ active: route.path.startsWith('/repair-orders') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M3 5h14M3 10h14M3 15h10"/>
                <circle cx="16" cy="15" r="2"/>
              </svg>
            </span>
            <span class="nav-text">工单管理</span>
            <span class="nav-badge" v-if="pendingOrders > 0">{{ pendingOrders }}</span>
          </router-link>
        </div>

        <div class="nav-section" v-if="isAdmin || isWorker">
          <span class="nav-section-title">设备运维</span>

          <router-link v-if="isAdmin" to="/devices" class="nav-item" :class="{ active: route.path === '/devices' }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="2" y="3" width="16" height="12" rx="2"/>
                <path d="M6 18h8M10 15v3"/>
              </svg>
            </span>
            <span class="nav-text">设备管理</span>
          </router-link>

          <router-link to="/inspections" class="nav-item" :class="{ active: route.path === '/inspections' }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M4 10l4 4 8-8"/>
                <circle cx="10" cy="10" r="8"/>
              </svg>
            </span>
            <span class="nav-text">巡检管理</span>
          </router-link>
        </div>

        <div class="nav-section" v-if="!isSuperAdmin">
          <span class="nav-section-title">消息通知</span>

          <router-link to="/notices" class="nav-item" :class="{ active: route.path === '/notices' }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M4 5h12v10H4z" rx="1"/>
                <path d="M4 5l6 5 6-5"/>
              </svg>
            </span>
            <span class="nav-text">公告管理</span>
          </router-link>
        </div>

        <div class="nav-section" v-if="isAdmin || isSuperAdmin">
          <span class="nav-section-title">系统管理</span>

          <router-link v-if="isAdmin" to="/system" class="nav-item" :class="{ active: route.path === '/system' }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="10" cy="8" r="4"/>
                <path d="M3 18a7 7 0 0 1 14 0"/>
              </svg>
            </span>
            <span class="nav-text">用户管理</span>
          </router-link>

          <router-link v-if="isSuperAdmin" to="/admin/tenants" class="nav-item" :class="{ active: route.path === '/admin/tenants' }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="2" y="3" width="16" height="12" rx="2"/>
                <path d="M6 18h8M10 15v3"/>
              </svg>
            </span>
            <span class="nav-text">公司管理</span>
          </router-link>
        </div>
      </nav>

      <!-- System Status Indicator -->
      <div class="system-status">
        <div class="status-dot"></div>
        <span class="status-text">系统运行正常</span>
      </div>
    </el-aside>

    <!-- Main Content Area -->
    <el-container class="main-container">
      <!-- Header -->
      <el-header class="header">
        <div class="header-left">
          <!-- Hamburger toggle -->
          <button class="hamburger-btn" @click="sidebarCollapsed = !sidebarCollapsed" :title="sidebarCollapsed ? '展开菜单' : '收起菜单'">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2">
              <template v-if="sidebarCollapsed">
                <line x1="3" y1="5" x2="17" y2="5"/>
                <line x1="3" y1="10" x2="17" y2="10"/>
                <line x1="3" y1="15" x2="17" y2="15"/>
              </template>
              <template v-else>
                <line x1="5" y1="5" x2="15" y2="5"/>
                <line x1="5" y1="10" x2="15" y2="10"/>
                <line x1="5" y1="15" x2="15" y2="15"/>
              </template>
            </svg>
          </button>
          <div class="breadcrumb">
            <span class="greeting">{{ greeting }}，</span>
            <span class="username">{{ userStore.userInfo.username }}</span>
          </div>
          <span class="current-time">{{ currentTime }}</span>
        </div>

        <div class="header-right">
          <!-- User Dropdown -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-profile">
              <div class="avatar">
                {{ userStore.userInfo.username?.charAt(0).toUpperCase() }}
              </div>
              <div class="user-info">
                <span class="user-name">{{ userStore.userInfo.username }}</span>
                <span class="user-role">{{ userStore.userInfo.roleName }}</span>
              </div>
              <svg class="dropdown-arrow" width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M3 5l3 3 3-3"/>
              </svg>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item command="profile">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5">
                    <circle cx="8" cy="5" r="3"/>
                    <path d="M2 14a6 6 0 0112 0"/>
                  </svg>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M6 2H3a1 1 0 00-1 1v10a1 1 0 001 1h3M11 11l3-3-3-3M6 8h8"/>
                  </svg>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 个人信息弹窗 -->
        <el-dialog v-model="profileVisible" title="个人信息" width="440px">
          <el-descriptions :column="1" border v-if="userStore.userInfo">
            <el-descriptions-item label="用户名">{{ userStore.userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userStore.userInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ userStore.userInfo.roleName }}</el-descriptions-item>
            <el-descriptions-item label="租户ID">{{ userStore.userInfo.tenantId }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ userStore.userInfo.userId }}</el-descriptions-item>
          </el-descriptions>
        </el-dialog>
      </el-header>

      <!-- Main Content -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getDashboardStats } from '@/api/dashboard'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const pendingOrders = ref(0)
const currentTime = ref('')
const sidebarCollapsed = ref(false)
const isMobile = ref(false)
const profileVisible = ref(false)

const isSuperAdmin = computed(() => userStore.isSuperAdmin)
const isAdmin = computed(() => userStore.userInfo.roleName === '系统管理员')
const isWorker = computed(() => userStore.userInfo.roleName === '维修工')

const checkScreenSize = () => {
  const wasMobile = isMobile.value
  isMobile.value = window.innerWidth < 768

  if (isMobile.value && !wasMobile) {
    sidebarCollapsed.value = true
  } else if (!isMobile.value && wasMobile) {
    sidebarCollapsed.value = false
  }
}

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

const updateTime = () => {
  const now = new Date()
  const options = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }
  currentTime.value = now.toLocaleDateString('zh-CN', options)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    profileVisible.value = true
  }
}

let timeInterval

onMounted(async () => {
  updateTime()
  timeInterval = setInterval(updateTime, 60000)
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)

  if (window.innerWidth < 768) {
    sidebarCollapsed.value = true
  }

  try {
    const dashboardRes = await getDashboardStats()
    const d = dashboardRes.data
    if (d?.orderStats) {
      pendingOrders.value = (d.orderStats.pendingAccept || 0) +
                           (d.orderStats.pendingAssign || 0) +
                           (d.orderStats.pending || 0)
    }
  } catch (e) {}
})

onUnmounted(() => {
  if (timeInterval) clearInterval(timeInterval)
  window.removeEventListener('resize', checkScreenSize)
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  background: var(--color-bg-primary);
}

.aside {
  background: var(--color-bg-dark);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(180deg, rgba(0, 102, 255, 0.05) 0%, transparent 50%);
    pointer-events: none;
  }
}

.logo-section {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--color-primary-blue) 0%, #0052CC 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.3);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}

.logo-subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
}

.nav-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-section {
  margin-bottom: 24px;
}

.nav-section-title {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.35);
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 0 12px;
  margin-bottom: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  margin: 4px 0;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  transition: all 0.2s ease;
  position: relative;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    color: #fff;
  }

  &.active {
    background: var(--color-primary-blue);
    color: #fff;
    box-shadow: 0 4px 12px rgba(0, 102, 255, 0.4);

    .nav-icon {
      color: #fff;
    }
  }
}

.nav-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  flex: 1;
}

.nav-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;

  &.danger {
    background: var(--color-status-danger);
    animation: pulse-badge 2s infinite;
  }
}

@keyframes pulse-badge {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.system-status {
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-status-normal);
  box-shadow: 0 0 8px var(--color-status-normal-glow);
  animation: pulse 2s infinite;
}

.status-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 70px;
  background: var(--color-bg-secondary);
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  box-shadow: var(--shadow-sm);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.breadcrumb {
  display: flex;
  align-items: center;
}

.greeting {
  font-size: 15px;
  color: var(--color-text-secondary);
}

.username {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.current-time {
  font-size: 13px;
  color: var(--color-text-tertiary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px 6px 6px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;

  &:hover {
    background: var(--color-bg-tertiary);
    border-color: var(--color-border-light);
  }
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--color-primary-blue) 0%, #0052CC 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 102, 255, 0.25);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.user-role {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.dropdown-arrow {
  color: var(--color-text-tertiary);
  margin-left: 4px;
}

.user-dropdown {
  padding: 8px;

  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    border-radius: 8px;
    font-size: 14px;
    color: var(--color-text-primary);

    svg {
      color: var(--color-text-secondary);
    }

    &:hover {
      background: var(--color-bg-tertiary);
    }
  }
}

.main {
  flex: 1;
  background: var(--color-bg-primary);
  padding: 0;
  overflow-y: auto;
}

.hamburger-btn {
  display: none;
  width: 40px;
  height: 40px;
  border: none;
  background: var(--color-bg-tertiary);
  border-radius: 10px;
  cursor: pointer;
  color: var(--color-text-secondary);
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    background: var(--color-primary-blue-light);
    color: var(--color-primary-blue);
  }
}

.sidebar-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
  opacity: 0;
  transition: opacity 0.3s ease;

  &.visible {
    opacity: 1;
  }
}

.aside {
  transition: width 0.3s ease;

  &.collapsed {
    .logo-text,
    .nav-text,
    .nav-badge,
    .nav-section-title,
    .system-status .status-text {
      display: none;
    }

    .nav-item {
      justify-content: center;
      padding: 12px;
    }

    .logo-wrapper {
      justify-content: center;
    }

    .system-status {
      justify-content: center;
    }
  }
}

@media (max-width: 1024px) {
  .hamburger-btn {
    display: flex;
  }

  .aside {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 100;

    &:not(.mobile-open) {
      transform: translateX(-100%);
    }

    &.mobile-open {
      transform: translateX(0);
      box-shadow: 4px 0 24px rgba(0, 0, 0, 0.3);
    }
  }

  .header-right {
    gap: 8px;

    .user-profile {
      padding: 4px 8px 4px 4px;

      .user-info {
        display: none;
      }

      .dropdown-arrow {
        display: none;
      }
    }
  }
}

@media (max-width: 768px) {
  .sidebar-overlay {
    display: block;
  }

  .hamburger-btn {
    display: flex;
  }

  .aside {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 100;

    &:not(.mobile-open) {
      transform: translateX(-100%);
    }

    &.mobile-open {
      transform: translateX(0);
      box-shadow: 4px 0 24px rgba(0, 0, 0, 0.3);
    }
  }

  .header {
    height: 56px;
    padding: 0 12px;
  }

  .header-left {
    .greeting {
      font-size: 13px;
    }

    .username {
      font-size: 13px;
    }
  }

  .current-time {
    display: none;
  }

  .header-right {
    gap: 4px;

    .user-profile {
      padding: 4px;

      .user-info {
        display: none;
      }

      .dropdown-arrow {
        display: none;
      }
    }
  }

  .avatar {
    width: 32px;
    height: 32px;
    font-size: 13px;
    border-radius: 8px;
  }

}
</style>
