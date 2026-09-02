/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/common.ts
 * @Description: 分页请求与响应的公共类型，所有业务分页接口复用同一套结构
 */

/**
 * 后端分页参数统一为 pageNum/pageSize（MyBatis-Plus Page 的命名）。
 * 此前 Web 端与后端在 page/pageNum 上曾出现口径分裂，这里作为移动端的唯一约定
 */
export interface PageQuery {
  pageNum?: number
  pageSize?: number
}

/** 后端 Result<Page<T>> 序列化后的响应体，records/total 为列表页的取值来源 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
