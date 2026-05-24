<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">巡检管理</span>
    </div>

    <el-tabs v-model="activeTab" class="inspection-tabs">
      <el-tab-pane label="巡检任务" name="tasks">
        <div class="tab-header">
          <div class="filter-group">
            <el-select v-model="taskQueryParams.status" placeholder="任务状态" clearable style="width: 140px" @change="refreshTasks">
              <el-option label="待接单" :value="0" />
              <el-option label="进行中" :value="1" />
              <el-option label="已完成" :value="2" />
            </el-select>
            <el-select v-model="taskQueryParams.planId" placeholder="所属计划" clearable style="width: 180px" @change="refreshTasks" v-if="isAdmin">
              <el-option v-for="p in plans" :key="p.id" :label="p.name" :value="p.id" />
            </el-select>
          </div>
          <div class="filter-actions">
            <el-button :icon="Refresh" @click="refreshTasks">刷新</el-button>
          </div>
        </div>
        <div class="table-container">
          <el-table :data="tasks" v-loading="taskLoading">
            <el-table-column prop="planName" label="计划名称" min-width="160" />
            <el-table-column prop="buildingId" label="楼栋" width="120">
              <template #default="{ row }">{{ getBuildingName(row.buildingId) }}</template>
            </el-table-column>
            <el-table-column prop="categoryId" label="设备类型" width="120">
              <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
            </el-table-column>
            <el-table-column prop="taskDate" label="任务日期" width="120" />
            <el-table-column prop="assignedTo" label="负责人" width="100" v-if="isAdmin">
              <template #default="{ row }">{{ row.assignedToName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :class="getTaskStatusClass(row.status)">{{ getTaskStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" v-if="row.status === 0" @click="handleAcceptTask(row)">接单</el-button>
                <el-button link type="success" v-if="row.status === 1" @click="showCompleteDialog(row)">完成巡检</el-button>
                <el-tag type="success" v-if="row.status === 2" effect="plain">已完成</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="pagination-container" v-if="taskTotal > taskQueryParams.pageSize">
          <el-pagination
            v-model:current-page="taskQueryParams.pageNum"
            v-model:page-size="taskQueryParams.pageSize"
            :total="taskTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadTasks"
            @current-change="loadTasks"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="巡检计划" name="plans" v-if="isAdmin">
        <div class="tab-header">
          <div class="filter-group">
            <el-select v-model="planQueryParams.status" placeholder="计划状态" clearable style="width: 140px" @change="refreshPlans">
              <el-option label="草稿" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="已暂停" :value="2" />
            </el-select>
            <el-input v-model="planQueryParams.name" placeholder="计划名称" clearable style="width: 180px" @keyup.enter="refreshPlans">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="filter-actions">
            <el-button :icon="Refresh" @click="refreshPlans">刷新</el-button>
            <el-button type="primary" @click="showPlanDialog()">
              <el-icon><Plus /></el-icon>新增计划
            </el-button>
          </div>
        </div>
        <div class="table-container">
          <el-table :data="plans" v-loading="planLoading">
            <el-table-column prop="name" label="计划名称" min-width="160" />
            <el-table-column prop="buildingId" label="楼栋" width="120">
              <template #default="{ row }">{{ getBuildingName(row.buildingId) }}</template>
            </el-table-column>
            <el-table-column prop="categoryId" label="设备类型" width="120">
              <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
            </el-table-column>
            <el-table-column prop="cycle" label="周期(天)" width="100" align="center" />
            <el-table-column prop="nextTime" label="下次巡检时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getPlanStatusType(row.status)">{{ planStatusMap[row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="showPlanDialog(row)">编辑</el-button>
                <el-button link type="success" v-if="row.status === 0" @click="handlePublishPlan(row)">发布</el-button>
                <el-button link type="warning" v-if="row.status === 1" @click="handlePausePlan(row)">暂停</el-button>
                <el-button link type="success" v-if="row.status === 2" @click="handleResumePlan(row)">恢复</el-button>
                <el-button link type="danger" @click="handleDeletePlan(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="pagination-container" v-if="planTotal > planQueryParams.pageSize">
          <el-pagination
            v-model:current-page="planQueryParams.pageNum"
            v-model:page-size="planQueryParams.pageSize"
            :total="planTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadPlans"
            @current-change="loadPlans"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 完成巡检对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成巡检" width="500px" destroy-on-close>
      <div class="task-info" v-if="currentTask">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="计划名称">{{ currentTask.planName }}</el-descriptions-item>
          <el-descriptions-item label="任务日期">{{ currentTask.taskDate }}</el-descriptions-item>
          <el-descriptions-item label="楼栋">{{ getBuildingName(currentTask.buildingId) }}</el-descriptions-item>
          <el-descriptions-item label="设备类型">{{ getCategoryName(currentTask.categoryId) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form label-width="100px" style="margin-top: 20px;">
        <el-form-item label="巡检结果" required>
          <el-radio-group v-model="completeForm.result">
            <el-radio :value="1">
              <el-tag type="success" effect="plain">正常</el-tag>
            </el-radio>
            <el-radio :value="0">
              <el-tag type="danger" effect="plain">异常</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="completeForm.remark" type="textarea" :rows="4" placeholder="请输入巡检备注（如发现问题请详细描述）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleComplete" :loading="completeLoading">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑计划对话框 -->
    <el-dialog v-model="planDialogVisible" :title="isEditPlan ? '编辑巡检计划' : '新增巡检计划'" width="560px" destroy-on-close>
      <el-form ref="planFormRef" :model="planForm" :rules="planRules" label-width="100px">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="planForm.name" placeholder="请输入计划名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择楼栋" prop="buildingId">
              <el-select v-model="planForm.buildingId" placeholder="请选择楼栋" style="width: 100%" filterable>
                <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型" prop="categoryId">
              <el-select v-model="planForm.categoryId" placeholder="请选择设备类型" style="width: 100%" filterable>
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="巡检周期" prop="cycle">
              <el-input-number v-model="planForm.cycle" :min="1" :max="365" style="width: 100%" />
              <div class="form-tip">单位：天</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="首次巡检">
              <el-date-picker v-model="planForm.nextTime" type="date" placeholder="选择日期（可选）"
                value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePlan" :loading="planLoading2">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { getInspectionPlans, createInspectionPlan, updateInspectionPlan, deleteInspectionPlan, publishInspectionPlan,
         pauseInspectionPlan, resumeInspectionPlan,
         getInspectionTasks, getMyInspectionTasks, acceptInspectionTask, completeInspectionTask } from '@/api/inspection'
import { getBuildings } from '@/api/common'
import { getDeviceCategories } from '@/api/device'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo.roleName === '系统管理员')

const activeTab = ref('tasks')
const taskLoading = ref(false)
const planLoading = ref(false)
const tasks = ref([])
const plans = ref([])
const buildings = ref([])
const categories = ref([])

const taskQueryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  status: null,
  planId: null
})
const taskTotal = ref(0)

const planQueryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  status: null,
  name: ''
})
const planTotal = ref(0)

