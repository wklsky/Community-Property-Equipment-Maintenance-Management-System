<template>
  <el-dialog
    :model-value="modelValue"
    title="安全验证"
    width="380px"
    align-center
    append-to-body
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @update:model-value="handleVisibleChange"
  >
    <div class="captcha-body">
      <el-input
        v-model="inputCode"
        :maxlength="length"
        placeholder="请输入图形验证码"
        clearable
        size="large"
        @keyup.enter="handleConfirm"
      >
        <template #prefix>
          <el-icon><Key /></el-icon>
        </template>
      </el-input>

      <div class="captcha-image" title="点击刷新验证码" @click="emit('refresh')">
        <img v-if="image" :src="image" alt="图形验证码" />
        <span v-else class="captcha-fallback">点击刷新</span>
      </div>
    </div>

    <p class="captcha-tip">看不清？点击右侧图片换一张</p>

    <template #footer>
      <el-button @click="handleVisibleChange(false)">取 消</el-button>
      <el-button type="primary" @click="handleConfirm">确认登录</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/views/login/CaptchaDialog.vue
 * @Description: 图形验证码弹窗，只负责渲染与事件转发，校验逻辑由 useCaptcha 承担
 */

import { ref, watch } from 'vue'
import { Key } from '@element-plus/icons-vue'

interface CaptchaDialogProps {
  /** 弹窗显隐，配合 v-model 使用 */
  modelValue: boolean
  /** 验证码图片的 dataURL；空串代表渲染失败，此时展示可点击的占位区域 */
  image: string
  /** 验证码字符长度，用于限制输入框长度 */
  length?: number
}

const props = withDefaults(defineProps<CaptchaDialogProps>(), {
  modelValue: false,
  image: '',
  length: 4
})

const emit = defineEmits<{
  /** 显隐变更，配合 v-model 使用 */
  'update:model-value': [value: boolean]
  /** 用户点击验证码图片，请求上层刷新一张 */
  refresh: []
  /** 提交校验，回传用户输入的验证码原文 */
  confirm: [code: string]
}>()

const inputCode = ref('')

// 每次打开都是一次全新的验证，必须丢弃上一轮的输入
watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      inputCode.value = ''
    }
  }
)

const handleVisibleChange = (visible: boolean): void => {
  emit('update:model-value', visible)
}

const handleConfirm = (): void => {
  const code = inputCode.value.trim()
  if (!code) return

  emit('confirm', code)
  // 无论校验成功与否都清空：失败时弹窗保持打开，用户重新输入即可，
  // 保留错误值会让用户在原输入上反复微调，反而降低验证码的防刷效果
  inputCode.value = ''
}
</script>

<style scoped lang="scss">
.captcha-body {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-image {
  flex-shrink: 0;
  width: 120px;
  height: 44px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f6fc;

  img {
    width: 100%;
    height: 100%;
    display: block;
  }
}

.captcha-fallback {
  font-size: 12px;
  color: #909399;
}

.captcha-tip {
  margin: 12px 0 0;
  font-size: 12px;
  color: #909399;
}
</style>
