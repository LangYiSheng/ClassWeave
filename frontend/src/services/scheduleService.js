import { apiClient } from '@/services/http'

function sanitizeCoursePayload(payload = {}) {
  const { color, ...rest } = payload

  return rest
}

function sanitizeImportPayload(payload = {}) {
  return {
    ...payload,
    courses: Array.isArray(payload.courses)
      ? payload.courses.map((course) => sanitizeCoursePayload(course))
      : [],
  }
}

export async function fetchSchedules() {
  const response = await apiClient.get('/schedules')

  return response.data.data
}

export async function fetchScheduleDetail(scheduleId) {
  const response = await apiClient.get(`/schedules/${scheduleId}`)

  return response.data.data
}

export async function saveScheduleBasics(scheduleId, payload) {
  if (scheduleId === 'new') {
    const response = await apiClient.post('/schedules', payload)
    return response.data.data
  }

  await apiClient.put(`/schedules/${scheduleId}`, payload)

  return fetchScheduleDetail(scheduleId)
}

export async function saveTimeSlots(scheduleId, timeSlots) {
  await apiClient.put(`/schedules/${scheduleId}/time-slots`, { timeSlots })
  const detail = await fetchScheduleDetail(scheduleId)

  return detail.timeSlots
}

export async function upsertCourse(scheduleId, payload) {
  const normalizedPayload = sanitizeCoursePayload(payload)

  if (payload.id) {
    const response = await apiClient.put(
      `/schedules/${scheduleId}/courses/${payload.id}`,
      normalizedPayload,
    )
    return response.data.data
  }

  const response = await apiClient.post(`/schedules/${scheduleId}/courses`, normalizedPayload)

  return response.data.data
}

export async function deleteCourseById(scheduleId, courseId) {
  const response = await apiClient.delete(`/schedules/${scheduleId}/courses/${courseId}`)

  return response.data.data
}

export async function importScheduleJson(payload) {
  const response = await apiClient.post('/schedules/import/json', sanitizeImportPayload(payload))

  return response.data.data
}

export async function removeScheduleById(scheduleId) {
  const response = await apiClient.delete(`/schedules/${scheduleId}`)

  return response.data.data
}

export async function saveScheduleDisplaySettings(scheduleId, payload) {
  const response = await apiClient.put(`/schedules/${scheduleId}/display-settings`, payload)

  return response.data.data
}

export async function removeScheduleAccess(scheduleId) {
  const response = await apiClient.delete(`/schedules/${scheduleId}/access`)

  return response.data.data
}
