<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-width="labelWidth"
      :label-position="labelPosition"
      :disabled="loading"
    >
      <el-row :gutter="gutter">
        <template v-for="field in fields" :key="field.prop">
          <el-col :span="field.span || defaultSpan" v-if="!field.hidden">
            <el-form-item :label="field.label" :prop="field.prop">
              <!-- 输入框 -->
              <el-input
                v-if="field.type === 'input' || !field.type"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :maxlength="field.maxlength"
                :show-word-limit="field.showWordLimit"
                :disabled="field.disabled"
                clearable
              />

              <!-- 文本域 -->
              <el-input
                v-else-if="field.type === 'textarea'"
                v-model="formData[field.prop]"
                type="textarea"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :rows="field.rows || 3"
                :maxlength="field.maxlength"
                :show-word-limit="field.showWordLimit"
                :disabled="field.disabled"
              />

              <!-- 数字输入 -->
              <el-input-number
                v-else-if="field.type === 'number'"
                v-model="formData[field.prop]"
                :min="field.min"
                :max="field.max"
                :step="field.step || 1"
                :precision="field.precision"
                :disabled="field.disabled"
                style="width: 100%"
              />

              <!-- 选择框 -->
              <el-select
                v-else-if="field.type === 'select'"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :multiple="field.multiple"
                :disabled="field.disabled"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="opt in field.options"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>

              <!-- 单选框 -->
              <el-radio-group
                v-else-if="field.type === 'radio'"
                v-model="formData[field.prop]"
                :disabled="field.disabled"
              >
                <el-radio
                  v-for="opt in field.options"
                  :key="opt.value"
                  :label="opt.value"
                >
                  {{ opt.label }}
                </el-radio>
              </el-radio-group>

              <!-- 复选框 -->
              <el-checkbox-group
                v-else-if="field.type === 'checkbox'"
                v-model="formData[field.prop]"
                :disabled="field.disabled"
              >
                <el-checkbox
                  v-for="opt in field.options"
                  :key="opt.value"
                  :label="opt.value"
                >
                  {{ opt.label }}
                </el-checkbox>
              </el-checkbox-group>

              <!-- 开关 -->
              <el-switch
                v-else-if="field.type === 'switch'"
                v-model="formData[field.prop]"
                :active-value="field.activeValue ?? true"
                :inactive-value="field.inactiveValue ?? false"
                :disabled="field.disabled"
              />

              <!-- 日期选择 -->
              <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="formData[field.prop]"
                type="date"
                :placeholder="field.placeholder || `选择${field.label}`"
                value-format="YYYY-MM-DD"
                :disabled="field.disabled"
                style="width: 100%"
              />

              <!-- 日期时间选择 -->
              <el-date-picker
                v-else-if="field.type === 'datetime'"
                v-model="formData[field.prop]"
                type="datetime"
                :placeholder="field.placeholder || `选择${field.label}`"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled="field.disabled"
                style="width: 100%"
              />

              <!-- 日期范围 -->
              <el-date-picker
                v-else-if="field.type === 'daterange'"
                v-model="formData[field.prop]"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                :disabled="field.disabled"
                style="width: 100%"
              />

              <!-- 时间选择 -->
              <el-time-picker
                v-else-if="field.type === 'time'"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `选择${field.label}`"
                value-format="HH:mm:ss"
                :disabled="field.disabled"
                style="width: 100%"
              />

              <!-- 自定义插槽 -->
              <slot v-else-if="field.type === 'slot'" :name="field.prop" :form="formData"></slot>
            </el-form-item>
          </el-col>
        </template>
      </el-row>
    </el-form>

    <template #footer>
      <slot name="footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          {{ submitText }}
        </el-button>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({

  modelValue: { type: Boolean, default: false },

  title: { type: String, default: '' },

  width: { type: String, default: '560px' },

  fields: { type: Array, required: true },

  data: { type: Object, default: () => ({}) },

  rules: { type: Object, default: () => ({}) },

  labelWidth: { type: String, default: '100px' },

  labelPosition: { type: String, default: 'right' },

  gutter: { type: Number, default: 20 },

  defaultSpan: { type: Number, default: 24 },

  loading: { type: Boolean, default: false },

  submitText: { type: String, default: '确定' }
})

const emit = defineEmits(['update:modelValue', 'submit', 'cancel', 'close'])

const formRef = ref()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formData = ref({})

watch(() => props.data, (newData) => {
  formData.value = { ...newData }
}, { immediate: true, deep: true })

watch(visible, (val) => {
  if (val) {
    formData.value = { ...props.data }
  }
})

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    emit('submit', { ...formData.value })
  } catch (error) {
    console.log('表单校验失败:', error)
  }
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleClose = () => {
  formRef.value?.resetFields()
  emit('close')
}

defineExpose({
  formRef,
  validate: () => formRef.value?.validate(),
  resetFields: () => formRef.value?.resetFields(),
  getFormData: () => formData.value,
  setFormData: (data) => { formData.value = { ...data } }
})
</script>
