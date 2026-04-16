import { defineConfig } from "vitepress";

export default defineConfig({
  title: "数智学院学生信息管理系统",
  description: "wiki 文档",
  lang: "zh-CN",

  ignoreDeadLinks: true,

  themeConfig: {
    nav: [
      { text: "首页", link: "/" },
      { text: "详细设计", link: "/design/" },
      { text: "API", link: "/api/" },
    ],
    sidebar: [
      {
        text: "指南",
        items: [
          { text: "介绍", link: "/guide/" },
          { text: "快速开始", link: "/guide/getting-started" },
        ],
      },
      {
        items: [
          { text: "概述", link: "/design/" },
          { text: "架构概述", link: "/design/architecture" },
        ],
      },
      {
        text: "认证与授权",
        items: [
          { text: "JWT 认证流程", link: "/design/jwt-auth" },
          { text: "基于角色的访问控制", link: "/design/rbac" },
        ],
      },
      {
        text: "后端核心",
        items: [
          { text: "REST API 设计与控制器", link: "/design/rest-api" },
          { text: "学生档案数据模型", link: "/design/student-profile-model" },
          { text: "多实体成就系统", link: "/design/achievement-system" },
          { text: "审核与审批工作流", link: "/design/review-workflow" },
          { text: "管理面板与备份", link: "/design/admin-backup" },
          { text: "数据库 Schema 约定", link: "/design/database-schema" },
        ],
      },
      {
        text: "前端核心",
        items: [
          { text: "Vue Router 与路由守卫", link: "/design/router" },
          { text: "Axios 请求层", link: "/design/axios-layer" },
          { text: "仪表盘布局与导航", link: "/design/dashboard-layout" },
          { text: "成就编辑器组合式函数", link: "/design/achievement-editor" },
          { text: "学生档案导出工具", link: "/design/export-tools" },
        ],
      },
      {
        text: "设计与体验",
        items: [
          { text: "CSS 架构与约定", link: "/design/css-architecture" },
          { text: "动画与过渡模式", link: "/design/animation" },
          { text: "内嵌 Iframe 模式", link: "/design/iframe-embed" },
        ],
      },
      {
        text: "参考",
        items: [{ text: "配置参考", link: "/design/config-reference" }],
      },
    ],
  },
});
