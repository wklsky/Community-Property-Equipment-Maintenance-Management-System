const PHONE_REG = /^1[3-9]\d{9}$/

const PASSWORD_REG = /^.{6,20}$/

const CODE_REG = /^\d{6}$/

export const isValidPhone = (phone) => {
  return PHONE_REG.test(phone)
}

export const isValidPassword = (password) => {
  return PASSWORD_REG.test(password)
}

export const isValidCode = (code) => {
  return CODE_REG.test(code)
}

export const isNotEmpty = (value) => {
  return value !== null && value !== undefined && value.toString().trim() !== ''
}

export const isLengthInRange = (value, min, max) => {
  if (!value) return min === 0
  const len = value.toString().length
  return len >= min && len <= max
}

export const validator = {

  validateLogin(form) {
    if (!form.tenantId) {
      return { valid: false, message: '请选择租户' }
    }
    if (!isNotEmpty(form.phone)) {
      return { valid: false, message: '请输入手机号' }
    }
    if (!isValidPhone(form.phone)) {
      return { valid: false, message: '手机号格式不正确' }
    }
    if (!isNotEmpty(form.password)) {
      return { valid: false, message: '请输入密码' }
    }
    if (!isValidPassword(form.password)) {
      return { valid: false, message: '密码长度需6-20位' }
    }
    return { valid: true, message: '' }
  },

  validateCodeLogin(form) {
    if (!form.tenantId) {
      return { valid: false, message: '请选择租户' }
    }
    if (!isNotEmpty(form.phone)) {
      return { valid: false, message: '请输入手机号' }
    }
    if (!isValidPhone(form.phone)) {
      return { valid: false, message: '手机号格式不正确' }
    }
    if (!isNotEmpty(form.code)) {
      return { valid: false, message: '请输入验证码' }
    }
    if (!isValidCode(form.code)) {
      return { valid: false, message: '验证码为6位数字' }
    }
    return { valid: true, message: '' }
  },

  validateSendCode(form) {
    if (!form.tenantId) {
      return { valid: false, message: '请选择租户' }
    }
    if (!isNotEmpty(form.phone)) {
      return { valid: false, message: '请输入手机号' }
    }
    if (!isValidPhone(form.phone)) {
      return { valid: false, message: '手机号格式不正确' }
    }
    return { valid: true, message: '' }
  },

  validateResetPassword(form) {
    if (!form.tenantId) {
      return { valid: false, message: '请选择租户' }
    }
    if (!isNotEmpty(form.phone)) {
      return { valid: false, message: '请输入手机号' }
    }
    if (!isValidPhone(form.phone)) {
      return { valid: false, message: '手机号格式不正确' }
    }
    if (!isNotEmpty(form.code)) {
      return { valid: false, message: '请输入验证码' }
    }
    if (!isValidCode(form.code)) {
      return { valid: false, message: '验证码为6位数字' }
    }
    if (!isNotEmpty(form.newPassword)) {
      return { valid: false, message: '请输入新密码' }
    }
    if (!isValidPassword(form.newPassword)) {
      return { valid: false, message: '密码长度需6-20位' }
    }
    return { valid: true, message: '' }
  },

  validateRepairOrder(form) {
    if (!isNotEmpty(form.address)) {
      return { valid: false, message: '请输入地址' }
    }
    if (!isLengthInRange(form.address, 1, 200)) {
      return { valid: false, message: '地址长度不能超过200个字符' }
    }
    if (!isNotEmpty(form.faultDesc)) {
      return { valid: false, message: '请描述故障' }
    }
    if (!isLengthInRange(form.faultDesc, 1, 500)) {
      return { valid: false, message: '故障描述不能超过500个字符' }
    }
    return { valid: true, message: '' }
  }
}

export default {
  isValidPhone,
  isValidPassword,
  isValidCode,
  isNotEmpty,
  isLengthInRange,
  validator
}
