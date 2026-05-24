import { useUserStore } from '@/store/user'

export const permission = {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()

    if (value) {
      const hasPermission = userStore.hasPermission(value)

      if (!hasPermission) {

        el.parentNode?.removeChild(el)
      }
    }
  }
}

export const role = {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()

    if (value) {
      const hasRole = userStore.hasRole(value)

      if (!hasRole) {

        el.parentNode?.removeChild(el)
      }
    }
  }
}

export const permissionDisabled = {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()

    if (value) {
      const hasPermission = userStore.hasPermission(value)

      if (!hasPermission) {
        el.disabled = true
        el.classList.add('is-disabled')
        el.style.pointerEvents = 'none'
        el.style.opacity = '0.5'
      }
    }
  }
}

export function setupDirectives(app) {
  app.directive('permission', permission)
  app.directive('role', role)
  app.directive('permission-disabled', permissionDisabled)
}

export default {
  permission,
  role,
  permissionDisabled,
  setupDirectives
}
