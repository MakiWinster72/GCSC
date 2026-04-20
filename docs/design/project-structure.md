# 项目结构指南

## 概述

- **时长**: 7 分钟
- **等级**: 入门

本页面将引导你浏览 BDAI-SC 仓库中的每个目录和文件，帮助你快速熟悉一个此前从未接触过的全栈代码库。

## 仓库概览

BDAI-SC 采用清晰的 monorepo 布局，由三大支柱构成：Java/Spring Boot 后端、Vue 3 前端，以及用于存放配置和文档的共享项目根目录。

## 根级文件与目录

| 路径 | 用途 |
|------|------|
| `CLAUDE.md` | AI 辅助指引：架构摘要、构建命令、代码规范 |
| `README.md` | 面向开发者的快速入门、功能列表及设计模型截图 |
| `TODO.md` | 路线图与规划功能 |
| `assets/` | 设计模型图片 (.png) 和 Logo 文件 |
| `docs/` | 独立的设计参考：动画模式、认证说明、CSS 规范 |
| `scripts/generate_test_data.py` | 用于向数据库填充样本记录的实用工具 |

> 来源：CLAUDE.md, README.md, TODO.md

## 后端结构 (backend/)

后端是一个标准的基于 Maven 的 Spring Boot 3.3.5 应用，目标运行环境为 Java 21。

### 顶层构建文件

| 文件 | 职责 |
|------|------|
| `pom.xml` | Maven 项目描述符——声明 Spring Boot 父依赖、Java 21 版本及所有依赖项 |
| `src/main/resources/application.yml` | 所有运行时配置：数据库 URL、JWT 密钥、CORS、上传限制、服务端口 |

### Java 包布局

```
backend/src/main/java/com/gcsc/studentcenter/
├── StudentCenterApplication.java   ← Spring Boot 启动入口
├── config/        ← Security、CORS、JWT 过滤器、Web 配置
├── controller/    ← REST API 端点（10 个控制器）
├── service/       ← 业务逻辑层（12 个服务）
├── repository/    ← JPA 数据访问接口（18 个仓库）
├── entity/        ← JPA 实体类（20 个实体）
├── dto/           ← 请求/响应数据传输对象（约 30 个 DTO）
└── exception/     ← 全局异常处理器
```

### 逐层拆解

