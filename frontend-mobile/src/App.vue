<script>
export default {
  onLaunch() {
    console.log('App Launch')
    this.checkLogin()
    this.setupNavigationGuard()
  },
  onShow() {
    console.log('App Show')
    this.checkLogin()
  },
  onHide() {
    console.log('App Hide')
  },
  methods: {
    setupNavigationGuard() {

      const guardMethods = ['navigateTo', 'redirectTo', 'reLaunch', 'switchTab']
      const whiteList = ['pages/login/index', 'pages/register/index']
      guardMethods.forEach(method => {
        uni.addInterceptor(method, {
          invoke(e) {

            if (e && whiteList.some(p => e.url && e.url.includes(p))) {
              return true
            }

            const token = uni.getStorageSync('token')
            if (!token) {
              uni.reLaunch({ url: '/pages/login/index' })
              return false
            }
            return true
          }
        })
      })
    },
    checkLogin() {
      const token = uni.getStorageSync('token')
      const userInfo = uni.getStorageSync('userInfo')
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      const currentPath = currentPage ? currentPage.route : ''

      const whiteList = ['pages/login/index', 'pages/register/index']
      if (whiteList.includes(currentPath)) {
        return
      }

      if (!token) {
        uni.reLaunch({ url: '/pages/login/index' })
        return
      }

      if (userInfo && userInfo.roleName === '系统管理员') {
        uni.showModal({
          title: '提示',
          content: '请使用 PC 端管理后台',
          showCancel: false,
          confirmText: '重新登录',
          success: () => {
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.reLaunch({ url: '/pages/login/index' })
          }
        })
        return
      }

      const workerOnlyPages = ['pages/inspection/list', 'pages/inspection/execute']
      if (workerOnlyPages.some(p => currentPath.includes(p))) {
        if (userInfo && userInfo.roleName !== '维修工') {
          uni.showToast({ title: '无权访问该页面', icon: 'none' })
          uni.switchTab({ url: '/pages/index/index' })
          return
        }
      }

      const ownerOnlyPages = ['pages/repair/create']
      if (ownerOnlyPages.some(p => currentPath.includes(p))) {
        if (userInfo && userInfo.roleName !== '业主') {
          uni.showToast({ title: '无权访问该页面', icon: 'none' })
          uni.switchTab({ url: '/pages/repair/list' })
          return
        }
      }
    }
  },
  globalData: {

    roleHomePaths: {
      '业主': '/pages/index/index',
      '维修工': '/pages/repair/list'
    }
  }
}
</script>

<style>

page {
  --color-primary: #0066FF;
  --color-primary-light: #E6F0FF;
  --color-primary-dark: #0052CC;

  --color-text-primary: #1E293B;
  --color-text-secondary: #64748B;
  --color-text-tertiary: #94A3B8;
  --color-text-inverse: #FFFFFF;

  --color-bg-primary: #FDFDFD;
  --color-bg-secondary: #FFFFFF;
  --color-bg-tertiary: #F1F5F9;
  --color-bg-elevated: #FFFFFF;

  --color-status-safe: #10B981;
  --color-status-safe-bg: rgba(16, 185, 129, 0.1);
  --color-status-maintenance: #F59E0B;
  --color-status-maintenance-bg: rgba(245, 158, 11, 0.1);
  --color-status-urgent: #EF4444;
  --color-status-urgent-bg: rgba(239, 68, 68, 0.1);
  --color-status-info: #3B82F6;
  --color-status-info-bg: rgba(59, 130, 246, 0.1);

  --color-border: #E2E8F0;
  --color-border-light: #F1F5F9;

  --shadow-sm: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  --shadow-md: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 16rpx 48rpx rgba(0, 0, 0, 0.08);
  --shadow-primary: 0 8rpx 24rpx rgba(0, 102, 255, 0.25);

  --radius-sm: 12rpx;
  --radius-md: 20rpx;
  --radius-lg: 28rpx;
  --radius-xl: 36rpx;
  --radius-full: 9999rpx;

  --spacing-xs: 8rpx;
  --spacing-sm: 16rpx;
  --spacing-md: 24rpx;
  --spacing-lg: 32rpx;
  --spacing-xl: 48rpx;

  background-color: var(--color-bg-primary);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
  font-size: 28rpx;
  color: var(--color-text-primary);
  -webkit-font-smoothing: antialiased;
}

