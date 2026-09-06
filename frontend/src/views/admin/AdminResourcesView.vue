<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createRoom,
  createSeat,
  deleteRoom,
  deleteSeat,
  fetchAdminRooms,
  fetchRoomSeats,
  updateRoom,
  updateRoomStatus,
  updateSeat,
} from '../../api/admin'
import SeatMapBoard from '../../components/SeatMapBoard.vue'

const rooms = ref([])
const seats = ref([])
const selectedRoomId = ref(null)
const roomEditingId = ref(null)
const seatEditingId = ref(null)
const actionMessage = ref('')
const showRoomForm = ref(false)

const roomForm = reactive({
  roomName: '',
  floor: 1,
  status: 1,
  autoGenerateSeats: true,
  rowCount: 4,
  seatsPerRow: 8,
})

const seatForm = reactive({
  roomId: null,
  seatNumber: '',
  hasWindow: 0,
  hasPower: 0,
  status: 1,
})

const isRoomFormVisible = computed(() => showRoomForm.value || !!roomEditingId.value)
const isCreatingWithAutoSeats = computed(() => !roomEditingId.value && roomForm.autoGenerateSeats)

const generatedSeatCount = computed(() => {
  const rowCount = Number(roomForm.rowCount) || 0
  const seatsPerRow = Number(roomForm.seatsPerRow) || 0
  return rowCount * seatsPerRow
})

const generationPreview = computed(() => {
  if (!generatedSeatCount.value) {
    return '将生成 0 个座位。'
  }
  return `将生成 ${generatedSeatCount.value} 个座位，编号示例：A-01、A-${String(
    Number(roomForm.seatsPerRow) || 0,
  ).padStart(2, '0')}、B-01`
})

const previewSeats = computed(() => {
  if (!isCreatingWithAutoSeats.value) {
    return []
  }

  const rowCount = Number(roomForm.rowCount) || 0
  const seatsPerRow = Number(roomForm.seatsPerRow) || 0
  const generatedSeats = []

  for (let rowIndex = 1; rowIndex <= rowCount; rowIndex += 1) {
    const rowLabel = toExcelColumnLabel(rowIndex)
    for (let columnIndex = 1; columnIndex <= seatsPerRow; columnIndex += 1) {
      generatedSeats.push({
        id: rowIndex * 1000 + columnIndex,
        seatNumber: `${rowLabel}-${String(columnIndex).padStart(2, '0')}`,
        hasWindow: columnIndex === 1 || columnIndex === seatsPerRow ? 1 : 0,
        hasPower: 1,
        status: 1,
      })
    }
  }

  return generatedSeats
})

async function loadRooms() {
  const response = await fetchAdminRooms()
  rooms.value = response.data

  if (rooms.value.length > 0) {
    const currentRoomExists = rooms.value.some((room) => room.id === selectedRoomId.value)
    if (!currentRoomExists) {
      selectedRoomId.value = rooms.value[0].id
    }
  } else {
    selectedRoomId.value = null
  }

  if (selectedRoomId.value) {
    await loadSeats(selectedRoomId.value)
  } else {
    seats.value = []
  }
}

async function loadSeats(roomId) {
  if (!roomId) {
    seats.value = []
    return
  }

  const response = await fetchRoomSeats(roomId)
  seats.value = response.data
}

async function submitRoom() {
  const payload = {
    roomName: roomForm.roomName,
    floor: Number(roomForm.floor),
    status: Number(roomForm.status),
    autoGenerateSeats: isCreatingWithAutoSeats.value,
    rowCount: isCreatingWithAutoSeats.value ? Number(roomForm.rowCount) : null,
    seatsPerRow: isCreatingWithAutoSeats.value ? Number(roomForm.seatsPerRow) : null,
  }

  try {
    if (roomEditingId.value) {
      await updateRoom(roomEditingId.value, payload)
      actionMessage.value = '自习室信息已更新'
    } else {
      const response = await createRoom(payload)
      selectedRoomId.value = response.data.id
      actionMessage.value = isCreatingWithAutoSeats.value ? '自习室已创建，并自动生成座位图' : '自习室已创建'
    }

    resetRoomForm()
    await loadRooms()
  } catch (error) {
    window.alert(error.message || '提交自习室失败')
  }
}

