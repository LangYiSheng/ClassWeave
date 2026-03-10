import { apiClient } from '@/services/http'

export async function fetchStatusPanel({ scheduleIds, date, time, windowMinutes }) {
  if (!scheduleIds.length) {
    return {
      date,
      time,
      windowMinutes,
      current: [],
      free: [],
      soonStart: [],
      soonEnd: [],
    }
  }

  const response = await apiClient.get('/status/panel', {
    params: {
      scheduleIds: scheduleIds.join(','),
      date,
      time,
      windowMinutes,
    },
  })

  return response.data.data
}