.container {
  padding: var(--spacing-lg);
}

.card {
  background: var(--color-bg-elevated);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  box-shadow: var(--shadow-sm);
  border: 2rpx solid var(--color-border-light);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-md);
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: var(--color-text-inverse);
  border: none;
  border-radius: var(--radius-full);
  height: 100rpx;
  line-height: 100rpx;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
  box-shadow: var(--shadow-primary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-primary:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.btn-secondary {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  border: none;
  border-radius: var(--radius-full);
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
  font-weight: 500;
}

.btn-secondary:active {
  background: var(--color-border);
}

.btn-ghost {
  background: transparent;
  color: var(--color-primary);
  border: 2rpx solid var(--color-primary);
  border-radius: var(--radius-full);
  height: 88rpx;
  line-height: 84rpx;
  font-size: 30rpx;
  font-weight: 500;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 20rpx;
  border-radius: var(--radius-full);
  font-size: 24rpx;
  font-weight: 600;
}

.status-tag::before {
  content: '';
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 10rpx;
}

.status-safe {
  background: var(--color-status-safe-bg);
  color: var(--color-status-safe);
}
.status-safe::before {
  background: var(--color-status-safe);
}

.status-maintenance {
  background: var(--color-status-maintenance-bg);
  color: var(--color-status-maintenance);
}
.status-maintenance::before {
  background: var(--color-status-maintenance);
}

.status-urgent {
  background: var(--color-status-urgent-bg);
  color: var(--color-status-urgent);
}
.status-urgent::before {
  background: var(--color-status-urgent);
  animation: pulse 1.5s infinite;
}

.status-info {
  background: var(--color-status-info-bg);
  color: var(--color-status-info);
}
.status-info::before {
  background: var(--color-status-info);
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
}

.section-title::before {
  content: '';
  width: 8rpx;
  height: 36rpx;
  background: var(--color-primary);
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.input-field {
  height: 100rpx;
  background: var(--color-bg-tertiary);
  border: 2rpx solid transparent;
  border-radius: var(--radius-md);
  padding: 0 var(--spacing-lg);
  font-size: 30rpx;
  color: var(--color-text-primary);
  transition: all 0.2s ease;
}

.input-field:focus {
  background: var(--color-bg-secondary);
  border-color: var(--color-primary);
  box-shadow: 0 0 0 6rpx var(--color-primary-light);
}

.input-field::placeholder {
  color: var(--color-text-tertiary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx var(--spacing-lg);
  color: var(--color-text-tertiary);
}

.empty-state-icon {
  font-size: 120rpx;
  margin-bottom: var(--spacing-md);
  opacity: 0.5;
}

.empty-state-text {
  font-size: 28rpx;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  color: var(--color-text-tertiary);
  font-size: 26rpx;
}

.divider {
  height: 2rpx;
  background: var(--color-border-light);
  margin: var(--spacing-md) 0;
}

.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.glass-effect {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(40rpx);
  -webkit-backdrop-filter: blur(40rpx);
}

.gradient-primary {
  background: linear-gradient(135deg, var(--color-primary) 0%, #0052CC 100%);
}

.gradient-safe {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
}

.gradient-warning {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
}

.gradient-danger {
  background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
}

.fade-in {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.slide-up {
  animation: slideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(100rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.text-primary { color: var(--color-text-primary); }
.text-secondary { color: var(--color-text-secondary); }
.text-tertiary { color: var(--color-text-tertiary); }
.text-white { color: var(--color-text-inverse); }
.text-accent { color: var(--color-primary); }

.font-medium { font-weight: 500; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }

.text-center { text-align: center; }
.text-right { text-align: right; }

.flex { display: flex; }
.flex-col { flex-direction: column; }
.items-center { align-items: center; }
.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.flex-1 { flex: 1; }
.gap-sm { gap: var(--spacing-sm); }
.gap-md { gap: var(--spacing-md); }
</style>
