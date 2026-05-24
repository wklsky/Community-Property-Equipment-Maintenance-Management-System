<template>
  <div class="device-page">
    <!-- Page Header -->
    <div class="page-header-card">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="2" y="3" width="20" height="14" rx="2"/>
              <path d="M8 21h8M12 17v4"/>
            </svg>
            设备管理
          </h1>
          <p class="page-desc">管理社区内所有IoT设备，监控设备运行状态</p>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <span class="stat-value">{{ total }}</span>
            <span class="stat-label">设备总数</span>
          </div>
        </div>
      </div>
      <el-button type="primary" class="add-btn" @click="showDialog()">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="8" y1="2" x2="8" y2="14"/>
          <line x1="2" y1="8" x2="14" y2="8"/>
        </svg>
        新增设备
      </el-button>
    </div>

    <!-- Filter Bar -->
    <div class="filter-card">
      <div class="filter-group">
        <label class="filter-label">设备类型</label>
        <el-select v-model="queryParams.categoryId" placeholder="全部类型" clearable @change="loadData">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
      <div class="filter-group">
        <label class="filter-label">所属楼栋</label>
        <el-select v-model="queryParams.buildingId" placeholder="全部楼栋" clearable @change="loadData">
          <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </div>
      <div class="filter-group">
        <label class="filter-label">设备状态</label>
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable @change="loadData">
          <el-option label="正常" :value="1">
            <span class="status-option">
              <span class="status-dot normal"></span>
              正常
            </span>
          </el-option>
          <el-option label="故障" :value="2">
            <span class="status-option">
              <span class="status-dot danger"></span>
              故障
            </span>
          </el-option>
          <el-option label="维修中" :value="3">
            <span class="status-option">
              <span class="status-dot warning"></span>
              维修中
            </span>
          </el-option>
          <el-option label="停用" :value="4">
            <span class="status-option">
              <span class="status-dot info"></span>
              停用
            </span>
          </el-option>
        </el-select>
      </div>
      <div class="filter-actions">
        <el-button @click="resetFilters">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M1 1l12 12M13 1L1 13"/>
          </svg>
          重置
        </el-button>
      </div>
    </div>

    <!-- Data Table -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" class="device-table">
        <el-table-column prop="name" label="设备信息" min-width="200">
          <template #default="{ row }">
            <div class="device-info-cell">
              <div class="device-icon" :class="getCategoryClass(row.categoryId)">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="3" y="4" width="14" height="10" rx="1.5"/>
                  <path d="M7 17h6M10 14v3"/>
                </svg>
              </div>
              <div class="device-details">
                <span class="device-name">{{ row.name }}</span>
                <span class="device-model" v-if="row.model">型号: {{ row.model }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryId" label="设备类型" width="130">
          <template #default="{ row }">
            <span class="category-tag">{{ getCategoryName(row.categoryId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="buildingId" label="所属楼栋" width="120">
          <template #default="{ row }">
            <span class="building-text">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="2" y="3" width="10" height="10" rx="1"/>
                <path d="M5 6h1M8 6h1M5 9h1M8 9h1"/>
              </svg>
              {{ getBuildingName(row.buildingId) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="安装位置" min-width="150">
          <template #default="{ row }">
            <span class="location-text">{{ row.location || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="installDate" label="安装日期" width="120">
          <template #default="{ row }">
            <span class="date-text">{{ row.installDate || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="运行状态" width="130">
          <template #default="{ row }">
            <div class="status-badge" :class="getStatusClass(row.status)">
              <span class="status-indicator"></span>
              <span class="status-text">{{ getStatusText(row.status) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" @click="showDialog(row)">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M10 1.5l2.5 2.5L4 12.5H1.5V10L10 1.5z"/>
                </svg>
                编辑
              </el-button>
              <el-dropdown @command="(cmd) => handleStatusChange(row, cmd)" trigger="click">
                <el-button link type="primary">
                  状态
                  <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M3 5l3 3 3-3"/>
                  </svg>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="1" :disabled="row.status === 1">
                      <span class="dropdown-status">
                        <span class="status-dot normal"></span>
                        设为正常
                      </span>
                    </el-dropdown-item>
                    <el-dropdown-item :command="2" :disabled="row.status === 2">
                      <span class="dropdown-status">
                        <span class="status-dot danger"></span>
                        设为故障
                      </span>
                    </el-dropdown-item>
                    <el-dropdown-item :command="3" :disabled="row.status === 3">
                      <span class="dropdown-status">
                        <span class="status-dot warning"></span>
                        设为维修中
                      </span>
                    </el-dropdown-item>
                    <el-dropdown-item :command="4" :disabled="row.status === 4">
                      <span class="dropdown-status">
                        <span class="status-dot info"></span>
                        设为停用
                      </span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button link type="danger" @click="handleDelete(row)">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M2 4h10M5 4V2.5a.5.5 0 01.5-.5h3a.5.5 0 01.5.5V4M11 4v8a1 1 0 01-1 1H4a1 1 0 01-1-1V4"/>
                </svg>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination -->
    <div class="pagination-card">
      <div class="pagination-info">
        共 <span class="total-count">{{ total }}</span> 条记录
      </div>
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑设备' : '新增设备'" width="600px" destroy-on-close class="device-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="device-form">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备型号" prop="model">
              <el-input v-model="form.model" placeholder="请输入设备型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="设备类型" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择类型" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属楼栋" prop="buildingId">
              <el-select v-model="form.buildingId" placeholder="请选择楼栋" style="width: 100%">
                <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="安装位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入安装位置，如：A栋1单元负一层" />
        </el-form-item>
        <el-form-item label="安装日期" prop="installDate">
          <el-date-picker v-model="form.installDate" type="date" placeholder="选择安装日期"
            value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '确认添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDevices, createDevice, updateDevice, updateDeviceStatus, deleteDevice, getDeviceCategories } from '@/api/device'
import { getBuildings } from '@/api/common'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])
const buildings = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const queryParams = ref({ pageNum: 1, pageSize: 10, categoryId: null, buildingId: null, status: null })
const form = ref({ name: '', model: '', categoryId: null, buildingId: null, location: '', installDate: '' })
const rules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  buildingId: [{ required: true, message: '请选择所属楼栋', trigger: 'change' }]
}

const normalCount = computed(() => tableData.value.filter(d => d.status === 1).length)
const faultCount = computed(() => tableData.value.filter(d => [2, 3].includes(d.status)).length)

const statusMap = { 1: '正常', 2: '故障', 3: '维修中', 4: '停用' }
const getStatusText = (status) => statusMap[status] || '未知'
const getStatusClass = (status) => {
  const classes = { 1: 'normal', 2: 'danger', 3: 'warning', 4: 'info' }
  return classes[status] || ''
}

const getCategoryClass = (categoryId) => {
  const classes = ['elevator', 'pump', 'fire', 'power']
  return classes[(categoryId - 1) % classes.length] || 'default'
}

const getCategoryName = (id) => categories.value.find(c => c.id === id)?.name || '-'
const getBuildingName = (id) => buildings.value.find(b => b.id === id)?.name || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDevices(queryParams.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const res = await getDeviceCategories()
  categories.value = res.data
}

const loadBuildings = async () => {
  const res = await getBuildings()
  buildings.value = res.data
}

const resetFilters = () => {
  queryParams.value = { pageNum: 1, pageSize: 10, categoryId: null, buildingId: null, status: null }
  loadData()
}

const showDialog = (row) => {
  isEdit.value = !!row
  form.value = row ? { ...row } : { name: '', model: '', categoryId: null, buildingId: null, location: '', installDate: '' }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await updateDevice(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createDevice(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {

  }
}

const handleStatusChange = async (row, status) => {
  try {
    await updateDeviceStatus(row.id, status)
    ElMessage.success('状态更新成功')
    loadData()
  } catch (e) {

  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该设备吗？删除后无法恢复。', '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    await deleteDevice(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {

  }
}

onMounted(() => {
  loadData()
  loadCategories()
  loadBuildings()
})
</script>

<style scoped lang="scss">
.device-page {
  padding: var(--spacing-lg);
  background: var(--color-bg-primary);
  min-height: 100%;
}

.page-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-2xl);
}

.header-info {
  .page-title {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
    margin: 0 0 8px 0;

    svg {
      color: var(--color-primary-blue);
    }
  }

  .page-desc {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.header-stats {
  display: flex;
  gap: var(--spacing-xl);
  padding-left: var(--spacing-xl);
  border-left: 1px solid var(--color-border-light);

  .stat-item {
    text-align: center;

    .stat-value {
      display: block;
      font-size: 28px;
      font-weight: var(--font-weight-bold);
      color: var(--color-text-primary);
      line-height: 1.2;
    }

    .stat-label {
      display: block;
      font-size: var(--font-size-xs);
      color: var(--color-text-tertiary);
      margin-top: 4px;
    }

    &.normal .stat-value {
      color: var(--color-status-normal);
    }

    &.warning .stat-value {
      color: var(--color-status-warning);
    }
  }
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 24px;
  font-weight: var(--font-weight-medium);
}

.filter-card {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-lg);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg) var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .filter-label {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-medium);
    color: var(--color-text-secondary);
  }

  :deep(.el-select) {
    width: 160px;
  }
}

.filter-actions {
  margin-left: auto;
}

.status-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.normal { background: var(--color-status-normal); }
  &.danger { background: var(--color-status-danger); }
  &.warning { background: var(--color-status-warning); }
  &.info { background: var(--color-text-tertiary); }
}

.table-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.device-table {
  :deep(th.el-table__cell) {
    background: var(--color-bg-tertiary) !important;
    font-weight: var(--font-weight-semibold);
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  :deep(td.el-table__cell) {
    padding: 16px 12px;
  }

  :deep(.el-table__row) {
    &:nth-child(even) {
      background: var(--color-bg-tertiary);
    }

    &:hover > td {
      background: var(--color-primary-blue-light) !important;
    }
  }
}

.device-info-cell {
  display: flex;
  align-items: center;
  gap: 14px;
}

.device-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.elevator {
    background: linear-gradient(135deg, #E0E7FF 0%, #C7D2FE 100%);
    color: #4F46E5;
  }

  &.pump {
    background: linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%);
    color: #2563EB;
  }

  &.fire {
    background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
    color: #DC2626;
  }

  &.power {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
    color: #D97706;
  }
}

.device-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.device-model {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.category-tag {
  display: inline-block;
  padding: 4px 12px;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
}

.building-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);

  svg {
    color: var(--color-text-tertiary);
  }
}

.location-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.date-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);

  .status-indicator {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: currentColor;
  }

  &.normal {
    background: var(--color-status-normal-bg);
    color: var(--color-status-normal);

    .status-indicator {
      animation: pulse 2s infinite;
    }
  }

  &.danger {
    background: var(--color-status-danger-bg);
    color: var(--color-status-danger);

    .status-indicator {
      animation: pulse-danger 1s infinite;
    }
  }

  &.warning {
    background: var(--color-status-warning-bg);
    color: var(--color-status-warning);
  }

  &.info {
    background: var(--color-bg-tertiary);
    color: var(--color-text-tertiary);
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes pulse-danger {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.2); }
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-button {
    display: flex;
    align-items: center;
    gap: 4px;

    svg {
      flex-shrink: 0;
    }
  }
}

.dropdown-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md) var(--spacing-xl);
  margin-top: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
}

.pagination-info {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);

  .total-count {
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
  }
}

.device-dialog {
  :deep(.el-dialog__header) {
    padding: var(--spacing-lg) var(--spacing-xl);
    border-bottom: 1px solid var(--color-border-light);
    margin: 0;
  }

  :deep(.el-dialog__body) {
    padding: var(--spacing-xl);
  }

  :deep(.el-dialog__footer) {
    padding: var(--spacing-md) var(--spacing-xl);
    border-top: 1px solid var(--color-border-light);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
