<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  createAdminStudent,
  deleteAdminStudent,
  fetchAdminStudents,
  updateAdminStudent,
} from '../../api/admin'

const students = ref([])
const editingId = ref(null)
const actionMessage = ref('')
const showForm = ref(false)

const filter = reactive({
  keyword: '',
})

const form = reactive({
  studentId: '',
  realName: '',
  password: '',
})

async function loadStudents() {
  const response = await fetchAdminStudents({
    keyword: filter.keyword.trim() || undefined,
  })
  students.value = response.data
}

async function handleSubmit() {
  try {
    if (editingId.value) {
      await updateAdminStudent(editingId.value, {
        realName: form.realName.trim() || undefined,
        password: form.password.trim() || undefined,
      })
      actionMessage.value = '学生账号已更新'
    } else {
      await createAdminStudent({
        studentId: form.studentId.trim(),
        realName: form.realName.trim(),
        password: form.password.trim(),
      })
      actionMessage.value = '学生账号已创建'
    }

    resetForm()
    await loadStudents()
  } catch (error) {
    window.alert(error.message || '提交学生账号失败')
  }
}

function openCreateForm() {
  resetForm()
  showForm.value = true
}

function editStudent(student) {
  editingId.value = student.id
  showForm.value = true
  form.studentId = student.studentId
  form.realName = student.realName
  form.password = ''
}

async function removeStudent(student) {
  if (!window.confirm(`确认删除学生账号 ${student.studentId} 吗？`)) {
    return
  }

  try {
    await performDelete(student.id)
  } catch (error) {
    if (error.code === 409) {
      const futureReservationCount = error.payload?.data?.futureReservationCount ?? 0
      const confirmDelete = window.confirm(
        `该学生还有 ${futureReservationCount} 条未来预约。继续删除将自动取消这些预约并释放对应座位，是否继续？`,
      )
      if (!confirmDelete) {
        return
      }

      try {
        await performDelete(student.id, true)
      } catch (forceError) {
        window.alert(forceError.message || '删除学生账号失败')
      }
      return
    }

    window.alert(error.message || '删除学生账号失败')
  }
}

async function performDelete(studentId, force = false) {
  await deleteAdminStudent(studentId, force ? { force: true } : undefined)
  actionMessage.value = force
    ? '学生账号已删除，并已释放未来预约'
    : '学生账号已删除'

  if (editingId.value === studentId) {
    resetForm()
  }
  await loadStudents()
}

function resetForm() {
  editingId.value = null
  showForm.value = false
  form.studentId = ''
  form.realName = ''
  form.password = ''
}

onMounted(loadStudents)
</script>

<template>
  <section class="card">
    <div class="section-head">
      <div>
        <p class="eyebrow">学生用户</p>
        <h2>学生管理</h2>
      </div>
      <button v-if="!showForm" class="primary-btn compact" type="button" @click="openCreateForm">
        新增学生
      </button>
    </div>

    <p class="muted">
      学号创建后不可修改。删除学生时，历史预约与黑名单记录不再阻塞删除；若存在未来预约，系统会在二次确认后自动取消并释放座位。
    </p>

    <div class="toolbar">
      <label>
        <span>学号 / 姓名</span>
        <input v-model="filter.keyword" placeholder="输入学号或姓名查询" />
      </label>
      <button class="ghost-btn compact" type="button" @click="loadStudents">查询</button>
    </div>

    <form v-if="showForm" class="form-grid" @submit.prevent="handleSubmit">
      <label>
        <span>学号</span>
        <input
          v-model="form.studentId"
          :disabled="!!editingId"
          placeholder="例如 STU3001"
        />
      </label>

      <label>
        <span>姓名</span>
        <input v-model="form.realName" placeholder="请输入学生姓名" />
      </label>

      <label>
        <span>{{ editingId ? '新密码（留空则不修改）' : '初始密码' }}</span>
        <input v-model="form.password" type="password" placeholder="不少于 6 位" />
      </label>

      <div class="form-actions">
        <button class="primary-btn compact" type="submit">
          {{ editingId ? '更新学生' : '创建学生' }}
        </button>
        <button class="ghost-btn compact" type="button" @click="resetForm">
          {{ editingId ? '取消编辑' : '收起表单' }}
        </button>
      </div>
    </form>

    <p v-if="actionMessage" class="feedback success">{{ actionMessage }}</p>

    <div class="table-shell">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>学号</th>
            <th>姓名</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="student in students" :key="student.id">
            <td>{{ student.id }}</td>
            <td>{{ student.studentId }}</td>
            <td>{{ student.realName }}</td>
            <td>{{ student.createTime || '-' }}</td>
            <td class="actions">
              <button class="ghost-btn compact" type="button" @click="editStudent(student)">编辑</button>
              <button class="ghost-btn compact danger" type="button" @click="removeStudent(student)">删除</button>
            </td>
          </tr>
          <tr v-if="students.length === 0">
            <td colspan="5" class="empty-cell">暂无学生账号</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
