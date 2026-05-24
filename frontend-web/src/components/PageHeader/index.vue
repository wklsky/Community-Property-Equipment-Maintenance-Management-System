<template>
  <div class="page-header-wrapper">
    <div class="page-header" :class="{ 'with-tabs': showTabs }">
      <div class="header-content">
        <div class="header-left">
          <el-button
            v-if="showBack"
            :icon="ArrowLeft"
            circle
            @click="handleBack"
            class="back-btn"
          />
          <div class="title-section">
            <h1 class="page-title">
              <slot name="title">{{ title }}</slot>
            </h1>
            <p class="page-desc" v-if="description || $slots.description">
              <slot name="description">{{ description }}</slot>
            </p>
          </div>
        </div>
        <div class="header-right">
          <slot name="extra"></slot>
        </div>
      </div>

      <!-- 标签页 -->
      <div class="header-tabs" v-if="showTabs && tabs.length">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane
            v-for="tab in tabs"
            :key="tab.name"
            :label="tab.label"
            :name="tab.name"
          />
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const props = defineProps({

  title: { type: String, default: '' },

  description: { type: String, default: '' },

  showBack: { type: Boolean, default: false },

  backPath: { type: String, default: '' },

  showTabs: { type: Boolean, default: false },

  tabs: { type: Array, default: () => [] },

  activeTabName: { type: String, default: '' }
})

const emit = defineEmits(['back', 'tab-change'])

const router = useRouter()

const activeTab = ref(props.activeTabName || props.tabs[0]?.name || '')

watch(() => props.activeTabName, (val) => {
  if (val) activeTab.value = val
})

const handleBack = () => {
  if (props.backPath) {
    router.push(props.backPath)
  } else {
    router.back()
  }
  emit('back')
}

const handleTabChange = (name) => {
  emit('tab-change', name)
}

defineExpose({
  activeTab
})
</script>

<style scoped lang="scss">
.page-header-wrapper {
  margin-bottom: 24px;
}

.page-header {
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  &.with-tabs {
    padding-bottom: 0;
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}

.title-section {
  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #fff;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 10px;

    &::before {
      content: '';
      width: 4px;
      height: 24px;
      background: #fff;
      border-radius: 2px;
    }
  }

  .page-desc {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin: 8px 0 0 14px;
  }
}

.header-right {
  display: flex;
  gap: 12px;
}

.header-tabs {
  margin-top: 16px;

  :deep(.el-tabs) {
    .el-tabs__header {
      margin: 0;
    }

    .el-tabs__nav-wrap::after {
      display: none;
    }

    .el-tabs__item {
      color: rgba(255, 255, 255, 0.7);
      font-weight: 500;

      &:hover {
        color: #fff;
      }

      &.is-active {
        color: #fff;
      }
    }

    .el-tabs__active-bar {
      background: #fff;
    }
  }
}
</style>
