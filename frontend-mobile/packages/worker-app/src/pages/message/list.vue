<template>
  <view class="page">
    <view v-if="error" class="empty">
      <text class="empty-text">{{ error }}，下拉可重试</text>
    </view>

    <view v-else-if="isEmpty" class="empty">
      <text class="empty-text">暂无消息</text>
    </view>

    <view v-else class="card-list">
      <view
        v-for="message in list"
        :key="message.id"
        class="message-card"
        :class="{ unread: message.isRead === 0 }"
        @click="readMessage(message)"
      >
        <view class="card-header">
          <text class="type">{{ message.type }}</text>
          <text v-if="message.isRead === 0" class="dot" />
        </view>
        <text class="content">{{ message.content }}</text>
        <text class="time">{{ formatDateTime(message.createTime) }}</text>
      </view>
    </view>

    <view v-if="loading || loadingMore" class="tip">加载中...</view>
    <view v-else-if="finished" class="tip">没有更多了</view>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/message/list.vue
 * @Description: 维修工端消息中心，点击即标记已读并即时更新本地未读态
 */

import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { formatDateTime, getMessages, markMessageRead, usePagedList, type Message } from '@community/shared'

const { list, loading, loadingMore, finished, isEmpty, error, loadMore, refresh } = usePagedList<Message, Record<string, never>>({
  fetcher: getMessages
})

const readMessage = async (message: Message): Promise<void> => {
  if (message.isRead === 1) return
  try {
    await markMessageRead(message.id, { silent: true })
    // 就地更新而非重新拉取整页：消息列表是高频浏览场景，
    // 每次点击都刷新会让列表跳动并丢失当前滚动位置
    message.isRead = 1
  } catch {
    // 已读回写失败不改变本地展示，用户已经看到了内容
  }
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

.message-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
}

.message-card.unread {
  background: #f0f7ff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.type {
  font-size: 24rpx;
  color: #64748b;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #0066ff;
}

.content {
  display: block;
  font-size: 29rpx;
  color: #1e293b;
  line-height: 1.6;
  margin-bottom: 16rpx;
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
