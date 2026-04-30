# 多实体成就系统

## 概述


BDAI-SC 平台需要容纳异构的学生成果集合——每种成果具有根本不同的数据结构。系统实现了按类别分表的持久化策略，并通过模式驱动的多态外观为前端提供统一接口。

## 架构概览

- 九个独立的 JPA 实体，各自拥有专属的 `@Table` 和 `JpaRepository`
- 通过单一的 `AchievementService` 进行协调
- 控制器暴露了五个端点，位于 `/api/achievements` 路径下

> 来源：AchievementController.java

## 九种成果类别

| 类别键 | 实体类 | 数据库表 | 标题键 | 特定领域列 |
|--------|--------|----------|--------|-----------|
| contest | AchievementContest | achievement_contests | contestName | contestCategory, awardCategory, awardLevel... |
| paper | AchievementPaper | achievement_papers | paperTitle | journalName, publishDate, authorOrder... |
| journal | AchievementJournal | achievement_journals | workTitle | publicationName, publishDate |
| patent | AchievementPatent | achievement_patents | patentName | patentType, grantNo, grantDate... |
| certificate | AchievementCertificate | achievement_certificates | certificateName | certificateType, obtainDate |
| research | AchievementResearch | achievement_researches | projectName | teacherNo, projectLeader |
| works | AchievementWorks | achievement_works | workName | workCategory, workType, publishDate... |
| doubleHundred | AchievementDoubleHundred | achievement_double_hundreds | projectName | projectCategory, projectDomain... |
| ieerTraining | AchievementIeerTraining | achievement_ieer_trainings | projectName | collegeName, projectType, projectLeader... |

> 来源：AchievementContest.java, AchievementPaper.java

## AchievementService 中的类别路由

`AchievementService` 通过构造器注入接收 10 个仓储 Bean，通过 9 路 switch 语句分发操作：

- `list()` — 特权用户可跨学生查询
- `create()` / `update()` / `delete()` — 通过 `ensureOwner()` 守卫所有权

> 来源：AchievementService.java

## 统一的 DTO 契约

| DTO | 字段 |
|-----|------|
| AchievementRecordRequest | `imageUrl` + `Map<String, String> fields` |
| AchievementRecordResponse | `id` + `category` + `createdAt` + `fields` |

前端完全通过键值对映射操作，无需知道具体 Java 类型。

> 来源：AchievementRecordRequest.java, AchievementRecordResponse.java

## 前端模式驱动架构

### categoryFieldMap 结构

```javascript
{
  titleKey: "contestName",  // 卡片标题
  dateKey: "awardDate",    // 排序主日期
  fields: [                 // 表单字段定义
    { key, label, kind, placeholder, type }
  ]
}
```

### achievementEntries

将每个类别键映射到中文显示标签，包含特殊的 "all" 条目用于聚合过滤。

### categoryHints

为每个类别提供提交指导字符串，在编辑器视图中以可折叠提示形式渲染。

> 来源：achievementConstants.js

## useAchievementEditor

统一表单引擎，管理创建/编辑生命周期：

- `form` 对象跟踪 category、fields、imageUrl、imageUrls、attachments
- `openEditorForCategory()` — 新建
- `openEditorForEdit(item)` — 编辑现有记录
- `saveAchievement()` — 完整持久化流程

> 来源：useAchievementEditor.js

## useAchievementList

管理列表视图状态：

- `fetchAchievements()` — 调用 API 端点
- `dedupeAchievements()` — 防止过时条目累积
- `filteredAchievements` — 两层过滤（category + 学生级别）

> 来源：useAchievementList.js

## 所有权与访问控制

| 操作 | 控制策略 |
|------|----------|
| 单实体操作 (getById/update/delete) | `ensureOwner()` 守卫 |
| list() 特权查询 | ADMIN/TEACHER 可跨学生查询 |
| 普通学生 | 仅能查看自己的成果 |

> 来源：AchievementService.java

## 媒体常量

```javascript
IMAGE_URLS_FIELD = "_imageUrls"
ATTACHMENTS_FIELD = "_attachments"
```

> 来源：achievementConstants.js

## AchievementDetailSchemas

定义成果在只读上下文中的渲染方式：

```javascript
{
  tag: "类别标签",
  groups: [{
    label: "分组标签",
    rows: [
      row(label, valueFn, { hidden })
    ]
  }]
}
```

> 来源：achievementDetailSchemas.js

## 下一步阅读

- [审核与批准工作流](./review-workflow) — 成果审核请求的提交与处理
- [学生档案导出工具](./export-tools) — 成果数据如何通过 docxRenderer、pdfRenderer 导出
