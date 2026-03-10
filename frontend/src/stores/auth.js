import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { login, register, resetPassword, updateCurrentUser } from '@/services/authService'

const STORAGE_KEY = 'classweave-auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref(null)
  const initialized = ref(false)

  const isAuthenticated = computed(() => Boolean(token.value && user.value))

  function initialize() {
    if (initialized.value) {
      return
    }

    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      token.value = parsed.token
      user.value = parsed.user
      localStorage.setItem('classweave-token', parsed.token)
    }

    initialized.value = true
  }

  function persist() {
    if (!token.value || !user.value) {
      localStorage.removeItem(STORAGE_KEY)
      localStorage.removeItem('classweave-token')
      return
    }

    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: token.value,
        user: user.value,
      }),
    )
    localStorage.setItem('classweave-token', token.value)
  }

  async function signIn(payload) {
    const result = await login(payload)
    token.value = result.token
    user.value = result.user
    persist()
  }

  async function signUp(payload) {
    const result = await register(payload)
    token.value = result.token
    user.value = result.user
    persist()
  }

  async function changePassword(payload) {
    return resetPassword(payload)
  }

  async function updateProfile(payload) {
    const result = await updateCurrentUser(payload)
    user.value = result
    persist()
    return result
  }

  function logout() {
    token.value = ''
    user.value = null
    persist()
  }

  return {
    token,
    user,
    isAuthenticated,
    initialize,
    signIn,
    signUp,
    changePassword,
    updateProfile,
    logout,
  }
})