const completeDialogVisible = ref(false)
const currentTask = ref(null)
const completeForm = reactive({ result: 1, remark: '' })
const completeLoading = ref(false)

const planDialogVisible = ref(false)
const isEditPlan = ref(false)
const planFormRef = ref()
const planForm = reactive({
  id: null,
  name: '',
  buildingId: null,
  categoryId: null,
  deviceId: null,
  cycle: 7,
  nextTime: ''
})
const planLoading2 = ref(false)
const planRules = {
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  cycle: [{ required: true, message: '请输入巡检周期', trigger: 'blur' }]
}

const taskStatusMap = { 0: '待接单', 1: '进行中', 2: '已完成' }
const getTaskStatusText = (status) => taskStatusMap[status] || '未知'
const getTaskStatusClass = (status) => {
  const classes = { 0: 'status-warning', 1: 'status-info', 2: 'status-normal' }
  return classes[status] || ''
}

const planStatusMap = { 0: '草稿', 1: '已发布', 2: '已暂停' }
const getPlanStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning' }
  return types[status] || 'info'
}

const getBuildingName = (id) => buildings.value.find(b => b.id === id)?.name || '-'

const getCategoryName = (id) => categories.value.find(c => c.id === id)?.name || '-'

const loadTasks = async () => {
  taskLoading.value = true
  try {
    const api = isAdmin.value ? getInspectionTasks : getMyInspectionTasks
    const res = await api(taskQueryParams)
    tasks.value = res.data.records || res.data || []
    taskTotal.value = res.data.total || tasks.value.length
  } catch (error) {
    console.error('加载任务列表失败:', error)
    ElMessage.error('加载任务列表失败')
  } finally {
    taskLoading.value = false
  }
}

