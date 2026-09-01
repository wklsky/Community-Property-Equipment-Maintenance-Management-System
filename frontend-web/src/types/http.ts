/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 15:20
 * @FilePath: frontend-web/src/types/http.ts
 * @Description: 定义后端统一响应体与请求层的类型契约，为仍为 JS 的 request 模块提供类型收窄入口
 */

/**
 * 后端 Result<T> 的固定结构。
 * 约定：code === 200 才代表业务成功，其余 code 一律由拦截器转成异常，
 * 因此业务层拿到返回值时无需再判断 code
 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface RequestConfig {
  /**
   * silent 为 true 时拦截器不再弹出全局错误 Toast。
   * 登录等场景需要按业务码给出差异化引导（如"租户不存在，请检查输入"），
   * 若叠加拦截器自带的通用 Toast 会造成双重提示
   */
  silent?: boolean
  /** 上传等允许并发重复提交的场景，跳过全局请求取消托管 */
  allowRepeat?: boolean
  timeout?: number
  headers?: Record<string, string>
  responseType?: 'json' | 'blob' | 'arraybuffer'
}

/** request.js 的对外能力集合，用于把无类型的 JS 模块收窄成强类型客户端 */
export interface HttpClient {
  get<T = unknown>(
    url: string,
    params?: Record<string, unknown>,
    config?: RequestConfig
  ): Promise<ApiResponse<T>>
  post<T = unknown>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>>
  put<T = unknown>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>>
  delete<T = unknown>(
    url: string,
    params?: Record<string, unknown>,
    config?: RequestConfig
  ): Promise<ApiResponse<T>>
  patch<T = unknown>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>>
}
