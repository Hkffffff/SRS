<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { fetchMyBlacklistStatus, fetchRooms, fetchSeatsByRoom, reserveSeat } from '../../api/student'
import SeatMapBoard from '../../components/SeatMapBoard.vue'

const rooms = ref([])
const seats = ref([])
const selectedRoomId = ref(null)
const blacklistStatus = ref(false)
const loading = ref(false)
const reserveLoading = ref(false)
const message = ref('')
const messageType = ref('info')

const timeSlotOptions = [
  { value: 1, label: '上午（8:00-12:00，8:30前签到）' },
  { value: 2, label: '下午（14:00-17:00，14:30前签到）' },
  { value: 3, label: '晚上（18:00-22:00，18:30前签到）' },
]

const reserveForm = reactive({
  seatId: null,
  date: new Date().toISOString().slice(0, 10),
  timeSlot: 1,
})

const selectedRoom = computed(() =>
  rooms.value.find((room) => room.id === selectedRoomId.value) ?? null,
)

async function loadRooms() {
  loading.value = true
  try {
    const [roomResponse, blacklistResponse] = await Promise.all([
      fetchRooms(),
      fetchMyBlacklistStatus(),
    ])
    rooms.value = roomResponse.data
    blacklistStatus.value = blacklistResponse.data

    if (rooms.value.length > 0) {
      selectedRoomId.value = rooms.value[0].id
      await loadSeats(rooms.value[0].id)
    } else {
      seats.value = []
    }
  } catch (error) {
    showMessage(error.message || '加载数据失败', 'error')
  } finally {
    loading.value = false
  }
}

async function loadSeats(roomId) {
  if (!roomId) {
    seats.value = []
    reserveForm.seatId = null
    return
  }

  try {
    const response = await fetchSeatsByRoom(roomId, {
      date: reserveForm.date,
      timeSlot: reserveForm.timeSlot,
    })
    seats.value = response.data

    const selectedSeat = seats.value.find((seat) => seat.id === reserveForm.seatId && seat.status === 1)
    if (selectedSeat) {
      return
    }

    reserveForm.seatId = seats.value.find((seat) => seat.status === 1)?.id ?? null
  } catch (error) {
    showMessage(error.message || '加载座位失败', 'error')
  }
}

async function handleRoomChange(event) {
  selectedRoomId.value = Number(event.target.value)
  await loadSeats(selectedRoomId.value)
}

async function handleReserve() {
  if (!reserveForm.seatId) {
    showMessage('请先选择可预约座位', 'error')
    return
  }

  reserveLoading.value = true
  try {
    await reserveSeat({
      seatId: reserveForm.seatId,
      date: reserveForm.date,
      timeSlot: reserveForm.timeSlot,
    })
    showMessage('预约成功', 'success')
    await loadSeats(selectedRoomId.value)
  } catch (error) {
    showMessage(error.message || '预约失败', 'error')
  } finally {
    reserveLoading.value = false
  }
}

function selectSeat(seatId) {
  reserveForm.seatId = seatId
}

function handleSeatSelect(seat) {
  if (!seat || seat.status === 0) {
    return
  }
  selectSeat(seat.id)
}

function showMessage(text, type = 'info') {
  message.value = text
  messageType.value = type
}

watch(
  () => [reserveForm.date, reserveForm.timeSlot],
  async () => {
    await loadSeats(selectedRoomId.value)
  },
)

onMounted(loadRooms)
</script>

<template>
  <div class="page-grid">
    <section class="card summary-card">
      <div>
        <p class="eyebrow">当前状态</p>
        <h2>预约大厅</h2>
      </div>
      <div class="status-pills">
        <span class="pill">{{ rooms.length }} 间开放自习室</span>
        <span class="pill" :class="{ danger: blacklistStatus }">
          {{ blacklistStatus ? '当前处于黑名单封禁中' : '当前可正常预约' }}
        </span>
      </div>
    </section>

    <section class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">步骤 1</p>
          <h2>选择自习室</h2>
        </div>
        <select :value="selectedRoomId ?? ''" :disabled="loading || rooms.length === 0" @change="handleRoomChange">
          <option v-for="room in rooms" :key="room.id" :value="room.id">
            {{ room.roomName }} / {{ room.floor }} 楼
          </option>
        </select>
      </div>

      <p v-if="selectedRoom" class="muted">
        当前房间：{{ selectedRoom.roomName }}，总座位数 {{ selectedRoom.totalSeats }}
      </p>

      <SeatMapBoard
        :seats="seats"
        :selected-seat-id="reserveForm.seatId"
        empty-text="该自习室暂无座位"
        @select="handleSeatSelect"
      />

      <p class="muted">红色座位表示该日期该时段已被预约或当前不可用，无法再次选择。</p>

      <p v-if="reserveForm.seatId" class="muted">
        已选座位：{{ seats.find((seat) => seat.id === reserveForm.seatId)?.seatNumber || '未选择' }}
      </p>
      <p v-else class="muted">当前日期和时段下没有可预约座位。</p>
    </section>

    <section class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">步骤 2</p>
          <h2>提交预约</h2>
        </div>
      </div>

      <div class="form-grid">
        <label>
          <span>预约日期</span>
          <input v-model="reserveForm.date" type="date" />
        </label>

        <label>
          <span>时间段</span>
          <select v-model="reserveForm.timeSlot">
            <option v-for="item in timeSlotOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </label>
      </div>

      <p class="muted">切换日期或时间段后，系统会自动刷新当前已被预约的座位状态。</p>

      <p v-if="message" class="feedback" :class="messageType">{{ message }}</p>

      <button class="primary-btn" type="button" :disabled="reserveLoading || blacklistStatus || !reserveForm.seatId" @click="handleReserve">
        {{ reserveLoading ? '提交中...' : blacklistStatus ? '黑名单状态不可预约' : '确认预约' }}
      </button>
    </section>
  </div>
</template>
