<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  createBlacklist,
  fetchAdminBlacklists,
  releaseBlacklist,
} from '../../api/admin'

const blacklists = ref([])

const filter = reactive({
  studentId: '',
  status: '',
})

const form = reactive({
  studentId: '',
  reason: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  totalPages: 1,
})

async function loadBlacklists(targetPage = pagination.page) {
  const response = await fetchAdminBlacklists({
    studentId: filter.studentId.trim() || undefined,
    status: filter.status === '' ? undefined : Number(filter.status),
    page: targetPage,
    pageSize: pagination.pageSize,
  })

  blacklists.value = response.data.records
  pagination.page = Number(response.data.page) || 1
  pagination.pageSize = Number(response.data.pageSize) || 10
  pagination.total = Number(response.data.total) || 0
  pagination.totalPages = Math.max(Number(response.data.totalPages) || 0, 1)

  if (pagination.total > 0 && pagination.page > pagination.totalPages) {
    await loadBlacklists(pagination.totalPages)
  }
}

async function handleCreateBlacklist() {
  await createBlacklist({
    studentId: form.studentId.trim(),
    reason: form.reason.trim(),
  })
  form.studentId = ''
  form.reason = ''
  await loadBlacklists(1)
}

async function handleRelease(blacklistId) {
  await releaseBlacklist(blacklistId)
  await loadBlacklists()
}

async function handleQuery() {
  await loadBlacklists(1)
}

async function changePage(nextPage) {
  if (nextPage < 1 || nextPage > pagination.totalPages || nextPage === pagination.page) {
    return
  }
  await loadBlacklists(nextPage)
}

onMounted(loadBlacklists)
</script>

<template>
  <section class="card">
    <div class="section-head">
      <div>
        <p class="eyebrow">Blacklist Admin</p>
        <h2>黑名单管理</h2>
      </div>
    </div>

    <p class="muted">支持手动新增黑名单、解除封禁，并按学号与状态筛选记录，列表每页展示 10 条。</p>

    <form class="form-grid" @submit.prevent="handleCreateBlacklist">
      <label>
        <span>学号</span>
        <input v-model="form.studentId" placeholder="例如 STU2001" />
      </label>

      <label class="wide">
        <span>封禁原因</span>
        <input v-model="form.reason" placeholder="请输入封禁原因" />
      </label>

      <div class="form-actions">
        <button class="primary-btn compact" type="submit">新增黑名单</button>
      </div>
    </form>

    <div class="toolbar">
      <label>
        <span>学号</span>
        <input v-model="filter.studentId" placeholder="输入学号筛选" />
      </label>

      <label>
        <span>状态</span>
        <select v-model="filter.status">
          <option value="">全部</option>
          <option value="1">封禁中</option>
          <option value="0">已解除</option>
        </select>
      </label>

      <div class="toolbar-actions">
        <button class="ghost-btn compact" type="button" @click="handleQuery">查询</button>
      </div>
    </div>

    <div class="table-shell">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>学号</th>
            <th>原因</th>
            <th>违规时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in blacklists" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.studentId }}</td>
            <td>{{ item.reason }}</td>
            <td>{{ item.violationDate || '-' }}</td>
            <td>{{ item.status === 1 ? '封禁中' : '已解除' }}</td>
            <td class="actions">
              <button
                v-if="item.status === 1"
                class="ghost-btn compact"
                type="button"
                @click="handleRelease(item.id)"
              >
                解除封禁
              </button>
            </td>
          </tr>
          <tr v-if="blacklists.length === 0">
            <td colspan="6" class="empty-cell">暂无黑名单记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pager">
      <p class="pager-info">共 {{ pagination.total }} 条，当前第 {{ pagination.page }} / {{ pagination.totalPages }} 页</p>
      <div class="pager-actions">
        <button
          class="ghost-btn compact"
          type="button"
          :disabled="pagination.page <= 1"
          @click="changePage(pagination.page - 1)"
        >
          上一页
        </button>
        <button
          class="ghost-btn compact"
          type="button"
          :disabled="pagination.page >= pagination.totalPages"
          @click="changePage(pagination.page + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </section>
</template>
