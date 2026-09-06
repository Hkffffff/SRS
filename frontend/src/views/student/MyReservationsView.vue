<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import {
  cancelReservation,
  checkInReservation,
  fetchMyReservations,
} from '../../api/student'
import { getCheckInHint, getTimeSlotLabel } from '../../utils/reservationTime'

const records = ref([])
const loading = ref(false)
const actionLoadingId = ref(null)
const now = ref(new Date())
const filter = reactive({
  date: '',
  status: '',
})

const statusText = {
  0: '待签到',
  1: '已签到',
  2: '已取消',
  3: '已违约',
}

let timerId = null

async function loadReservations() {
  loading.value = true
  try {
    const response = await fetchMyReservations({
      date: filter.date || undefined,
      status: filter.status === '' ? undefined : Number(filter.status),
    })
    records.value = response.data
  } finally {
    loading.value = false
  }
}

function getHint(reservation) {
  return getCheckInHint(reservation, now.value)
}

async function handleCheckIn(reservationId) {
  actionLoadingId.value = reservationId
  try {
    await checkInReservation(reservationId)
    await loadReservations()
  } catch (error) {
    window.alert(error.message || '签到失败')
  } finally {
    actionLoadingId.value = null
  }
}

async function handleCancel(reservationId) {
  actionLoadingId.value = reservationId
  try {
    await cancelReservation(reservationId)
    await loadReservations()
  } catch (error) {
    window.alert(error.message || '取消失败')
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(async () => {
  await loadReservations()
  timerId = window.setInterval(() => {
    now.value = new Date()
  }, 30000)
})

onUnmounted(() => {
  if (timerId) {
    window.clearInterval(timerId)
  }
})
</script>

<template>
  <section class="card">
    <div class="section-head">
      <div>
        <p class="eyebrow">预约记录</p>
        <h2>我的预约</h2>
      </div>
      <button class="ghost-btn" type="button" @click="loadReservations">刷新</button>
    </div>

    <p class="muted">
      当前签到规则：上午 8:30 前、下午 14:30 前、晚上 18:30 前完成签到；页面每 30 秒自动刷新签到状态提示。
    </p>

    <div class="toolbar">
      <label>
        <span>日期筛选</span>
        <input v-model="filter.date" type="date" />
      </label>
      <label>
        <span>状态筛选</span>
        <select v-model="filter.status">
          <option value="">全部</option>
          <option value="0">待签到</option>
          <option value="1">已签到</option>
          <option value="2">已取消</option>
          <option value="3">已违约</option>
        </select>
      </label>
      <button class="primary-btn compact" type="button" :disabled="loading" @click="loadReservations">
        查询
      </button>
    </div>

    <div class="table-shell">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>自习室</th>
            <th>座位号</th>
            <th>日期</th>
            <th>时段</th>
            <th>状态</th>
            <th>签到提示</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in records" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.roomName || '-' }}</td>
            <td>{{ item.seatNumber || item.seatId }}</td>
            <td>{{ item.reservationDate }}</td>
            <td>{{ getTimeSlotLabel(item.timeSlot) }}</td>
            <td>
              <span class="pill">{{ statusText[item.status] }}</span>
            </td>
            <td>
              <span class="pill" :class="getHint(item).tone">{{ getHint(item).text }}</span>
            </td>
            <td class="actions">
              <button
                v-if="item.status === 0"
                class="ghost-btn compact"
                type="button"
                :disabled="actionLoadingId === item.id || !getHint(item).canCheckIn"
                :title="getHint(item).text"
                @click="handleCheckIn(item.id)"
              >
                签到
              </button>
              <button
                v-if="item.status === 0"
                class="ghost-btn compact danger"
                type="button"
                :disabled="actionLoadingId === item.id"
                @click="handleCancel(item.id)"
              >
                取消
              </button>
            </td>
          </tr>
          <tr v-if="records.length === 0">
            <td colspan="8" class="empty-cell">暂无预约记录</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
