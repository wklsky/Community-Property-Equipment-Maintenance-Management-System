<template>
  <div class="dashboard">
    <!-- Bento Grid Layout -->
    <div class="bento-container">
      <!-- Row 1: Welcome + Stats -->
      <div class="bento-row bento-row-1">
        <!-- Welcome Hero Card -->
        <div class="bento-card welcome-card">
          <div class="welcome-bg"></div>
          <div class="welcome-content">
            <div class="welcome-text">
              <span class="welcome-label">{{ greeting }}</span>
              <h1 class="welcome-name">{{ userStore.userInfo.username }}</h1>
              <p class="welcome-desc">欢迎使用智慧社区物业设备维护管理系统</p>
            </div>
            <div class="welcome-meta">
              <div class="date-display">
                <span class="date-day">{{ currentDay }}</span>
                <div class="date-info">
                  <span class="date-month">{{ currentMonth }}</span>
                  <span class="date-week">{{ currentWeek }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Stats Cards -->
        <div class="bento-card stat-card stat-pending" @click="router.push('/repair-orders')">
          <div class="stat-header">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12,6 12,12 16,14"/>
              </svg>
            </div>
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ stats.pending }}</span>
            <span class="stat-label">待处理工单</span>
          </div>
          <div class="stat-glow"></div>
        </div>

        <div class="bento-card stat-card stat-processing" @click="router.push('/repair-orders')">
          <div class="stat-header">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2v4m0 12v4M4.93 4.93l2.83 2.83m8.48 8.48l2.83 2.83M2 12h4m12 0h4M4.93 19.07l2.83-2.83m8.48-8.48l2.83-2.83"/>
              </svg>
            </div>
            <div class="stat-badge">进行中</div>
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ stats.processing }}</span>
            <span class="stat-label">处理中工单</span>
          </div>
          <div class="stat-glow"></div>
        </div>

        <div class="bento-card stat-card stat-completed" @click="router.push('/repair-orders')">
          <div class="stat-header">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22,4 12,14.01 9,11.01"/>
              </svg>
            </div>
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ stats.completed }}</span>
            <span class="stat-label">已完成工单</span>
          </div>
          <div class="stat-glow"></div>
        </div>

        <div class="bento-card stat-card stat-messages" @click="router.push('/messages')">
          <div class="stat-header">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
            </div>
            <div class="stat-badge danger" v-if="stats.unread > 0">{{ stats.unread }}</div>
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ stats.unread }}</span>
            <span class="stat-label">未读消息</span>
          </div>
          <div class="stat-glow"></div>
        </div>
      </div>

      <!-- Row 2: Order Trend Line Chart -->
      <div class="bento-row bento-row-2">
        <div class="bento-card chart-card trend-card">
          <div class="card-header">
            <h3 class="card-title">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <polyline points="2,12 6,7 10,12 14,5 18,12"/>
              </svg>
              近30天工单趋势
            </h3>
          </div>
          <div class="chart-container chart-lg" ref="trendChartRef"></div>
        </div>
      </div>

      <!-- Row 3: Order Status Charts -->
      <div class="bento-row bento-row-3">
        <!-- Order Status Donut Chart -->
        <div class="bento-card chart-card">
          <div class="card-header">
            <h3 class="card-title">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M17 10a7 7 0 1 1-14 0 7 7 0 0 1 14 0z"/>
                <path d="M10 3v7l5 3"/>
              </svg>
              工单状态分布
            </h3>
          </div>
          <div class="chart-container" ref="pieChartRef"></div>
        </div>

        <!-- Order Status Bar Chart -->
        <div class="bento-card chart-card">
          <div class="card-header">
            <h3 class="card-title">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="2" y="12" width="4" height="6" rx="1"/>
                <rect x="8" y="6" width="4" height="12" rx="1"/>
                <rect x="14" y="2" width="4" height="16" rx="1"/>
              </svg>
              工单统计概览
            </h3>
          </div>
          <div class="chart-container" ref="barChartRef"></div>
        </div>
      </div>

      <!-- Row 4: Quick Actions + Map -->
      <div class="bento-row bento-row-4">
        <!-- Quick Actions -->
        <div class="bento-card actions-card">
          <div class="card-header">
            <h3 class="card-title">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="2" y="2" width="7" height="7" rx="1.5"/>
                <rect x="11" y="2" width="7" height="7" rx="1.5"/>
                <rect x="2" y="11" width="7" height="7" rx="1.5"/>
                <rect x="11" y="11" width="7" height="7" rx="1.5"/>
              </svg>
              快捷操作
            </h3>
          </div>

          <div class="action-grid">
            <div class="action-item" @click="router.push('/repair-orders')">
              <div class="action-icon primary">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
              </div>
              <span class="action-text">提交报修</span>
            </div>

            <div class="action-item" @click="router.push('/repair-orders')">
              <div class="action-icon info">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14,2 14,8 20,8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </div>
              <span class="action-text">我的工单</span>
            </div>

            <div class="action-item" @click="router.push('/notices')">
              <div class="action-icon warning">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                </svg>
              </div>
              <span class="action-text">公告通知</span>
            </div>

            <div class="action-item" @click="router.push('/messages')">
              <div class="action-icon success">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
              </div>
              <span class="action-text">消息中心</span>
            </div>
          </div>
        </div>

        <!-- Community Buildings Widget -->
        <div class="bento-card map-card">
          <div class="card-header">
            <h3 class="card-title">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <polygon points="1,6 1,18 7,15 13,18 19,15 19,3 13,6 7,3"/>
                <line x1="7" y1="3" x2="7" y2="15"/>
                <line x1="13" y1="6" x2="13" y2="18"/>
              </svg>
              社区楼栋
            </h3>
            <span class="card-badge success" v-if="buildings.length > 0">{{ buildings.length }} 栋</span>
          </div>

          <div class="map-container">
            <div class="building-grid" v-if="buildings.length > 0">
              <div class="building-card" v-for="(b, i) in buildings" :key="b.id" :style="{ animationDelay: i * 0.1 + 's' }">
                <div class="building-icon" :class="'icon-' + (i % 4)">
                  <svg width="28" height="28" viewBox="0 0 28 28" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="8" width="22" height="18" rx="2"/>
                    <rect x="7" y="4" width="14" height="4" rx="1"/>
                    <line x1="10" y1="14" x2="18" y2="14"/>
                    <line x1="10" y1="18" x2="18" y2="18"/>
                    <line x1="10" y1="22" x2="14" y2="22"/>
                  </svg>
                </div>
                <span class="building-name">{{ b.name }}</span>
                <span class="building-id">#{{ b.id }}</span>
              </div>
            </div>

            <div class="empty-buildings" v-else>
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3">
                <rect x="6" y="14" width="36" height="28" rx="3"/>
                <rect x="14" y="8" width="20" height="6" rx="2"/>
              </svg>
              <span>暂无楼栋数据</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Row 5: Recent Orders -->
      <div class="bento-row bento-row-5">
        <div class="bento-card orders-card">
          <div class="card-header">
            <h3 class="card-title">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M9 5H7a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/>
                <rect x="7" y="1" width="6" height="4" rx="1"/>
              </svg>
              最近工单
            </h3>
            <el-button class="view-all-btn" @click="router.push('/repair-orders')">
              查看全部
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 12l4-4-4-4"/>
              </svg>
            </el-button>
          </div>

          <div class="orders-table" v-loading="loading">
            <el-table :data="recentOrders" :show-header="true" style="width: 100%">
              <el-table-column prop="orderNo" label="工单号" width="180">
                <template #default="{ row }">
                  <span class="order-no-link">{{ row.orderNo }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
              <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <div class="status-badge" :class="getStatusClass(row.status)">
                    <span class="status-dot"></span>
                    <span>{{ getStatusText(row.status) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180">
                <template #default="{ row }">
                  <span class="time-text">{{ row.createTime }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="router.push(`/repair-orders/${row.id}`)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="empty-state" v-if="recentOrders.length === 0 && !loading">
              <div class="empty-icon">
                <svg width="64" height="64" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="12" y="8" width="40" height="48" rx="4"/>
                  <line x1="20" y1="20" x2="44" y2="20"/>
                  <line x1="20" y1="28" x2="44" y2="28"/>
                  <line x1="20" y1="36" x2="36" y2="36"/>
                </svg>
              </div>
              <p class="empty-text">暂无工单记录</p>
              <el-button type="primary" @click="router.push('/repair-orders')">提交报修</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/dashboard'
import { getUnreadCount } from '@/api/message'
import { getBuildings } from '@/api/common'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const pieChartRef = ref(null)
const trendChartRef = ref(null)
const barChartRef = ref(null)
let pieChartInstance = null
let trendChartInstance = null
let barChartInstance = null
let chartResizeHandler = null

const stats = ref({
  pending: 0,
  processing: 0,
  completed: 0,
  unread: 0
})

const orderStatsForChart = ref({
  pendingAccept: 0,
  pendingAssign: 0,
  pending: 0,
  processing: 0,
  pendingEvaluate: 0,
  completed: 0,
  cancelled: 0
})

const orderTrend = ref([])
const recentOrders = ref([])
const buildings = ref([])

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

const currentDay = computed(() => {
  return new Date().getDate()
})

const currentMonth = computed(() => {
  const months = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
  return months[new Date().getMonth()]
})

const currentWeek = computed(() => {
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weeks[new Date().getDay()]
})

const statusMap = {
  0: '待受理', 1: '待派单', 2: '待处理', 3: '处理中', 4: '待评价', 5: '已完成', 6: '已取消'
}

const getStatusText = (status) => statusMap[status] || '未知'

const getStatusClass = (status) => {
  const classes = {
    0: 'warning', 1: 'warning', 2: 'warning',
    3: 'processing', 4: 'success', 5: 'success', 6: 'danger'
  }
  return classes[status] || ''
}

onMounted(async () => {
  loading.value = true
  try {
    const [dashboardRes, unreadRes] = await Promise.all([
      getDashboardStats(),
      getUnreadCount()
    ])

    const d = dashboardRes.data
    if (d) {
      stats.value.pending = (d.orderStats?.pendingAccept || 0) +
                           (d.orderStats?.pendingAssign || 0) +
                           (d.orderStats?.pending || 0)
      stats.value.processing = d.orderStats?.processing || 0
      stats.value.completed = d.orderStats?.completed || 0

      orderTrend.value = d.orderTrend || []

      if (d.recentOrders && d.recentOrders.length > 0) {
        recentOrders.value = d.recentOrders
      }
    }

    stats.value.unread = unreadRes.data || 0

    loadBuildings()

    if (d?.orderStats) {
      orderStatsForChart.value = {
        pendingAccept: d.orderStats.pendingAccept || 0,
        pendingAssign: d.orderStats.pendingAssign || 0,
        pending: d.orderStats.pending || 0,
        processing: d.orderStats.processing || 0,
        pendingEvaluate: d.orderStats.pendingEvaluate || 0,
        completed: d.orderStats.completed || 0,
        cancelled: d.orderStats.cancelled || 0
      }
    }

    await nextTick()
    initPieChart()
    initTrendChart()
    initBarChart()
  } catch (e) {
    console.error('加载仪表盘数据失败', e)
  } finally {
    loading.value = false
  }
})

const loadBuildings = async () => {
  try {
    const res = await getBuildings()
    buildings.value = res.data || []
  } catch (e) {
    console.error('加载楼栋数据失败:', e)
  }
}

const initPieChart = () => {
  if (!pieChartRef.value) return

  if (pieChartInstance) {
    pieChartInstance.dispose()
  }

  pieChartInstance = echarts.init(pieChartRef.value)

  const data = [
    { value: orderStatsForChart.value.completed, name: '已完成', itemStyle: { color: '#10B981' } },
    { value: orderStatsForChart.value.processing, name: '处理中', itemStyle: { color: '#0066FF' } },
    { value: orderStatsForChart.value.pending, name: '待处理', itemStyle: { color: '#3B82F6' } },
    { value: orderStatsForChart.value.pendingEvaluate, name: '待评价', itemStyle: { color: '#8B5CF6' } },
    { value: orderStatsForChart.value.pendingAccept, name: '待受理', itemStyle: { color: '#F59E0B' } },
    { value: orderStatsForChart.value.pendingAssign, name: '待派单', itemStyle: { color: '#F97316' } },
    { value: orderStatsForChart.value.cancelled, name: '已取消', itemStyle: { color: '#EF4444' } }
  ].filter(item => item.value > 0)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 单 ({d}%)',
      backgroundColor: 'rgba(30, 41, 59, 0.9)',
      borderColor: 'transparent',
      textStyle: { color: '#fff', fontSize: 13 }
    },
    series: [
      {
        name: '工单状态',
        type: 'pie',
        radius: ['55%', '82%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 3
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}\n{d}%',
          fontSize: 11,
          color: '#64748B'
        },
        labelLine: {
          length: 20,
          length2: 30,
          lineStyle: { color: '#CBD5E1' }
        },
        emphasis: {
          label: { fontSize: 16, fontWeight: 'bold' },
          scaleSize: 8
        },
        data
      }
    ]
  }

  pieChartInstance.setOption(option)

  chartResizeHandler = () => {
    pieChartInstance?.resize()
  }
  window.addEventListener('resize', chartResizeHandler)
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  if (trendChartInstance) trendChartInstance.dispose()

  trendChartInstance = echarts.init(trendChartRef.value)
  const dates = orderTrend.value.map(item => item.date.substring(5))
  const counts = orderTrend.value.map(item => item.count)
  const completed = orderTrend.value.map(item => item.completed)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 41, 59, 0.9)',
      borderColor: 'transparent',
      textStyle: { color: '#fff', fontSize: 13 }
    },
    legend: {
      data: ['新建工单', '已完成'],
      bottom: 0,
      textStyle: { color: '#64748B', fontSize: 12 }
    },
    grid: { top: 20, right: 30, bottom: 40, left: 50 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#E2E8F0' } },
      axisTick: { show: false },
      axisLabel: { color: '#94A3B8', fontSize: 10, rotate: 45 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#F1F5F9' } },
      axisLabel: { color: '#94A3B8', fontSize: 11 }
    },
    series: [
      {
        name: '新建工单',
        type: 'line',
        data: counts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#0066FF', width: 3 },
        itemStyle: { color: '#0066FF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 102, 255, 0.2)' },
            { offset: 1, color: 'rgba(0, 102, 255, 0.02)' }
          ])
        }
      },
      {
        name: '已完成',
        type: 'line',
        data: completed,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#10B981', width: 3 },
        itemStyle: { color: '#10B981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.2)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.02)' }
          ])
        }
      }
    ]
  }
  trendChartInstance.setOption(option)
}

