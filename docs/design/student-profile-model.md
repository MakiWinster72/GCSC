# 学生档案数据模型

## 概述

- **时长**: 18 分钟
- **等级**: 进阶

学生档案是 GCSC 系统中的核心身份数据实体，涵盖了从入学元数据到完整的中共党员发展流程的所有信息。

## 架构概览

档案模型遵循一对一至多的组合模式：

- `AppUser` 通过外键拥有一个 `StudentProfile`（`@OneToOne`）
- `StudentProfile` 拥有 `EducationExperience` 和 `CadreExperience` 两个集合（`@OneToMany`）
- 整个聚合体由 `StudentProfileService` 统一调度

> 来源：StudentProfile.java、AppUser.java、EducationExperience.java、CadreExperience.java

## 实体拆解

### StudentProfile — 核心实体

- 映射至 `student_profiles` 表
- 与 `AppUser` 的关系：`@OneToOne` + `@JoinColumn(name = "user_id", nullable = false, unique = true)`
- 包含约 55 个字段，按逻辑划分为：
  - 基本身份
  - 入学/班级元数据
  - 宿舍
  - 个人详情
  - 共青团员身份
  - 党员发展流程
  - 紧急联系人
  - 特殊状态标志
  - 父母信息

### 子实体

| 子实体 | 独有字段 | 数据表 |
|--------|----------|--------|
| EducationExperience | school_name、education_level、witness | education_experiences |
| CadreExperience | department、position、description | cadre_experiences |

> 来源：EducationExperience.java、CadreExperience.java

## 党员发展流程

| 阶段（英文） | 阶段（中文） | 日期字段 | 发展标志 |
|-------------|-------------|----------|----------|
| Application | 入党申请 | application_date | — |
| Activist | 积极分子 | activist_date | activist_developing |
| Party Training | 党校培训 | party_training_date | party_training_pending |
| Development Target | 发展对象 | development_target_date | development_developing |
| Probationary Member | 预备党员 | probationary_member_date | probationary_developing |
| Full Member | 正式党员 | full_member_date | full_member_developing |

::: tip 设计说明
发展标志充当 UI 进度指示器，而非严格的状态转换。服务层不会对阶段的先后顺序进行校验。
:::

## 数据标准化策略

- 每个字符串字段经过 `normalize()` 处理：修剪空白、空字符串转为 null
- `college` 字段硬编码为 "大数据与人工智能学院"——强制单租户约束

> 来源：StudentProfileService.java

## DTO 层与 API 契约

### 请求/响应的不对称性

| DTO | 与实体的关键差异 |
|-----|------------------|
| StudentProfileRequest | 新增 avatarUrl（存储于 AppUser），省略了 id 和 user 引用 |
| StudentProfileResponse | 新增来自 AppUser 的 id、username、displayName |
| StudentSearchItemResponse | 轻量级投影，仅包含搜索结果所需字段 |

### REST API 接口

| 方法 | 路径 | 认证上下文 | 用途 |
|------|------|-----------|------|
| GET | /api/student-profiles/me | 已认证学生 | 获取个人档案 |
| PUT | /api/student-profiles/me | 已认证学生 | 创建或更新个人档案 |
| GET | /api/student-profiles/{id} | 任何已认证用户 | 根据 ID 获取特定学生的档案 |
| PUT | /api/student-profiles/{id} | 仅限管理员 | 更新任意学生的档案 |
| GET | /api/student-profiles/search | 任何已认证用户 | 分页分面搜索 |

> 来源：StudentProfileController.java

## 集合同步模式

服务层采用**清空并重建策略**：

1. 调用 `target.clear()`（结合 `orphanRemoval = true` 删除所有现有子行）
2. 遍历传入的 DTO 列表创建全新实体实例

当 `isCurrent` 为 true 时，`endDate` 会被置空。

::: warning 并发编辑
清空并重建策略可能导致更新丢失。实际应用中通过 UI 模式缓解：学生编辑自己的档案，管理员编辑通常顺序执行。
:::

> 来源：StudentProfileService.java

## 用户摘要同步

每次保存档案触发 `syncUserSummary()`，将以下字段同步回 AppUser：
- displayName（来自 fullName）
- studentNo
- className
- college（固定常量）

> 来源：StudentProfileService.java

## 分面搜索实现

搜索端点支持八个可选过滤参数：`classYear`、`classNo`、`college`、`major`、`hkMoTw`、`specialStudent`、`studentCategory` 和 `keyword`。

使用 `COALESCE` 函数在档案级字段为 null 时回退到用户级字段。

> 来源：StudentProfileRepository.java

## 前端集成

- `profile.js` — API 层
- `StudentProfileEditor.vue` — 2300+ 行的单文件组件
- `MyInfosView.vue` — 视图包装器

编辑器为教育/干部经验预分配 5 个空槽位（最多 5 条记录）。

> 来源：StudentProfileEditor.vue、MyInfosView.vue

## 下一步阅读

- [REST API 设计与控制器](./rest-api) — API 层如何路由请求
- [数据库模式约定](./database-schema) — 表之间的相互关系
