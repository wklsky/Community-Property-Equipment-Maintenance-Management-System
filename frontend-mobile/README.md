# 社区物业设备维护管理系统 - 移动端 Monorepo

基于 pnpm workspace + Uni-app + Vue3 的移动端工作区。
移动端已按角色拆分为**业主 App** 与**维修工 App**，两者复用 `packages/shared` 中的登录模块与公共能力。

## 包结构

```
frontend-mobile/
├── packages/
│   ├── shared/          # @community/shared  公共包：登录模块、请求封装、角色常量、用户状态、路由守卫
│   ├── owner-app/       # @community/owner-app   业主端（仅允许"业主"角色登录）
│   ├── worker-app/      # @community/worker-app  维修工端（仅允许"维修工"角色登录）
│   └── legacy-app/      # @community/legacy-app  迁移前的混合端，过渡期保留，不再新增功能
├── tsconfig.base.json   # 各包共用的 TS 编译配置
├── pnpm-workspace.yaml  # 工作区声明与依赖构建脚本白名单
└── package.json         # 工作区根，仅含调度脚本
```

## 环境要求

- Node.js 18+
- pnpm 8+（本项目在 pnpm 11 下验证通过）
- HBuilderX（可选，用于 App 打包）

## 快速开始

```bash
# 1. 安装全部工作区依赖（会自动软链 @community/shared 到两个 App）
pnpm install

# 2. 运行
pnpm dev:owner     # 业主端 H5（端口 5173）
pnpm dev:worker    # 维修工端 H5（端口 5174）
pnpm dev:legacy    # 存量混合端 H5

# 3. 构建
pnpm build:owner
pnpm build:worker
pnpm build:mp:owner     # 微信小程序
pnpm build:mp:worker

# 4. 类型检查
pnpm typecheck
```

## 后端地址配置

各 App 通过 `.env.development` / `.env.production` 中的 `VITE_APP_BASE_URL` 注入，
由 `configureHttp()` 在入口处写入共享请求层，不需要修改任何源码。

> 真机联调时必须填写电脑的局域网 IP：手机上的 `localhost` 指向手机本身。

## 越权拦截机制

两个 App 的登录方式完全一致，登录接口只校验「账号密码是否正确」，并不关心用户打开的是哪个 App。
因此 **Token 合法 ≠ 有权进入当前 App**，前端必须在两处拦截：

| 拦截点 | 位置 | 行为 |
| --- | --- | --- |
| 登录成功回调 | `shared/src/composables/useLogin.ts` | 角色不匹配则**不落盘** Token，弹窗提示应去哪个 App |
| 页面跳转守卫 | `shared/src/router/guard.ts` | 角色不匹配则**清除登录态**并强制回登录页 |

两处调用同一个 `checkRoleAllowed()`，规则集中定义在 `shared/src/constants/roles.ts` 的 `APP_ALLOWED_ROLES`，
避免两处判定各自演化出现缝隙。

## 功能模块

- 用户登录/登出（密码登录 / 短信验证码登录，租户手动输入）
- 业主端：提交报修、工单进度跟踪、公告查看
- 维修工端：接单处理、巡检执行、完工上报

## 迁移路线

存量 `legacy-app` 中的页面按角色逐步迁移到 `owner-app` / `worker-app`：

1. 页面级组件迁入对应 App 的 `src/pages/`
2. 与角色无关的 API、Store、工具函数下沉到 `packages/shared`
3. 迁移完成后删除 `packages/legacy-app`

## 注意事项

- `packages/shared` 是源码包（入口指向 `src/index.ts`），宿主 App 需保持 `rollupOptions.preserveSymlinks`
- 新增需要执行构建脚本的依赖时，必须登记到 `pnpm-workspace.yaml` 的 `allowBuilds`
- 微信小程序需在开发者工具中配置合法域名