const initBarChart = () => {
  if (!barChartRef.value) return
  if (barChartInstance) barChartInstance.dispose()

  barChartInstance = echarts.init(barChartRef.value)

  const categories = ['待受理', '待派单', '待处理', '处理中', '待评价', '已完成', '已取消']
  const values = [
    orderStatsForChart.value.pendingAccept,
    orderStatsForChart.value.pendingAssign,
    orderStatsForChart.value.pending,
    orderStatsForChart.value.processing,
    orderStatsForChart.value.pendingEvaluate,
    orderStatsForChart.value.completed,
    orderStatsForChart.value.cancelled
  ]
  const colors = ['#F59E0B', '#F97316', '#3B82F6', '#0066FF', '#8B5CF6', '#10B981', '#EF4444']

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 41, 59, 0.9)',
      borderColor: 'transparent',
      textStyle: { color: '#fff', fontSize: 13 },
      formatter: '{b}: {c} 单'
    },
    grid: { top: 10, right: 30, bottom: 20, left: 20 },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: '#E2E8F0' } },
      axisTick: { show: false },
      axisLabel: { color: '#64748B', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#F1F5F9' } },
      axisLabel: { color: '#94A3B8', fontSize: 11 }
    },
    series: [{
      type: 'bar',
      data: values.map((v, i) => ({
        value: v,
        itemStyle: {
          color: colors[i],
          borderRadius: [4, 4, 0, 0]
        }
      })),
      barWidth: '55%',
      label: {
        show: true,
        position: 'top',
        color: '#64748B',
        fontSize: 11,
        formatter: p => p.value > 0 ? p.value : ''
      }
    }]
  }
  barChartInstance.setOption(option)
}

