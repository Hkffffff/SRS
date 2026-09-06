import request from './http'

export function fetchRooms() {
  return request.get('/api/room/list')
}

export function fetchSeatsByRoom(roomId, params) {
  return request.get(`/api/room/${roomId}/seats`, { params })
}

export function reserveSeat(params) {
  return request.post('/api/reservation/reserve', null, { params })
}

export function checkInReservation(reservationId) {
  return request.post('/api/reservation/checkIn', null, {
    params: { reservationId },
  })
}

export function cancelReservation(reservationId) {
  return request.post('/api/reservation/cancel', null, {
    params: { reservationId },
  })
}

export function fetchMyReservations(params) {
  return request.get('/api/reservation/my', { params })
}

export function fetchMyBlacklistStatus() {
  return request.get('/api/blacklist/status')
}

export function fetchMyBlacklistRecords() {
  return request.get('/api/blacklist/records')
}
