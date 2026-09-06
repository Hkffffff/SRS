function parseSeatNumber(seatNumber) {
  const normalized = String(seatNumber || '').trim()
  const matched = normalized.match(/^([A-Za-z]+)[-_ ]?(\d+)$/)

  if (!matched) {
    return {
      rowLabel: '其他',
      rowOrder: Number.MAX_SAFE_INTEGER,
      column: null,
    }
  }

  const rowLabel = matched[1].toUpperCase()
  const column = Number(matched[2])

  return {
    rowLabel,
    rowOrder: rowLabelToNumber(rowLabel),
    column,
  }
}

function rowLabelToNumber(label) {
  return label.split('').reduce((total, char) => total * 26 + char.charCodeAt(0) - 64, 0)
}

export function buildSeatMapRows(seats = []) {
  if (!Array.isArray(seats) || seats.length === 0) {
    return []
  }

  const parsedSeats = seats.map((seat) => ({
    ...seat,
    ...parseSeatNumber(seat.seatNumber),
  }))

  const grouped = new Map()
  for (const seat of parsedSeats) {
    const key = `${seat.rowOrder}:${seat.rowLabel}`
    if (!grouped.has(key)) {
      grouped.set(key, {
        key,
        label: seat.rowLabel,
        order: seat.rowOrder,
        seats: [],
      })
    }
    grouped.get(key).seats.push(seat)
  }

  return Array.from(grouped.values())
    .sort((left, right) => left.order - right.order)
    .map((row) => {
      const hasNumericColumns = row.seats.every((seat) => seat.column !== null)
      if (!hasNumericColumns) {
        return {
          label: row.label,
          seats: row.seats.sort((left, right) => String(left.seatNumber).localeCompare(String(right.seatNumber))),
        }
      }

      const maxColumn = Math.max(...row.seats.map((seat) => seat.column))
      const indexed = new Map(row.seats.map((seat) => [seat.column, seat]))
      const filledSeats = []
      for (let column = 1; column <= maxColumn; column++) {
        filledSeats.push(indexed.get(column) ?? null)
      }
      return {
        label: row.label,
        seats: filledSeats,
      }
    })
}
