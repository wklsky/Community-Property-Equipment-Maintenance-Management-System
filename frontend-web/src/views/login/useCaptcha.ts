/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/views/login/useCaptcha.ts
 * @Description: 图形验证码的生成与校验逻辑，对外只暴露图片 dataURL 与校验结果
 */

import { ref, type Ref } from 'vue'
import {
  CAPTCHA_HEIGHT,
  CAPTCHA_LENGTH,
  CAPTCHA_MAX_FAIL_COUNT,
  CAPTCHA_WIDTH
} from '@/constants/auth'

/**
 * 剔除 I / O / Z / 0 / 1 / 2 这类易混淆字符。
 * 验证码字符做过随机旋转与拉伸，用户在小尺寸图片上很难区分形近字符，
 * 剔除后能显著降低"明明输对了却提示错误"的投诉
 */
const CHARSET = 'ABCDEFGHJKLMNPQRSTUVWXY345678'

export interface UseCaptchaReturn {
  /** 验证码图片的 dataURL，纯数据形式便于任意 UI 组件渲染（H5 / 弹窗 / 小程序均可复用） */
  image: Ref<string>
  /**
   * 验证码是否可用。
   * 取不到 canvas 2d 上下文时图片为空，用户看不到验证码内容，
   * 此时调用方必须跳过验证环节，否则用户会被一个看不见的验证码永久挡在登录页外
   */
  available: Ref<boolean>
  /** 连续校验失败次数，供 UI 决定是否高亮提示 */
  failCount: Ref<number>
  refresh: () => void
  verify: (input: string) => boolean
  reset: () => void
}

const randomInt = (min: number, max: number): number =>
  Math.floor(Math.random() * (max - min + 1)) + min

/**
 * 生成一张验证码图片。
 * TODO: 目前为纯前端实现，校验发生在浏览器侧，仅能拦截误操作与低强度脚本，
 * 无法防御绕过前端直接调用 /auth/login 的场景。
 * 后端提供 /auth/captcha（下发 captchaId + 图片）后应改为服务端校验并删除本实现
 */
const createCaptcha = (): { code: string; image: string } => {
  let code = ''
  for (let i = 0; i < CAPTCHA_LENGTH; i += 1) {
    code += CHARSET[randomInt(0, CHARSET.length - 1)]
  }

  const canvas = document.createElement('canvas')
  canvas.width = CAPTCHA_WIDTH
  canvas.height = CAPTCHA_HEIGHT

  const ctx = canvas.getContext('2d')
  if (!ctx) {
    // 无痕模式或内存紧张时可能拿不到 2d 上下文，此时退化为无图验证码，
    // 绝不能让验证码自身成为登录流程的阻塞点
    return { code, image: '' }
  }

  ctx.fillStyle = '#f2f6fc'
  ctx.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT)

  // 干扰线控制在 4 条以内：再多会明显影响人眼识别，反而抬高正常用户的失败率
  for (let i = 0; i < 4; i += 1) {
    ctx.strokeStyle = `rgb(${randomInt(140, 210)},${randomInt(140, 210)},${randomInt(140, 210)})`
    ctx.beginPath()
    ctx.moveTo(randomInt(0, CAPTCHA_WIDTH), randomInt(0, CAPTCHA_HEIGHT))
    ctx.lineTo(randomInt(0, CAPTCHA_WIDTH), randomInt(0, CAPTCHA_HEIGHT))
    ctx.stroke()
  }

  const step = (CAPTCHA_WIDTH - 20) / CAPTCHA_LENGTH
  Array.from(code).forEach((char, index) => {
    ctx.save()
    ctx.font = `bold ${randomInt(22, 28)}px sans-serif`
    ctx.fillStyle = `rgb(${randomInt(0, 110)},${randomInt(0, 110)},${randomInt(0, 110)})`
    ctx.translate(12 + index * step, CAPTCHA_HEIGHT / 2 + randomInt(-3, 7))
    ctx.rotate((randomInt(-25, 25) * Math.PI) / 180)
    ctx.fillText(char, 0, 0)
    ctx.restore()
  })

  return { code, image: canvas.toDataURL('image/png') }
}

export function useCaptcha(): UseCaptchaReturn {
  const image = ref('')
  const available = ref(true)
  const failCount = ref(0)
  let currentCode = ''

  const refresh = (): void => {
    const captcha = createCaptcha()
    currentCode = captcha.code
    image.value = captcha.image
    available.value = captcha.image !== ''
  }

  const verify = (input: string): boolean => {
    // 忽略大小写：字符被旋转后，用户大小写辨识率明显下降
    if (input.trim().toLowerCase() === currentCode.toLowerCase()) {
      failCount.value = 0
      return true
    }

    failCount.value += 1

    // 达到阈值立刻换图，压缩单张验证码可被尝试的次数
    if (failCount.value >= CAPTCHA_MAX_FAIL_COUNT) {
      failCount.value = 0
      refresh()
    }
    return false
  }

  const reset = (): void => {
    failCount.value = 0
    refresh()
  }

  reset()

  return { image, available, failCount, refresh, verify, reset }
}
