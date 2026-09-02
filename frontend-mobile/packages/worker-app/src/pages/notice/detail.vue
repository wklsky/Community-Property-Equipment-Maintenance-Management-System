<template>
  <view class="page">
    <view v-if="notice" class="card">
      <text class="title">{{ notice.title }}</text>
      <text class="meta">{{ formatDateTime(notice.createTime) }}</text>
      <text class="content">{{ notice.content }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-02 15:20
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/worker-app/src/pages/notice/detail.vue
 * @Description: 维修工端公告详情，进入即回写已读状态
 */

import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { formatDateTime, getNotice, markNoticeRead, type Notice } from '@community/shared'

const notice = ref<Notice | null>(null)

const loadNotice = async (id: number): Promise<void> => {
  try {
    const res = await getNotice(id, { silent: true })
    notice.value = res.data
    // 已读回写失败不影响阅读：这是统计口径，不应让用户为一个埋点失败买单
    await markNoticeRead(id, { silent: true })
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '公告加载失败',
      icon: 'none'
    })
  }
}

onLoad((options?: Record<string, string>) => {
  const id = Number(options?.id ?? 0)
  void loadNotice(id)
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 24rpx;
}

.card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
}

.title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.4;
  margin-bottom: 16rpx;
}

.meta {
  display: block;
  font-size: 24rpx;
  color: #94a3b8;
  margin-bottom: 32rpx;
}

.content {
  display: block;
  font-size: 30rpx;
  color: #334155;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