const loadPlans = async () => {
  planLoading.value = true
  try {
    const res = await getInspectionPlans(planQueryParams)
    plans.value = res.data.records || res.data || []
    planTotal.value = res.data.total || plans.value.length
  } catch (error) {
    console.error('加载计划列表失败:', error)
    ElMessage.error('加载计划列表失败')
  } finally {
    planLoading.value = false
  }
}

const loadBuildings = async () => {
  try {
    const res = await getBuildings()
    buildings.value = res.data || []
  } catch (error) {
    console.error('加载楼栋列表失败:', error)
  }
}

const loadCategories = async () => {
  try {
    const res = await getDeviceCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载设备类型列表失败:', error)
  }
}

const handleAcceptTask = async (row) => {
  try {
    await ElMessageBox.confirm('确定接受该巡检任务吗？', '提示', { type: 'info' })
    await acceptInspectionTask(row.id)
    ElMessage.success('接单成功')
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('接单失败: ' + (error.message || '未知错误'))
    }
  }
}

const showCompleteDialog = (row) => {
  currentTask.value = row
  completeForm.result = 1
  completeForm.remark = ''
  completeDialogVisible.value = true
}

const handleComplete = async () => {
  completeLoading.value = true
  try {
    await completeInspectionTask(currentTask.value.id, completeForm)
    ElMessage.success('巡检完成')
    completeDialogVisible.value = false
    loadTasks()
  } catch (error) {
    ElMessage.error('提交失败: ' + (error.message || '未知错误'))
  } finally {
    completeLoading.value = false
  }
}

const showPlanDialog = (row = null) => {
  isEditPlan.value = !!row
  if (row) {
    Object.assign(planForm, { ...row })
  } else {
    Object.assign(planForm, {
      id: null,
      name: '',
      buildingId: null,
      categoryId: null,
      deviceId: null,
      cycle: 7,
      nextTime: ''
    })
  }
  planDialogVisible.value = true
}

const cleanPlanForm = () => {
  const data = { ...planForm }
  if (data.nextTime === '' || data.nextTime === null) {
    data.nextTime = null
  }
  if (!isEditPlan.value) {
    data.id = null
  }
  return data
}

const handleSavePlan = async () => {
  try {
    await planFormRef.value.validate()
  } catch {
    return
  }

  planLoading2.value = true
  try {
    const payload = cleanPlanForm()
    console.log('[DEBUG] 发送巡检计划请求体:', JSON.stringify(payload, null, 2))
    if (isEditPlan.value) {
      await updateInspectionPlan(planForm.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createInspectionPlan(payload)
      ElMessage.success('创建成功')
    }
    planDialogVisible.value = false
    loadPlans()
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    planLoading2.value = false
  }
}

const handlePublishPlan = async (row) => {
  try {
    await ElMessageBox.confirm(
      '发布后将立即生成一个巡检任务，并根据周期自动生成后续任务。确定发布吗？',
      '发布确认',
      { type: 'warning' }
    )
    await publishInspectionPlan(row.id)
    ElMessage.success('发布成功，已生成巡检任务')
    loadPlans()
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败: ' + (error.message || '未知错误'))
    }
  }
}

const handlePausePlan = async (row) => {
  try {
    await ElMessageBox.confirm('暂停后不会再自动生成巡检任务。确定暂停吗？', '暂停确认', { type: 'warning' })
    await pauseInspectionPlan(row.id)
    ElMessage.success('已暂停')
    loadPlans()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('暂停失败: ' + (error.message || '未知错误'))
    }
  }
}

