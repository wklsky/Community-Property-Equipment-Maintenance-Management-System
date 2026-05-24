# 社区物业设备维护管理系统 - 后端

基于 Spring Boot 3.2 + MyBatis Plus 的后端服务。

## 技术栈

- Spring Boot 3.2
- MyBatis Plus 3.5（多租户 + 分页插件）
- MySQL 8.0+
- JWT 认证（Access Token + Refresh Token）
- Spring Security
- AOP 权限切面（@RequirePermission / @RequireRole / @RequireSuperAdmin）

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

## 快速开始

### 1. 初始化数据库

```bash
# 建表
mysql -u root -p < ../community_schema.sql

# 导入初始数据（含3个物业公司 + 超级管理员）
mysql -u root -p < ../community_data.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/property_system
    username: root
    password: your_password
```

### 3. 运行项目

```bash
mvn spring-boot:run
# 或
mvn clean package && java -jar target/property-system-1.0.0.jar
```

服务启动后访问：http://localhost:8080

## 系统角色

| 角色 | 说明 | 权限范围 |
|------|------|----------|
| 超级管理员 | 平台级管理员，不属于任何物业公司 | 管理所有物业公司、跨租户用户管理 |
| 系统管理员 | 物业公司管理员 | 管理本公司用户、设备、工单、公告等 |
| 维修工 | 执行维修与巡检任务 | 查看/接单/完成工单，执行巡检任务 |
| 业主 | 社区居民 | 提交报修、评价工单、查看公告 |

### 预置账号

| 角色 | 手机号 | 密码 | 说明 |
|------|--------|------|------|
| 超级管理员 | 13800000000 | 123456 | 平台级，登录时勾选"超级管理员登录" |
| 万科总管 | 13800001111 | 123456 | 万科物业管理员 |
| 保利总管 | 13800002222 | 123456 | 保利物业管理 |
| 绿城总管 | 13800003333 | 123456 | 绿城物业管理 |

## API 接口

所有接口统一前缀：`/api/v1`

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/login | 登录（超级管理员无需 tenantId） |
| POST | /auth/login-by-code | 验证码登录 |
| POST | /auth/send-code | 发送短信验证码 |
| POST | /auth/reset-password | 重置密码 |
| POST | /auth/refresh | 刷新 Token |
| GET | /public/tenants | 获取租户列表（公开） |

### 超级管理员接口

所有接口需要 `@RequireSuperAdmin` 权限，通过 JWT 中的 `isSuperAdmin` 声明校验。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /admin/tenants | 公司列表（分页，支持 name 筛选） |
| GET | /admin/tenants/{id} | 公司详情 |
| POST | /admin/tenants | 创建公司（自动创建3个角色 + 初始管理员） |
| PUT | /admin/tenants/{id} | 更新公司名称 |
| PUT | /admin/tenants/{id}/status?status= | 启用/禁用公司 |
| GET | /admin/users | 跨租户用户列表（支持 tenantId/roleId 筛选） |
| GET | /admin/roles?tenantId= | 获取指定公司的角色列表 |

### 工单接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /repair-orders | 工单列表 |
| GET | /repair-orders/my?status= | 我的工单 |
| GET | /repair-orders/assigned?status= | 指派工单 |
| GET | /repair-orders/{id} | 工单详情 |
| POST | /repair-orders | 创建工单 |
| POST | /repair-orders/{id}/assign?workerId= | 派单（1→2） |
| POST | /repair-orders/{id}/accept | 接单（2→3） |
| POST | /repair-orders/{id}/complete?processDesc= | 完成（3→4） |
| POST | /repair-orders/{id}/evaluate?rating=&comment= | 评价（4→5） |
| POST | /repair-orders/{id}/cancel | 取消工单 |
| POST | /repair-orders/{id}/transfer?workerId=&reason= | 转派 |
| GET | /repair-orders/export-statistics | 导出统计报表 Excel |

### 工单状态流转

```
业主提交 → [0] 待受理
              ├── 审核通过 → [1] 待派单
              │                └── 派单 → [2] 待处理
              │                             ├── 接单 → [3] 处理中
              │                             │           ├── 完成 → [4] 待评价
              │                             │           │          └── 评价 → [5] 已完成
              │                             │           └── 转派 → [2] 待处理
              │                             └── 取消 → [6] 已取消
              ├── 拒绝 → [6] 已取消
              └── 取消 → [6] 已取消
```

### 设备接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /devices | 设备列表 |
| GET | /devices/{id} | 设备详情 |
| POST | /devices | 创建设备 |
| PUT | /devices/{id} | 更新设备 |
| DELETE | /devices/{id} | 删除设备 |

### 巡检接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /inspections/plans | 巡检计划列表 |
| POST | /inspections/plans | 创建计划 |
| PUT | /inspections/plans/{id} | 更新计划 |
| DELETE | /inspections/plans/{id} | 删除计划 |
| POST | /inspections/plans/{id}/publish | 发布计划 |
| GET | /inspections/tasks | 巡检任务列表 |
| POST | /inspections/tasks/{id}/accept | 接受任务 |
| POST | /inspections/tasks/{id}/complete | 完成任务并记录 |

### 公告接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /notices | 公告列表 |
| GET | /notices/{id} | 公告详情 |
| POST | /notices | 创建公告 |
| PUT | /notices/{id} | 更新公告 |
| DELETE | /notices/{id} | 删除公告 |
| POST | /notices/{id}/publish | 发布公告 |

### 消息接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /messages | 消息列表 |
| POST | /messages/{id}/read | 标记已读 |
| GET | /messages/unread-count | 未读数量 |

### 系统管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /system/users | 用户列表 |
| GET | /system/users/{id} | 用户详情 |
| POST | /system/users | 创建用户（超管可指定 tenantId） |
| PUT | /system/users/{id} | 更新用户 |
| PUT | /system/users/{id}/status?status= | 启用/禁用用户 |
| GET | /system/roles | 角色列表 |

## 多租户架构

- 使用 MyBatis Plus `TenantLineInnerInterceptor` 实现行级租户隔离
- 租户上下文通过 `TenantContextHolder`（ThreadLocal）传递
- JWT Token 中包含 `tenantId` 和 `isSuperAdmin` 声明
- 超级管理员设置 `ignoreTenant = true`，绕过租户隔离
- 跨租户查询的 Mapper 方法使用 `@InterceptorIgnore(tenantLine = "true")`

## 权限控制

- `@RequirePermission`：检查用户是否拥有指定权限码（支持 AND/OR 逻辑）
- `@RequireRole`：检查用户角色（超级管理员自动通过）
- `@RequireSuperAdmin`：仅超级管理员可访问
- 权限码映射见 `UserController.getRolePermissions()`

## 测试用例

```bash
# 超级管理员登录（无需 tenantId）
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800000000","password":"123456"}'

# 普通管理员登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"tenantId":1,"phone":"13800001111","password":"123456"}'

# 使用 Token 访问接口
curl http://localhost:8080/api/v1/repair-orders \
  -H "Authorization: Bearer YOUR_TOKEN"

# 超级管理员查看所有公司
curl http://localhost:8080/api/v1/admin/tenants \
  -H "Authorization: Bearer SUPER_ADMIN_TOKEN"
```
