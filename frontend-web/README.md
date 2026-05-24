# 社区物业设备维护管理系统 - Web前端

基于 Vue3 + Element Plus 的管理后台。

## 技术栈

- Vue 3.4
- Vue Router 4
- Pinia
- Element Plus
- Axios
- Vite 5

## 环境要求

- Node.js 18+
- npm 或 yarn

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 配置后端地址

开发环境已配置代理，默认指向 `http://localhost:8080`。

如需修改，编辑 `vite.config.js`：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 3. 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:3000

### 4. 构建生产版本

```bash
npm run build
```

构建产物在 `dist` 目录。

## 功能模块

- 用户登录/登出
- 首页仪表盘
- 工单管理（管理员）
  - 审核通过 / 拒绝 / 派单 / 转派 / 取消
  - 按状态、优先级、工单号、楼栋筛选
  - 工单详情查看
  - 导出工单（单个 / 当前列表 / 统计报表）
- 我的工单（业主/维修工）
  - 提交报修、接单、完成、评价、取消
- 设备管理（管理员）
- 巡检管理（管理员/维修工）
- 公告管理
- 消息中心

## 目录结构

```
frontend-web/
├── src/
│   ├── api/          # API 接口
│   ├── assets/       # 静态资源
│   ├── components/   # 公共组件
│   ├── router/       # 路由配置
│   ├── store/        # 状态管理
│   ├── utils/        # 工具函数
│   ├── views/        # 页面组件
│   ├── App.vue
│   └── main.js
├── index.html
├── package.json
└── vite.config.js
```

## 测试

1. 确保后端服务已启动
2. 访问 http://localhost:3000
3. 使用测试账号登录：
   - 管理员：13800000001 / 123456
   - 维修工：13800000002 / 123456
   - 业主：13800000003 / 123456