onUnmounted(() => {
  if (chartResizeHandler) {
    window.removeEventListener('resize', chartResizeHandler)
  }
  if (pieChartInstance) {
    pieChartInstance.dispose()
    pieChartInstance = null
  }
  if (trendChartInstance) {
    trendChartInstance.dispose()
    trendChartInstance = null
  }
  if (barChartInstance) {
    barChartInstance.dispose()
    barChartInstance = null
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: var(--spacing-lg);
  background: var(--color-bg-primary);
  min-height: 100%;
}

.bento-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.bento-row {
  display: grid;
  gap: var(--spacing-lg);
}

.bento-row-1 {
  grid-template-columns: 2fr repeat(4, 1fr);

  @media (max-width: 1400px) {
    grid-template-columns: 1fr 1fr 1fr;
    .welcome-card { grid-column: span 3; }
    .stat-card { min-height: 120px; }
  }

  @media (max-width: 1024px) {
    grid-template-columns: 1fr 1fr;
    .welcome-card { grid-column: span 2; }
  }

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    .welcome-card { grid-column: span 1; }
  }
}

.bento-row-2 {
  grid-template-columns: 1fr;
}

.bento-row-3 {
  grid-template-columns: 1fr 1fr;

  @media (max-width: 992px) {
    grid-template-columns: 1fr;
    .chart-container { height: 280px; }
  }
}

.bento-row-4 {
  grid-template-columns: 1fr 1fr;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.bento-row-5 {
  grid-template-columns: 1fr;
}

.bento-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  overflow: hidden;

  &:hover {
    box-shadow: var(--shadow-lg);
    border-color: var(--color-border-medium);
  }
}

.welcome-card {
  position: relative;
  padding: var(--spacing-xl);
  background: linear-gradient(135deg, var(--color-primary-navy) 0%, #334155 100%);
  overflow: hidden;

  .welcome-bg {
    position: absolute;
    top: 0;
    right: 0;
    width: 300px;
    height: 100%;
    background: radial-gradient(circle at 80% 50%, rgba(0, 102, 255, 0.15) 0%, transparent 60%);
  }

  .welcome-content {
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;

    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--spacing-lg);
    }
  }

  .welcome-label {
    display: block;
    font-size: var(--font-size-sm);
    color: rgba(255, 255, 255, 0.6);
    margin-bottom: 4px;
  }

  .welcome-name {
    font-size: var(--font-size-2xl);
    font-weight: var(--font-weight-bold);
    color: #fff;
    margin: 0 0 8px 0;
  }

  .welcome-desc {
    font-size: var(--font-size-sm);
    color: rgba(255, 255, 255, 0.7);
    margin: 0;
  }

  .date-display {
    display: flex;
    align-items: center;
    gap: 12px;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    padding: 12px 20px;
    border-radius: var(--radius-lg);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .date-day {
    font-size: 36px;
    font-weight: var(--font-weight-bold);
    color: #fff;
    line-height: 1;
  }

  .date-info {
    display: flex;
    flex-direction: column;
  }

  .date-month {
    font-size: var(--font-size-sm);
    color: rgba(255, 255, 255, 0.8);
    font-weight: var(--font-weight-medium);
  }

  .date-week {
    font-size: var(--font-size-xs);
    color: rgba(255, 255, 255, 0.5);
  }
}

.stat-card {
  padding: var(--spacing-lg);
  cursor: pointer;
  position: relative;
  overflow: hidden;

  .stat-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--spacing-md);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;

    svg {
      width: 24px;
      height: 24px;
    }
  }

  .stat-trend {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: var(--font-size-xs);
    font-weight: var(--font-weight-semibold);

    &.up {
      color: var(--color-status-normal);
    }

    &.down {
      color: var(--color-status-danger);
    }
  }

  .stat-badge {
    font-size: var(--font-size-xs);
    padding: 2px 8px;
    border-radius: var(--radius-full);
    font-weight: var(--font-weight-medium);

    &.danger {
      background: var(--color-status-danger);
      color: #fff;
    }
  }

  .stat-body {
    position: relative;
    z-index: 1;
  }

  .stat-value {
    display: block;
    font-size: 32px;
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
    line-height: 1.2;
  }

  .stat-label {
    display: block;
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
    margin-top: 4px;
  }

  .stat-glow {
    position: absolute;
    bottom: -20px;
    right: -20px;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    opacity: 0.1;
    transition: all var(--transition-normal);
  }

  &:hover .stat-glow {
    opacity: 0.2;
    transform: scale(1.2);
  }

  &.stat-pending {
    .stat-icon {
      background: var(--color-status-warning-bg);
      color: var(--color-status-warning);
    }
    .stat-glow {
      background: var(--color-status-warning);
    }
  }

  &.stat-processing {
    .stat-icon {
      background: var(--color-status-info-bg);
      color: var(--color-status-info);
    }
    .stat-badge {
      background: var(--color-status-info-bg);
      color: var(--color-status-info);
    }
    .stat-glow {
      background: var(--color-status-info);
    }
  }

  &.stat-completed {
    .stat-icon {
      background: var(--color-status-normal-bg);
      color: var(--color-status-normal);
    }
    .stat-glow {
      background: var(--color-status-normal);
    }
  }

  &.stat-messages {
    .stat-icon {
      background: var(--color-primary-blue-light);
      color: var(--color-primary-blue);
    }
    .stat-glow {
      background: var(--color-primary-blue);
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border-light);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0;

  svg {
    color: var(--color-text-secondary);
  }
}

