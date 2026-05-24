import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {

  const sidebarCollapsed = ref(localStorage.getItem('sidebarCollapsed') === 'true')

  const loading = ref(false)

  const pageTitle = ref('')

  const breadcrumbs = ref([])

  const theme = ref(localStorage.getItem('theme') || 'light')

  const settings = ref({
    showBreadcrumb: true,
    showTagsView: true,
    fixedHeader: true,
    sidebarLogo: true
  })

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
    localStorage.setItem('sidebarCollapsed', sidebarCollapsed.value)
  }

  function setLoading(status) {
    loading.value = status
  }

  function setPageTitle(title) {
    pageTitle.value = title
    document.title = title ? `${title} - 物业管理系统` : '物业管理系统'
  }

  function setBreadcrumbs(items) {
    breadcrumbs.value = items
  }

  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    localStorage.setItem('theme', theme.value)
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  function updateSettings(newSettings) {
    settings.value = { ...settings.value, ...newSettings }
  }

  return {

    sidebarCollapsed,
    loading,
    pageTitle,
    breadcrumbs,
    theme,
    settings,

    toggleSidebar,
    setLoading,
    setPageTitle,
    setBreadcrumbs,
    toggleTheme,
    updateSettings
  }
})
