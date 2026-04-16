# 快速入门

## 概述

- **时长**: 11 分钟
- **等级**: 入门

GCSC Student Center（GCSC 学生中心）是一个面向高校学院场景的全栈 Web 应用，旨在为学生提供个人成就管理、个人信息维护和校园信息查看的综合平台。系统采用前后端分离架构，后端基于 Java 21 + Spring Boot 3.3.5 提供 RESTful API，前端基于 Vue 3 Composition API 构建响应式用户界面，两端通过 JWT（JSON Web Token）实现无状态身份认证。

目前系统支持三种用户角色——学生（STUDENT）、教师（TEACHER）、管理员（ADMIN），分别拥有不同的功能访问权限，覆盖从个人信息填报、成就记录到数据导出与后台管理的完整业务链路。

> 来源：README.md、CLAUDE.md、UserRole.java

## 系统架构

GCSC 采用经典的前后端分离架构模式：

- **前端开发**: 通过 Vite 开发服务器（端口 5173）运行，向后端 Spring Boot 服务（端口 8080）发起 API 请求
- **生产部署**: 前端构建产物可部署到任意静态文件服务器，后端 JAR 独立运行
- **数据持久化**: 使用 MySQL 数据库，JPA 的 `ddl-auto: update` 策略让数据库表结构自动同步实体定义
- **文件存储**: 所有文件上传存储在 `backend/uploads/` 目录中，通过 `/api/upload` 端点和 `/uploads/**` 静态资源路径对外提供服务

> 来源：application.yml、pom.xml、package.json

## 技术栈

| 层级 | 技术 | 版本 | 项目职责 |
|------|------|------|----------|
| 后端运行时 | Java | 21 | 应用运行时，支持现代语言特性 |
| 后端框架 | Spring Boot | 3.3.5 | Web 服务、依赖注入、自动配置 |
| ORM | Spring Data JPA / Hibernate | — | 实体映射、数据库 CRUD 操作 |
| 安全框架 | Spring Security + JWT (jjwt) | 0.12.6 | 无状态身份认证与角色授权 |
| 数据库 | MySQL | — | 持久化存储（utf8mb4 字符集） |
| 前端框架 | Vue 3 (Composition API) | ^3.5.12 | 响应式 UI 构建 |
| 构建工具 | Vite | ^5.4.9 | 前端开发服务器与生产构建 |
| HTTP 客户端 | Axios | ^1.7.7 | API 请求、JWT 拦截器 |
| 数据表格 | AG Grid | ^31.3.2 | 学生信息表格（教师/管理员） |
| 数据导出 | jsPDF + xlsx | ^2.5.1 / ^0.18.5 | PDF 与 Excel 文件导出 |

> 来源：pom.xml、package.json

## 核心功能一览

GCSC 围绕"学生成就管理"这一核心目标，构建了多个相互关联的功能模块。每个模块对应后端的 Controller-Service-Repository 三层结构和前端的 View-Composable-API 三层结构，形成清晰的功能边界。

### 身份认证与用户管理

用户通过显示名称、用户名和密码完成注册，登录后获得包含用户名、显示名称和角色的 JWT。Token 存储在 localStorage 中，键名为 `gcsc_token`，前端 Axios 拦截器自动为每个请求附加 `Authorization: Bearer <token>` 头。密码使用 BCrypt 加密存储，用户名要求 4-32 位的字母数字下划线组合。

> 来源：request.js、CLAUDE.md

### 学生信息

学生信息模块（StudentProfile）包含 30+ 个字段，覆盖：

- 基本信息（姓名、学号、班级、学院）
- 联系方式（电话、宿舍、紧急联系人）
- 政治面貌（团/党各阶段状态及日期）
- 家庭成员信息
- 通过 `@ElementCollection` 嵌入的教育经历和干部经历列表

学生初次登录会被引导填写个人信息，后续修改可通过提交变更请求进行审核。

> 来源：StudentProfile.java

### 成就系统

成就模块是系统的核心亮点，支持 9 种成就类型：

1. 学科竞赛
2. 学术论文
3. 期刊作品
4. 专利/著作权
5. 职业资格证书
6. 科研参与
7. 代表作品
8. 双百工程
9. 创新创业训练计划

每种成就类型对应独立的实体类和数据库表，但共享统一的上传、审核和展示流程，支持图片、视频、文档等多媒体附件，形成证书画廊式的展示效果。

> 来源：achievementConstants.js、CLAUDE.md

### 基于角色的访问控制

系统通过菜单可见性规则和路由守卫实现三级权限控制：

| 角色 | 可见范围 | 核心能力 |
|------|----------|----------|
| STUDENT | 个人信息、成就、通知、设置 | 填写/修改个人资料，记录成就，查看通知 |
| TEACHER | 学生 + 学生信息页 | 搜索筛选学生，导出学生信息（XLSX/PDF/CSV），审核学生提交 |
| ADMIN | 教师 + 后台管理页 | 用户管理、系统设置、数据备份、审核配置 |

> 来源：menu.js、router/index.js

## 项目结构

```
GCSC/
├── backend/
│   ├── pom.xml                   # Maven 依赖配置
│   └── src/main/
│       ├── java/com/gcsc/studentcenter/
│       │   ├── config/           # 安全、CORS、JWT 过滤器配置
│       │   ├── controller/       # REST 控制器（9 个）
│       │   ├── service/          # 业务逻辑层（12 个）
│       │   ├── repository/       # JPA 数据访问层（17 个）
│       │   ├── entity/           # JPA 实体（17 个）
│       │   ├── dto/              # 请求/响应数据传输对象
│       │   └── exception/        # 全局异常处理
│       └── resources/
│           └── application.yml   # 应用配置
│
├── frontend/                     # Vue 3 前端
│   ├── package.json              # npm 依赖配置
│   └── src/
│       ├── api/                  # Axios 请求模块（12 个）
│       ├── assets/styles/        # 全局 CSS 样式（20+ 文件）
│       ├── components/           # 可复用组件（16 个）
│       ├── composables/          # Vue 组合式函数（11 个）
│       ├── constants/            # 菜单、成就常量
│       ├── layouts/              # 布局组件
│       ├── router/               # Vue Router 路由配置
│       ├── utils/                # 导出工具（PDF/XLSX/DOCX）
│       └── views/                # 页面级组件（8 个）
│
├── docs/                         # 开发文档
├── scripts/                      # 辅助脚本
└── assets/                       # README 截图与图标资源
```

> 来源：CLAUDE.md、README.md

## 开发状态

截至当前版本，系统已实现的核心功能：

- ✅ 注册登录与 JWT 认证
- ✅ 学生个人信息管理
- ✅ 多类型成就记录
- ✅ 审核与审批工作流
- ✅ 教师数据导出（XLSX/PDF/CSV）
- ✅ 后台管理与数据备份
- ✅ 嵌入式 Iframe 模式支持

规划中的功能包括暗色主题、移动端胶囊 UI 优化等，详见 TODO.md。

> 来源：README.md、TODO.md

## 后续阅读

- [快速开始](./getting-started) — 通过分步指南，搭建本地开发环境
- [项目结构指南](./project-structure) — 深入了解代码库目录组织与命名约定
- [架构概览](./architecture) — 理解分层架构、请求生命周期
