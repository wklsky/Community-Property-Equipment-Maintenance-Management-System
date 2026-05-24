# 社区物业设备维护管理系统 - 移动端

基于 Uni-app + Vue3 的移动端应用。

## 技术栈

- Uni-app
- Vue 3
- Pinia

## 环境要求

- Node.js 18+
- HBuilderX（可选，用于 App 开发）

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 配置后端地址

编辑 `src/utils/request.js`，修改 `BASE_URL`：

```javascript
const BASE_URL = 'http://localhost:8080/api/v1'
```

### 3. 运行项目

```bash
# H5 开发模式
npm run dev:h5

# 微信小程序
npm run dev:mp-weixin

# App（需要 HBuilderX）
npm run dev:app
```

### 4. 构建

```bash
# H5
npm run build:h5

# 微信小程序
npm run build:mp-weixin

# App
npm run build:app
```

## 功能模块

- 用户登录/登出
- 首页（统计、快捷操作）
- 我的工单（业主）
  - 提交报修
  - 工单列表（查看各状态工单）
  - 工单详情（含拒绝/转派原因展示）
  - 评价/取消
- 工单中心（维修工）
  - 待接单 / 处理中 / 已完成 / 已取消 四个Tab
  - 接单、完成维修
  - 拒绝/转派原因展示
- 公告列表/详情
- 消息中心
- 个人中心

## 目录结构

```
frontend-mobile/
├── pages/              # 页面
│   ├── login/          # 登录
│   ├── index/          # 首页
│   ├── repair/         # 工单
│   ├── notice/         # 公告
│   ├── message/        # 消息
│   └── mine/           # 我的
│── api/                # API 接口
│── store/              # 状态管理
│── utils/              # 工具函数
├── static/             # 静态资源
├── App.vue
├── main.js
├── manifest.json       # 应用配置
├── pages.json          # 页面配置
└── package.json
```

## 运行在 Android 模拟器

1. 安装 HBuilderX
2. 打开项目
3. 运行 -> 运行到手机或模拟器 -> 选择设备

## 测试

1. 确保后端服务已启动
2. 运行 H5 模式：`npm run dev:h5`
3. 访问显示的地址
4. 使用测试账号登录：
   - 管理员：13800000001 / 123456
   - 维修工：13800000002 / 123456
   - 业主：13800000003 / 123456

## 注意事项

- tabbar 图标为 SVG 占位符，实际使用需替换为 PNG 图片
- 移动端需要配置正确的后端地址才能正常访问 API
- 微信小程序需要在微信开发者工具中配置合法域名
