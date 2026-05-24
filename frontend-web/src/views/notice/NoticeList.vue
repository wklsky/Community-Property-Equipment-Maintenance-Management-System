<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">公告管理</span>
      <el-button type="primary" @click="showDialog()" v-if="isAdmin">
        <el-icon><Plus /></el-icon>发布公告
      </el-button>
    </div>

    <div class="search-bar" v-if="isAdmin">
      <el-select v-model="queryParams.publishStatus" placeholder="发布状态" clearable style="width: 140px" @change="loadData">
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="定时发布" :value="2" />
      </el-select>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <span class="notice-title" @click="viewDetail(row)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :class="getStatusClass(row.publishStatus)">
              {{ getStatusText(row.publishStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scheduledTime" label="定时发布时间" width="180" v-if="isAdmin">
          <template #default="{ row }">
            {{ row.scheduledTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right" v-if="isAdmin">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link type="primary" @click="showDialog(row)">编辑</el-button>
            <el-button link type="success" v-if="row.publishStatus === 0" @click="handlePublish(row)">立即发布</el-button>
            <el-button link type="warning" v-if="row.publishStatus === 0" @click="showScheduleDialog(row)">定时发布</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" v-else>
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 编辑公告对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入公告内容" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 公告详情对话框 -->
    <el-dialog v-model="detailVisible" title="公告详情" width="640px">
      <div class="notice-detail">
        <h2 class="detail-title">{{ currentNotice.title }}</h2>
        <div class="detail-meta">
          <span class="meta-time">发布时间：{{ currentNotice.createTime }}</span>
          <el-tag :class="getStatusClass(currentNotice.publishStatus)" size="small">
            {{ getStatusText(currentNotice.publishStatus) }}
          </el-tag>
        </div>
        <el-divider />
        <div class="detail-content">{{ currentNotice.content }}</div>
      </div>
    </el-dialog>

    <!-- 定时发布对话框 -->
    <el-dialog v-model="scheduleDialogVisible" title="定时发布" width="400px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="scheduledTime"
            type="datetime"
            placeholder="选择发布时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSchedulePublish">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllNotices, getNotices, createNotice, updateNotice, deleteNotice, publishNotice, scheduleNotice, markNoticeRead } from '@/api/notice'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo.roleName === '系统管理员')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const scheduleDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentNotice = ref({})
const scheduledTime = ref('')

const queryParams = ref({ pageNum: 1, pageSize: 10, publishStatus: null })
const form = ref({ title: '', content: '' })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const statusMap = { 0: '草稿', 1: '已发布', 2: '定时发布' }
const getStatusText = (status) => statusMap[status] || '未知'
const getStatusClass = (status) => {
  const classes = { 0: 'status-info', 1: 'status-normal', 2: 'status-warning' }
  return classes[status] || ''
}

const disabledDate = (time) => time.getTime() < Date.now() - 8.64e7

const loadData = async () => {
  loading.value = true
  try {
    const api = isAdmin.value ? getAllNotices : getNotices
    const res = await api(queryParams.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const showDialog = (row) => {
  isEdit.value = !!row
  form.value = row ? { ...row } : { title: '', content: '' }
  dialogVisible.value = true
}

const viewDetail = async (row) => {
  currentNotice.value = row
  detailVisible.value = true
  try {
    await markNoticeRead(row.id)
  } catch (e) {}
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await updateNotice(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createNotice(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {

  }
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定立即发布该公告吗？', '提示')
    await publishNotice(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (e) {

  }
}

const showScheduleDialog = (row) => {
  currentNotice.value = row
  scheduledTime.value = ''
  scheduleDialogVisible.value = true
}

const handleSchedulePublish = async () => {
  if (!scheduledTime.value) {
    ElMessage.warning('请选择发布时间')
    return
  }
  try {
    await scheduleNotice(currentNotice.value.id, scheduledTime.value)
    ElMessage.success('定时发布设置成功')
    scheduleDialogVisible.value = false
    loadData()
  } catch (e) {

  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该公告吗？', '提示', { type: 'warning' })
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {

  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.notice-title {
  color: #409EFF;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.notice-detail {
  .detail-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }

  .detail-meta {
    display: flex;
    align-items: center;
    gap: 16px;

    .meta-time {
      font-size: 14px;
      color: #909399;
    }
  }

  .detail-content {
    font-size: 15px;
    line-height: 1.8;
    color: #606266;
    white-space: pre-wrap;
  }
}
</style>
