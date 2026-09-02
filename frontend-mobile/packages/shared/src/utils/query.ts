/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/utils/query.ts
 * @Description: 查询参数序列化工具，统一处理空值剔除与数组转逗号串
 */

/**
 * 剔除 undefined / null / 空串。
 * uni.request 对 undefined 的处理在各端不一致（小程序端可能拼出 "key=undefined"），
 * 统一在这里清洗，避免把无意义的空参数发给后端
 */
export const compactParams = <T extends Record<string, unknown>>(params: T): Record<string, unknown> =>
  Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
  )

/**
 * 把多选的状态/ID 数组序列化成逗号分隔串。
 * 后端 /repair-orders/my 与 /assigned 的 statuses 参数是 String 类型并在服务端 split，
 * 前端用数组表达语义，序列化只在这一处完成，避免各页面重复 join 且格式不一
 */
export const joinIds = (ids?: readonly number[]): string | undefined =>
  ids && ids.length > 0 ? ids.join(',') : undefined

/** 拼接 URL 查询串，供「参数必须走 @RequestParam、而 body 已被占用」的接口使用 */
export const toQueryString = (params: Record<string, unknown>): string => {
  const search = Object.entries(compactParams(params))
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&')
  return search ? `?${search}` : ''
}
