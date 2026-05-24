<template>
  <div class="page-container">
    <el-page-header @back="router.back()" content="工单详情" />

    <el-card class="detail-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>工单信息</span>
          <div class="header-actions">
            <el-button type="success" v-if="order.status === 0 && isAdmin" @click="handleApprove">审核通过</el-button>
            <el-button type="danger" v-if="order.status === 0 && isAdmin" @click="showRejectDialog">拒绝</el-button>
            <el-button type="success" v-if="order.status === 1 && isAdmin" @click="showAssignDialog">派单</el-button>
            <el-button type="warning" v-if="[2, 3].includes(order.status) && isAdmin" @click="showTransferDialog">转派</el-button>
            <el-button type="danger" v-if="[0, 1, 2].includes(order.status) && isAdmin" @click="handleCancel">取消</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(order.status)">{{ statusMap[order.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址">{{ order.address }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="order.priority === 1 ? 'danger' : 'info'">
            {{ order.priority === 1 ? '紧急' : '普通' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ order.faultDesc }}</el-descriptions-item>
        <el-descriptions-item label="维修工" v-if="order.assignTo">
          {{ order.assignToName || 'ID:' + order.assignTo }}
        </el-descriptions-item>
        <el-descriptions-item label="预约时间" v-if="order.appointTime">{{ order.appointTime }}</el-descriptions-item>
        <el-descriptions-item label="处理说明" :span="2" v-if="order.processDesc">{{ order.processDesc }}</el-descriptions-item>
        <el-descriptions-item label="转派/拒绝原因" :span="2" v-if="order.transferReason">
          <span class="reason-text">{{ order.transferReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间" v-if="order.finishTime">{{ order.finishTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 评价信息 -->
      <div class="evaluation-section" v-if="order.rating">
        <h4>评价信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="评分">
            <el-rate v-model="order.rating" disabled show-score />
          </el-descriptions-item>
          <el-descriptions-item label="评价内容" :span="2" v-if="order.comment">{{ order.comment }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 审核通过确认 -->
    <el-dialog v-model="approveDialogVisible" title="审核确认" width="400px">
      <p>确定审核通过工单 {{ order.orderNo }} 吗？</p>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleApproveConfirm" :loading="actionLoading">确认通过</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝工单" width="450px">
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="2" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleRejectConfirm" :loading="actionLoading">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 派单对话框 -->
    <el-dialog v-model="assignDialogVisible" title="派单给维修工" width="450px">
      <el-form label-width="80px">
        <el-form-item label="维修工">
          <el-select v-model="assignWorkerId" placeholder="请选择维修工" style="width: 100%" filterable>
            <el-option v-for="w in workers" :key="w.id" :label="w.username" :value="w.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignConfirm" :loading="actionLoading">确认派单</el-button>
      </template>
    </el-dialog>

    <!-- 转派对话框 -->
    <el-dialog v-model="transferDialogVisible" title="转派工单" width="450px">
      <el-form label-width="80px">
        <el-form-item label="新维修工">
          <el-select v-model="transferWorkerId" placeholder="请选择维修工" style="width: 100%" filterable>
            <el-option v-for="w in workers" :key="w.id" :label="w.username" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转派原因">
          <el-input v-model="transferReason" type="textarea" :rows="2" placeholder="请输入转派原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTransferConfirm" :loading="actionLoading">确认转派</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRepairOrder, approveOrder, rejectOrder, assignOrder, transferOrder, cancelOrder } from '@/api/repair'
import { getWorkers } from '@/api/common'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.roleName === '系统管理员')

const loading = ref(false)
const order = ref({})
const actionLoading = ref(false)

const approveDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const assignDialogVisible = ref(false)
const assignWorkerId = ref(null)
const transferDialogVisible = ref(false)
const transferWorkerId = ref(null)
const transferReason = ref('')
const workers = ref([])

const statusMap = {
  0: '待受理', 1: '待派单', 2: '待处理', 3: '处理中', 4: '待评价', 5: '已完成', 6: '已取消', 7: '转单中'
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'primary', 4: 'success', 5: 'success', 6: 'danger', 7: 'warning' }
  return types[status] || 'info'
}

const loadOrder = async () => {
  loading.value = true
  try {
    const res = await getRepairOrder(route.params.id)
    order.value = res.data
  } finally {
    loading.value = false
  }
}

const loadWorkers = async () => {
  if (workers.value.length > 0) return
  try {
    const res = await getWorkers()
    workers.value = res.data || []
  } catch (e) {
    console.error('加载维修工失败', e)
  }
}

const handleApprove = () => { approveDialogVisible.value = true }
const handleApproveConfirm = async () => {
  actionLoading.value = true
  try {
    await approveOrder(order.value.id)
    ElMessage.success('审核通过')
    approveDialogVisible.value = false
    loadOrder()
  } catch (e) {  }
  finally { actionLoading.value = false }
}

const showRejectDialog = () => { rejectReason.value = ''; rejectDialogVisible.value = true }
const handleRejectConfirm = async () => {
  if (!rejectReason.value.trim()) { ElMessage.warning('请输入拒绝原因'); return }
  actionLoading.value = true
  try {
    await rejectOrder(order.value.id, rejectReason.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadOrder()
  } catch (e) {  }
  finally { actionLoading.value = false }
}

const showAssignDialog = async () => { assignWorkerId.value = null; await loadWorkers(); assignDialogVisible.value = true }
const handleAssignConfirm = async () => {
  if (!assignWorkerId.value) { ElMessage.warning('请选择维修工'); return }
  actionLoading.value = true
  try {
    await assignOrder(order.value.id, assignWorkerId.value)
    ElMessage.success('派单成功')
    assignDialogVisible.value = false
    loadOrder()
  } catch (e) {  }
  finally { actionLoading.value = false }
}

const showTransferDialog = async () => { transferWorkerId.value = null; transferReason.value = ''; await loadWorkers(); transferDialogVisible.value = true }
const handleTransferConfirm = async () => {
  if (!transferWorkerId.value) { ElMessage.warning('请选择新维修工'); return }
  actionLoading.value = true
  try {
    await transferOrder(order.value.id, transferWorkerId.value, transferReason.value)
    ElMessage.success('转派成功')
    transferDialogVisible.value = false
    loadOrder()
  } catch (e) {  }
  finally { actionLoading.value = false }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(`确定取消工单 ${order.value.orderNo} 吗？取消后不可恢复。`, '取消确认', {
      confirmButtonText: '确定取消', cancelButtonText: '返回', type: 'warning'
    })
    await cancelOrder(order.value.id)
    ElMessage.success('取消成功')
    loadOrder()
  } catch (e) {  }
}

onMounted(() => { loadOrder() })
</script>

<style scoped>
.detail-card { margin-top: 20px; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.reason-text {
  color: #f56c6c;
  background: #fef0f0;
  padding: 4px 12px;
  border-radius: 4px;
  border-left: 3px solid #f56c6c;
}

.evaluation-section {
  margin-top: 20px;
}

.evaluation-section h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}
</style>
