# REST API 设计与控制器

## 概述

GCSC 后端暴露了一个基于 Spring Boot 构建的无状态 REST API，作为 Vue 前端的唯一集成接口。

## 控制器架构概述

所有控制器均位于 `com.gcsc.studentcenter.controller` 包中，遵循瘦控制器约定：端点接收经过验证的 DTO，将其委托给服务层处理，并将结果包装在 ResponseEntity 中。

## 安全配置与公开端点

- permitAll: POST /api/auth/register, POST /api/auth/login
- permitAll: GET /api/achievements/**
- permitAll: GET /uploads/**
- authenticated: 所有其他 /api/** 路由

## 两种身份验证策略

策略 A: Spring Security Authentication 注入
- 被 6 个控制器使用
- 通过 authentication.getName() 获取用户名

策略 B: 手动 JWT 请求头解析
- 被 AchievementController、AdminController 使用
- 手动检查 role 声明

## 端点参考

### 身份验证 (/api/auth)

| 方法 | 端点 | 需要身份验证 |
|------|------|--------------|
| POST | /api/auth/register | 否 |
| POST | /api/auth/login | 否 |
| GET | /api/auth/me | 是 |
| POST | /api/auth/change-password | 是 |
| GET | /api/auth/login-history | 是 |

### 学生档案 (/api/student-profiles)

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | /api/student-profiles/me | 获取自己的档案 |
| GET | /api/student-profiles/{id} | 按 ID 获取档案 |
| PUT | /api/student-profiles/me | 创建或更新自己的档案 |
| PUT | /api/student-profiles/{id} | 按 ID 更新特定档案 |
| GET | /api/student-profiles/search | 分页的多条件搜索 |

### 成就 (/api/achievements)

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | /api/achievements | 列出/过滤成就 |
| GET | /api/achievements/{category}/{id} | 获取单个成就 |
| POST | /api/achievements/{category} | 创建成就 |
| PUT | /api/achievements/{category}/{id} | 更新成就 |
| DELETE | /api/achievements/{category}/{id} | 删除成就 |

### 审核工作流

- GET: /api/achievement-review-requests, /api/profile-review-requests
- POST: 提交新的审核请求
- POST .../approve: 批准待处理的请求
- POST .../reject: 拒绝并附带原因
- DELETE: 取消自己待处理的请求

### 管理 (/api/admin)

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | /api/admin/users | 分页的用户列表 |
| PUT | /api/admin/users/{id} | 更新用户详细信息 |
| DELETE | /api/admin/users/{id} | 删除用户账户 |
| GET | /api/admin/backup/db | 数据库转储下载 |
| POST | /api/admin/restore/db | 从文件还原 |
| GET | /api/admin/backup/attachments | 上传文件下载 |
| POST | /api/admin/restore/attachments | 从文件还原上传内容 |

### 文件上传 (/api/upload)

POST /api/upload: 接收 file 参数和可选的 context 参数

### 设置 (/api/settings/*)

- ReviewSettingsController: /api/settings/review
- AchievementUploadSettingsController: /api/settings/achievement-upload

## 错误处理约定

| 异常 | HTTP 状态码 |
|------|-------------|
| IllegalArgumentException | 400 |
| MethodArgumentNotValidException | 400 |
