/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/composables/usePagedList.ts
 * @Description: 移动端通用分页列表，统一处理加载、刷新、触底追加、竞态防护与错误态
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
  /** 首次加载与刷新（整页替换） */
  const loading = ref(false)
  /** 触底追加，与 loading 分开标记，避免"加载更多"时整页被空态覆盖 */
  const loadingMore = ref(false)
  const refreshing = ref(false)
  const error = ref('')

  /**
   * 请求序号。
   * 切换筛选条件时，先发出的请求可能后返回，若不加序号，
   * 旧条件的响应会覆盖新条件的列表，表现为"点了 tab 却显示上一个 tab 的数据"
   */
  let requestSeq = 0

  let currentQuery: Q = { ...((baseQuery ?? {}) as Q) }

  const finished = computed<boolean>(() => list.value.length >= total.value && total.value > 0)
  // 加载失败时不显示空态：把"请求失败"渲染成"暂无数据"会误导用户以为真的没有数据
  const isEmpty = computed<boolean>(
    () => !loading.value && !error.value && list.value.length === 0
  )

  const requestPage = async (targetPage: number, append: boolean): Promise<void> => {
    const seq = ++requestSeq

    if (append) {
      loadingMore.value = true
    } else {
      loading.value = true
    }
    error.value = ''

    try {
      const res = await fetcher(
        { ...currentQuery, pageNum: targetPage, pageSize },
        { silent, quiet: append }
      )
      // 期间已有更新的请求发起，本次结果作废，避免旧响应覆盖新列表
      if (seq !== requestSeq) return

      const records = res.data?.records ?? []
      total.value = res.data?.total ?? 0
      list.value = append ? [...list.value, ...records] : records
      pageNum.value = targetPage
    } catch (err) {
      if (seq !== requestSeq) return
      error.value = err instanceof Error ? err.message : '加载失败'
      // 追加失败时保留已加载数据，用户可继续下拉；整页刷新失败则清空，避免展示过期数据
      if (!append) {
        list.value = []
        total.value = 0
      }
    } finally {
      // 只有仍是最新请求才收起 loading，否则会提前关闭后发起请求仍在等待的加载态
      if (seq === requestSeq) {
        loading.value = false
        loadingMore.value = false
      }
    }
  }

  /** 重新加载第一页，用于下拉刷新与筛选条件变更 */
  const reload = async (): Promise<void> => {
    await requestPage(DEFAULT_PAGE_NUM, false)
  }

  /** 触底追加下一页。刷新进行中不追加，否则会把第 N 页结果拼进新条件的列表 */
  const loadMore = async (): Promise<void> => {
    if (finished.value || loading.value || loadingMore.value) return
    await requestPage(pageNum.value + 1, true)
  }

  /**
   * 变更筛选条件并重新加载。
   * 这里刻意不加 loading 锁：切换 tab 时上一次请求可能仍在途，
   * 直接 return 会让新条件永远等不到结果，列表停留在旧数据
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
    loadingMore,
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