async function submitSeat() {
  const payload = {
    roomId: Number(seatForm.roomId),
    seatNumber: seatForm.seatNumber,
    hasWindow: Number(seatForm.hasWindow),
    hasPower: Number(seatForm.hasPower),
    status: Number(seatForm.status),
  }

  try {
    if (seatEditingId.value) {
      await updateSeat(seatEditingId.value, payload)
      actionMessage.value = '座位已更新'
    } else {
      await createSeat(payload)
      actionMessage.value = '座位已新增'
    }

    resetSeatForm()
    await loadRooms()
  } catch (error) {
    window.alert(error.message || '提交座位失败')
  }
}

function openCreateRoomForm() {
  roomEditingId.value = null
  showRoomForm.value = true
}

function editRoom(room) {
  roomEditingId.value = room.id
  showRoomForm.value = true
  roomForm.roomName = room.roomName
  roomForm.floor = room.floor
  roomForm.status = room.status
}

function editSeat(seat) {
  seatEditingId.value = seat.id
  seatForm.roomId = seat.roomId
  seatForm.seatNumber = seat.seatNumber
  seatForm.hasWindow = seat.hasWindow
  seatForm.hasPower = seat.hasPower
  seatForm.status = seat.status
}

async function removeRoom(room) {
  if (!window.confirm(`确认删除自习室“${room.roomName}”吗？`)) {
    return
  }

  if (Number(room.totalSeats) > 0) {
    const confirmed = window.confirm(
      `该自习室下还有 ${room.totalSeats} 个座位。继续删除将连同其下全部座位一起删除，确认继续吗？`,
    )
    if (!confirmed) {
      return
    }
  }

  try {
    await deleteRoom(room.id)
    if (selectedRoomId.value === room.id) {
      selectedRoomId.value = null
    }
    actionMessage.value = Number(room.totalSeats) > 0 ? '自习室及其下全部座位已删除' : '自习室已删除'
    await loadRooms()
  } catch (error) {
    window.alert(error.message || '删除自习室失败')
  }
}

async function removeSeat(seatId) {
  if (!window.confirm('确认删除该座位吗？')) {
    return
  }

  try {
    await deleteSeat(seatId)
    actionMessage.value = '座位已删除'
    await loadRooms()
  } catch (error) {
    window.alert(error.message || '删除座位失败')
  }
}

async function toggleRoomStatus(room) {
  try {
    await updateRoomStatus(room.id, room.status === 1 ? 0 : 1)
    actionMessage.value = `自习室已${room.status === 1 ? '关闭' : '开放'}`
    await loadRooms()
  } catch (error) {
    window.alert(error.message || '更新房间状态失败')
  }
}

function resetRoomForm() {
  roomEditingId.value = null
  showRoomForm.value = false
  roomForm.roomName = ''
  roomForm.floor = 1
  roomForm.status = 1
  roomForm.autoGenerateSeats = true
  roomForm.rowCount = 4
  roomForm.seatsPerRow = 8
}

function resetSeatForm() {
  seatEditingId.value = null
  seatForm.roomId = selectedRoomId.value
  seatForm.seatNumber = ''
  seatForm.hasWindow = 0
  seatForm.hasPower = 0
  seatForm.status = 1
}

async function selectRoom(roomId) {
  selectedRoomId.value = roomId
  seatEditingId.value = null
  seatForm.roomId = roomId
  await loadSeats(roomId)
}

function handleSeatSelect(seat) {
  if (!seat) {
    return
  }

  editSeat(seat)
}

function toExcelColumnLabel(index) {
  let current = index
  let label = ''

  while (current > 0) {
    current -= 1
    label = String.fromCharCode(65 + (current % 26)) + label
    current = Math.floor(current / 26)
  }

  return label
}

onMounted(async () => {
  await loadRooms()
  seatForm.roomId = selectedRoomId.value
})
</script>

