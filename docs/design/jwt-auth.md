# JWT 认证流程

## 概述


本页面详细记录了 BDAI-SC 中完整的身份验证生命周期——从用户注册、JWT 签发、请求级别的身份验证，到会话失效与强制登出。

## 架构概览

身份验证系统分为四层：

1. **Spring Security 过滤器链** — 安全策略执行
2. **JwtService** — Token 生成与解析
3. **AuthService** — 凭证业务逻辑
4. **前端 Axios 拦截器 + Vue Router 守卫** — 客户端状态管理

> 来源：SecurityConfig.java, JwtService.java

## 后端安全配置

`SecurityConfig.java` 为整个应用建立了基础的安全策略：

- CSRF 保护被显式禁用（第 27 行）
- 会话管理设为 `STATELESS`（第 28 行）
- 自定义 `authenticationEntryPoint` 返回 JSON 错误 `{"success":false,"message":"未登录或登录已过期"}`（第 29-33 行）

端点白名单：
- `POST /api/auth/register`
- `POST /api/auth/login`
- `/uploads/**`
- `/api/achievements/**`

> 来源：SecurityConfig.java

## JWT Token 生成与解析

### Token 结构

| 组件 | 字段 | 来源 | 示例 |
|------|------|------|------|
| Header | alg | 固定 | HS256 |
| Payload | sub | username 参数 | 2024001 |
| Payload | displayName | displayName 参数 | 张三 |
| Payload | role | role 参数 | STUDENT |
| Payload | iat | 签发时间 | Unix 时间戳 |
| Payload | exp | now + expiresMinutes | Unix 时间戳 (+120 min) |
| Signature | HMAC-SHA | security.jwt.secret | Base64 编码 |

`JwtService.java` 使用 HMAC-SHA 签名，密钥通过 `Keys.hmacShaKeyFor()` 从配置密钥派生。

> 来源：JwtService.java, application.yml

## 请求级别的身份验证过滤器

`JwtAuthenticationFilter` 继承 `OncePerRequestFilter`：

1. 读取 `Authorization` 请求头
2. 若缺失或非 "Bearer " 开头，立即放行
3. 解析 Token，提取 `sub` 和 `role` 声明
4. 使用 `ROLE_` 前缀构造 `SimpleGrantedAuthority`
5. 将 `UsernamePasswordAuthenticationToken` 存储在 `SecurityContextHolder` 中

::: tip 重要
第 50 行权限上的 `ROLE_` 前缀至关重要。Spring Security 基于表达式的访问控制会自动添加 `ROLE_` 前缀。
:::

> 来源：JwtAuthenticationFilter.java

## 注册流程

`POST /api/auth/register` 在单个 `@Transactional` 方法中：

1. 检查 `displayName` 是否非空
2. 验证 username 格式（`^[a-zA-Z0-9_]{4,32}$`）
3. 验证用户名唯一性
4. BCrypt 编码密码
5. 创建关联的 `StudentProfile` 实体
6. 生成 JWT 并返回

`RegisterRequest` 验证约束：
- `displayName`: `@NotBlank`, `@Size(2-64)`
- `username`: `@NotBlank`, `@Size(4-32)`, `@Pattern(字母数字+下划线)`
- `password`: `@NotBlank`, `@Size(6-32)`

> 来源：AuthController.java, AuthService.java, RegisterRequest.java

## 登录流程

`POST /api/auth/login`：

1. 提取客户端 IP 和 User-Agent
2. 验证用户名格式
3. 检索用户并比对 BCrypt 密码
4. 获取上一次登录记录
5. 生成 JWT 并返回

::: warning 安全设计
"用户未找到"和"密码错误"返回相同的错误消息，防止用户名枚举攻击。
:::

> 来源：AuthController.java, AuthService.java

## 前端 Token 持久化与注入

使用两个 localStorage 键：
- `gcsc_token` — JWT 字符串
- `gcsc_user` — 用户信息 JSON

**请求拦截器**（request.js 第 19-31 行）：
- 每次请求注入 `Authorization: Bearer <token>`

**响应拦截器**：
- 捕获 HTTP 401，清除 localStorage，执行硬重定向到 `/login`

::: tip 设计选择
使用 `window.location.href` 而非 `router.push()` 是因为拦截器在 Vue 组件生命周期之外运行。
:::

> 来源：request.js, LoginView.vue

## Vue Router 路由守卫

| 路由 | 守卫类型 | 逻辑 |
|------|----------|------|
| `/` 及其子路由 | `requiresAuth` | 若无 `gcsc_token` 则重定向到 `/login` |
| `/student-info` | `allowedRoles: ['TEACHER', 'ADMIN']` | 学生被重定向到 `/myinfos` |
| `/admin` | `allowedRoles: ['ADMIN']` | 非管理员被重定向到 `/myinfos` |
| `/login`, `/register` | `guestOnly` | 已登录用户被重定向到 `/myinfos` |

> 来源：router/index.js

## 补充身份验证端点

| 端点 | 功能 |
|------|------|
| `GET /api/auth/me` | 刷新当前用户信息 |
| `POST /api/auth/change-password` | 修改密码（不使现有 Token 失效） |
| `GET /api/auth/login-history` | 登录记录分页列表（自动清理 30 天前记录） |

> 来源：AuthController.java, LoginHistoryService.java

## 错误处理

| 异常类型 | HTTP 状态码 | 响应 |
|----------|-------------|------|
| `IllegalArgumentException` | 400 | `{"success": false, "message": "..."}` |
| `MethodArgumentNotValidException` | 400 | 首个字段验证错误消息 |
| Token 缺失/无效 | 401 | `{"success":false,"message":"未登录或登录已过期"}` |

> 来源：GlobalExceptionHandler.java

## 配置参考

| 属性 | 默认值 | 描述 |
|------|--------|------|
| `security.jwt.secret` | `gcsc-student-center-jwt-secret-key-change-in-production-2026` | JWT 签名密钥（至少 256 位） |
| `security.jwt.expires-minutes` | 120 | Token 有效期（分钟） |
| `security.cors.allowed-origins` | `*` | CORS 来源白名单 |

> 来源：application.yml

## 下一步阅读

- [基于角色的访问控制](./rbac) — JWT 中的 role 声明如何映射到服务端授权决策
- [REST API 设计与控制器](./rest-api) — Authentication 对象如何流入控制器方法
- [Vue Router 与路由守卫](./router) — 路由层的详细演练
- [Axios 请求层](./axios-layer) — HTTP 客户端配置与错误传播策略
