# 架构概述

## 概述

- **时长**: 6 分钟
- **等级**: 进阶

本页面提供了 GCSC 学生活动中心内部结构的高层架构图——即将其 Spring Boot 后端和 Vue 3 前端连接为一个单一、连贯系统的分层模式。

## 技术栈概览

GCSC 是一个单体全栈应用，分为两个独立进程：一个 Spring Boot REST API 和一个 Vue 3 单页应用。

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 语言（后端） | Java | 21 | 运行时与核心逻辑 |
| 框架（后端） | Spring Boot | 3.3.5 | Web、安全、数据、依赖注入 |
| ORM | Spring Data JPA | (受管) | 通过 Repository 进行数据库访问 |
| 数据库 | MySQL | 8.x | 持久化存储 |
| 认证 | Spring Security + jjwt | 0.12.6 | 无状态 JWT 认证 |
| 语言（前端） | JavaScript (ES2022+) | — | UI 与客户端逻辑 |
| 框架（前端） | Vue 3 (Composition API) | 3.5.x | 组件系统 |
| 构建工具（前端） | Vite | 5.4.x | 开发服务器与打包工具 |
| 路由 | Vue Router | 4.4.x | SPA 导航与守卫 |
| HTTP 客户端 | Axios | 1.7.x | API 通信 |
| 数据表格 | AG Grid Community | 31.3.x | 表格数据展示 |
| 导出 | jsPDF, SheetJS (xlsx), docx-preview | — | PDF/Excel/Word 渲染 |

> 来源：pom.xml, package.json

## 后端架构：分层单体

后端遵循由 Spring 依赖注入强制执行的严格四层架构。每一层都有单一的职责，且依赖关系严格向下流动。

### 请求生命周期

1. HTTP 请求到达 Spring 的 DispatcherServlet（端口 8080）
2. `JwtAuthenticationFilter` 在请求到达任何控制器之前拦截，提取 JWT 并设置 SecurityContext
3. `SecurityConfig` 检查过滤器链的授权规则
4. Controller 接收请求，委托给相应的 Service
5. Service 执行业务逻辑
6. Repository 将 Service 调用转换为针对 MySQL 的 JPA 查询
7. 响应以 JSON 格式回流，错误由 `GlobalExceptionHandler` 捕获

> 来源：JwtAuthenticationFilter.java, SecurityConfig.java, GlobalExceptionHandler.java

### 后端核心包

| 包 | 文件数 | 职责 |
|-----|--------|------|
| `controller/` | 9 个类 | REST 端点定义、请求路由、HTTP 状态码 |
| `service/` | 13 个类 | 业务逻辑、数据转换、跨 Repository 编排 |
| `repository/` | 16 个接口 | JpaRepository 接口 —— Spring Data 在运行时生成 SQL |
| `entity/` | 20+ 个类 | 与 MySQL 表一对一映射的 JPA @Entity 类 |
| `dto/` | 30+ 个类 | 带有校验注解的请求/响应数据传输对象 |
| `config/` | 4 个类 | 安全过滤器链、CORS、JWT 过滤器、静态资源服务 |
| `exception/` | 1 个类 | 用于集中格式化错误信息的 @RestControllerAdvice |

> 来源：CLAUDE.md, 后端包结构

## 前端架构：基于 Composables 的 Composition API

前端是一个 Vue 3 单页应用，完全使用 Composition API（`<script setup>` 语法）。

### 目录职责

```
frontend/src/
├── api/              → 11 个模块：1 个基础模块 (request.js) + 10 个特定领域模块
├── components/       → 15 个共享组件 + achievement/ 子文件夹
├── composables/      → 11 个封装有状态逻辑的 Composable 函数
├── constants/        → 菜单定义、成果字段模式、词汇表
├── layouts/          → DashboardLayout.vue
├── router/           → 带有 beforeEach 守卫的 Vue Router 配置
├── views/            → 8 个页面级组件
├── utils/            → 纯工具函数
└── assets/styles/    → 16 个模块化 CSS 文件
```

### Dashboard Shell 模式

前端最重要的架构模式是 **Dashboard Shell** ——一个持久化的布局包装器，在每个经过认证的视图周围渲染侧边栏、页眉和页脚。

所有经过认证的路由都作为 `/` 路径的子路由嵌套，并以 DashboardLayout 作为组件。这意味着侧边栏和页眉只渲染一次并在路由切换时保持不变。

