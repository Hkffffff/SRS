import request from './http'

export function login(payload) {
  return request.post('/api/user/login', payload)
}

export function fetchCurrentUser() {
  return request.get('/api/user/me')
}

export function logout() {
  return request.post('/api/user/logout')
}
