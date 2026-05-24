<template>
  <div class="pro-table">
    <!-- 搜索栏 -->
    <div class="search-bar" v-if="showSearch && searchFields.length">
      <el-form :model="searchForm" :inline="true" @submit.prevent="handleSearch">
        <el-form-item
          v-for="field in searchFields"
          :key="field.prop"
          :label="field.label"
        >
          <!-- 输入框 -->
          <el-input
            v-if="field.type === 'input' || !field.type"
            v-model="searchForm[field.prop]"
            :placeholder="field.placeholder || `请输入${field.label}`"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />

          <!-- 选择框 -->
          <el-select
            v-else-if="field.type === 'select'"
            v-model="searchForm[field.prop]"
            :placeholder="field.placeholder || `请选择${field.label}`"
            clearable
            :multiple="field.multiple"
            :filterable="field.filterable"
            style="width: 180px"
            @change="field.onChange ? field.onChange(searchForm[field.prop]) : null"
          >
            <el-option
              v-for="opt in field.options"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>

          <!-- 远程搜索选择框 -->
          <el-select
            v-else-if="field.type === 'remote-select'"
            v-model="searchForm[field.prop]"
            :placeholder="field.placeholder || `请选择${field.label}`"
            clearable
            filterable
            remote
            :remote-method="field.remoteMethod"
            :loading="field.loading"
            style="width: 180px"
          >
            <el-option
              v-for="opt in field.options"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>

          <!-- 日期选择 -->
          <el-date-picker
            v-else-if="field.type === 'date'"
            v-model="searchForm[field.prop]"
            type="date"
            :placeholder="field.placeholder || `选择${field.label}`"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />

          <!-- 日期范围 -->
          <el-date-picker
            v-else-if="field.type === 'daterange'"
            v-model="searchForm[field.prop]"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            :shortcuts="dateShortcuts"
          />

          <!-- 日期时间范围 -->
          <el-date-picker
            v-else-if="field.type === 'datetimerange'"
            v-model="searchForm[field.prop]"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />

          <!-- 数字输入 -->
          <el-input-number
            v-else-if="field.type === 'number'"
            v-model="searchForm[field.prop]"
            :min="field.min"
            :max="field.max"
            :placeholder="field.placeholder"
            style="width: 180px"
          />

          <!-- 级联选择 -->
          <el-cascader
            v-else-if="field.type === 'cascader'"
            v-model="searchForm[field.prop]"
            :options="field.options"
            :props="field.cascaderProps"
            :placeholder="field.placeholder || `请选择${field.label}`"
            clearable
            style="width: 220px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button v-if="showExport" @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar" v-if="$slots.toolbar || showToolbar">
      <div class="toolbar-left">
        <slot name="toolbar"></slot>
      </div>
      <div class="toolbar-right">
        <!-- 多租户信息展示 -->
        <div class="tenant-info" v-if="showTenantInfo && currentTenant">
          <el-tag type="info" effect="plain">
            <el-icon><OfficeBuilding /></el-icon>
            {{ currentTenant }}
          </el-tag>
        </div>

        <el-button-group v-if="showColumnSetting">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" @click="handleRefresh" />
          </el-tooltip>
          <el-tooltip content="密度">
            <el-dropdown trigger="click" @command="handleDensityChange">
              <el-button :icon="Grid" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="default" :class="{ active: tableDensity === 'default' }">默认</el-dropdown-item>
                  <el-dropdown-item command="medium" :class="{ active: tableDensity === 'medium' }">中等</el-dropdown-item>
                  <el-dropdown-item command="small" :class="{ active: tableDensity === 'small' }">紧凑</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-tooltip>
          <el-tooltip content="列设置">
            <el-button :icon="Setting" @click="showColumnDialog = true" />
          </el-tooltip>
          <el-tooltip content="全屏" v-if="showFullscreen">
            <el-button :icon="isFullscreen ? Minus : FullScreen" @click="toggleFullscreen" />
          </el-tooltip>
        </el-button-group>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-bar" v-if="showSelection && selectedRows.length > 0">
      <span class="selected-info">
        已选择 <span class="count">{{ selectedRows.length }}</span> 项
      </span>
      <slot name="batch" :selection="selectedRows"></slot>
      <el-button link @click="clearSelection">取消选择</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-container" ref="tableContainerRef" :class="{ fullscreen: isFullscreen }">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        :row-key="rowKey"
        :border="border"
        :stripe="stripe"
        :height="computedHeight"
        :max-height="maxHeight"
        :size="tableDensity"
        :empty-text="emptyText"
        :default-sort="defaultSort"
        :row-class-name="rowClassName"
        :cell-class-name="cellClassName"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        @row-click="handleRowClick"
        @row-dblclick="handleRowDblclick"
        v-bind="$attrs"
      >
        <!-- 展开列 -->
        <el-table-column
          v-if="showExpand"
          type="expand"
          width="50"
          fixed="left"
        >
          <template #default="scope">
            <slot name="expand" v-bind="scope"></slot>
          </template>
        </el-table-column>

        <!-- 选择列 -->
        <el-table-column
          v-if="showSelection"
          type="selection"
          width="55"
          align="center"
          fixed="left"
          :selectable="selectable"
        />

        <!-- 序号列 -->
        <el-table-column
          v-if="showIndex"
          type="index"
          label="序号"
          width="60"
          align="center"
          fixed="left"
          :index="indexMethod"
        />

        <!-- 数据列 -->
        <template v-for="col in visibleColumns" :key="col.prop">
          <el-table-column
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
            :min-width="col.minWidth || 100"
            :fixed="col.fixed"
            :sortable="col.sortable"
            :align="col.align || 'left'"
            :header-align="col.headerAlign || col.align || 'left'"
            :show-overflow-tooltip="col.showOverflowTooltip !== false"
            :formatter="col.formatter"
            :class-name="col.className"
          >
            <template #header v-if="col.headerSlot">
              <slot :name="col.headerSlot" :column="col"></slot>
            </template>
            <template #default="scope" v-if="col.slot || col.render || col.tag || col.dict">
              <!-- 自定义插槽 -->
              <slot :name="col.slot || col.prop" v-bind="scope" v-if="col.slot">
              </slot>
              <!-- 渲染函数 -->
              <component
                v-else-if="col.render"
                :is="col.render(scope.row, scope.$index, scope.column)"
              />
              <!-- 标签展示 -->
              <el-tag
                v-else-if="col.tag"
                :type="getTagType(scope.row[col.prop], col.tagMap)"
                :effect="col.tagEffect || 'light'"
              >
                {{ getTagLabel(scope.row[col.prop], col.tagMap) }}
              </el-tag>
              <!-- 字典转换 -->
              <span v-else-if="col.dict">
                {{ getDictLabel(scope.row[col.prop], col.dict) }}
              </span>
            </template>
          </el-table-column>
        </template>

        <!-- 操作列 -->
        <el-table-column
          v-if="$slots.action"
          label="操作"
          :width="actionWidth"
          fixed="right"
          align="center"
        >
          <template #default="scope">
            <slot name="action" v-bind="scope"></slot>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="showPagination">
      <div class="pagination-info">
        共 <span class="total-count">{{ total }}</span> 条数据
      </div>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="pageSizes"
        :layout="paginationLayout"
        :background="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 列设置对话框 -->
    <el-dialog v-model="showColumnDialog" title="列设置" width="450px" destroy-on-close>
      <div class="column-setting">
        <div class="column-setting-header">
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
          >
            全选
          </el-checkbox>
          <el-button link type="primary" @click="resetColumns">重置</el-button>
        </div>
        <el-checkbox-group v-model="checkedColumns" @change="handleCheckedColumnsChange">
          <draggable
            v-model="sortableColumns"
            item-key="prop"
            handle=".drag-handle"
            @end="handleColumnSort"
          >
            <template #item="{ element }">
              <div class="column-item">
                <el-icon class="drag-handle"><Rank /></el-icon>
                <el-checkbox :label="element.prop">{{ element.label }}</el-checkbox>
                <el-checkbox v-model="element.fixed" :true-label="'left'" :false-label="false" size="small">
                  固定
                </el-checkbox>
              </div>
            </template>
          </draggable>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="showColumnDialog = false">取消</el-button>
        <el-button type="primary" @click="handleColumnConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Search, Refresh, Setting, Download, FullScreen, Minus, Grid, Rank, OfficeBuilding } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import draggable from 'vuedraggable'

