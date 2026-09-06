<script setup>
import { computed } from 'vue'
import { buildSeatMapRows } from '../utils/seatMap'

defineOptions({
  name: 'SeatMapBoard',
})

const props = defineProps({
  seats: {
    type: Array,
    default: () => [],
  },
  selectedSeatId: {
    type: Number,
    default: null,
  },
  mode: {
    type: String,
    default: 'student',
  },
  emptyText: {
    type: String,
    default: '暂无座位数据',
  },
  interactive: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['select'])

const rows = computed(() => buildSeatMapRows(props.seats))

function handleSelect(seat) {
  if (!seat || !props.interactive) {
    return
  }

  if (props.mode === 'student' && seat.status === 0) {
    return
  }

  emit('select', seat)
}
</script>

<template>
  <div class="seat-map-board" :class="{ readonly: !interactive }">
    <div class="seat-map-screen">讲台 / 入口方向</div>

    <div class="seat-map-legend">
      <span class="legend-item"><i class="legend-box available"></i>可用</span>
      <span class="legend-item"><i class="legend-box selected"></i>当前选中</span>
      <span class="legend-item"><i class="legend-box unavailable"></i>不可用 / 维修</span>
    </div>

    <div v-if="rows.length === 0" class="seat-map-empty">
      {{ emptyText }}
    </div>

    <div v-else class="seat-map-grid">
      <div v-for="row in rows" :key="row.label" class="seat-map-row">
        <div class="seat-map-row-label">{{ row.label }}</div>

        <div class="seat-map-row-seats">
          <button
            v-for="(seat, index) in row.seats"
            :key="seat ? seat.id : `${row.label}-${index}`"
            type="button"
            class="seat-map-seat"
            :class="{
              selected: seat && seat.id === selectedSeatId,
              unavailable: seat && seat.status === 0,
              placeholder: !seat,
              admin: mode === 'admin',
              readonly: !interactive,
            }"
            :disabled="!seat || !interactive || (mode === 'student' && seat.status === 0)"
            @click="handleSelect(seat)"
          >
            <template v-if="seat">
              <strong>{{ seat.seatNumber }}</strong>
              <span>{{ seat.hasWindow ? '窗' : '内' }} / {{ seat.hasPower ? '电' : '普' }}</span>
            </template>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
