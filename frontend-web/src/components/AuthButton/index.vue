<template>
  <el-button
    v-if="hasPermission"
    v-bind="$attrs"
    :type="type"
    :size="size"
    :icon="icon"
    :loading="loading"
    :disabled="disabled"
    @click="handleClick"
  >
    <slot></slot>
  </el-button>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const props = defineProps({

  permission: { type: [String, Array], default: '' },

  role: { type: [String, Array], default: '' },

  type: { type: String, default: 'default' },

  size: { type: String, default: 'default' },

  icon: { type: [String, Object], default: '' },

  loading: { type: Boolean, default: false },

  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['click'])

const userStore = useUserStore()

const hasPermission = computed(() => {

  if (!props.permission && !props.role) {
    return true
  }

  if (props.permission && !userStore.hasPermission(props.permission)) {
    return false
  }

  if (props.role && !userStore.hasRole(props.role)) {
    return false
  }

  return true
})

const handleClick = (event) => {
  emit('click', event)
}
</script>
