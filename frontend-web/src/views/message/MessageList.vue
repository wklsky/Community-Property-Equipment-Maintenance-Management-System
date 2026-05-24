<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">消息中心</span>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag>{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="isRead" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isRead === 1 ? 'info' : 'danger'">
            {{ row.isRead === 1 ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" v-if="row.isRead === 0" @click="handleRead(row)">标记已读</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessages, markMessageRead } from '@/api/message'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = ref({ pageNum: 1, pageSize: 10 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMessages(queryParams.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleRead = async (row) => {
  await markMessageRead(row.id)
  ElMessage.success('已标记为已读')
  loadData()
}

onMounted(() => loadData())
</script>
