# 社区物业设备维护管理系统 - 移动端 Monorepo

基于 pnpm workspace + Uni-app + Vue3 的移动端工作区。
移动端已按角色拆分为**业主 App** 与**维修工 App**，两者复用 `packages/shared` 中的登录模块与公共能力。

> **迁移状态**：`legacy-app` 的业务页面已全部迁移到双端（业主端 / 维修工端各 11 个路由），
> 业务 API、类型、常量与分页逻辑已下沉到 `packages/shared`。`legacy-app` 仅作过渡期保留，不再新增功能。

## 包结构

```
frontend-mobile/
├── packages/
│   ├── shared/          # @community/shared  公共包：请求封装、角色常量、用户状态、路由守卫 + 全量业务 API / 类型 / 常量 / 分页 Hook
│   ├── owner-app/       # @community/owner-app   业主端（仅允许"业主"角色登录）
│   ├── worker-app/      # @community/worker-app  维修工端（仅允许"维修工"角色登录）
│   └── legacy-app/      # @community/legacy-app  迁移前的混合端，过渡期保留，不再新增功能
├── tsconfig.base.json   # 各包共用的 TS 编译配置
├── pnpm-workspace.yaml  # 工作区声明与依赖构建脚本白名单
└── package.json         # 工作区根，仅含调度脚本
```

### shared 包内部结构

双端共用的能力全部集中于此，业务字段与后端实体 / DTO 严格对齐：

```
shared/src/
├── api/         # 全量业务接口：auth / repair / device / inspection / notice / message / address / common / dashboard
├── types/       # 请求入参与响应结构：auth / common / repair / device / inspection / notice / message / address / dashboard / property
├── constants/   # roles（角色与 App 白名单）、auth（登录阈值）、business（工单 / 设备 / 巡检 / 公告状态枚举）
├── composables/ # useLogin（登录流程）、usePagedList（通用分页列表）
├── utils/       # request（请求封装与无感刷新）、query（参数序列化）、format（时间与状态文案）、role / validate / debounce
├── stores/      # user（登录态与角色）
├── router/      # guard（跨 App 越权拦截）
└── components/  # LoginForm
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

| 能力 | 业主端 | 维修工端 |
| --- | :---: | :---: |
| 登录 / 登出（密码 / 短信验证码，租户手动输入） | ✅ | ✅ |
| 首页看板（工单与巡检概览） | ✅ | ✅ |
| 提交报修 | ✅ | — |
| 我的工单（分状态筛选、触底分页） | ✅ | ✅ |
| 工单详情 | 取消、评价 | 接单、完成 |
| 巡检任务（接单 / 执行并提交记录） | — | ✅ |
| 社区公告 | ✅ | ✅ |
| 消息中心（已读回写） | ✅ | ✅ |
| 设备台账 | ✅ | ✅ |
| 我的地址（系统房产 + 自定义地址） | ✅ | — |

> 「提交报修」与「巡检任务」按后端权限天然分端：`POST /repair-orders` 仅对系统管理员与业主开放，
> 巡检任务仅对系统管理员与维修工开放，因此维修工端不提供报修入口、业主端不提供巡检入口。
> 同理，工单的「转单」与「取消」仅在业主端提供（后端对二者限定了各自的角色）。

### 页面清单

- **业主端**（tabBar：首页 / 工单 / 我的）
  `pages/repair/{create,list,detail}`、`pages/notice/{list,detail}`、`pages/message/list`、`pages/address/list`、`pages/device/list`
- **维修工端**（tabBar：首页 / 工单 / 巡检 / 我的）
  `pages/repair/{list,detail}`、`pages/inspection/{list,execute}`、`pages/notice/{list,detail}`、`pages/message/list`、`pages/device/list`

## 迁移路线

存量 `legacy-app` 中的页面按角色逐步迁移到 `owner-app` / `worker-app`：

1. ✅ **页面迁移已完成**：业务页面已按角色拆分并迁入双端（各 11 个路由），一并从 JavaScript 改写为 TypeScript
2. ✅ **公共能力下沉已完成**：业务 API、类型、常量与分页 Hook 已下沉到 `packages/shared`
3. ⏳ **待执行**：确认双端在真机环境稳定后删除 `packages/legacy-app`

## 开发约定

以下约定均来自实际踩过的坑：**违反时不会报错，但功能会静默失效**，改动前请先阅读。

- **tabBar 页面必须用 `switchTab`**：`navigateTo` 跳转到 tabBar 页面会被 uni 静默丢弃，
  表现为「点击菜单毫无反应」。首页快捷入口与个人中心菜单混合了两类页面，
  统一通过 `config/app.ts` 的 `TAB_BAR_PATHS` 分流。
- **输入一律用页面内表单，不要用 `uni.showModal({ editable: true })`**：
  H5 平台的 `showModal` 不支持输入框，该参数会被直接忽略（地址新增 / 编辑曾因此在 H5 上完全不可用）。
- **列表统一使用 `usePagedList`**：它内置请求序号，切换筛选条件时会作废「先发后至」的过期响应。
  不要自行用 `loading` 给刷新 / 切换加锁，否则新条件的请求会被丢弃，列表停留在旧数据。
- **列表必须渲染 `error` 态**：请求失败时 `isEmpty` 为 `false` 而列表为空，
  不渲染错误态就会得到一片空白且没有任何提示。
- **新增页面必须同步 `pages.json`**：uni-app 不做文件路由，未登记的页面无法跳转。
- **接口参数位置随后端注解而定**：`complete` / `evaluate` / `transfer` / `assign` 走 query（后端 `@RequestParam`），
  而 `completeInspectionTask` 走请求体（`@RequestBody`），写反会导致后端拿不到参数。
- **巡检结果 `result = 0` 表示异常**：后端据此自动生成一张维修工单，
  与「0 通常代表否 / 失败」的直觉相反，改动前务必确认。

## 注意事项

- `packages/shared` 是源码包（入口指向 `src/index.ts`），宿主 App 需保持 `rollupOptions.preserveSymlinks`
- 新增需要执行构建脚本的依赖时，必须登记到 `pnpm-workspace.yaml` 的 `allowBuilds`
- 各 App 的后端地址来自 `.env.development` / `.env.production` 的 `VITE_APP_BASE_URL`，
  未配置时会回退到共享层的默认 `/api/v1`（`configureHttp` 会忽略 `undefined`，不会被静默覆盖成 `undefined`）
- 微信小程序需在开发者工具中配置合法域名
