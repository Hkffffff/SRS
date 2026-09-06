<script setup>
import { computed } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useSessionStore } from '../../stores/session'

const router = useRouter()
const route = useRoute()
const session = useSessionStore()

const userName = computed(() => session.user.value?.realName || session.user.value?.studentId || '')

const menuItems = [
  { label: '资源管理', to: '/admin' },
  { label: '预约记录', to: '/admin/reservations' },
  { label: '黑名单管理', to: '/admin/blacklists' },
  { label: '学生管理', to: '/admin/students' },
]

async function handleLogout() {
  await session.signOut()
  router.push('/login')
}

function isActive(path) {
  return route.path === path
}
</script>

<template>
  <div class="dashboard-shell admin-theme">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <p class="eyebrow">Admin Console</p>
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
          <p class="eyebrow">管理员端</p>
          <h1>{{ userName }}</h1>
        </div>
        <button class="ghost-btn" type="button" @click="handleLogout">退出登录</button>
      </header>

      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>