const props = defineProps({

  data: { type: Array, default: () => [] },

  columns: { type: Array, required: true },

  loading: { type: Boolean, default: false },

  total: { type: Number, default: 0 },

  rowKey: { type: String, default: 'id' },

  border: { type: Boolean, default: false },

  stripe: { type: Boolean, default: true },

  height: { type: [String, Number], default: undefined },

  maxHeight: { type: [String, Number], default: undefined },

  autoHeight: { type: Boolean, default: false },

  showSelection: { type: Boolean, default: false },

  selectable: { type: Function, default: () => true },

  showIndex: { type: Boolean, default: false },

  showExpand: { type: Boolean, default: false },

  showSearch: { type: Boolean, default: true },

  searchFields: { type: Array, default: () => [] },

  showToolbar: { type: Boolean, default: true },

  showColumnSetting: { type: Boolean, default: true },

  showFullscreen: { type: Boolean, default: false },

  showExport: { type: Boolean, default: false },

  showPagination: { type: Boolean, default: true },

  pageSizes: { type: Array, default: () => [10, 20, 50, 100] },

  paginationLayout: { type: String, default: 'total, sizes, prev, pager, next, jumper' },

  actionWidth: { type: [String, Number], default: 200 },

  defaultPageSize: { type: Number, default: 10 },

  emptyText: { type: String, default: '暂无数据' },

  defaultSort: { type: Object, default: () => ({}) },

  rowClassName: { type: [String, Function], default: '' },

  cellClassName: { type: [String, Function], default: '' },

  showTenantInfo: { type: Boolean, default: false },

  dicts: { type: Object, default: () => ({}) }
})

