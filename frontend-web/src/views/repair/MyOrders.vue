<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">我的工单</span>
      <el-button type="primary" @click="showCreateDialog">提交报修</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="我提交的" name="my" />
      <el-tab-pane label="待我处理" name="assigned" v-if="isWorker" />
    </el-tabs>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="orderNo" label="工单号" width="180" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="faultDesc" label="故障描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          <el-button link type="primary" v-if="row.status === 2 && isWorker" @click="handleAccept(row)">接单</el-button>
          <el-button link type="primary" v-if="row.status === 3 && isWorker" @click="showCompleteDialog(row)">完成</el-button>
          <el-button link type="primary" v-if="row.status === 4 && !isWorker" @click="showEvaluateDialog(row)">评价</el-button>
          <el-button link type="danger" v-if="[0,1,2].includes(row.status) && !isWorker" @click="handleCancel(row)">取消</el-button>
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

    <!-- 创建工单对话框 -->
    <el-dialog v-model="createDialogVisible" title="提交报修" width="500px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="地址" prop="address">
          <el-input v-model="createForm.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="关联设备">
          <el-select v-model="createForm.deviceId" placeholder="请选择关联设备(可选)" clearable style="width: 100%">
            <el-option v-for="d in devices" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="createForm.faultDesc" type="textarea" :rows="3" placeholder="请描述故障情况" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="createForm.priority">
            <el-radio :value="0">普通</el-radio>
            <el-radio :value="1">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">提交</el-button>
      </template>
    </el-dialog>

    <!-- 完成工单对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成工单" width="500px">
      <el-form label-width="80px">
        <el-form-item label="处理说明">
          <el-input v-model="processDesc" type="textarea" :rows="3" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleComplete">确定</el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="evaluateDialogVisible" title="评价工单" width="500px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="evaluateForm.rating" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="evaluateForm.comment" type="textarea" :rows="3" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEvaluate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOrders, getAssignedOrders, createRepairOrder, acceptOrder, completeOrder, evaluateOrder, cancelOrder } from '@/api/repair'
import { getDevices } from '@/api/device'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const isWorker = computed(() => userStore.userInfo.roleName === '维修工')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const activeTab = ref('my')

const queryParams = ref({ pageNum: 1, pageSize: 10 })

const devices = ref([])
const createDialogVisible = ref(false)
const createFormRef = ref()
const createForm = ref({ address: '', faultDesc: '', priority: 0, deviceId: null })
const createRules = {
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  faultDesc: [{ required: true, message: '请描述故障', trigger: 'blur' }]
}

const completeDialogVisible = ref(false)
const processDesc = ref('')
const currentOrder = ref(null)

const evaluateDialogVisible = ref(false)
const evaluateForm = ref({ rating: 5, comment: '' })

const statusMap = {
  0: '待受理', 1: '待派单', 2: '待处理', 3: '处理中', 4: '待评价', 5: '已完成', 6: '已取消', 7: '转单中'
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'primary', 4: 'success', 5: 'success', 6: 'danger', 7: 'warning' }
  return types[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const api = activeTab.value === 'my' ? getMyOrders : getAssignedOrders
    const res = await api(queryParams.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => router.push(`/repair-orders/${row.id}`)

const showCreateDialog = () => {
  createForm.value = { address: '', faultDesc: '', priority: 0, deviceId: null }
  createDialogVisible.value = true
}

const handleCreate = async () => {
  try {
    await createFormRef.value.validate()
    await createRepairOrder(createForm.value)
    ElMessage.success('提交成功')
    createDialogVisible.value = false
    loadData()
  } catch (e) {

  }
}

const handleAccept = async (row) => {
  try {
    await acceptOrder(row.id)
    ElMessage.success('接单成功')
    loadData()
  } catch (e) {

  }
}

const showCompleteDialog = (row) => {
  currentOrder.value = row
  processDesc.value = ''
  completeDialogVisible.value = true
}

const handleComplete = async () => {
  try {
    await completeOrder(currentOrder.value.id, processDesc.value)
    ElMessage.success('完成成功')
    completeDialogVisible.value = false
    loadData()
  } catch (e) {

  }
}

const showEvaluateDialog = (row) => {
  currentOrder.value = row
  evaluateForm.value = { rating: 5, comment: '' }
  evaluateDialogVisible.value = true
}

const handleEvaluate = async () => {
  try {
    await evaluateOrder(currentOrder.value.id, evaluateForm.value.rating, evaluateForm.value.comment)
    ElMessage.success('评价成功')
    evaluateDialogVisible.value = false
    loadData()
  } catch (e) {

  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该工单吗？', '提示')
    await cancelOrder(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (e) {

  }
}

const loadDevices = async () => {
  try {
    const res = await getDevices({ pageNum: 1, pageSize: 500 })
    devices.value = res.data.records || []
  } catch (e) {
    console.error('加载设备列表失败:', e)
  }
}

onMounted(() => {
  loadData()
  loadDevices()
})
</script>