当视图在查询字符串中收到 `?embed=1` 时，会激活嵌入模式——布局会隐藏侧边栏、页眉和页脚。

> 来源：DashboardLayout.vue, useDashboardShell.js

### Composable 模式

| Composable | 用途 |
|------------|------|
| `useAchievementEditor` | 用于成果表单编辑的 CRUD 状态机 |
| `useAchievementList` | 成果列表的分页、筛选和数据获取 |
| `useAchievementUpload` | 带有进度追踪的文件上传队列管理 |
| `useDashboardShell` | 通过 provide/inject 控制侧边栏的打开/关闭 |
| `useNotifications` | 通知轮询与未读计数 |
| `useOverlayState` | 弹窗/抽屉的打开/关闭切换状态 |
| `useReviewSettings` | 审核工作流配置状态 |
| `useStudentPdfExport` | 学生个人资料的 PDF 生成编排 |
| `useToast` | Toast 通知队列 |
| `useAchievementPreview` | 媒体预览状态 |
| `useAchievementUploadSettings` | 按类别划分的上传配置 |

> 来源：Composables 目录, main.js

## 数据架构：多实体成果模型

GCSC 中最独特的架构决策是其**多实体成果系统**。它没有使用单一的多态 achievements 表，而是让每个成果类别映射到自己的 JPA 实体、数据库表和 Repository。

### 九大成果实体

| 实体 | 数据表 | 类别键 | 领域 |
|------|--------|--------|------|
| `AchievementContest` | `achievement_contests` | contest | 学术与艺术竞赛 |
| `AchievementPaper` | `achievement_papers` | paper | 已发表的学术论文 |
| `AchievementJournal` | `achievement_journals` | journal | 已发表的期刊作品 |
| `AchievementPatent` | `achievement_patents` | patent | 专利与版权 |
| `AchievementCertificate` | `achievement_certificates` | certificate | 职业资格证书 |
| `AchievementResearch` | `achievement_researches` | research | 教师科研参与 |
| `AchievementWorks` | `achievement_works` | works | 创作与表演作品 |
| `AchievementDoubleHundred` | `achievement_double_hundreds` | doubleHundred | 双百工程项目 |
| `AchievementIeerTraining` | `achievement_ieer_trainings` | ieerTraining | 创新训练项目 |

> 来源：Entity 目录, AchievementService.java, achievementConstants.js

## 学生档案：核心实体

StudentProfile 是系统中字段最丰富的实体，包含 60 多列。它与 AppUser 具有 `@OneToOne` 关系，并拥有两个 `@ElementCollection` 集合：`educationExperiences` 和 `cadeExperiences`。

> 来源：StudentProfile.java

## 认证与授权边界

安全机制被实现为一个无状态的 JWT 系统，具有三个不同的执行点：

1. **前端 Axios 拦截器**：注入 JWT，处理 401 响应
2. **Vue Router 守卫**：检查 Token 和角色权限
3. **后端 SecurityConfig + JwtAuthenticationFilter**：URL 模式和 Service 层所有权检查

> 来源：request.js, index.js (router)

## 文件上传与媒体架构

文件上传由 `/api/upload` 处理，文件被写入 `backend/uploads/` 目录，并作为静态资源对外提供服务。

> 来源：UploadController.java, WebConfig.java

## 配置拓扑

| 配置键 | 值 | 控制范围 |
|--------|------|----------|
| `server.port` | 8080 | 后端 HTTP 端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/gcsc` | 数据库连接 |
| `spring.jpa.hibernate.ddl-auto` | update | Schema 自动迁移 |
| `app.upload-dir` | `./uploads` | 文件存储路径 |
| `security.jwt.secret` | (静态密钥) | JWT 签名密钥 |
| `security.jwt.expires-minutes` | 120 | Token 有效期 |

> 来源：application.yml, vite.config.js, request.js

## 下一步阅读

- [JWT 身份验证流程](./jwt-auth) — 深入剖析 Token 生成、验证及安全过滤器链
- [REST API 设计与控制器](./rest-api) — 全部 9 个控制器的完整端点参考
- [Vue Router 与路由守卫](./router) — 前端导航、角色过滤与视图切换
- [多实体成果系统](./achievement-system) — 九实体模式的详细分析及其权衡
- [CSS 架构与约定](./css-architecture) — 16 个模块化 CSS 文件的组织与导入方式
- [数据库 Schema 约定](./database-schema) — 命名模式、关系建模与迁移策略
