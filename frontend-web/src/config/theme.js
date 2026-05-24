export const themeConfig = {

  primaryColor: '#667eea',

  gradientStart: '#667eea',
  gradientEnd: '#764ba2',

  successColor: '#00b894',

  warningColor: '#ff9800',

  dangerColor: '#f44336',

  infoColor: '#2196f3',

  textPrimary: '#303133',
  textRegular: '#606266',
  textSecondary: '#909399',
  textPlaceholder: '#c0c4cc',

  borderBase: '#dcdfe6',
  borderLight: '#e4e7ed',
  borderLighter: '#ebeef5',
  borderExtraLight: '#f2f6fc',

  bgBase: '#f0f2f5',
  bgLight: '#f5f7fa',
  bgLighter: '#fafafa',
  bgWhite: '#ffffff',

  shadowBase: '0 2px 8px rgba(0, 0, 0, 0.06)',
  shadowLight: '0 4px 12px rgba(0, 0, 0, 0.08)',
  shadowMedium: '0 8px 24px rgba(0, 0, 0, 0.1)',

  borderRadiusSmall: '4px',
  borderRadiusBase: '8px',
  borderRadiusMedium: '12px',
  borderRadiusLarge: '16px',

  sidebarWidth: '240px',
  sidebarCollapsedWidth: '64px',
  sidebarBgColor: '#1a1f36',

  headerHeight: '70px',
  headerBgColor: '#ffffff'
}

export const statusColors = {

  order: {
    0: { color: '#2196f3', bg: '#e3f2fd', label: '待受理' },
    1: { color: '#ff9800', bg: '#fff3e0', label: '待派单' },
    2: { color: '#ff9800', bg: '#fff3e0', label: '待处理' },
    3: { color: '#2196f3', bg: '#e3f2fd', label: '处理中' },
    4: { color: '#4caf50', bg: '#e8f5e9', label: '待评价' },
    5: { color: '#4caf50', bg: '#e8f5e9', label: '已完成' },
    6: { color: '#f44336', bg: '#ffebee', label: '已取消' }
  },

  device: {
    1: { color: '#4caf50', bg: '#e8f5e9', label: '正常' },
    2: { color: '#f44336', bg: '#ffebee', label: '故障' },
    3: { color: '#ff9800', bg: '#fff3e0', label: '维修中' },
    4: { color: '#909399', bg: '#f4f4f5', label: '停用' }
  },

  inspection: {
    0: { color: '#2196f3', bg: '#e3f2fd', label: '待接单' },
    1: { color: '#ff9800', bg: '#fff3e0', label: '进行中' },
    2: { color: '#4caf50', bg: '#e8f5e9', label: '已完成' }
  },

  notice: {
    0: { color: '#909399', bg: '#f4f4f5', label: '草稿' },
    1: { color: '#4caf50', bg: '#e8f5e9', label: '已发布' },
    2: { color: '#ff9800', bg: '#fff3e0', label: '定时发布' }
  }
}

export function getStatusConfig(type, status) {
  return statusColors[type]?.[status] || { color: '#909399', bg: '#f4f4f5', label: '未知' }
}

export const priorityConfig = {
  0: { color: '#4caf50', label: '普通' },
  1: { color: '#f44336', label: '紧急' }
}

export default themeConfig