<template>
  <div class="page-grid admin-grid">
    <section class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">资源维护</p>
          <h2>自习室管理</h2>
        </div>
        <button
          v-if="!isRoomFormVisible"
          class="primary-btn compact"
          type="button"
          @click="openCreateRoomForm"
        >
          新增自习室
        </button>
      </div>

      <form v-if="isRoomFormVisible" class="form-grid" @submit.prevent="submitRoom">
        <label>
          <span>名称</span>
          <input v-model="roomForm.roomName" placeholder="例如：图书馆四楼北区" />
        </label>

        <label>
          <span>楼层</span>
          <input v-model="roomForm.floor" type="number" min="1" />
        </label>

        <label>
          <span>状态</span>
          <select v-model="roomForm.status">
            <option :value="1">开放</option>
            <option :value="0">关闭</option>
          </select>
        </label>

        <label v-if="!roomEditingId" class="wide checkbox-line">
          <input v-model="roomForm.autoGenerateSeats" type="checkbox" />
          <span>创建房间时自动批量生成座位图</span>
        </label>

        <template v-if="isCreatingWithAutoSeats">
          <label>
            <span>排数</span>
            <input v-model="roomForm.rowCount" type="number" min="1" max="60" />
          </label>

          <label>
            <span>每排座位数</span>
            <input v-model="roomForm.seatsPerRow" type="number" min="1" max="30" />
          </label>

          <div class="card muted-card seat-preview-card">
            <p class="eyebrow">批量生成预览</p>
            <strong>{{ generatedSeatCount }} 个座位</strong>
            <p class="muted">{{ generationPreview }}</p>
          </div>

          <div class="wide room-preview-panel">
            <div class="preview-head">
              <div>
                <p class="eyebrow">布局预览</p>
                <h3>新自习室座位图</h3>
              </div>
              <p class="muted">预览仅用于展示批量生成后的排布，不会写入数据库。</p>
            </div>

            <SeatMapBoard
              :seats="previewSeats"
              mode="admin"
              empty-text="请输入有效的排数和每排座位数"
              :interactive="false"
            />
          </div>
        </template>

        <div class="form-actions">
          <button class="primary-btn compact" type="submit">
            {{ roomEditingId ? '更新自习室' : '新增自习室' }}
          </button>
          <button class="ghost-btn compact" type="button" @click="resetRoomForm">
            {{ roomEditingId ? '取消编辑' : '收起表单' }}
          </button>
        </div>
      </form>

      <p v-if="actionMessage" class="feedback success">{{ actionMessage }}</p>

      <div class="table-shell">
        <table class="data-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>楼层</th>
              <th>座位数</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="room in rooms" :key="room.id">
              <td>{{ room.roomName }}</td>
              <td>{{ room.floor }}</td>
              <td>{{ room.totalSeats }}</td>
              <td>{{ room.status === 1 ? '开放' : '关闭' }}</td>
              <td class="actions">
                <button class="ghost-btn compact" type="button" @click="selectRoom(room.id)">查看座位</button>
                <button class="ghost-btn compact" type="button" @click="editRoom(room)">编辑</button>
                <button class="ghost-btn compact" type="button" @click="toggleRoomStatus(room)">
                  {{ room.status === 1 ? '关闭' : '开放' }}
                </button>
                <button class="ghost-btn compact danger" type="button" @click="removeRoom(room)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">座位维护</p>
          <h2>当前房间座位图</h2>
        </div>
      </div>

      <SeatMapBoard
        :seats="seats"
        :selected-seat-id="seatEditingId"
        mode="admin"
        empty-text="当前房间还没有座位，请先批量生成或手动新增"
        @select="handleSeatSelect"
      />

      <p class="muted">管理员可以直接点击座位图中的座位，把信息带入下方表单进行编辑。</p>

      <form class="form-grid" @submit.prevent="submitSeat">
        <label>
          <span>所属房间</span>
          <select v-model="seatForm.roomId">
            <option v-for="room in rooms" :key="room.id" :value="room.id">{{ room.roomName }}</option>
          </select>
        </label>

        <label>
          <span>座位编号</span>
          <input v-model="seatForm.seatNumber" placeholder="A-01" />
        </label>

        <label>
          <span>靠窗</span>
          <select v-model="seatForm.hasWindow">
            <option :value="1">是</option>
            <option :value="0">否</option>
          </select>
        </label>

        <label>
          <span>插座</span>
          <select v-model="seatForm.hasPower">
            <option :value="1">有</option>
            <option :value="0">无</option>
          </select>
        </label>

        <label>
          <span>状态</span>
          <select v-model="seatForm.status">
            <option :value="1">正常</option>
            <option :value="0">损坏</option>
          </select>
        </label>

        <div class="form-actions">
          <button class="primary-btn compact" type="submit">
            {{ seatEditingId ? '更新座位' : '新增座位' }}
          </button>
          <button class="ghost-btn compact" type="button" @click="resetSeatForm">清空</button>
        </div>
      </form>

      <div v-if="seatEditingId" class="actions">
        <button class="ghost-btn compact danger" type="button" @click="removeSeat(seatEditingId)">
          删除当前选中座位
        </button>
      </div>
    </section>
  </div>
</template>