const emit = defineEmits([
  'search',
  'reset',
  'refresh',
  'selection-change',
  'sort-change',
  'page-change',
  'row-click',
  'row-dblclick',
  'export'
])

const userStore = useUserStore()
const currentTenant = computed(() => userStore.userInfo?.tenantName || '')

const tableRef = ref()
const tableContainerRef = ref()

const searchForm = ref({})

const currentPage = ref(1)
const pageSize = ref(props.defaultPageSize)

const selectedRows = ref([])

const showColumnDialog = ref(false)
const checkedColumns = ref([])
const sortableColumns = ref([])
const checkAll = ref(true)
const isIndeterminate = ref(false)

const tableDensity = ref('default')

const isFullscreen = ref(false)

const tableData = computed(() => props.data)

const visibleColumns = computed(() => {
  return sortableColumns.value.filter(col => checkedColumns.value.includes(col.prop))
})

const computedHeight = computed(() => {
  if (isFullscreen.value) {
    return 'calc(100vh - 200px)'
  }
  if (props.autoHeight) {
    return undefined
  }
  return props.height
})

const dateShortcuts = [
  { text: '今天', value: () => [new Date(), new Date()] },
  { text: '最近一周', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 7); return [start, end] } },
  { text: '最近一个月', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 30); return [start, end] } },
  { text: '最近三个月', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 90); return [start, end] } }
]

const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

const initColumns = () => {
  sortableColumns.value = props.columns.map(col => ({ ...col }))
  checkedColumns.value = props.columns.map(col => col.prop)
}

onMounted(() => {
  initColumns()

  props.searchFields.forEach(field => {
    if (field.defaultValue !== undefined) {
      searchForm.value[field.prop] = field.defaultValue
    }
  })
})

watch(() => props.columns, () => {
  initColumns()
}, { deep: true })

const handleSearch = () => {
  currentPage.value = 1
  emitSearch()
}

const handleReset = () => {
  searchForm.value = {}

  props.searchFields.forEach(field => {
    if (field.defaultValue !== undefined) {
      searchForm.value[field.prop] = field.defaultValue
    }
  })
  currentPage.value = 1
  emit('reset')
  emitSearch()
}

