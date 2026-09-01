# 社区物业设备维护管理系统 - Web 管理端

基于 Vue3 + TypeScript + Element Plus 的物业管理后台。

## 技术栈

- Vue 3.4
- TypeScript
- Vue Router 4
- Pinia
- Element Plus
- Axios（请求封装见 `src/utils/request.js`）
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

开发环境已配置代理，默认指向 `http://localhost:8080`：

```javascript
// vite.config.js
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

### 4. 构建与类型检查

```bash
npm run build      # 生产构建，产物在 dist/
npm run typecheck  # vue-tsc 严格类型检查（提交前必跑）
```

## 登录模块（视图与逻辑分离）

登录页严格遵循「视图层（UI）与业务逻辑（Hooks）分离」原则，组件本身只负责布局与事件绑定：

```
src/views/login/
├── useLogin.ts            # 核心业务 Hook：租户解析、模式切换、表单校验、验证码流程、登录请求、异常映射
├── useCaptcha.ts          # 图形验证码生成（canvas）与校验，对外只暴露 dataURL 与校验结果
├── useResetPassword.ts    # 忘记密码逻辑（保留原有功能）
├── CaptchaDialog.vue      # 验证码弹窗（纯 UI）
├── ResetPasswordDialog.vue# 忘记密码弹窗（自身持有 Hook）
└── index.ts               # 统一出口
```

### 两种登录模式

- **模式 A（密码登录）**：填写「社区 + 手机号 + 密码」，点击登录后先弹出图形验证码，校验通过才请求登录。
- **模式 B（短信登录）**：填写「社区 + 手机号 + 短信验证码」，点击登录直接完成登录；短信验证码本身即第二因子，不再叠加图形验证码。

### 关键设计

- **租户手动输入**：后端 `/auth/login` 只接受 `tenantId`（Long），因此用户在输入框键入社区名称，由 `resolveTenant()` 完成「文本 → ID」解析，**不允许歧义匹配**（同名/多命中直接报错），避免登录到错误社区造成数据越权。
- **点击登录防抖**：首次点击前沿触发立即响应，400ms 内的重复点击被丢弃，避免重复提交。
- **验证码可用性降级**：当浏览器禁用 canvas 导致拿不到验证码图片时，自动跳过图形验证直接登录，避免用户被看不见的验证码永久挡在登录页外。
- **异常拦截与 Toast**：请求层把后端业务码挂在异常实例上透传，登录页按业务码给出差异化提示（租户不存在 / 密码错误 / 账号禁用等），密码错误额外清空输入框避免重复提交错误凭据。

## 功能模块

- 用户登录/登出
- 首页仪表盘
- 工单管理（管理员）：审核、派单、转派、取消、筛选、详情、导出
- 我的工单（业主/维修工）：提交报修、接单、完成、评价、取消
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
│   ├── constants/    # 常量（登录阈值、错误码映射等）
│   ├── router/       # 路由配置
│   ├── store/        # 状态管理（Pinia）
│   ├── types/        # TS 类型契约（auth / http 等）
│   ├── utils/        # 工具函数（request.js、debounce.ts 等）
│   ├── views/        # 页面组件
│   │   └── login/    # 登录模块（UI 与 Hook 分离）
│   ├── App.vue
│   └── main.js
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.js
```

## 测试

1. 确保后端服务已启动
2. 访问 http://localhost:3000
3. 使用测试账号登录：
   - 管理员：13800000001 / 123456
   - 维修工：13800000002 / 123456
   - 业主：13800000003 / 123456