.card-badge {
  font-size: var(--font-size-xs);
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-weight: var(--font-weight-medium);

  &.success {
    background: var(--color-status-normal-bg);
    color: var(--color-status-normal);
  }

  &.warning {
    background: var(--color-status-warning-bg);
    color: var(--color-status-warning);
  }
}

.chart-card {
  .chart-container {
    width: 100%;
    height: 340px;
    padding: var(--spacing-md);
  }

  .chart-lg {
    height: 360px;
  }
}

.trend-card {
  .card-header {
    border-bottom: none;
    padding-bottom: 0;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.actions-card {
  .action-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-md);
    padding: var(--spacing-lg);
  }

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-sm);
    padding: var(--spacing-lg);
    border-radius: var(--radius-lg);
    cursor: pointer;
    transition: all var(--transition-fast);

    &:hover {
      background: var(--color-bg-tertiary);
      transform: translateY(-2px);
    }
  }

  .action-icon {
    width: 52px;
    height: 52px;
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;

    &.primary {
      background: linear-gradient(135deg, var(--color-primary-blue) 0%, #0052CC 100%);
      box-shadow: 0 4px 12px rgba(0, 102, 255, 0.3);
    }

    &.info {
      background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }

    &.warning {
      background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
      box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
    }

    &.success {
      background: linear-gradient(135deg, #10B981 0%, #059669 100%);
      box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
    }
  }

  .action-text {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
    font-weight: var(--font-weight-medium);
  }
}

.map-card {
  .map-container {
    padding: var(--spacing-lg);
    min-height: 180px;
  }

  .building-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: var(--spacing-md);
  }

  .building-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-sm);
    padding: var(--spacing-lg) var(--spacing-md);
    background: var(--color-bg-tertiary);
    border-radius: var(--radius-lg);
    border: 1px solid var(--color-border-light);
    transition: all var(--transition-fast);
    animation: fadeInUp 0.4s ease both;
    cursor: pointer;

    &:hover {
      border-color: var(--color-primary-blue);
      box-shadow: 0 4px 12px rgba(0, 102, 255, 0.1);
      transform: translateY(-2px);
    }
  }

  .building-icon {
    width: 52px;
    height: 52px;
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;

    &.icon-0 {
      background: linear-gradient(135deg, #E0E7FF 0%, #C7D2FE 100%);
      color: #4F46E5;
    }
    &.icon-1 {
      background: linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%);
      color: #059669;
    }
    &.icon-2 {
      background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
      color: #D97706;
    }
    &.icon-3 {
      background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
      color: #DC2626;
    }
  }

  .building-name {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
  }

  .building-id {
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
  }

  .empty-buildings {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-md);
    padding: var(--spacing-2xl);
    color: var(--color-text-tertiary);
    font-size: var(--font-size-sm);
  }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.orders-card {
  .view-all-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: var(--font-size-sm);
    color: var(--color-primary-blue);
    background: transparent;
    border: none;
    padding: 0;

    &:hover {
      color: var(--color-primary-blue-hover);
    }
  }

  .orders-table {
    padding: var(--spacing-md);
  }

  .order-no-link {
    color: var(--color-primary-blue);
    font-weight: var(--font-weight-medium);
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }

  .status-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    border-radius: var(--radius-full);
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-medium);

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: currentColor;
    }

    &.warning {
      background: var(--color-status-warning-bg);
      color: var(--color-status-warning);
    }

    &.processing {
      background: var(--color-status-info-bg);
      color: var(--color-status-info);

      .status-dot {
        animation: pulse 1.5s infinite;
      }
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

  .time-text {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: var(--spacing-2xl);

    .empty-icon {
      color: var(--color-text-tertiary);
      opacity: 0.5;
      margin-bottom: var(--spacing-md);
    }

    .empty-text {
      font-size: var(--font-size-base);
      color: var(--color-text-tertiary);
      margin-bottom: var(--spacing-lg);
    }
  }
}
</style>
