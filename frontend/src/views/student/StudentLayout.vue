<script setup>
import { computed } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useSessionStore } from '../../stores/session'

const router = useRouter()
const route = useRoute()
const session = useSessionStore()

const userName = computed(() => session.user.value?.realName || session.user.value?.studentId || '')

async function handleLogout() {
  await session.signOut()
  router.push('/login')
}

const menuItems = [
  { label: '预约大厅', to: '/student' },
  { label: '我的预约', to: '/student/reservations' },
  { label: '我的信誉', to: '/student/credit' },
]

function isActive(path) {
  return route.path === path
}
</script>

<template>
  <div class="dashboard-shell">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <p class="eyebrow">Student Console</p>
        <h2>SRS</h2>
      </div>

      <nav class="sidebar-nav">
        <RouterLink
          v-for="item in menuItems"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          :class="{ active: isActive(item.to) }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <div class="page-shell">
      <header class="page-header">
        <div>
          <p class="eyebrow">学生端</p>
          <h1>你好，{{ userName }}</h1>
        </div>
        <button class="ghost-btn" type="button" @click="handleLogout">退出登录</button>
      </header>

      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>
