import { computed, reactive } from 'vue'
import { fetchCurrentUser, login, logout } from '../api/auth'

const state = reactive({
  initialized: false,
  loading: false,
  user: null,
})

async function initSession() {
  if (state.initialized) {
    return state.user
  }

  state.loading = true
  try {
    const response = await fetchCurrentUser()
    state.user = response.data
  } catch (error) {
    if (error.code === 401) {
      state.user = null
    } else {
      throw error
    }
  } finally {
    state.initialized = true
    state.loading = false
  }

  return state.user
}

async function signIn(payload) {
  const response = await login(payload)
  state.user = response.data
  state.initialized = true
  return response.data
}

async function signOut() {
  await logout()
  state.user = null
  state.initialized = true
}

function clearSession() {
  state.user = null
  state.initialized = true
}

export function useSessionStore() {
  return {
    state,
    user: computed(() => state.user),
    isAdmin: computed(() => state.user?.role === 1),
    initSession,
    signIn,
    signOut,
    clearSession,
  }
}