**config/**

| 文件 | 职责 |
|------|------|
| `SecurityConfig.java` | 定义哪些 API 路径公开、哪些需要认证，配置过滤器链 |
| `JwtAuthenticationFilter.java` | 从每个请求中提取并验证 JWT，设置 Spring Security 上下文 |
| `GlobalCorsFilter.java` | 允许来自前端开发服务器的跨域请求 |
| `WebConfig.java` | 其他 Web 级别配置（如静态资源处理） |

**controller/**

| 控制器 | API 领域 |
|--------|----------|
| `AuthController` | 登录、注册、令牌刷新 |
| `StudentProfileController` | 学生个人信息 CRUD |
| `AchievementController` | 成就记录（全部 9 种类型） |
| `AchievementReviewRequestController` | 成就审核/审批工作流 |
| `AchievementUploadSettingsController` | 每种成就类型的上传配置 |
| `ProfileReviewRequestController` | 个人资料变更审核工作流 |
| `ReviewSettingsController` | 审核系统配置 |
| `AdminController` | 管理面板端点（用户、备份、系统设置） |
| `UploadController` | 文件上传（最大 200 MB） |

> 来源：CLAUDE.md

## 前端结构 (frontend/)

前端是一个使用 Vite 构建的 Vue 3 单页应用。

### 顶层配置

| 文件 | 职责 |
|------|------|
| `package.json` | 声明依赖项及脚本命令 |
| `vite.config.js` | Vite 开发服务器配置——绑定到 0.0.0.0:5173 |
| `index.html` | SPA 入口 HTML 文件 |
| `public/assets/icons/` | 静态图标文件，构建时原样复制 |

### 源码目录布局 (frontend/src/)

```
frontend/src/
├── main.js              ← 应用引导、全局 Toast 注册
├── App.vue              ← 根组件：RouterView + ToastContainer
├── styles.css           ← Vite 入口 CSS
├── api/                 ← Axios 请求模块（12 个文件）
├── assets/styles/       ← 全局 CSS 文件（19 个文件）
├── components/          ← 可复用的 Vue 组件（18 个文件）
├── composables/         ← Vue 组合式函数（11 个文件）
├── constants/           ← 菜单配置、成就模式定义
├── layouts/             ← DashboardLayout 布局包装器
├── router/              ← Vue Router 配置
├── utils/               ← 纯工具函数（11 个文件）
└── views/              ← 页面级组件（8 个文件）
```

### views/ — 页面组件

| 视图 | 路由 | 访问权限 |
|------|------|----------|
| `LoginView.vue` | `/login` | 仅访客 |
| `RegisterView.vue` | `/register` | 仅访客 |
| `MyInfosView.vue` | `/myinfos` | 已认证学生 |
| `AchievementsView.vue` | `/achievements` | 已认证学生 |
| `StudentInfoView.vue` | `/student-info` | TEACHER、ADMIN |
| `AdminView.vue` | `/admin` | 仅 ADMIN |
| `NotificationsView.vue` | `/notifications` | 已认证用户 |
| `SettingsView.vue` | `/settings` | 已认证用户 |

### components/ — 共享可复用组件

| 组件 | 用途 |
|------|------|
| `DashboardSidebar.vue` | 左侧边栏导航 |
| `MobileCapsule.vue` | 移动端底部导航栏 |
| `AppFooter.vue` | 仪表盘页脚 |
| `BrandHeader.vue` | 带 Logo 的顶部标题栏 |
| `OverlayPanel.vue` | 通用滑动覆盖面板 |
| `StudentExportDialog.vue` | 导出学生档案的对话框 |
| `StudentProfileEditor.vue` | 编辑学生信息的表单 |
| `ToastContainer.vue` | 全局 Toast 通知容器 |
| `PaginationBar.vue` | 可复用的分页控件 |
| `AchievementCardBody.vue` | 列表/网格视图中的成就卡片 |

### api/ — 请求模块

| 模块 | 后端领域 |
|------|----------|
| `request.js` | Axios 实例、JWT 注入、401 处理 |
| `auth.js` | 登录、注册、令牌操作 |
| `profile.js` | 学生档案 CRUD |
| `achievement.js` | 单类型成就操作 |
| `achievements.js` | 跨类型成就查询 |
| `upload.js` | 文件上传端点 |
| `admin.js` | 管理面板 API |

### composables/ — Vue 组合式函数

| 组合式函数 | 功能 |
|------------|------|
| `useAchievementEditor.js` | 成就创建/编辑表单逻辑 |
| `useAchievementList.js` | 分页成就列表 |
| `useAchievementUpload.js` | 带进度跟踪的文件上传 |
| `useDashboardShell.js` | 仪表盘布局状态 |
| `useToast.js` | Toast 通知管理 |
| `useOverlayState.js` | 模态框/覆盖层的开启与关闭切换 |
| `useStudentPdfExport.js` | 学生档案的 PDF 导出流水线 |
| `useReviewSettings.js` | 审核工作流配置 |

### assets/styles/ — 全局 CSS

| CSS 文件 | 覆盖范围 |
|----------|----------|
| `_variables.css` | CSS 自定义属性（颜色、间距、过渡时间） |
| `_base.css` | 样式重置、排版、基础元素样式 |
| `layout.css` | 仪表盘外壳、网格、响应式断点 |
| `dialogs.css` | 模态覆盖层、Toast 定位、底部抽屉面板 |
| `achievements.css` | 成就卡片网格、表单布局、媒体画廊 |
| `auth.css` | 登录/注册页面样式 |
| `buttons.css` | 幽灵按钮和操作按钮的基础样式 |
| `cards.css` | 通用卡片组件样式 |

### utils/ — 纯工具函数

| 工具 | 用途 |
|------|------|
| `achievementFormatters.js` | 成就的日期格式化、分类标签 |
| `studentProfileExport.js` | 导出编排（委托给 PDF/DOCX/XLSX 渲染器） |
| `pdfRenderer.js` | 基于 jsPDF 的档案文档生成 |
| `docxRenderer.js` | Word 文档档案生成 |
| `sheetRenderer.js` | Excel 电子表格档案生成 |
| `viewTransition.js` | 页面过渡动画辅助函数 |
| `media.js` | 文件类型检测、预览 URL 处理 |

### constants/ — 静态配置

`menu.js` 文件定义了侧边栏导航结构及基于角色的可见性规则。

## 技术栈总结

| 层级 | 技术 | 版本 |
|------|------|------|
| 运行环境（后端） | Java | 21 |
| 框架（后端） | Spring Boot | 3.3.5 |
| ORM | Spring Data JPA / Hibernate | （由 Boot 管理） |
| 安全 | Spring Security + JJWT | 0.12.6 |
| 数据库 | MySQL | 8.x |
| 运行环境（前端） | Node.js | （推荐 LTS 版本） |
| 框架（前端） | Vue 3 (Composition API) | 3.5.x |
| 构建工具（前端） | Vite | 5.4.x |
| 路由 | Vue Router | 4.4.x |
| HTTP 客户端 | Axios | 1.7.x |
| 数据表格 | AG Grid Vue | 31.3.x |
| PDF 生成 | jsPDF + AutoTable | 2.5.x |
| 电子表格 | SheetJS (xlsx) | 0.18.x |
| 构建工具（后端） | Apache Maven | （含 Wrapper） |

> 来源：pom.xml, package.json, CLAUDE.md

## 后续步骤

- [架构概览](./architecture) — 了解系统的设计模式
- [快速开始](./getting-started) — 在本地运行该项目
- [JWT 身份验证流程](./jwt-auth) — 理解认证机制
