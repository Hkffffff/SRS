const TIME_SLOT_RULES = {
  1: {
    label: '上午',
    range: '8:00-12:00',
    startHour: 8,
    startMinute: 0,
    endHour: 12,
    endMinute: 0,
    deadlineHour: 8,
    deadlineMinute: 30,
  },
  2: {
    label: '下午',
    range: '14:00-17:00',
    startHour: 14,
    startMinute: 0,
    endHour: 17,
    endMinute: 0,
    deadlineHour: 14,
    deadlineMinute: 30,
  },
  3: {
    label: '晚上',
    range: '18:00-22:00',
    startHour: 18,
    startMinute: 0,
    endHour: 22,
    endMinute: 0,
    deadlineHour: 18,
    deadlineMinute: 30,
  },
}

function pad(value) {
  return String(value).padStart(2, '0')
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  return `${year}-${month}-${day}`
}

function toMinutes(hour, minute) {
  return hour * 60 + minute
}

function formatTime(hour, minute) {
  return `${hour}:${pad(minute)}`
}

export function getTimeSlotRule(timeSlot) {
  return TIME_SLOT_RULES[timeSlot] ?? null
}

export function getTimeSlotLabel(timeSlot) {
  const rule = getTimeSlotRule(timeSlot)
  if (!rule) {
    return '未知时段'
  }
  return `${rule.label}（${rule.range}）`
}

export function getCheckInHint(reservation, now = new Date()) {
  const rule = getTimeSlotRule(reservation?.timeSlot)
  if (!rule) {
    return {
      canCheckIn: false,
      text: '时段异常',
      tone: 'danger',
    }
  }

  if (reservation?.status !== 0) {
    return {
      canCheckIn: false,
      text: '当前记录无需签到',
      tone: 'neutral',
    }
  }

  const today = formatDate(now)
  if (reservation.reservationDate > today) {
    return {
      canCheckIn: false,
      text: `预约未到期，可于当日 ${formatTime(rule.startHour, rule.startMinute)} 后签到`,
      tone: 'neutral',
    }
  }

  if (reservation.reservationDate < today) {
    return {
      canCheckIn: false,
      text: '预约日期已过，系统将按规则处理',
      tone: 'danger',
    }
  }

  const currentMinutes = toMinutes(now.getHours(), now.getMinutes())
  const startMinutes = toMinutes(rule.startHour, rule.startMinute)
  const deadlineMinutes = toMinutes(rule.deadlineHour, rule.deadlineMinute)

  if (currentMinutes < startMinutes) {
    return {
      canCheckIn: false,
      text: `未到签到时间，可于 ${formatTime(rule.startHour, rule.startMinute)} 后签到`,
      tone: 'neutral',
    }
  }

  if (currentMinutes <= deadlineMinutes) {
    return {
      canCheckIn: true,
      text: `当前可签到，最晚 ${formatTime(rule.deadlineHour, rule.deadlineMinute)} 前完成`,
      tone: 'success',
    }
  }

  return {
    canCheckIn: false,
    text: `已超过 ${formatTime(rule.deadlineHour, rule.deadlineMinute)} 签到截止时间`,
    tone: 'danger',
  }
}
