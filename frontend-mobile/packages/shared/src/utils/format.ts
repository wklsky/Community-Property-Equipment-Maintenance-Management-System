/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/utils/format.ts
 * @Description: 展示层格式化工具，统一处理后端时间串与状态码的兜底展示
 */

/**
 * 后端 LocalDateTime 默认序列化为 ISO 字符串（2026-09-02T15:20:30），
 * 中间的 T 直接展示对移动端不友好，统一归一为「年-月-日 时:分」
 */
export const formatDateTime = (value?: string | null): string => {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

/** 只取日期部分，用于巡检任务日期、趋势图坐标等无需精确到秒的场景 */
export const formatDate = (value?: string | null): string => {
  if (!value) return ''
  return value.slice(0, 10)
}

/**
 * 状态码转展示文案。
 * 兜底返回"未知"而非空串：后端新增状态时若前端未同步，
 * 空文案会让用户以为数据缺失，而"未知"至少能定位到是状态未对齐
 */
export const resolveStatusName = (
  nameMap: Record<number, string>,
  code?: number | null
): string => {
  if (code === undefined || code === null) return '未知'
  return nameMap[code] ?? '未知'
}
