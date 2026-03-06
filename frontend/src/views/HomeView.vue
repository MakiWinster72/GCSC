<template>
  <div class="home-layout">
    <section class="home-panel">
      <p class="home-tip">GCSC 学生中心</p>
      <h1 class="home-title">你好，{{ displayName }}</h1>
      <p class="home-subtitle">你已成功登录。下一步我们可以继续接入学生中心业务模块。</p>

      <div class="home-actions">
        <RouterLink class="home-link" to="/register">创建新账号</RouterLink>
        <button class="home-logout" type="button" @click="logout">退出登录</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('gcsc_user') || '{}')
  } catch {
    return {}
  }
})

const displayName = computed(() => user.value.displayName || user.value.username || '同学')

function logout() {
  localStorage.removeItem('gcsc_user')
  router.push('/login')
}
</script>
