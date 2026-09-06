<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useSessionStore } from '../stores/session'

const router = useRouter()
const session = useSessionStore()

const form = reactive({
  studentId: '',
  password: '',
})

const errorMessage = ref('')
const loading = ref(false)

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true
  try {
    const user = await session.signIn({
      studentId: form.studentId.trim(),
      password: form.password,
    })
    await router.push(user.role === 1 ? '/admin' : '/student')
  } catch (error) {
    errorMessage.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}

function fillDemo(role) {
  if (role === 'admin') {
    form.studentId = 'ADMIN01'
    form.password = 'admin123'
    return
  }
  form.studentId = 'STU2001'
  form.password = 'pass123'
}
</script>

<template>
  <div class="login-shell">
    <section class="login-card">
      <div class="login-brand">
        <p class="eyebrow">SRS Frontend</p>
        <h1>考研自习室预约管理系统</h1>
        <p class="subtitle">
          基于 Vue 3 的前后端分离前端。当前接入 Spring Boot 会话鉴权接口，支持学生端与管理员端分流。
        </p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <label>
          <span>学号</span>
          <input v-model="form.studentId" placeholder="请输入学号" autocomplete="username" />
        </label>

        <label>
          <span>密码</span>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </label>

        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

        <button class="primary-btn" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录系统' }}
        </button>
      </form>

      <div class="demo-panel">
        <p>演示账号</p>
        <div class="demo-actions">
          <button type="button" class="ghost-btn" @click="fillDemo('student')">
            填入学生账号
          </button>
          <button type="button" class="ghost-btn" @click="fillDemo('admin')">
            填入管理员账号
          </button>
        </div>
      </div>
    </section>
  </div>
</template>
