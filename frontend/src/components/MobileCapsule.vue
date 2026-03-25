<template>
  <div class="mobile-capsule">
    <div class="capsule-left">
      <div
        class="capsule-action"
        role="button"
        tabindex="0"
        aria-label="打开菜单"
        @click="$emit('open-sidebar')"
        @keydown.enter="$emit('open-sidebar')"
        @keydown.space.prevent="$emit('open-sidebar')"
      >
        <span class="capsule-icon" aria-hidden="true">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </span>
      </div>
    </div>
    <div class="capsule-right">
      <slot name="right" />
    </div>
  </div>
</template>

<script setup>
defineEmits(["open-sidebar"]);
</script>
