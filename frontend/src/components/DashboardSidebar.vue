<template>
  <aside class="dashboard-left" :class="{ open: sidebarOpen }">
    <CardMenu
      :profile="profile"
      :active-menu="activeMenu"
      :active-achievement="activeAchievement"
      :show-achievements-drawer="showAchievementsDrawer"
      @menu-click="$emit('menu-click', $event)"
      @achievement-entry-click="$emit('achievement-entry-click', $event)"
    />
  </aside>
</template>

<script setup>
import CardMenu from "./CardMenu.vue";

defineProps({
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
  sidebarOpen: {
    type: Boolean,
    default: false,
  },
});

defineEmits([
  "menu-click",
  "achievement-entry-click",
]);
</script>
