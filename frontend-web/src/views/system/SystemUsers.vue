<template>
  <div class="system-page">
    <div class="page-header-card">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="8" r="4"/>
              <path d="M4 20a8 8 0 0 1 16 0"/>
            </svg>
            用户管理
          </h1>
          <p class="page-desc">管理系统用户及其角色分配</p>
        </div>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-row">
        <el-select v-if="isSuperAdmin" v-model="queryParams.tenantId" placeholder="按公司筛选" clearable style="width: 180px" @change="onTenantChange">
          <el-option v-for="t in allTenants" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-input v-model="queryParams.keyword" placeholder="搜索用户名或手机号" clearable style="width: 220px" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="queryParams.roleId" placeholder="按角色筛选" clearable style="width: 160px" @change="loadData">
          <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="showCreateDialog">新增用户</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column v-if="isSuperAdmin" prop="tenantName" label="所属公司" width="140" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.roleName)">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-text="启用"
              inactive-text="禁用"
              @change="(val) => handleToggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 创建/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone" v-if="!isEdit">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? null : 'password'">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空则不修改密码' : '请输入密码(至少6位)'" />
        </el-form-item>
        <el-form-item v-if="isSuperAdmin && !isEdit" label="所属公司" prop="tenantId">
          <el-select v-model="form.tenantId" placeholder="请选择公司" style="width: 100%" @change="onFormTenantChange">
            <el-option v-for="t in allTenants" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
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
import { getUsers, createUser, updateUser, updateUserStatus, getRoles } from '@/api/system'
import { getCrossTenantUsers, getCrossTenantRoles } from '@/api/admin'
import { useUserStore } from '@/store/user'
import { getTenants } from '@/api/admin'

const userStore = useUserStore()
const isSuperAdmin = userStore.isSuperAdmin

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const roles = ref([])
const allTenants = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  roleId: null,
  tenantId: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()
const submitLoading = ref(false)
const form = reactive({
  username: '',
  phone: '',
  password: '',
  roleId: null,
  tenantId: null
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleType = (roleName) => {
  const types = { '系统管理员': '', '维修工': 'warning', '业主': 'success' }
  return types[roleName] || 'info'
}

onMounted(async () => {
  await loadRoles()
  if (isSuperAdmin) {
    await loadTenants()
  }
  loadData()
})


const loadRoles = async () => {
  try {
    if (isSuperAdmin && queryParams.tenantId) {
      const res = await getCrossTenantRoles(queryParams.tenantId)
      roles.value = res.data || []
    } else if (!isSuperAdmin) {
      const res = await getRoles()
      roles.value = res.data || []
    }
  } catch (e) {}
}

const loadTenants = async () => {
  try {
    const res = await getTenants({ pageNum: 1, pageSize: 100 })
    allTenants.value = res.data?.records || []
  } catch (e) {}
}

const onTenantChange = async (tenantId) => {
  queryParams.roleId = null
  if (tenantId) {
    roles.value = []
    const res = await getCrossTenantRoles(tenantId)
    roles.value = res.data || []
  } else {
    roles.value = []
  }
  loadData()
}

const onFormTenantChange = async (tenantId) => {
  form.roleId = null
  if (tenantId) {
    roles.value = []
    const res = await getCrossTenantRoles(tenantId)
    roles.value = res.data || []
  } else {
    roles.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (queryParams.keyword) params.keyword = queryParams.keyword
    if (queryParams.roleId) params.roleId = queryParams.roleId
    if (isSuperAdmin && queryParams.tenantId) params.tenantId = queryParams.tenantId

    let res
    if (isSuperAdmin) {
      res = await getCrossTenantUsers(params)
    } else {
      const sysParams = { ...queryParams }
      if (!sysParams.keyword) delete sysParams.keyword
      if (!sysParams.roleId) delete sysParams.roleId
      res = await getUsers(sysParams)
    }
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载用户列表失败:', e)
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  form.username = ''
  form.phone = ''
  form.password = ''
  form.roleId = null
  form.tenantId = queryParams.tenantId || null
  if (isSuperAdmin) {
    roles.value = []
  }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.username = row.username
  form.phone = row.phone
  form.password = ''
  form.roleId = row.roleId
  form.tenantId = row.tenantId || null
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
    const data = {
      username: form.username,
      roleId: form.roleId
    }
    if (form.password) {
      data.password = form.password
    }
    if (!isEdit.value) {
      data.phone = form.phone
      if (isSuperAdmin && form.tenantId) {
        data.tenantId = form.tenantId
      }
      await createUser(data)
      ElMessage.success('创建成功')
    } else {
      await updateUser(editId.value, data)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error('保存失败:', e)
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (row, enabled) => {
  try {
    await updateUserStatus(row.id, enabled ? 1 : 0)
    row.status = enabled ? 1 : 0
    ElMessage.success(enabled ? '已启用' : '已禁用')
  } catch (e) {
    console.error('操作失败:', e)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？此操作不可恢复。`, '删除确认', { type: 'warning' })
    await updateUserStatus(row.id, 0)
    ElMessage.success('已禁用（软删除）')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败:', e)
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
