<template>
  <div class="system-page">
    <div class="page-header-card">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/>
              <rect x="14" y="14" width="7" height="7" rx="1"/>
            </svg>
            公司管理
          </h1>
          <p class="page-desc">管理平台所有物业公司及其管理员</p>
        </div>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-row">
        <el-input v-model="queryParams.name" placeholder="搜索公司名称" clearable style="width: 240px" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="showCreateDialog">新增公司</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="公司名称" min-width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公司' : '新增公司'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公司名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物业公司名称" />
        </el-form-item>
        <template v-if="!isEdit">
          <el-form-item label="管理员用户名" prop="adminUsername">
            <el-input v-model="form.adminUsername" placeholder="请输入管理员用户名" />
          </el-form-item>
          <el-form-item label="管理员手机号" prop="adminPhone">
            <el-input v-model="form.adminPhone" placeholder="请输入管理员手机号" />
          </el-form-item>
          <el-form-item label="管理员密码" prop="adminPassword">
            <el-input v-model="form.adminPassword" type="password" show-password placeholder="请输入密码(至少6位)" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getTenants, createTenant, updateTenant, updateTenantStatus } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()
const submitLoading = ref(false)
const form = reactive({
  name: '',
  adminUsername: '',
  adminPhone: '',
  adminPassword: ''
})

const rules = {
  name: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  adminUsername: [{ required: true, message: '请输入管理员用户名', trigger: 'blur' }],
  adminPhone: [
    { required: true, message: '请输入管理员手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  adminPassword: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: queryParams.pageNum, pageSize: queryParams.pageSize }
    if (queryParams.name) params.name = queryParams.name
    const res = await getTenants(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载公司列表失败:', e)
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  form.name = ''
  form.adminUsername = ''
  form.adminPhone = ''
  form.adminPassword = ''
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitLoading.value = true
  try {
    if (!isEdit.value) {
      await createTenant({
        tenantName: form.name,
        adminUsername: form.adminUsername,
        adminPhone: form.adminPhone,
        adminPassword: form.adminPassword
      })
      ElMessage.success('公司创建成功')
    } else {
      await updateTenant(editId.value, { name: form.name })
      ElMessage.success('公司信息已更新')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error('保存失败:', e)
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}公司「${row.name}」吗？`, `${action}确认`, { type: 'warning' })
    await updateTenantStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(`已${action}`)
  } catch (e) {
    if (e !== 'cancel') console.error('操作失败:', e)
  }
}
</script>

<style scoped lang="scss">
.system-page {
  padding: var(--spacing-lg);
}

.page-header-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);

  .header-content { display: flex; justify-content: space-between; align-items: center; }
  .page-title { display: flex; align-items: center; gap: 10px; font-size: 20px; font-weight: 600; color: var(--color-text-primary); margin: 0; }
  .page-desc { font-size: 13px; color: var(--color-text-tertiary); margin: 6px 0 0 0; }
}

.filter-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);

  .filter-row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
}

.table-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  overflow: hidden;
}

.pagination-container {
  display: flex; justify-content: flex-end;
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
}
</style>
