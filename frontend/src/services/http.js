import axios from 'axios'

import { APP_CONFIG } from '@/config/app'

export const apiClient = axios.create({
  baseURL: APP_CONFIG.apiBaseUrl,
  timeout: 5000,
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('classweave-token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

apiClient.interceptors.response.use(
  (response) => {
    const payload = response.data

    if (payload && typeof payload === 'object' && 'code' in payload && payload.code !== 0) {
      return Promise.reject(new Error(payload.message || '请求失败'))
    }

    return response
  },
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      '网络请求失败'

    return Promise.reject(new Error(message))
  },
)