const handleResumePlan = async (row) => {
  try {
    await ElMessageBox.confirm('恢复后将按周期自动生成巡检任务。确定恢复吗？', '恢复确认', { type: 'warning' })
    await resumeInspectionPlan(row.id)
    ElMessage.success('已恢复')
    loadPlans()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('恢复失败: ' + (error.message || '未知错误'))
    }
  }
}

const handleDeletePlan = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该计划吗？删除后无法恢复。', '删除确认', { type: 'warning' })
    await deleteInspectionPlan(row.id)
    ElMessage.success('删除成功')
    loadPlans()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

const refreshTasks = () => {
  taskQueryParams.pageNum = 1
  loadTasks()
}

const refreshPlans = () => {
  planQueryParams.pageNum = 1
  loadPlans()
}

watch(activeTab, (newTab) => {
  if (newTab === 'tasks') {
    loadTasks()
  } else if (newTab === 'plans' && isAdmin.value) {
    loadPlans()
  }
})

onMounted(() => {
  loadTasks()
  loadBuildings()
  loadCategories()
  if (isAdmin.value) {
    loadPlans()
  }
})
</script>

<style scoped lang="scss">
.page-container {
  padding: var(--spacing-lg);
  background: var(--color-bg-primary);
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-lg) var(--spacing-xl);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
}

.page-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);

  &::before {
    content: '';
    width: 4px;
    height: 24px;
    background: var(--color-primary-blue);
    border-radius: 2px;
  }
}

.inspection-tabs {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);

  :deep(.el-tabs__header) {
    margin-bottom: var(--spacing-lg);
    border-bottom: 1px solid var(--color-border-light);
  }

  :deep(.el-tabs__item) {
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-medium);
    color: var(--color-text-secondary);
    padding: 0 var(--spacing-lg);
    height: 48px;
    line-height: 48px;

    &.is-active {
      color: var(--color-primary-blue);
      font-weight: var(--font-weight-semibold);
    }

    &:hover {
      color: var(--color-primary-blue);
    }
  }

  :deep(.el-tabs__active-bar) {
    background: var(--color-primary-blue);
    height: 3px;
    border-radius: 2px;
  }
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);

  .filter-group {
    display: flex;
    gap: var(--spacing-md);
  }

  .filter-actions {
    display: flex;
    gap: var(--spacing-sm);
  }
}

.pagination-container {
  margin-top: var(--spacing-lg);
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  display: flex;
  justify-content: flex-end;
}

.task-info {
  margin-bottom: var(--spacing-md);
}

.form-tip {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: 4px;
}

.table-container {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  overflow: hidden;

  :deep(.el-table) {
    th.el-table__cell {
      background: var(--color-bg-tertiary) !important;
      font-weight: var(--font-weight-semibold);
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
    }

    .el-table__row {
      &:nth-child(even) {
        background: var(--color-bg-tertiary);
      }

      &:hover > td {
        background: var(--color-primary-blue-light) !important;
      }
    }
  }
}

.status-warning {
  background: var(--color-status-warning-bg) !important;
  color: var(--color-status-warning) !important;
  border: none !important;
}

.status-info {
  background: var(--color-status-info-bg) !important;
  color: var(--color-status-info) !important;
  border: none !important;
}

.status-normal {
  background: var(--color-status-normal-bg) !important;
  color: var(--color-status-normal) !important;
  border: none !important;
}

:deep(.el-dialog) {
  border-radius: var(--radius-xl);

  .el-dialog__header {
    padding: var(--spacing-lg) var(--spacing-xl);
    border-bottom: 1px solid var(--color-border-light);
    margin: 0;
    background: var(--color-primary-navy);

    .el-dialog__title {
      color: #fff;
      font-weight: var(--font-weight-semibold);
    }

    .el-dialog__headerbtn .el-dialog__close {
      color: #fff;
    }
  }

  .el-dialog__body {
    padding: var(--spacing-xl);
  }

  .el-dialog__footer {
    padding: var(--spacing-md) var(--spacing-xl);
    border-top: 1px solid var(--color-border-light);
  }
}

:deep(.el-radio-group) {
  display: flex;
  gap: var(--spacing-lg);

  .el-radio {
    margin-right: 0;
  }
}
</style>
