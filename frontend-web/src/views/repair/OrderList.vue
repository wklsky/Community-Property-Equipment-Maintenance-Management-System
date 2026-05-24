<template>
  <div class="order-page">
    <!-- Hero Header -->
    <div class="hero-card">
      <div class="hero-bg-decor"></div>
      <div class="hero-content">
        <div class="hero-info">
          <h1 class="hero-title">
            <span class="title-icon-box">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/>
                <rect x="9" y="3" width="6" height="4" rx="1"/>
                <path d="M9 12h6M9 16h6"/>
              </svg>
            </span>
            工单管理
          </h1>
          <p class="hero-subtitle">管理报修工单，提交报修、派单和状态跟踪</p>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="showCreateDialog" class="btn-create">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            提交报修
          </el-button>
        </div>
        <div class="stat-cards-row">
          <div class="stat-card-item pending">
            <div class="stat-icon-circle">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
              </svg>
            </div>
            <div class="stat-text">
              <span class="stat-num">{{ statsData.pending }}</span>
              <span class="stat-desc">待处理</span>
            </div>
          </div>
          <div class="stat-card-item processing">
            <div class="stat-icon-circle">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
              </svg>
            </div>
            <div class="stat-text">
              <span class="stat-num">{{ statsData.processing }}</span>
              <span class="stat-desc">处理中</span>
            </div>
          </div>
          <div class="stat-card-item completed">
            <div class="stat-icon-circle">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <div class="stat-text">
              <span class="stat-num">{{ statsData.completed }}</span>
              <span class="stat-desc">已完成</span>
            </div>
          </div>
          <div class="stat-card-item total">
            <div class="stat-icon-circle">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/>
              </svg>
            </div>
            <div class="stat-text">
              <span class="stat-num">{{ statsData.total }}</span>
              <span class="stat-desc">工单总数</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tabs-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部工单" name="all" v-if="isAdmin" />
        <el-tab-pane label="我提交的" name="my" />
        <el-tab-pane label="待我处理" name="assigned" v-if="isWorker" />
      </el-tabs>
    </div>

    <!-- Admin Filter Bar (only for "all" tab) -->
    <div class="filter-card" v-if="activeTab === 'all' && isAdmin">
      <div class="filter-header">
        <span class="filter-title">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
          </svg>
          筛选条件
        </span>
      </div>
      <div class="filter-body">
        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">工单状态</label>
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable @change="loadData">
              <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="Number(k)">
                <span class="status-option">
                  <span class="status-dot" :class="getStatusClass(Number(k))"></span>
                  {{ v }}
                </span>
              </el-option>
            </el-select>
          </div>
          <div class="filter-group">
            <label class="filter-label">优先级</label>
            <el-select v-model="queryParams.priority" placeholder="全部" clearable @change="loadData">
              <el-option label="普通" :value="0" />
              <el-option label="紧急" :value="1" />
            </el-select>
          </div>
          <div class="filter-group">
            <label class="filter-label">工单号</label>
            <el-input v-model="queryParams.orderNo" placeholder="输入工单号" clearable @keyup.enter="loadData" style="width: 160px" />
          </div>
          <div class="filter-group">
            <label class="filter-label">楼栋</label>
            <el-select v-model="queryParams.buildingId" placeholder="全部楼栋" clearable @change="loadData">
              <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
            </el-select>
          </div>
        </div>
        <div class="active-filters" v-if="activeFilters.length > 0">
          <span class="active-filters-label">已选:</span>
          <el-tag
            v-for="f in activeFilters"
            :key="f.key"
            closable
            :type="f.tagType"
            size="small"
            @close="clearFilter(f.key)"
          >
            {{ f.label }}
          </el-tag>
        </div>

        <div class="filter-actions">
          <el-button type="primary" @click="loadData" class="btn-search">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>
            </svg>
            查询
          </el-button>
          <el-button @click="resetFilters" class="btn-reset">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>
            </svg>
            重置
          </el-button>
          <el-dropdown @command="handleExport" class="export-dropdown">
            <el-button class="btn-export">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
              </svg>
              导出 ▾
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="list">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/>
                  </svg>
                  导出当前筛选列表
                </el-dropdown-item>
                <el-dropdown-item command="statistics">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
                  </svg>
                  导出统计报表
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- Admin Table (all tab) -->
    <div class="table-card" v-if="activeTab === 'all' && isAdmin">
      <el-table :data="tableData" v-loading="loading" class="order-table" stripe>
        <el-table-column prop="orderNo" label="工单号" width="180">
          <template #default="{ row }">
            <span class="order-no-link" @click="viewDetail(row)">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="报修地址" min-width="180">
          <template #default="{ row }">
            <div class="address-cell">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
              </svg>
              <span>{{ row.address }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="fault-desc">{{ row.faultDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <span class="status-badge" :class="getStatusClass(row.status)">
              {{ statusMap[row.status] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">
            <span class="priority-tag" :class="row.priority === 1 ? 'urgent' : 'normal'">
              {{ row.priority === 1 ? '紧急' : '普通' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            <span class="time-text">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" @click="showDetailDialog(row)" class="action-link">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                </svg>
                查看
              </el-button>
              <el-button link type="primary" @click="viewDetail(row)" class="action-link">详情</el-button>
              <el-button link type="success" v-if="row.status === 0" @click="handleApprove(row)" class="action-link action-approve">审核通过</el-button>
              <el-button link type="success" v-if="row.status === 1" @click="showAssignDialog(row)" class="action-link">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/>
                </svg>
                派单
              </el-button>
              <el-button link type="danger" v-if="row.status === 0" @click="showRejectDialog(row)" class="action-link">拒绝</el-button>
              <el-button link type="warning" v-if="[2, 3, 7].includes(row.status)" @click="showTransferDialog(row)" class="action-link">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/>
                </svg>
                转派
              </el-button>
              <el-button link type="danger" v-if="[0, 1, 2].includes(row.status)" @click="handleCancel(row)" class="action-link">取消</el-button>
              <el-button link type="info" @click="handleExportSingle(row)" class="action-link action-export-single">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
                导出
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Personal Table (my / assigned tabs) -->
    <div class="table-card" v-if="activeTab !== 'all' || !isAdmin">
      <el-table :data="personalTableData" v-loading="personalLoading" class="order-table" stripe>
        <el-table-column prop="orderNo" label="工单号" width="180">
          <template #default="{ row }">
            <span class="order-no-link" @click="viewDetail(row)">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-badge" :class="getStatusClass(row.status)">
              {{ statusMap[row.status] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" @click="viewDetail(row)" class="action-link">详情</el-button>
              <el-button link type="primary" v-if="row.status === 2 && isWorker" @click="handleAccept(row)" class="action-link">接单</el-button>
              <el-button link type="primary" v-if="row.status === 3 && isWorker" @click="showCompleteDialog(row)" class="action-link">完成</el-button>
              <el-button link type="primary" v-if="row.status === 4 && !isWorker" @click="showEvaluateDialog(row)" class="action-link">评价</el-button>
              <el-button link type="danger" v-if="[0,1,2].includes(row.status) && !isWorker" @click="handleCancel(row)" class="action-link">取消</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination -->
    <div class="pagination-card">
      <div class="pagination-info">
        共 <strong>{{ activeTab === 'all' && isAdmin ? total : personalTotal }}</strong> 条工单
      </div>
      <el-pagination
        v-model:current-page="currentPageParams.pageNum"
        v-model:page-size="currentPageParams.pageSize"
        :total="activeTab === 'all' && isAdmin ? total : personalTotal"
        :page-sizes="[10, 20, 50]"
        layout="sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- Assign Dialog -->
    <el-dialog v-model="assignDialogVisible" title="派单给维修工" width="460px" class="order-dialog">
      <div class="dialog-order-preview">
        <div class="preview-row">
          <span class="preview-label">工单号</span>
          <span class="preview-value">{{ currentOrder?.orderNo }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">故障描述</span>
          <span class="preview-value">{{ currentOrder?.faultDesc }}</span>
        </div>
      </div>
      <el-form label-width="80px" class="dialog-form">
        <el-form-item label="维修工">
          <el-select v-model="assignWorkerId" placeholder="请选择维修工" style="width: 100%" filterable>
            <el-option v-for="w in workers" :key="w.id" :label="w.username" :value="w.id">
              <div class="worker-option">
                <div class="worker-avatar">{{ w.username?.charAt(0) }}</div>
                <div class="worker-info">
                  <span class="worker-name">{{ w.username }}</span>
                  <span class="worker-phone" v-if="w.phone">{{ w.phone }}</span>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAssign" :loading="assignLoading">确认派单</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Reject Dialog -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝工单" width="460px" class="order-dialog">
      <div class="dialog-order-preview">
        <div class="preview-row">
          <span class="preview-label">工单号</span>
          <span class="preview-value">{{ currentOrder?.orderNo }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">故障描述</span>
          <span class="preview-value">{{ currentOrder?.faultDesc }}</span>
        </div>
      </div>
      <el-form label-width="80px" class="dialog-form">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="handleReject" :loading="rejectLoading">确认拒绝</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Transfer Dialog -->
    <el-dialog v-model="transferDialogVisible" title="转派工单" width="460px" class="order-dialog">
      <div class="dialog-order-preview">
        <div class="preview-row">
          <span class="preview-label">工单号</span>
          <span class="preview-value">{{ currentOrder?.orderNo }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">当前维修工</span>
          <span class="preview-value">{{ currentOrder?.assignedWorker || '未分配' }}</span>
        </div>
      </div>
      <el-form label-width="80px" class="dialog-form">
        <el-form-item label="新维修工">
          <el-select v-model="transferWorkerId" placeholder="请选择维修工" style="width: 100%" filterable>
            <el-option v-for="w in workers" :key="w.id" :label="w.username" :value="w.id">
              <div class="worker-option">
                <div class="worker-avatar">{{ w.username?.charAt(0) }}</div>
                <div class="worker-info">
                  <span class="worker-name">{{ w.username }}</span>
                  <span class="worker-phone" v-if="w.phone">{{ w.phone }}</span>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="转派原因">
          <el-input v-model="transferReason" type="textarea" :rows="3" placeholder="请输入转派原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transferDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleTransfer" :loading="transferLoading">确认转派</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailDialogVisible" title="工单详情" width="640px" class="detail-dialog-wrapper">
      <div v-loading="detailLoading">
        <template v-if="orderDetail">
          <div class="detail-section">
            <h4 class="section-title">基本信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="工单号">{{ orderDetail.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <span class="status-badge" :class="getStatusClass(orderDetail.status)">
                  {{ statusMap[orderDetail.status] }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="优先级">
                <el-tag :type="orderDetail.priority === 1 ? 'danger' : 'info'" effect="light">
                  {{ orderDetail.priority === 1 ? '紧急' : '普通' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ orderDetail.createTime }}</el-descriptions-item>
              <el-descriptions-item label="报修地址" :span="2">{{ orderDetail.address }}</el-descriptions-item>
              <el-descriptions-item label="故障描述" :span="2">{{ orderDetail.faultDesc }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div class="detail-section" v-if="orderDetail.assignToName || orderDetail.transferReason">
            <h4 class="section-title">处理信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="维修工" v-if="orderDetail.assignToName">{{ orderDetail.assignToName }}</el-descriptions-item>
              <el-descriptions-item label="预约时间" v-if="orderDetail.appointTime">{{ orderDetail.appointTime }}</el-descriptions-item>
              <el-descriptions-item label="处理描述" :span="2" v-if="orderDetail.processDesc">{{ orderDetail.processDesc }}</el-descriptions-item>
              <el-descriptions-item label="转派/拒绝原因" :span="2" v-if="orderDetail.transferReason">
                <span class="reason-text-inline">{{ orderDetail.transferReason }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="完成时间" v-if="orderDetail.finishTime">{{ orderDetail.finishTime }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div class="detail-section" v-if="orderDetail.rating">
            <h4 class="section-title">评价信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="评分">
                <el-rate v-model="orderDetail.rating" disabled />
              </el-descriptions-item>
              <el-descriptions-item label="评价内容" :span="2" v-if="orderDetail.comment">{{ orderDetail.comment }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="viewDetail(orderDetail)" v-if="orderDetail">查看完整详情</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Create Order Dialog -->
    <el-dialog v-model="createDialogVisible" title="提交报修" width="500px" class="order-dialog">
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
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreate" :loading="createLoading">提交</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Complete Order Dialog -->
    <el-dialog v-model="completeDialogVisible" title="完成工单" width="500px" class="order-dialog">
      <el-form label-width="80px">
        <el-form-item label="处理说明">
          <el-input v-model="processDesc" type="textarea" :rows="3" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="completeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleComplete" :loading="completeLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Evaluate Dialog -->
    <el-dialog v-model="evaluateDialogVisible" title="评价工单" width="500px" class="order-dialog">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="evaluateForm.rating" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="evaluateForm.comment" type="textarea" :rows="3" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="evaluateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEvaluate" :loading="evaluateLoading">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRepairOrders, getMyOrders, getAssignedOrders, createRepairOrder, acceptOrder, completeOrder, evaluateOrder, assignOrder, getRepairOrder, approveOrder, rejectOrder, transferOrder, cancelOrder, exportSingleOrder, exportOrderList, exportOrderStatistics } from '@/api/repair'
import { getWorkers, getBuildings } from '@/api/common'
import { getDevices } from '@/api/device'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo.roleName === '系统管理员')
const isWorker = computed(() => userStore.userInfo.roleName === '维修工')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const workers = ref([])
const buildings = ref([])

const activeTab = ref(isAdmin.value ? 'all' : 'my')

const personalLoading = ref(false)
const personalTableData = ref([])
const personalTotal = ref(0)
const personalQueryParams = ref({ pageNum: 1, pageSize: 10 })

const assignDialogVisible = ref(false)
const assignWorkerId = ref(null)
const assignLoading = ref(false)
const currentOrder = ref(null)

const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectLoading = ref(false)

const transferDialogVisible = ref(false)
const transferWorkerId = ref(null)
const transferReason = ref('')
const transferLoading = ref(false)

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const orderDetail = ref(null)

const devices = ref([])
const createDialogVisible = ref(false)
const createFormRef = ref()
const createLoading = ref(false)
const createForm = ref({ address: '', faultDesc: '', priority: 0, deviceId: null })
const createRules = {
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  faultDesc: [{ required: true, message: '请描述故障', trigger: 'blur' }]
}

const completeDialogVisible = ref(false)
const processDesc = ref('')
const completeLoading = ref(false)

const evaluateDialogVisible = ref(false)
const evaluateLoading = ref(false)
const evaluateForm = ref({ rating: 5, comment: '' })

const statsData = reactive({
  pending: 0,
  processing: 0,
  completed: 0,
  total: 0
})

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  status: null,
  priority: null,
  orderNo: '',
  buildingId: null,
  dateRange: null
})

const statusMap = {
  0: '待受理',
  1: '待派单',
  2: '待处理',
  3: '处理中',
  4: '待评价',
  5: '已完成',
  6: '已取消',
  7: '转单中'
}

const priorityMap = {
  0: '普通',
  1: '紧急'
}

const currentPageParams = computed(() => {
  if (activeTab.value === 'all' && isAdmin.value) {
    return queryParams.value
  }
  return personalQueryParams.value
})

const getBuildingName = (id) => {
  const b = buildings.value.find(b => b.id === id)
  return b ? b.name : ''
}

const activeFilters = computed(() => {
  const filters = []
  const q = queryParams.value
  if (q.status !== null && q.status !== '') {
    const cls = getStatusClass(Number(q.status))
    filters.push({
      key: 'status',
      label: `状态: ${statusMap[q.status]}`,
      tagType: cls === 'danger' ? 'danger' : cls === 'success' ? 'success' : cls === 'warning' ? 'warning' : 'info'
    })
  }
  if (q.priority !== null && q.priority !== '') {
    filters.push({
      key: 'priority',
      label: `优先级: ${priorityMap[q.priority]}`,
      tagType: q.priority === 1 ? 'danger' : 'info'
    })
  }
  if (q.orderNo) {
    filters.push({
      key: 'orderNo',
      label: `工单号: ${q.orderNo}`,
      tagType: 'primary'
    })
  }
  if (q.buildingId !== null && q.buildingId !== '') {
    filters.push({
      key: 'buildingId',
      label: `楼栋: ${getBuildingName(q.buildingId)}`,
      tagType: ''
    })
  }
  return filters
})

const clearFilter = (key) => {
  if (key === 'status') {
    queryParams.value.status = null
  } else if (key === 'priority') {
    queryParams.value.priority = null
  } else if (key === 'orderNo') {
    queryParams.value.orderNo = ''
  } else if (key === 'buildingId') {
    queryParams.value.buildingId = null
  }
  queryParams.value.pageNum = 1
  loadData()
}

const getStatusClass = (status) => {
  const classes = {
    0: 'info',
    1: 'warning',
    2: 'warning',
    3: 'processing',
    4: 'success',
    5: 'success',
    6: 'danger',
    7: 'warning'
  }
  return classes[status] || 'info'
}

const onTabChange = () => {
  if (activeTab.value === 'all' && isAdmin.value) {
    queryParams.value.pageNum = 1
  } else {
    personalQueryParams.value.pageNum = 1
  }
  loadData()
}

const loadData = async () => {
  if (activeTab.value === 'all' && isAdmin.value) {
    await loadAdminData()
  } else {
    await loadPersonalData()
  }
}

const loadAdminData = async () => {
  loading.value = true
  try {
    const params = { ...queryParams.value }
    if (params.dateRange && params.dateRange.length === 2) {
      params.startDate = params.dateRange[0]
      params.endDate = params.dateRange[1]
    }
    delete params.dateRange

    const res = await getRepairOrders(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0

    updateStats()
  } catch (error) {
    console.error('加载工单列表失败:', error)
    ElMessage.error('加载工单列表失败')
  } finally {
    loading.value = false
  }
}

const loadPersonalData = async () => {
  personalLoading.value = true
  try {
    const api = activeTab.value === 'my' ? getMyOrders : getAssignedOrders
    const res = await api(personalQueryParams.value)
    personalTableData.value = res.data.records || []
    personalTotal.value = res.data.total || 0
  } catch (error) {
    console.error('加载工单列表失败:', error)
    ElMessage.error('加载工单列表失败')
  } finally {
    personalLoading.value = false
  }
}

const updateStats = () => {
  statsData.total = total.value
  statsData.pending = tableData.value.filter(o => [0, 1].includes(o.status)).length
  statsData.processing = tableData.value.filter(o => [2, 3, 7].includes(o.status)).length
  statsData.completed = tableData.value.filter(o => [4, 5].includes(o.status)).length
}

const loadWorkers = async () => {
  try {
    const res = await getWorkers()
    workers.value = res.data || []
  } catch (error) {
    console.error('加载维修工列表失败:', error)
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

const loadDevices = async () => {
  try {
    const res = await getDevices({ pageNum: 1, pageSize: 500 })
    devices.value = res.data.records || []
  } catch (e) {
    console.error('加载设备列表失败:', e)
  }
}

const resetFilters = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    status: null,
    priority: null,
    orderNo: '',
    buildingId: null,
    dateRange: null
  }
  loadData()
}

const viewDetail = (row) => {
  router.push(`/repair-orders/${row.id}`)
}

const showDetailDialog = async (row) => {
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    const res = await getRepairOrder(row.id)
    orderDetail.value = res.data
  } catch (error) {
    ElMessage.error('获取工单详情失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const showAssignDialog = async (row) => {
  currentOrder.value = row
  assignWorkerId.value = null
  if (workers.value.length === 0) {
    await loadWorkers()
  }
  assignDialogVisible.value = true
}

const handleAssign = async () => {
  if (!assignWorkerId.value) {
    ElMessage.warning('请选择维修工')
    return
  }
  assignLoading.value = true
  try {
    await assignOrder(currentOrder.value.id, assignWorkerId.value)
    ElMessage.success('派单成功')
    assignDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('派单失败: ' + (error.message || '未知错误'))
  } finally {
    assignLoading.value = false
  }
}

const showRejectDialog = (row) => {
  currentOrder.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  rejectLoading.value = true
  try {
    await rejectOrder(currentOrder.value.id, rejectReason.value)
    ElMessage.success('已拒绝该工单')
    rejectDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
  } finally {
    rejectLoading.value = false
  }
}

const showTransferDialog = async (row) => {
  currentOrder.value = row
  transferWorkerId.value = null
  transferReason.value = ''
  if (workers.value.length === 0) {
    await loadWorkers()
  }
  transferDialogVisible.value = true
}

const handleTransfer = async () => {
  if (!transferWorkerId.value) {
    ElMessage.warning('请选择新维修工')
    return
  }
  transferLoading.value = true
  try {
    await transferOrder(currentOrder.value.id, transferWorkerId.value, transferReason.value)
    ElMessage.success('转派成功')
    transferDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('转派失败: ' + (error.message || '未知错误'))
  } finally {
    transferLoading.value = false
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定审核通过工单 ${row.orderNo} 吗？`, '审核确认', {
      confirmButtonText: '审核通过',
      cancelButtonText: '取消',
      type: 'success'
    })
    await approveOrder(row.id)
    ElMessage.success('审核通过')
    loadData()
  } catch (e) {
    // user cancelled
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确定取消工单 ${row.orderNo} 吗？取消后不可恢复。`, '取消确认', {
      confirmButtonText: '确定取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await cancelOrder(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (e) {
    // user cancelled
  }
}

const handleExportSingle = async (row) => {
  try {
    await exportSingleOrder(row.id)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

const handleExport = async (command) => {
  try {
    if (command === 'list') {
      const params = { ...queryParams.value }
      if (params.dateRange && params.dateRange.length === 2) {
        params.startDate = params.dateRange[0]
        params.endDate = params.dateRange[1]
      }
      delete params.dateRange
      delete params.pageNum
      delete params.pageSize
      await exportOrderList(params)
      ElMessage.success('列表导出成功')
    } else if (command === 'statistics') {
      await exportOrderStatistics()
      ElMessage.success('统计报表导出成功')
    }
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

// Personal order actions
const showCreateDialog = () => {
  createForm.value = { address: '', faultDesc: '', priority: 0, deviceId: null }
  createDialogVisible.value = true
}

const handleCreate = async () => {
  try {
    await createFormRef.value.validate()
  } catch {
    return
  }
  createLoading.value = true
  try {
    await createRepairOrder(createForm.value)
    ElMessage.success('提交成功')
    createDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('提交失败: ' + (e.message || '未知错误'))
  } finally {
    createLoading.value = false
  }
}

const handleAccept = async (row) => {
  try {
    await acceptOrder(row.id)
    ElMessage.success('接单成功')
    loadData()
  } catch (e) {
    ElMessage.error('接单失败: ' + (e.message || '未知错误'))
  }
}

const showCompleteDialog = (row) => {
  currentOrder.value = row
  processDesc.value = ''
  completeDialogVisible.value = true
}

const handleComplete = async () => {
  completeLoading.value = true
  try {
    await completeOrder(currentOrder.value.id, processDesc.value)
    ElMessage.success('完成成功')
    completeDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败: ' + (e.message || '未知错误'))
  } finally {
    completeLoading.value = false
  }
}

const showEvaluateDialog = (row) => {
  currentOrder.value = row
  evaluateForm.value = { rating: 5, comment: '' }
  evaluateDialogVisible.value = true
}

const handleEvaluate = async () => {
  evaluateLoading.value = true
  try {
    await evaluateOrder(currentOrder.value.id, evaluateForm.value.rating, evaluateForm.value.comment)
    ElMessage.success('评价成功')
    evaluateDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('评价失败: ' + (e.message || '未知错误'))
  } finally {
    evaluateLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadBuildings()
  loadDevices()
})
</script>

<style scoped lang="scss">
.order-page {
  padding: var(--spacing-lg);
  background: var(--color-bg-primary);
  min-height: 100%;
}

.hero-card {
  position: relative;
  background: linear-gradient(135deg, var(--color-primary-navy) 0%, #253548 50%, #1a2d3d 100%);
  border-radius: var(--radius-xl);
  margin-bottom: var(--spacing-lg);
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(30, 41, 59, 0.12), 0 1px 4px rgba(0, 0, 0, 0.08);
}

.hero-bg-decor {
  position: absolute;
  top: -60px;
  right: -40px;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 102, 255, 0.15) 0%, transparent 70%);
  pointer-events: none;
}

.hero-content {
  position: relative;
  padding: var(--spacing-xl);
  z-index: 1;
}

.hero-info {
  margin-bottom: var(--spacing-lg);

  .hero-title {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: #fff;
    margin: 0 0 6px 0;
    letter-spacing: -0.3px;
  }

  .title-icon-box {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 38px;
    height: 38px;
    background: rgba(0, 102, 255, 0.25);
    border-radius: var(--radius-md);
    color: #60a5fa;
  }

  .hero-subtitle {
    font-size: var(--font-size-sm);
    color: rgba(255, 255, 255, 0.55);
    margin: 0 0 0 50px;
  }
}

.hero-actions {
  position: absolute;
  top: var(--spacing-xl);
  right: var(--spacing-xl);
  z-index: 2;

  .btn-create {
    background: rgba(255, 255, 255, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.25);
    color: #fff;
    font-weight: var(--font-weight-semibold);
    display: flex;
    align-items: center;
    gap: 6px;

    &:hover {
      background: rgba(255, 255, 255, 0.25);
      border-color: rgba(255, 255, 255, 0.4);
    }
  }
}

.stat-cards-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
}

.stat-card-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(6px);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all var(--transition-normal);
  cursor: default;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.15);
    transform: translateY(-2px);
  }

  .stat-icon-circle {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 42px;
    height: 42px;
    border-radius: var(--radius-md);
    flex-shrink: 0;
  }

  .stat-text {
    display: flex;
    flex-direction: column;

    .stat-num {
      font-size: var(--font-size-2xl);
      font-weight: var(--font-weight-bold);
      line-height: 1.1;
      color: #fff;
    }

    .stat-desc {
      font-size: var(--font-size-xs);
      color: rgba(255, 255, 255, 0.5);
      margin-top: 2px;
    }
  }

  &.pending .stat-icon-circle {
    background: rgba(245, 158, 11, 0.2);
    color: #fbbf24;
  }

  &.processing .stat-icon-circle {
    background: rgba(59, 130, 246, 0.2);
    color: #60a5fa;
  }

  &.completed .stat-icon-circle {
    background: rgba(16, 185, 129, 0.2);
    color: #34d399;
  }

  &.total .stat-icon-circle {
    background: rgba(148, 163, 184, 0.2);
    color: #cbd5e1;
  }
}

.tabs-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  padding: 0 var(--spacing-lg);

  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  :deep(.el-tabs__item) {
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-medium);
    height: 48px;
    line-height: 48px;
  }
}

.filter-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.filter-header {
  display: flex;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--color-bg-tertiary);
  border-bottom: 1px solid var(--color-border-light);
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;

  svg {
    color: var(--color-primary-blue);
  }
}

.filter-body {
  padding: var(--spacing-lg) var(--spacing-xl);
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .filter-label {
    font-size: var(--font-size-xs);
    font-weight: var(--font-weight-medium);
    color: var(--color-text-tertiary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

.active-filters {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
  padding: var(--spacing-sm) 0;
  margin-bottom: var(--spacing-sm);
}

.active-filters-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  font-weight: var(--font-weight-medium);
}

.filter-actions {
  display: flex;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);

  .btn-search {
    box-shadow: 0 2px 8px rgba(0, 102, 255, 0.25);

    &:hover {
      box-shadow: 0 4px 14px rgba(0, 102, 255, 0.35);
      transform: translateY(-1px);
    }
  }

  .btn-reset {
    color: var(--color-text-secondary);

    &:hover {
      color: var(--color-primary-blue);
      border-color: var(--color-primary-blue);
      background: var(--color-primary-blue-light);
    }
  }

  .btn-export {
    color: var(--color-text-secondary);

    &:hover {
      color: var(--color-status-info);
      border-color: var(--color-status-info);
      background: var(--color-status-info-bg);
    }
  }
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

  &.info { background: var(--color-text-tertiary); }
  &.warning { background: var(--color-status-warning); }
  &.processing { background: var(--color-status-info); }
  &.success { background: var(--color-status-normal); }
  &.danger { background: var(--color-status-danger); }
}

.table-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.order-table {
  :deep(.el-table__header th) {
    background: var(--color-bg-tertiary) !important;
    font-weight: var(--font-weight-semibold);
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
    text-transform: uppercase;
    letter-spacing: 0.4px;
  }

  :deep(.el-table__row) {
    transition: background var(--transition-fast);

    &:hover > td {
      background: var(--color-primary-blue-light) !important;
    }
  }
}

.order-no-link {
  color: var(--color-primary-blue);
  font-weight: var(--font-weight-semibold);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    color: var(--color-primary-blue-hover);
    text-decoration: underline;
  }
}

.address-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);

  svg {
    color: var(--color-text-tertiary);
    flex-shrink: 0;
  }
}

.fault-desc {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.3px;
  white-space: nowrap;

  &.info {
    background: var(--color-bg-tertiary);
    color: var(--color-text-tertiary);
  }

  &.warning {
    background: var(--color-status-warning-bg);
    color: var(--color-status-warning);
  }

  &.processing {
    background: var(--color-status-info-bg);
    color: var(--color-status-info);
    animation: statusPulse 2s infinite;
  }

  &.success {
    background: var(--color-status-normal-bg);
    color: var(--color-status-normal);
  }

  &.danger {
    background: var(--color-status-danger-bg);
    color: var(--color-status-danger);
  }
}

@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.priority-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);

  &.normal {
    background: var(--color-bg-tertiary);
    color: var(--color-text-tertiary);
  }

  &.urgent {
    background: var(--color-status-danger-bg);
    color: var(--color-status-danger);
  }
}

.time-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  font-variant-numeric: tabular-nums;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-wrap: wrap;
}

.action-link {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 8px !important;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs) !important;
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
  color: var(--color-text-secondary) !important;

  &:hover {
    background: var(--color-bg-tertiary);
  }

  svg {
    flex-shrink: 0;
  }
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

  strong {
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
  }
}

.order-dialog {
  :deep(.el-dialog) {
    border-radius: var(--radius-xl);
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    background: var(--color-primary-navy);
    padding: var(--spacing-lg) var(--spacing-xl);
    margin: 0;

    .el-dialog__title {
      color: #fff;
      font-weight: var(--font-weight-semibold);
      font-size: var(--font-size-md);
    }

    .el-dialog__headerbtn .el-dialog__close {
      color: rgba(255, 255, 255, 0.7);
      font-size: 18px;

      &:hover {
        color: #fff;
      }
    }
  }

  :deep(.el-dialog__body) {
    padding: var(--spacing-xl);
  }

  :deep(.el-dialog__footer) {
    padding: var(--spacing-md) var(--spacing-xl);
    border-top: 1px solid var(--color-border-light);
    background: var(--color-bg-tertiary);
  }
}

.detail-dialog-wrapper {
  :deep(.el-dialog) {
    border-radius: var(--radius-xl);
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    background: var(--color-primary-navy);
    padding: var(--spacing-lg) var(--spacing-xl);
    margin: 0;

    .el-dialog__title {
      color: #fff;
      font-weight: var(--font-weight-semibold);
      font-size: var(--font-size-md);
    }

    .el-dialog__headerbtn .el-dialog__close {
      color: rgba(255, 255, 255, 0.7);

      &:hover {
        color: #fff;
      }
    }
  }

  :deep(.el-dialog__body) {
    padding: var(--spacing-xl);
    max-height: 60vh;
    overflow-y: auto;
  }

  :deep(.el-dialog__footer) {
    padding: var(--spacing-md) var(--spacing-xl);
    border-top: 1px solid var(--color-border-light);
    background: var(--color-bg-tertiary);
  }
}

.dialog-order-preview {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.preview-row {
  display: flex;
  gap: var(--spacing-md);
  padding: 6px 0;

  &:not(:last-child) {
    border-bottom: 1px solid var(--color-border-light);
  }

  .preview-label {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
    width: 75px;
    flex-shrink: 0;
  }

  .preview-value {
    font-size: var(--font-size-sm);
    color: var(--color-text-primary);
    font-weight: var(--font-weight-medium);
  }
}

.dialog-form {
  :deep(.el-form-item__label) {
    font-weight: var(--font-weight-medium);
    color: var(--color-text-primary);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
}

.worker-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.worker-avatar {
  width: 32px;
  height: 32px;
  background: var(--color-primary-blue);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: var(--font-weight-semibold);
  flex-shrink: 0;
}

.worker-info {
  display: flex;
  flex-direction: column;

  .worker-name {
    font-weight: var(--font-weight-medium);
    font-size: var(--font-size-sm);
  }

  .worker-phone {
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
  }
}

.detail-section {
  margin-bottom: var(--spacing-lg);

  &:last-child {
    margin-bottom: 0;
  }

  .section-title {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
    margin: 0 0 var(--spacing-md) 0;
    padding-left: 10px;
    border-left: 3px solid var(--color-primary-blue);
  }
}

.reason-text-inline {
  display: inline-block;
  color: #dc2626;
  background: #fef2f2;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  border-left: 3px solid #ef4444;
  font-size: var(--font-size-sm);
}

@media (max-width: 1024px) {
  .stat-cards-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .order-page {
    padding: var(--spacing-md);
  }

  .hero-content {
    padding: var(--spacing-lg);
  }

  .hero-actions {
    position: static;
    margin-bottom: var(--spacing-md);
  }

  .stat-cards-row {
    grid-template-columns: 1fr 1fr;
    gap: var(--spacing-sm);
  }

  .stat-card-item {
    padding: var(--spacing-sm) var(--spacing-md);
    gap: var(--spacing-sm);

    .stat-icon-circle {
      width: 34px;
      height: 34px;
      border-radius: var(--radius-sm);

      svg {
        width: 16px;
        height: 16px;
      }
    }

    .stat-text .stat-num {
      font-size: var(--font-size-lg);
    }
  }

  .filter-row {
    flex-direction: column;
    gap: var(--spacing-md);
  }

  .filter-group :deep(.el-select),
  .filter-group :deep(.el-input) {
    width: 100% !important;
  }

  .filter-actions {
    flex-wrap: wrap;

    .el-button {
      flex: 1;
    }
  }

  .pagination-card {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: flex-start;
  }
}
</style>
