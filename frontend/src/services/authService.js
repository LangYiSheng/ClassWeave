import { apiClient } from '@/services/http'

export async function login(payload) {
  const response = await apiClient.post('/auth/login', payload)

  return response.data.data
}

export async function register(payload) {
  const response = await apiClient.post('/auth/register', payload)

  return response.data.data
}

export async function resetPassword(payload) {
  const response = await apiClient.post('/auth/reset-password', payload)

  return response.data.data
}

export async function updateCurrentUser(payload) {
  const response = await apiClient.put('/users/me', payload)

  return response.data.data
}

export async function fetchCurrentUser() {
  const response = await apiClient.get('/users/me')
  return response.data.data
}
