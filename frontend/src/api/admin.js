import request from './http'

export function fetchAdminRooms(params) {
  return request.get('/api/admin/room/list', { params })
}

export function createRoom(payload) {
  return request.post('/api/admin/room', payload)
}

export function updateRoom(roomId, payload) {
  return request.put(`/api/admin/room/${roomId}`, payload)
}

export function updateRoomStatus(roomId, status) {
  return request.put(`/api/admin/room/${roomId}/status`, null, {
    params: { status },
  })
}

export function deleteRoom(roomId) {
  return request.delete(`/api/admin/room/${roomId}`)
}

export function fetchRoomSeats(roomId) {
  return request.get(`/api/admin/seat/room/${roomId}/list`)
}

export function createSeat(payload) {
  return request.post('/api/admin/seat', payload)
}

export function updateSeat(seatId, payload) {
  return request.put(`/api/admin/seat/${seatId}`, payload)
}

export function deleteSeat(seatId) {
  return request.delete(`/api/admin/seat/${seatId}`)
}

export function fetchAdminReservations(params) {
  return request.get('/api/admin/reservation/list', { params })
}

export function adminCancelReservation(reservationId) {
  return request.put(`/api/admin/reservation/${reservationId}/cancel`)
}

export function markReservationViolated(reservationId) {
  return request.put(`/api/admin/reservation/${reservationId}/violate`)
}

export function processOverdueReservations() {
  return request.put('/api/admin/reservation/process-overdue')
}

export function fetchAdminBlacklists(params) {
  return request.get('/api/admin/blacklist/list', { params })
}

export function createBlacklist(payload) {
  return request.post('/api/admin/blacklist', payload)
}

export function releaseBlacklist(blacklistId) {
  return request.put(`/api/admin/blacklist/${blacklistId}/release`)
}

export function fetchAdminStudents(params) {
  return request.get('/api/admin/student/list', { params })
}

export function createAdminStudent(payload) {
  return request.post('/api/admin/student', payload)
}

export function updateAdminStudent(userId, payload) {
  return request.put(`/api/admin/student/${userId}`, payload)
}

export function deleteAdminStudent(userId, params) {
  return request.delete(`/api/admin/student/${userId}`, { params })
}
