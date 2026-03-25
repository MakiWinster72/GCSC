<template>
  <aside class="dashboard-left" :class="{ open: sidebarOpen }">
    <section class="profile-card">
      <div class="profile-row profile-main">
        <div class="profile-avatar">
          <img
            v-if="profile.avatarUrl"
            :src="resolveMediaUrl(profile.avatarUrl)"
            alt="头像"
          />
          <span v-else>{{ avatarText }}</span>
        </div>
        <div class="profile-name-wrap">
          <p class="profile-name">
            {{ profile.displayName || profile.username || "同学" }}
          </p>
          <p class="profile-role">{{ roleLabel }}</p>
        </div>
        <button
          class="profile-settings"
          type="button"
          aria-label="设置"
          @click="$emit('settings-click')"
        >
          <img src="/assets/icons/settings.svg" alt="设置" />
        </button>
      </div>
      <div class="profile-row">学号：{{ profile.studentNo || "未填写" }}</div>
      <div class="profile-row">班级：{{ profile.className || "未填写" }}</div>
      <div class="profile-row">{{ profileExtraLabel }}：{{ profileExtraValue || "未填写" }}</div>
    </section>

    <section class="menu-card">
      <template v-for="item in menuItems" :key="item.key">
        <div
          v-if="item.key === 'achievements'"
          class="menu-drawer"
          :class="{ open: achievementsOpen }"
        >
          <button
            class="menu-item menu-drawer-trigger"
            :class="{
              active: activeMenu === item.key,
              disabled: !isMenuEnabled(item.key),
            }"
            type="button"
            :disabled="!isMenuEnabled(item.key)"
            @click="handleAchievementsClick"
          >
            <span>{{ item.label }}</span>
            <span class="menu-drawer-caret" aria-hidden="true"></span>
          </button>
          <transition name="menu-drawer-panel">
            <div
              v-show="drawerVisible"
              class="menu-drawer-panel"
              :class="drawerAnimClass"
            >
              <div
                class="menu-drawer-indicator"
                :style="drawerIndicatorStyle"
                aria-hidden="true"
              ></div>
              <button
                v-for="entry in achievementEntries"
                :key="entry.key"
                class="menu-drawer-item"
                :class="{ active: activeAchievement === entry.key }"
                type="button"
                @click="$emit('achievement-entry-click', entry.key)"
              >
                {{ entry.label }}
              </button>
            </div>
          </transition>
        </div>
        <button
          v-else
          class="menu-item"
          :class="{
            active: activeMenu === item.key,
            disabled: !isMenuEnabled(item.key),
          }"
          type="button"
          :disabled="!isMenuEnabled(item.key)"
          @click="$emit('menu-click', item.key)"
        >
          {{ item.label }}
        </button>
      </template>
    </section>
  </aside>
</template>

<script setup>
import { computed, onActivated, onDeactivated, onMounted, ref, watch } from "vue";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";
import { API_BASE } from "../api/request";

const props = defineProps({
  profile: {
    type: Object,
    required: true,
  },
  activeMenu: {
    type: String,
    default: "",
  },
  activeAchievement: {
    type: String,
    default: "all",
  },
  showAchievementsDrawer: {
    type: Boolean,
    default: false,
  },
  achievementsOpen: {
    type: Boolean,
    default: false,
  },
  sidebarOpen: {
    type: Boolean,
    default: false,
  },
  profileExtraLabel: {
    type: String,
    default: "学院",
  },
});

const emit = defineEmits([
  "menu-click",
  "achievement-entry-click",
  "settings-click",
  "toggle-achievements",
]);

// 抽屉开闭动画状态：控制 v-show 和动画类的独立状态
const drawerVisible = ref(false);    // 控制 DOM 显示/隐藏 (display:none)
const drawerAnimClass = ref("");     // 控制动画 class: '' | 'is-open' | 'is-closing'
let closeTimer = null;

function openDrawer() {
  // 1. 先隐藏（display:none 状态，让浏览器重排）
  drawerVisible.value = false;
  drawerAnimClass.value = "";
  // 2. 微任务后显示 + 开始进入动画
  queueMicrotask(() => {
    drawerVisible.value = true;
    drawerAnimClass.value = "is-open";
    // 动画结束后清除 class，让元素保持静态的最终状态
    setTimeout(() => {
      drawerAnimClass.value = "";
    }, 350);
  });
}

function closeDrawer() {
  // 开始退出动画
  drawerAnimClass.value = "is-closing";
  // 动画结束后才真正隐藏
  clearTimeout(closeTimer);
  closeTimer = setTimeout(() => {
    drawerAnimClass.value = "";
    drawerVisible.value = false;
  }, 350);
}

// 监听父级打开/关闭指令
watch(
  () => props.achievementsOpen,
  (open) => {
    if (!props.showAchievementsDrawer) return;
    if (open) {
      openDrawer();
    } else {
      closeDrawer();
    }
  },
);

// 组件挂载时：根据初始 prop 决定是否显示
onMounted(() => {
  if (props.achievementsOpen && props.showAchievementsDrawer) {
    openDrawer();
  }
});

// 组件被 KeepAlive 缓存后重新激活时
onActivated(() => {
  if (props.achievementsOpen && props.showAchievementsDrawer) {
    openDrawer();
  }
});

// 组件被 KeepAlive 缓存时
onDeactivated(() => {
  clearTimeout(closeTimer);
  drawerVisible.value = false;
  drawerAnimClass.value = "";
});

const menuItems = computed(() => filterMenuItemsByRole(props.profile.role));

const achievementEntries = [
  { key: "all", label: "全部" },
  { key: "contest", label: "学科竞赛、文体艺术" },
  { key: "paper", label: "发表学术论文" },
  { key: "journal", label: "发表期刊作品" },
  { key: "patent", label: "专利(著作权)授权数(项)" },
  { key: "certificate", label: "职业资格证书" },
  { key: "research", label: "学生参与教师科研项目情况" },
  { key: "works", label: "创作、表演的代表性作品" },
  { key: "doubleHundred", label: "双百工程" },
  { key: "ieerTraining", label: "大学生创新创业训练计划项目" },
];

const activeAchievementIndex = computed(() => {
  const index = achievementEntries.findIndex(
    (entry) => entry.key === props.activeAchievement,
  );
  return index === -1 ? 0 : index;
});

const drawerIndicatorStyle = computed(() => ({
  transform: `translateY(calc(${activeAchievementIndex.value} * (var(--drawer-item-height) + var(--drawer-item-gap))))`,
}));

const avatarText = computed(() => {
  const name = props.profile.displayName || props.profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});

const roleLabel = computed(() => {
  if (props.profile.role === "ADMIN") {
    return "管理员";
  }
  if (props.profile.role === "TEACHER") {
    return "教师";
  }
  return "学生";
});

const profileExtraValue = computed(() => {
  return props.profile.studentCategory || props.profile.college || "";
});

function resolveMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE}${url}`;
}

function toggleAchievements() {
  if (props.showAchievementsDrawer) {
    emit("toggle-achievements");
  }
}

function handleAchievementsClick() {
  if (props.showAchievementsDrawer) {
    toggleAchievements();
  } else {
    emit("menu-click", "achievements");
  }
}
</script>
