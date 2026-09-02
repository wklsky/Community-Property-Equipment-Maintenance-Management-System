/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/composables/usePagedList.ts
 * @Description: 移动端通用分页列表，统一处理加载、刷新、触底追加与空态判定
 */

import { computed, ref, type Ref } from 'vue'
import { DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE } from '../constants/business'
import type { ApiResponse, HttpOptions } from '../types/auth'
import type { PageResult } from '../types/common'

/** 列表数据获取器：由调用方注入具体接口，本 Hook 不感知业务细节 */
export type PageFetcher<T, Q> = (
  query: Q & { pageNum: number; pageSize: number },
  options?: HttpOptions
) => Promise<ApiResponse<PageResult<T>>>

export interface UsePagedListOptions<T, Q> {
  fetcher: PageFetcher<T, Q>
  /** 除分页外的固定筛选条件，变更后会触发重新加载 */
  baseQuery?: Q
  pageSize?: number
  /** 为 true 时不弹全局错误提示，由页面自行处理（如轮询场景） */
  silent?: boolean
}

export function usePagedList<T, Q extends Record<string, unknown>>(
  options: UsePagedListOptions<T, Q>
) {
  const { fetcher, baseQuery, pageSize = DEFAULT_PAGE_SIZE, silent = false } = options

  /**
   * 必须显式断言为 Ref<T[]>。
   * ref<T[]>([]) 的推导结果是 Ref<UnwrapRef<T[]>>，其 value 会被展开成 UnwrapRefSimple<T>[]，
   * 把后端返回的 T[] 赋回时会类型不匹配；而断言成 { value: T[] } 又会丢失 Ref 语义，
   * 使模板层无法自动解包。断言为 Ref<T[]> 是同时满足两者的唯一写法
   */
  const list = ref([]) as Ref<T[]>
  const total = ref(0)
  const pageNum = ref(DEFAULT_PAGE_NUM)
  const loading = ref(false)
  const refreshing = ref(false)
  const error = ref<string>('')

  // 已取回的条数达到总数即视为到底，避免再发一次必然返回空数组的请求
  const finished = computed<boolean>(() => list.value.length >= total.value && total.value > 0)
  const isEmpty = computed<boolean>(() => !loading.value && list.value.length === 0)

  let currentQuery: Q = { ...((baseQuery ?? {}) as Q) }

  const requestPage = async (targetPage: number, append: boolean): Promise<void> => {
    if (loading.value) return

    loading.value = true
    error.value = ''

    try {
      const res = await fetcher(
        { ...currentQuery, pageNum: targetPage, pageSize },
        { silent, quiet: append }
      )
      const records = res.data?.records ?? []
      total.value = res.data?.total ?? 0
      list.value = append ? [...list.value, ...records] : records
      pageNum.value = targetPage
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载失败'
      // 追加失败时保持原有列表不变，用户仍可看到已加载的数据并继续下拉重试
      if (!append) {
        list.value = []
      }
    } finally {
      loading.value = false
    }
  }

  /** 重新加载第一页，用于下拉刷新与筛选条件变更 */
  const reload = async (): Promise<void> => {
    await requestPage(DEFAULT_PAGE_NUM, false)
  }

  /** 触底追加下一页 */
  const loadMore = async (): Promise<void> => {
    if (finished.value) return
    await requestPage(pageNum.value + 1, true)
  }

  /**
   * 变更筛选条件并重新加载。
   * 条件变化后必须回到第一页，否则会出现"筛选后页码越界导致列表为空"的假象
   */
  const setQuery = async (patch: Partial<Q>): Promise<void> => {
    currentQuery = { ...currentQuery, ...patch }
    await reload()
  }

  /** 下拉刷新的包装：单独维护 refreshing 状态以便页面控制动画结束时机 */
  const refresh = async (): Promise<void> => {
    refreshing.value = true
    try {
      await reload()
    } finally {
      refreshing.value = false
    }
  }

  return {
    list,
    total,
    loading,
    refreshing,
    finished,
    isEmpty,
    error,
    reload,
    loadMore,
    setQuery,
    refresh
  }
}
