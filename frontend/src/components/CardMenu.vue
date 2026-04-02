<template>
  <section class="menu-card">
    <div class="menu-card-title">导航</div>

    <div class="menu-list">
      <button
        v-for="item in menuItems"
        :key="item.key"
        class="menu-item"
        :class="{ active: activeMenu === item.key, disabled: !isMenuEnabled(item.key) }"
        type="button"
        :disabled="!isMenuEnabled(item.key)"
        @click="$emit('menu-click', item.key)"
      >
        {{ item.label }}
      </button>
    </div>

    <div v-if="showAchievementsDrawer" class="menu-section">
      <div class="menu-section-title">个人成就</div>
      <div class="menu-sublist">
        <button
          v-for="entry in achievementEntries"
          :key="entry.key"
          class="menu-subitem"
          :class="{ active: activeMenu === 'achievements' && activeAchievement === entry.key }"
          type="button"
          @click="$emit('achievement-entry-click', entry.key)"
        >
          {{ entry.label }}
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";

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
    default: true,
  },
});

defineEmits(["menu-click", "achievement-entry-click"]);

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
</script>
