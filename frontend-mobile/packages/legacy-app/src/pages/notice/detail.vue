<template>
  <view class="notice-detail-page">
    <view class="notice-card" v-if="notice.id">
      <text class="notice-title">{{ notice.title }}</text>
      <text class="notice-time">{{ notice.createTime }}</text>
      <view class="notice-content">
        <text>{{ notice.content }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNotice, markNoticeRead } from '../../api/notice'

const notice = ref({})

onLoad(async (options) => {
  const id = options.id
  if (!id) return

  try {
    const res = await getNotice(id)
    notice.value = res.data

    await markNoticeRead(id)
  } catch (e) {
    console.error('获取公告详情失败', e)
    uni.showToast({ title: '获取公告详情失败', icon: 'none' })
  }
})
</script>

<style scoped>
.notice-detail-page {
  padding: 20rpx;
}

.notice-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.notice-title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20rpx;
}

.notice-time {
  display: block;
  font-size: 24rpx;
  color: #909399;
  margin-bottom: 30rpx;
  padding-bottom: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.notice-content {
  font-size: 28rpx;
  color: #606266;
  line-height: 1.8;
}
</style>
