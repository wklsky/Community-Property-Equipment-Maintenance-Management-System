# 社区物业设备维护管理系统

面向社区物业的设备报修、工单流转、巡检执行与公告消息一体化管理平台。采用 **多端同源、按角色拆分** 的前端架构：一个后台管理端、两个移动端 App（业主端 / 维修工端），后端提供统一的 `RESTful` 接口（`/api/v1`）。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Spring Boot + MySQL（详见 `backend/README.md`） |
| Web 管理端 | Vue 3 + TypeScript + Element Plus + Vite + Axios |
| 移动端 | Uni-app + Vue 3 + Pinia，按 pnpm workspace 拆分为独立 App |

## 仓库结构

```
.
├── backend/              # Spring Boot 服务端（提供 /api/v1 接口）
├── frontend-web/        # Vue3 + TS 管理后台（详见 frontend-web/README.md）
├── frontend-mobile/     # pnpm workspace 移动端 Monorepo（详见 frontend-mobile/README.md）
│   └── packages/
│       ├── shared/      # @community/shared 公共包：登录模块、请求封装、角色常量、用户状态、路由守卫
│       ├── owner-app/   # @community/owner-app 业主端（仅允许"业主"登录）
│       ├── worker-app/  # @community/worker-app 维修工端（仅允许"维修工"登录）
│       └── legacy-app/  # @community/legacy-app 迁移前的混合端，过渡期保留
├── community_schema.sql # 数据库表结构
└── community_data.sql   # 示例数据
```

## 核心设计：多租户 + 按角色隔离

1. **多租户数据隔离**：登录时用户需先选定「社区（租户）」，后端以此约束数据可见范围；前端在登录表单中由用户输入社区名称，解析为 `tenantId` 后提交，**不允许歧义匹配**以避免登录到错误社区。
2. **角色与 App 绑定**：后端登录接口只校验「账号密码是否正确」，并不关心用户打开的是哪个 App。因此 **Token 合法 ≠ 有权进入当前 App**：
   - 登录成功回调中按 `appId` 校验角色，不匹配则**不落盘** Token；
   - 页面跳转时由路由守卫二次拦截，**清除登录态**并强制回登录页。
   - 两处判定共用 `packages/shared` 中的 `checkRoleAllowed()`，规则集中在 `constants/roles.ts` 的 `APP_ALLOWED_ROLES`，避免割裂。
3. **视图与逻辑分离**：Web 端登录页拆分为纯 `UI` 组件与 `useLogin` / `useCaptcha` / `useResetPassword` 等业务 Hook；移动端登录组件复用 `shared` 包，宿主仅声明 `appId` 与跳转策略。

## 快速开始

各子项目均独立运行，详见对应目录的 README：

| 子项目 | 安装 / 运行 |
| --- | --- |
| 后端 | 见 `backend/README.md`（Spring Boot，默认端口 8080） |
| Web 管理端 | `cd frontend-web && npm install && npm run dev`（默认 3000） |
| 移动端 | `cd frontend-mobile && pnpm install && pnpm dev:owner`（5173）/ `pnpm dev:worker`（5174） |

> 移动端真机联调时，App 内的后端地址必须填电脑的局域网 IP（手机上的 `localhost` 指向手机本身）；生产环境需替换为 HTTPS 域名。

## 测试账号

- 管理员：13800000001 / 123456
- 维修工：13800000002 / 123456
- 业主：13800000003 / 123456
