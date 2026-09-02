/**
 * @Author: kian
 * @Date: 2026-09-01 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/utils/debounce.ts
 * @Description: 通用防抖工具，默认立即执行首次触发（适用于登录按钮等点击类交互）
 */

export interface DebouncedFn<Args extends unknown[]> {
  (...args: Args): void
  /** 取消防抖窗口内尚未触发的调用，组件卸载时必须调用以免在已销毁的上下文里执行 */
  cancel: () => void
}

/**
 * 防抖。
 * immediate 默认为 true（前沿触发）：登录这类操作需要第一次点击立即响应，
 * 窗口内的重复点击直接丢弃。若改成后沿触发，用户会感觉"点了没反应"
 */
export function debounce<Args extends unknown[]>(
  fn: (...args: Args) => void,
  wait = 300,
  immediate = true
): DebouncedFn<Args> {
  let timer: ReturnType<typeof setTimeout> | null = null

  const clear = (): void => {
    if (timer !== null) {
      clearTimeout(timer)
      timer = null
    }
  }

  const debounced = (...args: Args): void => {
    const shouldCallNow = immediate && timer === null

    clear()

    if (shouldCallNow) {
      fn(...args)
    }

    timer = setTimeout(() => {
      timer = null
      if (!immediate) {
        fn(...args)
      }
    }, wait)
  }

  debounced.cancel = clear

  return debounced
}