const handleRefresh = () => {
  emit('refresh')
  emitSearch()
}

const handleExport = () => {
  emit('export', { ...searchForm.value, pageNum: currentPage.value, pageSize: pageSize.value })
}

const emitSearch = () => {

  const params = { ...searchForm.value }
  props.searchFields.forEach(field => {
    if ((field.type === 'daterange' || field.type === 'datetimerange') && params[field.prop]) {
      const [start, end] = params[field.prop]
      params[field.startProp || `${field.prop}Start`] = start
      params[field.endProp || `${field.prop}End`] = end
      delete params[field.prop]
    }
  })
  emit('search', { ...params, pageNum: currentPage.value, pageSize: pageSize.value })
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

const clearSelection = () => {
  tableRef.value?.clearSelection()
}

const handleSortChange = ({ prop, order }) => {
  emit('sort-change', { prop, order })
}

const handleRowClick = (row, column, event) => {
  emit('row-click', row, column, event)
}

const handleRowDblclick = (row, column, event) => {
  emit('row-dblclick', row, column, event)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  emit('page-change', { pageNum: 1, pageSize: size })
  emitSearch()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  emit('page-change', { pageNum: page, pageSize: pageSize.value })
  emitSearch()
}

const handleDensityChange = (density) => {
  tableDensity.value = density
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const handleCheckAllChange = (val) => {
  checkedColumns.value = val ? props.columns.map(col => col.prop) : []
  isIndeterminate.value = false
}

const handleCheckedColumnsChange = (value) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === props.columns.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < props.columns.length
}

const handleColumnSort = () => {

}

const resetColumns = () => {
  initColumns()
}

const handleColumnConfirm = () => {
  showColumnDialog.value = false
}

const getTagType = (value, tagMap) => {
  if (!tagMap) return 'info'
  const item = tagMap[value]
  return item?.type || 'info'
}

const getTagLabel = (value, tagMap) => {
  if (!tagMap) return value
  const item = tagMap[value]
  return item?.label || value
}

const getDictLabel = (value, dictKey) => {
  const dict = props.dicts[dictKey]
  if (!dict) return value
  const item = dict.find(d => d.value === value)
  return item?.label || value
}

defineExpose({
  tableRef,
  refresh: handleRefresh,
  resetSearch: handleReset,
  search: handleSearch,
  getSearchForm: () => searchForm.value,
  setSearchForm: (form) => { searchForm.value = { ...searchForm.value, ...form } },
  getSelectedRows: () => selectedRows.value,
  clearSelection,
  toggleRowSelection: (row, selected) => tableRef.value?.toggleRowSelection(row, selected),
  setCurrentRow: (row) => tableRef.value?.setCurrentRow(row),
  doLayout: () => tableRef.value?.doLayout()
})
</script>

<style scoped lang="scss">
.pro-table {
  .search-bar {
    margin-bottom: 16px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    :deep(.el-form-item) {
      margin-bottom: 12px;
      margin-right: 16px;
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #fff;
    border-radius: 8px;

    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .tenant-info {
      :deep(.el-tag) {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .batch-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #e6f7ff;
    border: 1px solid #91d5ff;
    border-radius: 8px;

    .selected-info {
      color: #1890ff;

      .count {
        font-weight: 600;
        margin: 0 4px;
      }
    }
  }

  .table-container {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    &.fullscreen {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 2000;
      border-radius: 0;
      padding: 20px;
    }
  }

  .pagination-container {
    margin-top: 16px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .pagination-info {
      font-size: 14px;
      color: #909399;

      .total-count {
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .column-setting {
    .column-setting-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 12px;
      margin-bottom: 12px;
      border-bottom: 1px solid #f0f0f0;
    }

    .column-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .drag-handle {
        cursor: move;
        color: #909399;

        &:hover {
          color: #409eff;
        }
      }

      :deep(.el-checkbox) {
        flex: 1;
      }
    }
  }
}

:deep(.el-dropdown-menu__item.active) {
  color: #409eff;
  background-color: #ecf5ff;
}
</style>
