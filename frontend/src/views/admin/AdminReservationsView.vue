<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  adminCancelReservation,
  fetchAdminReservations,
  fetchAdminRooms,
  markReservationViolated,
  processOverdueReservations,
} from '../../api/admin'

const reservations = ref([])
const rooms = ref([])

const reservationFilter = reactive({
  studentId: '',
  roomId: '',
  date: '',
  timeSlot: '',
  status: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  totalPages: 1,
})

const timeSlotText = {
  1: '上午（8:00-12:00）',
  2: '下午（14:00-17:00）',
  3: '晚上（18:00-22:00）',
}

const statusText = ['待签到', '已签到', '已取消', '已违约']

async function loadRooms() {
  const response = await fetchAdminRooms()
  rooms.value = response.data
}

async function loadReservations(targetPage = pagination.page) {
  const response = await fetchAdminReservations({
    studentId: reservationFilter.studentId.trim() || undefined,
    roomId: reservationFilter.roomId === '' ? undefined : Number(reservationFilter.roomId),
    date: reservationFilter.date || undefined,
    timeSlot: reservationFilter.timeSlot === '' ? undefined : Number(reservationFilter.timeSlot),
    status: reservationFilter.status === '' ? undefined : Number(reservationFilter.status),
    page: targetPage,
    pageSize: pagination.pageSize,
  })

  reservations.value = response.data.records
  pagination.page = Number(response.data.page) || 1
  pagination.pageSize = Number(response.data.pageSize) || 10
  pagination.total = Number(response.data.total) || 0
  pagination.totalPages = Math.max(Number(response.data.totalPages) || 0, 1)

  if (pagination.total > 0 && pagination.page > pagination.totalPages) {
    await loadReservations(pagination.totalPages)
  }
}

async function handleCancel(reservationId) {
  await adminCancelReservation(reservationId)
  await loadReservations()
}

async function handleViolate(reservationId) {
  await markReservationViolated(reservationId)
  await loadReservations()
}

async function handleProcessOverdue() {
  const response = await processOverdueReservations()
  window.alert(`本次自动处理 ${response.data} 条逾期未签到预约`)
  await loadReservations()
}

async function handleQuery() {
  await loadReservations(1)
}

async function changePage(nextPage) {
  if (nextPage < 1 || nextPage > pagination.totalPages || nextPage === pagination.page) {
    return
  }
  await loadReservations(nextPage)
}

onMounted(async () => {
  await Promise.all([loadRooms(), loadReservations()])
})
</script>

<template>
  <section class="card">
    <div class="section-head">
      <div>
        <p class="eyebrow">Reservation Admin</p>
        <h2>预约记录</h2>
      </div>
      <button class="primary-btn compact" type="button" @click="handleProcessOverdue">
        扫描逾期预约
      </button>
    </div>

    <p class="muted">支持按学号、自习室、日期、时间段和状态筛选预约记录，列表每页展示 10 条。</p>

    <div class="toolbar">
      <label>
        <span>学号</span>
        <input v-model="reservationFilter.studentId" placeholder="输入学号筛选" />
      </label>

      <label>
        <span>自习室</span>
        <select v-model="reservationFilter.roomId">
          <option value="">全部</option>
          <option v-for="room in rooms" :key="room.id" :value="room.id">{{ room.roomName }}</option>
        </select>
      </label>

      <label>
        <span>日期</span>
        <input v-model="reservationFilter.date" type="date" />
      </label>

      <label>
        <span>时间段</span>
        <select v-model="reservationFilter.timeSlot">
          <option value="">全部</option>
          <option value="1">上午</option>
          <option value="2">下午</option>
          <option value="3">晚上</option>
        </select>
      </label>

      <label>
        <span>状态</span>
        <select v-model="reservationFilter.status">
          <option value="">全部</option>
          <option value="0">待签到</option>
          <option value="1">已签到</option>
          <option value="2">已取消</option>
          <option value="3">已违约</option>
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
            <th>自习室</th>
            <th>座位号</th>
            <th>日期</th>
            <th>时间段</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in reservations" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.studentId }}</td>
            <td>{{ item.roomName || '-' }}</td>
            <td>{{ item.seatNumber || item.seatId }}</td>
            <td>{{ item.reservationDate }}</td>
            <td>{{ timeSlotText[item.timeSlot] || '-' }}</td>
            <td>{{ statusText[item.status] || '-' }}</td>
            <td class="actions">
              <button
                v-if="item.status === 0"
                class="ghost-btn compact"
                type="button"
                @click="handleCancel(item.id)"
              >
                取消预约
              </button>
              <button
                v-if="item.status === 0"
                class="ghost-btn compact danger"
                type="button"
                @click="handleViolate(item.id)"
              >
                标记违约
              </button>
            </td>
          </tr>
          <tr v-if="reservations.length === 0">
            <td colspan="8" class="empty-cell">暂无预约记录</td>
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
