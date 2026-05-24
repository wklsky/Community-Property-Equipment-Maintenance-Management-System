const PHONE_REG = /^1[3-9]\d{9}$/

const EMAIL_REG = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

const ID_CARD_REG = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/

const URL_REG = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/

const PASSWORD_REG = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/

export const required = (message = '此项为必填项') => ({
  required: true,
  message,
  trigger: ['blur', 'change']
})

export const phone = (message = '请输入正确的手机号') => ({
  pattern: PHONE_REG,
  message,
  trigger: 'blur'
})

export const email = (message = '请输入正确的邮箱地址') => ({
  pattern: EMAIL_REG,
  message,
  trigger: 'blur'
})

export const idCard = (message = '请输入正确的身份证号') => ({
  pattern: ID_CARD_REG,
  message,
  trigger: 'blur'
})

export const url = (message = '请输入正确的URL地址') => ({
  pattern: URL_REG,
  message,
  trigger: 'blur'
})

export const password = (message = '密码至少8位，需包含字母和数字') => ({
  pattern: PASSWORD_REG,
  message,
  trigger: 'blur'
})

export const length = (min, max, message) => ({
  min,
  max,
  message: message || `长度应在 ${min} 到 ${max} 个字符之间`,
  trigger: 'blur'
})

export const minLength = (min, message) => ({
  min,
  message: message || `长度不能少于 ${min} 个字符`,
  trigger: 'blur'
})

export const maxLength = (max, message) => ({
  max,
  message: message || `长度不能超过 ${max} 个字符`,
  trigger: 'blur'
})

export const range = (min, max, message) => ({
  type: 'number',
  min,
  max,
  message: message || `数值应在 ${min} 到 ${max} 之间`,
  trigger: 'blur'
})

export const positiveInteger = (message = '请输入正整数') => ({
  pattern: /^[1-9]\d*$/,
  message,
  trigger: 'blur'
})

export const number = (message = '请输入数字') => ({
  pattern: /^-?\d+(\.\d+)?$/,
  message,
  trigger: 'blur'
})

export const chinese = (message = '请输入中文') => ({
  pattern: /^[\u4e00-\u9fa5]+$/,
  message,
  trigger: 'blur'
})

export const english = (message = '请输入英文') => ({
  pattern: /^[a-zA-Z]+$/,
  message,
  trigger: 'blur'
})

export const alphanumeric = (message = '只能输入字母和数字') => ({
  pattern: /^[a-zA-Z0-9]+$/,
  message,
  trigger: 'blur'
})

export const validator = (validatorFn, message = '校验失败') => ({
  validator: (rule, value, callback) => {
    if (validatorFn(value)) {
      callback()
    } else {
      callback(new Error(message))
    }
  },
  trigger: 'blur'
})

export const asyncValidator = (asyncValidatorFn) => ({
  asyncValidator: async (rule, value) => {
    return asyncValidatorFn(value)
  },
  trigger: 'blur'
})

export const confirmPassword = (getPassword, message = '两次输入的密码不一致') => ({
  validator: (rule, value, callback) => {
    if (value !== getPassword()) {
      callback(new Error(message))
    } else {
      callback()
    }
  },
  trigger: 'blur'
})

export const rules = (...ruleList) => ruleList

export const commonRules = {

  requiredPhone: [required('请输入手机号'), phone()],

  requiredEmail: [required('请输入邮箱'), email()],

  requiredPassword: [required('请输入密码'), password()],

  requiredUsername: [required('请输入用户名'), length(2, 20, '用户名长度应在2-20个字符之间')],

  requiredDesc: [required('请输入描述'), maxLength(500, '描述不能超过500个字符')],

  optionalPhone: [phone()],

  optionalEmail: [email()]
}

export default {
  required,
  phone,
  email,
  idCard,
  url,
  password,
  length,
  minLength,
  maxLength,
  range,
  positiveInteger,
  number,
  chinese,
  english,
  alphanumeric,
  validator,
  asyncValidator,
  confirmPassword,
  rules,
  commonRules
}
