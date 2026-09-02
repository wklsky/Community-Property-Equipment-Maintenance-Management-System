<template>
  <view class="page">
    <view v-if="error" class="empty">
      <text class="empty-text">{{ error }}，下拉可重试</text>
    </view>

    <view v-else-if="isEmpty" class="empty">
      <text class="empty-text">暂无社区公告</text>
    </view>

    <view v-else class="card-list">
      <view
        v-for="notice in list"
        :key="notice.id"
        class="notice-card"
        @click="goDetail(notice.id)"
      >
        <text class="title">{{ notice.title }}</text>
        <text class="summary">{{ notice.content }}</text>
        <text class="time">{{ formatDateTime(notice.createTime) }}</text>
      </view>
    </view>

    <view v-if="loading || loadingMore" class="tip">加载中...</view>
    <view v-else-if="finished" class="tip">没有更多了</view>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/owner-app/src/pages/notice/list.vue
 * @Description: 业主端公告列表，只展示后端已发布的公告
 */

import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { formatDateTime, getNotices, usePagedList, type Notice } from '@community/shared'

const { list, loading, loadingMore, finished, isEmpty, error, loadMore, refresh } = usePagedList<Notice, Record<string, never>>({
  fetcher: getNotices
})

const goDetail = (id: number): void => {
  uni.navigateTo({ url: `/pages/notice/detail?id=${id}` })
}

onShow(() => {
  void refresh()
})

onPullDownRefresh(() => {
  void refresh().finally(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  void loadMore()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 24rpx;
}

.empty {
  padding: 200rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #94a3b8;
}

.notice-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
}

.title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12rpx;
}

.summary {
  display: block;
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 16rpx;
  /* 列表只做摘要，超出两行截断，完整内容在详情页展示 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.time {
  font-size: 24rpx;
  color: #94a3b8;
}

.tip {
  text-align: center;
  padding: 24rpx 0;
  font-size: 24rpx;
  color: #94a3b8;
}
</style>
